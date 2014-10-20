package com.fedex.mn.daos.impls;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.transaction.annotation.Transactional;

import com.fedex.mn.daos.ApprovalDaoInterface;
import com.fedex.mn.models.impls.ApprovalModel;
import com.fedex.mn.utils.Util;

@Repository
@Transactional(rollbackFor = Exception.class)
public class ApprovalDAO 
	extends NamedParameterJdbcDaoSupport
	implements ApprovalDaoInterface
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

	public int add(ApprovalModel approval)
	{
		final String query = "insert into approval_details " +
			"(user_id, approval_id, signed_date, mn_id, approval_type, comments) " +
			"values (:user, :approvalID, :signed, :mn, :approvalType, :comments)";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("user", approval.getUser().getId())
			.addValue("approvalID", approval.getApprovalType().getId())
			.addValue("signed", approval.getDateSigned())
			.addValue("mn", approval.getMnId())
			.addValue("approvalType", approval.getType())
			.addValue("comments", approval.getComments());
		
		KeyHolder key = new GeneratedKeyHolder();		
		
		int modified = getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});		
		
		approval.setId(key.getKey().intValue());
		
		return modified;
	}

	public ApprovalModel get(int id)
	{
		String query = "select " +
			"user_id, signed_date, mn_id, approval_id, approval_name, approval_type, comments " +
			"from approval_view " +
			"where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", id);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		ApprovalModel approval = null;
		
		if(rows.next())
		{
			approval = new ApprovalModel();
			
			approval.setId(id);
			approval.setMnId(rows.getInt("mn_id"));
			approval.setApprovalType(Util.createProp(rows, "approval"));
			approval.setType(rows.getInt("approval_type"));
			approval.setDateSigned(rows.getDate("signed_date"));
			approval.setUser(userDao.getId(rows.getInt("user_id")));
			
			SerialClob clob = (SerialClob)rows.getObject("comments");
			
			try
			{
				if(clob != null)
					approval.setComments(clob.getSubString(1, (int) clob.length()));
				else
					approval.setComments("");
			}
			catch(SerialException e)
			{
				approval.setComments("");
			}
		}
		
		return approval;
	}

	public List<ApprovalModel> getAll(int mnId)
	{
		String query = "select " +
				"user_id, signed_date, id, approval_id, approval_name, approval_type, comments, id " +
				"from approval_view " +
				"where mn_id=:id";
			
			SqlParameterSource params = new MapSqlParameterSource()
				.addValue("id", mnId);
			
			SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
			
			List<ApprovalModel> approvals = new ArrayList<ApprovalModel>();
			
			while(rows.next())
			{
				ApprovalModel approval = new ApprovalModel();
				
				approval.setId(rows.getInt("id"));
				approval.setMnId(mnId);
				approval.setApprovalType(Util.createProp(rows, "approval"));
				approval.setType(rows.getInt("approval_type"));
				approval.setDateSigned(rows.getDate("signed_date"));
				approval.setUser(userDao.getId(rows.getInt("user_id")));
				
				SerialClob clob = (SerialClob)rows.getObject("comments");
				
				try
				{
					if(clob != null)
						approval.setComments(clob.getSubString(1, (int) clob.length()));
					else
						approval.setComments("");
				}
				catch(SerialException e)
				{
					approval.setComments("");
				}
				
				approvals.add(approval);
			}
			
			return approvals;
	}
	
	public int update(ApprovalModel approval)
	{
		final String query = "update approval_details " +
			"set comments=:comm " +
			"where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("comm", approval.getComments())
			.addValue("id", approval.getId());
		
		return getNamedParameterJdbcTemplate().update(query, params);
	}

	public int remove(int id)
	{
		final String query = "delete from approval_details " +
			"where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", id);
		
		return getNamedParameterJdbcTemplate().update(query, params);
	}
}
