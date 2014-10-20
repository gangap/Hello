package com.fedex.mn.daos.impls;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.fedex.mn.daos.TestDetailDaoInterface;
import com.fedex.mn.models.impls.TestDetailModel;
import com.fedex.mn.utils.Util;

@Repository
public class TestDetailDAO
	extends NamedParameterJdbcDaoSupport
	implements TestDetailDaoInterface
{	
	@Autowired
	@Qualifier("ds")
	private DataSource ds;
	
	@Autowired
	private UserDAO userDao;
	
	@PostConstruct
	public void init()
	{
		setDataSource(ds);
	}
	
	public int add(TestDetailModel test)
	{
		final String query = "insert into test_details (type, justification, test_data, " +
				"test_data_text, expected_results, steps, sqa_user, sqa_comment) " +
				"values (:type, :just, :dataID, :data, :exp, :steps, :sqaID, :sqaComm)";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("type", test.getTestType() == null || test.getTestType().getId() == 0 || test.getTestType().getId() == -1 ? null : test.getTestType().getId())
			.addValue("just", test.getTestableJustification())
			.addValue("dataID", test.getTestData())
			.addValue("data", test.getTestDataText())
			.addValue("exp", test.getExpectedResults())
			.addValue("steps", test.getSteps())
			.addValue("sqaID", (test.getSqaUser() == null || test.getSqaUser().getId() == 0) || test.getSqaUser().getId() == -1 ? null : test.getSqaUser().getId())
			.addValue("sqaComm", test.getSqaComments());
		
		KeyHolder key = new GeneratedKeyHolder();

		int modified = getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		test.setId(key.getKey().intValue());
		
		return modified;
	}
	
	public TestDetailModel get(int id)
	{
		String query = "select * " +
				"from test_detail_view " +
				"where test_id=:id"; 
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
		TestDetailModel test = null;

		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(rows.next())
		{
			test = new TestDetailModel();
			
			test.setId(id);	
			test.setTestType(Util.createProp(rows, "type"));
			
			SerialClob clob = (SerialClob)rows.getObject("justification");
			
			try
			{
				if(clob != null)
					test.setTestableJustification(clob.getSubString(1, (int) clob.length()));
				else
					test.setTestableJustification("");
			}
			catch(SerialException e)
			{
				test.setTestableJustification("");
			}
			
			test.setTestData(rows.getInt("test_data"));
			clob = (SerialClob)rows.getObject("test_data_text");
			
			try
			{
				if(clob != null)
					test.setTestDataText(clob.getSubString(1, (int) clob.length()));
				else
					test.setTestDataText("");
			}
			catch(SerialException e)
			{
				test.setTestDataText("");
			}
			
			clob = (SerialClob)rows.getObject("expected_results");
			
			try
			{
				if(clob != null)
					test.setExpectedResults(clob.getSubString(1, (int) clob.length()));
				else
					test.setExpectedResults("");
			}
			catch(SerialException e)
			{
				test.setExpectedResults("");
			}
			
			clob = (SerialClob)rows.getObject("steps");
			
			try
			{
				if(clob != null)
					test.setSteps(clob.getSubString(1, (int) clob.length()));
				else
					test.setSteps("");
			}
			catch(SerialException e)
			{
				test.setSteps("");
			}
			
			test.setSqaUser(userDao.getId(rows.getInt("sqa_user_id")));
			clob = (SerialClob)rows.getObject("sqa_comments");
			
			try
			{
				if(clob != null)
					test.setSqaComments(clob.getSubString(1, (int) clob.length()));
				else
					test.setSqaComments("");
			}
			catch(SerialException e)
			{
				test.setSqaComments("");
			}
			
		}
		
		return test;
	}

	@Override
	public int update(TestDetailModel test)
	{
		final String query = "update test_details set type=:type, justification=:just, test_data=:dataID, " +
				"test_data_text=:data, expected_results=:exp, steps=:steps, sqa_user=:sqaID, sqa_comment=:sqaComm " +
				"where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("type", test.getTestType() == null || test.getTestType().getId() == 0 || test.getTestType().getId() == -1 ? null : test.getTestType().getId())
			.addValue("just", test.getTestableJustification())
			.addValue("dataID", test.getTestData())
			.addValue("data", test.getTestDataText())
			.addValue("exp", test.getExpectedResults())
			.addValue("steps", test.getSteps())
			.addValue("sqaID", (test.getSqaUser() == null || test.getSqaUser().getId() == 0) || test.getSqaUser().getId() == -1 ? null : test.getSqaUser().getId())
			.addValue("sqaComm", test.getSqaComments())
			.addValue("id", test.getId());
		
		return getNamedParameterJdbcTemplate().update(query, params);
	}
}
