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

import com.fedex.mn.daos.BuildDetailDaoInterface;
import com.fedex.mn.models.impls.BuildDetailModel;
import com.fedex.mn.utils.Util;

@Repository
@Transactional(rollbackFor = Exception.class)
public class BuildDetailDAO
	extends NamedParameterJdbcDaoSupport
	implements BuildDetailDaoInterface
{	
	@Autowired
	@Qualifier("ds")
	private DataSource ds;
	
	@PostConstruct
	public void init()
	{
		setDataSource(ds);
	}
	
	public int add(BuildDetailModel build)
	{
		final String query = "insert into build_details (tracker_num, os, build_project, scm, fml, " +
				"special_instructions, special_instructions_text, source_code, expected_deliverables, " +
				"actual_deliverables, file_packaged, patch_location) " +
				"values (:tracker, :os, :proj, :scm, :fml, :spi, :spiText, " +
				":sc, :exp, :act, :fp, :pl)";	
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("tracker", build.getTrackerNum())
			.addValue("os", build.getOs() == null || build.getOs().getId() == 0 || build.getOs().getId() == -1 ? null : build.getOs().getId())
			.addValue("proj", build.getBuildProject() == null || build.getBuildProject().getId() == 0 || build.getBuildProject().getId() == -1 ? null : build.getBuildProject().getId())
			.addValue("scm", build.getScm() == null || build.getScm().getId() == 0 || build.getScm().getId() == -1 ? null : build.getScm().getId())
			.addValue("fml", build.getFml())
			.addValue("spi", build.getSpecialInstructions())
			.addValue("spiText", build.getSpecialInstructionsText())
			.addValue("sc", build.getSourceCode())
			.addValue("exp", build.getExpDeliverables())
			.addValue("act", build.getActualDeliverables())
			.addValue("fp", build.getFilePackaged())
			.addValue("pl", build.getPatchLocation());
		
		KeyHolder key = new GeneratedKeyHolder();
		
		int modified = getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		
		build.setId(key.getKey().intValue());		
		
		return modified;
	}
	
	public BuildDetailModel get(int id)
	{
		String query = "select * " +
				"from build_detail_view " +
				"where build_id=:id"; 
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
		BuildDetailModel build = null;
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(rows.next())
		{
			build = new BuildDetailModel();
			
			build.setId(id);	
			build.setTrackerNum(rows.getString("tracker_id"));
			build.setOs(Util.createProp(rows, "os"));
			build.setBuildProject(Util.createProp(rows, "project"));
			build.setScm(Util.createProp(rows, "scm"));
			build.setFml(rows.getInt("fml"));
			build.setSpecialInstructions(rows.getInt("special_instructions_id"));
			
			SerialClob clob = (SerialClob)rows.getObject("special_instructions_text");
			
			try
			{
				if(clob != null)
					build.setSpecialInstructionsText(clob.getSubString(1, (int) clob.length()));
				else
					build.setSpecialInstructionsText("");
			}
			catch(SerialException e)
			{
				build.setSpecialInstructionsText("");
			}
			
			clob = (SerialClob)rows.getObject("source_code");
			
			try
			{
				if(clob != null)
					build.setSourceCode(clob.getSubString(1, (int) clob.length()));
				else
					build.setSourceCode("");
			}
			catch(SerialException e)
			{
				build.setSourceCode("");
			}
			
			clob = (SerialClob)rows.getObject("expected_deliverables");
			
			try
			{
				if(clob != null)
					build.setExpDeliverables(clob.getSubString(1, (int) clob.length()));
				else
					build.setExpDeliverables("");
			}
			catch(SerialException e)
			{
				build.setExpDeliverables("");
			}
			
			clob = (SerialClob)rows.getObject("actual_deliverables");
			
			try
			{
				if(clob != null)
					build.setActualDeliverables(clob.getSubString(1, (int) clob.length()));
				else
					build.setActualDeliverables("");
			}
			catch(SerialException e)
			{
				build.setActualDeliverables("");
			}
			
			clob = (SerialClob)rows.getObject("files_packaged");
			
			try
			{
				if(clob != null)
					build.setFilePackaged(clob.getSubString(1, (int) clob.length()));
				else
					build.setFilePackaged("");
			}
			catch(SerialException e)
			{
				build.setFilePackaged(""); 
			}
			
			clob = (SerialClob)rows.getObject("patch_locations");
			
			try
			{
				if(clob != null)
					build.setPatchLocation(clob.getSubString(1, (int) clob.length()));
				else
					build.setPatchLocation("");
			}
			catch(SerialException e)
			{
				build.setPatchLocation("");
			}
		}
		
		return build;
	}

	@Override
	public int update(BuildDetailModel build)
	{
		final String query = "update build_details set tracker_num=:tracker, os=:os, build_project=:proj, " +
				"scm=:scm, fml=:fml, special_instructions=:spi, special_instructions_text=:spiText, " +
				"source_code=:sc, expected_deliverables=:exp, actual_deliverables=:act, " +
				"file_packaged=:fp, patch_location=:pl where id=:id";		
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("tracker", build.getTrackerNum())
			.addValue("os", build.getOs() == null || build.getOs().getId() == 0 || build.getOs().getId() == -1 ? null : build.getOs().getId())
			.addValue("proj", build.getBuildProject() == null || build.getBuildProject().getId() == 0 || build.getBuildProject().getId() == -1 ? null : build.getBuildProject().getId())
			.addValue("scm", build.getScm() == null || build.getScm().getId() == 0 || build.getScm().getId() == -1 ? null : build.getScm().getId())
			.addValue("fml", build.getFml())
			.addValue("spi", build.getSpecialInstructions())
			.addValue("spiText", build.getSpecialInstructionsText())
			.addValue("sc", build.getSourceCode())
			.addValue("exp", build.getExpDeliverables())
			.addValue("act", build.getActualDeliverables())
			.addValue("fp", build.getFilePackaged())
			.addValue("pl", build.getPatchLocation())
			.addValue("id", build.getId());
		
		return getNamedParameterJdbcTemplate().update(query, params);
	}
}
