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
import org.springframework.transaction.annotation.Transactional;

import com.fedex.mn.daos.DeploymentDetailDaoInterface;
import com.fedex.mn.models.impls.DeploymentDetailModel;

@Repository
@Transactional(rollbackFor = Exception.class)
public class DeploymentDetailDAO 
	extends NamedParameterJdbcDaoSupport
	implements DeploymentDetailDaoInterface
{
	@Autowired
	@Qualifier("ds")
	private DataSource ds;
	
	@PostConstruct
	public void init()
	{
		setDataSource(ds);
	}

	@Override
	public int add(DeploymentDetailModel model)
	{
		String query = "insert into deployment_details " +
				"(alps_config, wl_config, pre_patch_text, post_patch_text, deployment_text) " +
				"values (:alps, :wl, :preText, :postText, :depText)";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("alps", model.getAlpsConfig())
			.addValue("wl", model.getWlConfig())
			.addValue("preText", model.getPrePatchText())
			.addValue("postText", model.getPostPatchText())
			.addValue("depText", model.getDeploymentText());
		
		KeyHolder key = new GeneratedKeyHolder();
		
		int modified = getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		
		model.setId(key.getKey().intValue());
		
		return modified;
	}

	@Override
	public DeploymentDetailModel get(int id)
	{
		String query = "select alps_config, wl_config, pre_patch_text, post_patch_text, deployment_text " +
				"from deployment_details where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
		
		DeploymentDetailModel model = null;
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
				
		if(rows.next())
		{
			model = new DeploymentDetailModel();
			model.setId(id);
			
			SerialClob clob = (SerialClob)rows.getObject("alps_config");
			
			try
			{
				if(clob != null)
					model.setAlpsConfig(clob.getSubString(1, (int)clob.length()));
				else
					model.setAlpsConfig("");
			}
			catch(Exception e)
			{
				model.setAlpsConfig("");
			}
			
			clob = (SerialClob) rows.getObject("wl_config");
			
			try
			{
				if(clob != null)
					model.setWlConfig(clob.getSubString(1, (int)clob.length()));
				else
					model.setWlConfig("");
			}
			catch(Exception e)
			{
				model.setWlConfig("");
			}
			
			clob = (SerialClob) rows.getObject("pre_patch_text");
			
			try
			{
				if(clob != null)
					model.setPrePatchText(clob.getSubString(1, (int)clob.length()));
				else
					model.setPrePatchText("");
			}
			catch(SerialException e)
			{
				model.setPrePatchText("");
			}
			
			clob = (SerialClob) rows.getObject("post_patch_text");
			
			try
			{
				if(clob != null)
					model.setPostPatchText(clob.getSubString(1, (int)clob.length()));
				else
					model.setPostPatchText("");
			}
			catch(SerialException e)
			{
				model.setPostPatchText("");
			}
			
			clob = (SerialClob) rows.getObject("deployment_text");
			
			try
			{
				if(clob != null)
					model.setDeploymentText(clob.getSubString(1, (int)clob.length()));
				else
					model.setDeploymentText("");
			}
			catch(SerialException e)
			{
				model.setDeploymentText("");
			}
		}
		
		return model;
	}

	@Override
	public int update(DeploymentDetailModel model)
	{
		String query = "update deployment_details set " +
				"alps_config=:alps, wl_config=:wl, pre_patch_text=:preText, post_patch_text=:postText, deployment_text=:depText " +
				"where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("alps", model.getAlpsConfig())
			.addValue("wl", model.getWlConfig())
			.addValue("preText", model.getPrePatchText())
			.addValue("postText", model.getPostPatchText())
			.addValue("depText", model.getDeploymentText())
			.addValue("id", model.getId());
		
		return getNamedParameterJdbcTemplate().update(query, params);
	}

}
