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

import com.fedex.mn.daos.BasicDetailDaoInterface;
import com.fedex.mn.models.impls.SyncModel;
import com.fedex.mn.models.impls.BasicDetailModel;
import com.fedex.mn.utils.Util;

@Repository
@Transactional(rollbackFor = Exception.class)
public class BasicDetailDAO
	extends NamedParameterJdbcDaoSupport
	implements BasicDetailDaoInterface
{	
	@Autowired
	@Qualifier("ds")
	private DataSource ds;
	
	@Autowired
	private UserDAO userDao;
	
	@Autowired
	private ReleaseDAO relDao;
	
	@Autowired
	private StaticTableDAO stDao;
	
	@PostConstruct
	public void init()
	{
		setDataSource(ds);
	}
	
	public int add(BasicDetailModel basic)
	{
		if(basic.getSyncs() != null)
			addSyncs(basic.getSyncs());			
		
		final String query = "insert into basic_details (status, symphony_profile, " +
				"load_type, originator, change_type, artifact, dependency, dependency_mn, " +
				"load_team, summary, comments, destination, release_id, " +
				"expected_load_date, sync_data) " +
				"values (:status, :srs, :load, :orig, :change, :art, :dep, :depMN, :loadTeam, " +
				":summary, :comments, :dest, :release, :exp, :bl)";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("status", basic.getStatus().getId())
			.addValue("srs", basic.getSymphonyProfile().getId())
			.addValue("load", basic.getLoadType().getId())
			.addValue("orig", basic.getOriginator().getId())
			.addValue("change", basic.getChangeType().getId())
			.addValue("art", basic.getArtifact())
			.addValue("dep", basic.getDependency().getId())
			.addValue("depMN", Util.createString(basic.getDependencyIds()))
			.addValue("loadTeam", Util.makeStringofProperties(basic.getLoadTeam()))
			.addValue("summary", basic.getSummary())
			.addValue("comments", basic.getComments())
			.addValue("dest", basic.getDestination().getId())
			.addValue("release", basic.getRelease().getId())
			.addValue("exp", basic.getExpectedLoadDate())
			.addValue("bl", (basic.getSyncs() == null ? null : basic.getSyncs().getId()));		
		
		KeyHolder key = new GeneratedKeyHolder();				
		
		int modified = getNamedParameterJdbcTemplate().update(query, params, key, new String[] {"id"});	

		basic.setId(key.getKey().intValue());	
		
		return modified;
	}
	
	public BasicDetailModel get(int id)
	{
		String query = "select actual_load_date, change_id, " +
				"change_name, comments, artifact, dependency_id, dependency_name, dependency_mn, destination_id, " +
				"destination_name, load_teams, load_type_id, load_type_name, originator_id, release_id, " +
				"expected_load_date, status_name, status_id, summary, srs_id, srs_name, " +
				"sync_id, sync_all, sync_none, sync_2, sync_3, sync_4, sync_5, sync_6 " +
				"from basic_detail_view " +
				"where basic_id=:id"; 
				
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
		BasicDetailModel basic = null;
		
		SqlRowSet row = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
			
		if(row.next())
		{
			basic = new BasicDetailModel();
			
			basic.setId(id);	
			basic.setActualLoadDate(row.getDate("actual_load_date"));
											
			basic.setChangeType(Util.createProp(row, "change"));
			
			SerialClob clob = (SerialClob)row.getObject("comments");
			
			try
			{
				if(clob != null)
					basic.setComments(clob.getSubString(1, (int) clob.length()));
				else
					basic.setComments("");
			}
			catch(SerialException e)
			{
				basic.setComments("");
			}
			
			basic.setArtifact(row.getString("artifact"));
			basic.setDependency(Util.createProp(row, "dependency"));
			
			clob = (SerialClob)row.getObject("dependency_mn");
			
			try
			{
				if(clob != null)
					basic.setDependencyIds(Util.createList(clob.getSubString(1, (int) clob.length())));
			}
			catch(SerialException e)
			{}
			
			basic.setDestination(Util.createProp(row, "destination"));
			basic.setLoadTeam(Util.makeListofProperties(row.getString("load_teams"), stDao.getProperties("symphony_profile_types")));
			basic.setLoadType(Util.createProp(row, "load_type"));
			basic.setExpectedLoadDate(row.getDate("expected_load_date"));
			basic.setOriginator(userDao.getId(row.getInt("originator_id")));
			basic.setRelease(relDao.getRelease(row.getInt("release_id")));
			basic.setStatus(Util.createProp(row, "status"));
			
			clob = (SerialClob)row.getObject("summary");
			
			try
			{
				if(clob != null)					
					basic.setSummary(clob.getSubString(1, (int) clob.length()));
				else
					basic.setSummary("");
			}
			catch(SerialException e)
			{
				basic.setSummary("");
			}
			
			basic.setSymphonyProfile(Util.createProp(row, "srs"));
			
			SyncModel sync = new SyncModel();
			sync.setId(row.getInt("sync_id"));
			
			sync.getValues().put("all", Util.convertIntToBoolean(row.getInt("sync_all")));
			sync.getValues().put("none", Util.convertIntToBoolean(row.getInt("sync_none")));
			sync.getValues().put("2", Util.convertIntToBoolean(row.getInt("sync_2")));
			sync.getValues().put("3", Util.convertIntToBoolean(row.getInt("sync_3")));
			sync.getValues().put("4", Util.convertIntToBoolean(row.getInt("sync_4")));
			sync.getValues().put("5", Util.convertIntToBoolean(row.getInt("sync_5")));
			sync.getValues().put("6", Util.convertIntToBoolean(row.getInt("sync_6")));
			
			basic.setSyncs(sync);
		}
		
		return basic;
	}

	@Override
	public int update(BasicDetailModel basic)
	{
		if(basic.getSyncs() != null)
		{
			if(basic.getSyncs().getId() != 0)
				updateSyncs(basic.getSyncs());
			else
				addSyncs(basic.getSyncs());
		}
		
		final String query = "update basic_details set status=:status, symphony_profile=:srs, " +
				"load_type=:load, change_type=:change, artifact=:art, dependency=:dep, sync_data=:sync, " +
				"dependency_mn=:depMN, load_team=:loadTeam, summary=:summary, comments=:comments, " +
				"destination=:dest, release_id=:release, expected_load_date=:exp, actual_load_date=:act " +
				"where id=:id";
				
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("status", basic.getStatus().getId())
			.addValue("srs", basic.getSymphonyProfile().getId())
			.addValue("load", basic.getLoadType().getId())
			.addValue("sync", (basic.getSyncs() == null ? null : basic.getSyncs().getId()))
			.addValue("change", basic.getChangeType().getId())
			.addValue("art", basic.getArtifact())
			.addValue("dep", basic.getDependency().getId())
			.addValue("depMN", Util.createString(basic.getDependencyIds()))
			.addValue("loadTeam", Util.makeStringofProperties(basic.getLoadTeam()))
			.addValue("summary", basic.getSummary())
			.addValue("comments", basic.getComments())
			.addValue("dest", basic.getDestination().getId())
			.addValue("release", basic.getRelease().getId())
			.addValue("exp", basic.getExpectedLoadDate())
			.addValue("act", basic.getActualLoadDate())
			.addValue("id", basic.getId());
		
		return getNamedParameterJdbcTemplate().update(query, params);
	}
	
	private int addSyncs(SyncModel sync)
	{
		final String query = "insert into sync_details " +
			"(sync_all, sync_none, sync_2, sync_3, sync_4, sync_5, sync_6) " +
			"values(:all, :none, :2, :3, :4, :5, :6)";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("all", sync.getValues().get("all"))
			.addValue("none", sync.getValues().get("none"))
			.addValue("2", sync.getValues().get("2"))
			.addValue("3", sync.getValues().get("3"))
			.addValue("4", sync.getValues().get("4"))
			.addValue("5", sync.getValues().get("5"))
			.addValue("6", sync.getValues().get("6"));
		
		KeyHolder key = new GeneratedKeyHolder();
		
		int row = getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		
		if(row > 0)
			sync.setId(key.getKey().intValue());
		
		return row;
	}
	
	private int updateSyncs(SyncModel sync)
	{				
		final String query = "update sync_details " +
			"set sync_all=:all, sync_none=:none, sync_2=:2, sync_3=:3, sync_4=:4, sync_5=:5, sync_6=:6 " +
			"where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("all", sync.getValues().get("all"))
			.addValue("none", sync.getValues().get("none"))
			.addValue("2", sync.getValues().get("2"))
			.addValue("3", sync.getValues().get("3"))
			.addValue("4", sync.getValues().get("4"))
			.addValue("5", sync.getValues().get("5"))
			.addValue("6", sync.getValues().get("6"))
			.addValue("id", sync.getId());
		
		return getNamedParameterJdbcTemplate().update(query, params);
	}
}
