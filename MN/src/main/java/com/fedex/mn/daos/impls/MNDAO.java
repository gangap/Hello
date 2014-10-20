package com.fedex.mn.daos.impls;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fedex.mn.daos.BasicDetailDaoInterface;
import com.fedex.mn.daos.BuildDetailDaoInterface;
import com.fedex.mn.daos.DeploymentDetailDaoInterface;
import com.fedex.mn.daos.EditTransDaoInterface;
import com.fedex.mn.daos.MNDaoInterface;
import com.fedex.mn.daos.TestDetailDaoInterface;
import com.fedex.mn.models.impls.BuildDetailModel;
import com.fedex.mn.models.impls.DeploymentDetailModel;
import com.fedex.mn.models.impls.EditTrans;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.models.impls.MNView;
import com.fedex.mn.models.impls.TestDetailModel;

@Repository
@Transactional(rollbackFor = Exception.class)
public class MNDAO
	extends NamedParameterJdbcDaoSupport
	implements MNDaoInterface
{		
	@Autowired
	@Qualifier("ds")
	private DataSource ds;
	
	@PostConstruct
	public void init()
	{
		setDataSource(ds);
		((JdbcTemplate)getNamedParameterJdbcTemplate().getJdbcOperations()).setFetchSize(500);
	}
	
	@Autowired
	private BasicDetailDaoInterface basicDao;
	
	@Autowired
	private TestDetailDaoInterface testDao;
	
	@Autowired
	private BuildDetailDaoInterface buildDao;
	
	@Autowired
	private DeploymentDetailDaoInterface deployDao;
	
	@Autowired
	private EditTransDaoInterface editDao;

	public int add(MNModel model)
	{
		basicDao.add(model.getBasicDetail());
		testDao.add(model.getTestDetail());
		buildDao.add(model.getBuildDetail());
		deployDao.add(model.getDeploymentDetail());
		
		final String query = "insert into mns (parent_id, basic_detail_data, test_detail_data, " +
				"build_detail_data, deployment_detail_data, last_modified, date_created) " +
				"values (:parent, :basic, :test, :build, :deploy, :last, :create)";
		
		model.setDateCreated(Calendar.getInstance().getTime());
		model.setLastModified(model.getDateCreated());
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("parent", model.getParentID() == 0 ? null : model.getParentID())
			.addValue("basic", model.getBasicDetail().getId())
			.addValue("test", model.getTestDetail().getId())
			.addValue("build", model.getBuildDetail().getId())
			.addValue("deploy", model.getDeploymentDetail().getId())
			.addValue("last", model.getLastModified())
			.addValue("create", model.getDateCreated());
		
		KeyHolder key = new GeneratedKeyHolder();
		
		int modified = getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		
		model.setId(key.getKey().intValue());	
		
		return modified;
	}

	public MNModel get(int mnId)
	{
		String query = "select parent_id, basic_detail_data, test_detail_data, build_detail_data, " +
				"deployment_detail_data, last_modified, date_created from mns where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", mnId);
		
		MNModel mn = new MNModel();
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(rows.next())
		{
			mn.setId(mnId);
			
			mn.setParentID(rows.getInt("parent_id"));
			
			mn.setBasicDetail(basicDao.get(rows.getInt("basic_detail_data")));					
			mn.setBuildDetail(buildDao.get(rows.getInt("build_detail_data")));		
			if(mn.getBuildDetail() == null)
			{
				mn.setBuildDetail(new BuildDetailModel());
			}
			mn.setTestDetail(testDao.get(rows.getInt("test_detail_data")));
			if(mn.getTestDetail() == null)
			{
				mn.setTestDetail(new TestDetailModel());
			}
			mn.setDeploymentDetail(deployDao.get(rows.getInt("deployment_detail_data")));
			if(mn.getDeploymentDetail() == null)
			{
				mn.setDeploymentDetail(new DeploymentDetailModel());
			}
			mn.setLastModified(rows.getDate("last_modified"));
			mn.setDateCreated(rows.getDate("date_created"));			
		}		
		
		return mn;
	}	

	public MNView getMNView(int mnId)
	{
		String query = "select mn_id, parent_id, created, artifact, status_val, exp_load_date," +
				"project_val, srs_val, orig_first, orig_last, tracker, patch_names, dest_val, " +
				"release_val, comments, change_val, load_val, source_code, actual_deliverables " +
				"from basic_mn_view " +
				"where mn_id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", mnId);
		
		MNView view = new MNView();
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(rows.next())
		{
			view.setMnId(mnId);
			view.setParentId(rows.getInt("parent_id"));
			
			SerialClob clob = (SerialClob)rows.getObject("comments");
			try
			{
				if(clob != null)
					view.setComment(clob.getSubString(1, (int) clob.length()));
				else
					view.setComment("");
			}
			catch(SerialException e)
			{
				view.setComment("");
			}
			
			view.setCreated(rows.getDate("created"));
			view.setExpLoadDate(rows.getDate("exp_load_date"));
			
			view.setArtifact(rows.getString("artifact"));
			view.setTracker(rows.getString("tracker"));
			view.setPatchNames(rows.getString("patch_names"));
			view.setName(rows.getString("orig_first") + " " + rows.getString("orig_last"));
			view.setProject(rows.getString("project_val"));
			view.setRelease(rows.getString("release_val"));
			view.setSrs(rows.getString("srs_val"));
			view.setStatus(rows.getString("status_val"));
			view.setChangeType(rows.getString("change_val"));
			view.setDest(rows.getString("dest_val"));

			clob = (SerialClob)rows.getObject("actual_deliverables");
			try
			{
				if(clob != null)
					view.setActDeliverables(clob.getSubString(1, (int) clob.length()));
				else
					view.setActDeliverables("");
			}
			catch(SerialException e)
			{
				view.setActDeliverables("");
			}
			
			clob = (SerialClob)rows.getObject("source_code");
			try
			{
				if(clob != null)
					view.setSourceCode(clob.getSubString(1, (int) clob.length()));
				else
					view.setSourceCode("");	
			}
			catch(SerialException e)
			{
				view.setSourceCode("");
			}
			
			view.setLoadType(rows.getString("load_val"));
		}
		
		return view;
	}
	
	public List<MNView> getAllMNViews()
	{
		String query = "select mn_id, parent_id, created, artifact, status_val, project_val, exp_load_date, dest_val, " +
				"srs_val, orig_first, orig_last, tracker, patch_names, source_code, actual_deliverables, " +
				"release_val, comments, change_val, load_val " +
				"from basic_mn_view " +
				"order by mn_id";
		
		List<MNView> views = new ArrayList<MNView>();
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, new MapSqlParameterSource());
		
		while(rows.next())
		{
			MNView view = new MNView();
			
			view.setMnId(rows.getInt("mn_id"));
			view.setParentId(rows.getInt("parent_id"));
			view.setChangeType(rows.getString("change_val"));
			
			SerialClob clob = (SerialClob)rows.getObject("comments");
			try
			{
				if(clob != null)
					view.setComment(clob.getSubString(1, (int) clob.length()));
				else
					view.setComment("");
			}
			catch(SerialException e)
			{
				view.setComment("");
			}
			
			view.setArtifact(rows.getString("artifact"));
			view.setCreated(rows.getDate("created"));
			view.setExpLoadDate(rows.getDate("exp_load_date"));
			view.setTracker(rows.getString("tracker"));
			view.setPatchNames(rows.getString("patch_names"));
			view.setLoadType(rows.getString("load_val"));
			view.setName(rows.getString("orig_first") + " " + rows.getString("orig_last"));
			view.setProject(rows.getString("project_val"));
			view.setRelease(rows.getString("release_val"));
			view.setDest(rows.getString("dest_val"));
			
			clob = (SerialClob)rows.getObject("actual_deliverables");
			try
			{
				if(clob != null)
					view.setActDeliverables(clob.getSubString(1, (int) clob.length()));
				else
					view.setActDeliverables("");
			}
			catch(SerialException e)
			{
				view.setActDeliverables("");
			}
			
			clob = (SerialClob)rows.getObject("source_code");
			try
			{
				if(clob != null)
					view.setSourceCode(clob.getSubString(1, (int) clob.length()));
				else
					view.setSourceCode("");	
			}
			catch(SerialException e)
			{
				view.setSourceCode("");
			}
			
			view.setSrs(rows.getString("srs_val"));
			view.setStatus(rows.getString("status_val"));
			
			views.add(view);
		}
		
		return views;
	}

	public List<MNView> getAllMNViewsFilters(String filterType, String filterVal)
	{
		String query = "";
		
		if(filterType.toLowerCase().equals("release"))
		{
			query = "select mn_id, parent_id, created, artifact, status_val, project_val, exp_load_date," +
					"srs_val, orig_first, orig_last, patch_names, tracker, source_code, dest_val, " +
					"release_val, comments, actual_deliverables  " +
					"from basic_mn_view " +
					"where release_val=:relName order by mn_id";
		}
		
		List<MNView> views = new ArrayList<MNView>();
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, new MapSqlParameterSource());
		
		if(rows.next())
		{
			MNView view = new MNView();			
			
			view.setMnId(rows.getInt("mn_id"));
			view.setParentId(rows.getInt("parent_id"));
			view.setChangeType(rows.getString("change_val"));
			
			SerialClob clob = (SerialClob)rows.getObject("comments");
			try
			{
				if(clob != null)
					view.setComment(clob.getSubString(1, (int) clob.length()));
				else
					view.setComment("");
			}
			catch(SerialException e)
			{
				view.setComment("");
			}
			
			view.setArtifact(rows.getString("artifact"));
			view.setCreated(rows.getDate("created"));
			view.setExpLoadDate(rows.getDate("exp_load_date"));
			view.setTracker(rows.getString("tracker"));
			view.setPatchNames(rows.getString("patch_names"));
			view.setLoadType(rows.getString("load_val"));
			view.setName(rows.getString("orig_first") + " " + rows.getString("orig_last"));
			view.setProject(rows.getString("project_val"));
			view.setRelease(rows.getString("release_val"));
			view.setSrs(rows.getString("srs_val"));
			view.setDest(rows.getString("dest_val"));
			
			clob = (SerialClob)rows.getObject("actual_deliverables");
			try
			{
				if(clob != null)
					view.setActDeliverables(clob.getSubString(1, (int) clob.length()));
				else
					view.setActDeliverables("");
			}
			catch(SerialException e)
			{
				view.setActDeliverables("");
			}
			
			clob = (SerialClob)rows.getObject("source_code");
			try
			{
				if(clob != null)
					view.setSourceCode(clob.getSubString(1, (int) clob.length()));
				else
					view.setSourceCode("");	
			}
			catch(SerialException e)
			{
				view.setSourceCode("");
			}
			
			view.setStatus(rows.getString("status"));
			
			views.add(view);
		}
		
		return views;
	}

	@Override
	public int update(MNModel mn, EditTrans edits)
	{
		basicDao.update(mn.getBasicDetail());
		testDao.update(mn.getTestDetail());
		buildDao.update(mn.getBuildDetail());
		deployDao.update(mn.getDeploymentDetail());
		
		if(edits != null)
			editDao.add(edits);
		
		String query = "update mns set last_modified=:last, last_modified_by=:last_by where id=:id";
		
		mn.setLastModified(Calendar.getInstance().getTime());
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("last", mn.getLastModified())
			.addValue("last_by", mn.getLastModifiedBy().getId())
			.addValue("id", mn.getId());
		
		return getNamedParameterJdbcTemplate().update(query, params);
	}

	@Override
	public List<Integer> getChildMN(int mnId)
	{
		List<Integer> mns = new ArrayList<Integer>();
		
		String query = "select id " +
			"from mns " +
			"where parent_id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", mnId);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		while(rows.next())
		{
			mns.add(rows.getInt("id"));
		}			
		
		return mns;
	}
}
