package com.fedex.mn.daos.impls;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import com.fedex.mn.models.impls.AppStateModel;
import com.fedex.mn.models.impls.DBProperty;

@Repository
public class StaticTableDAO
	extends NamedParameterJdbcDaoSupport
{	
	@Autowired
	@Qualifier("ds")
	private DataSource ds;
	
	@PostConstruct
	public void init()
	{
		setDataSource(ds);
	}
	
	public void add(String table, DBProperty prop)
	{
		String query = "insert into " + table + " (name, date_created) values (:name, :created)";
		
		SqlParameterSource map = new MapSqlParameterSource()
			.addValue("name", prop.getName())
			.addValue("created", Calendar.getInstance().getTime());
		
		KeyHolder key = new GeneratedKeyHolder();
		
		getNamedParameterJdbcTemplate().update(query, map, key, new String[]{"id"});
		
		prop.setId(key.getKey().intValue());
	}
	
	public DBProperty getProperty(String table, int id)
	{
		String query = "select name, date_created from " + table + " where id=:id";
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", id);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		DBProperty prop = new DBProperty();
		if(rows.next())
		{			
			prop.setName(rows.getString("name"));
			prop.setId(id);
			prop.setDateCreated(rows.getDate("date_created"));
		}
		
		return prop;
	}
	
	public int getId(String table, String name)
	{
		String query = "select id from " + table + " where name=:name";
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("name", name);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		int id = -1;
		while(rows.next())
		{
			id = rows.getInt("id");
		}
		
		return id;		
	}
	
	public int remove(String table, int id)
	{
		String query = "delete from " + table + " where id=:id";
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", id);
		
		return getNamedParameterJdbcTemplate().update(query, params);
	}
	
	public List<DBProperty> getProperties(String table)
	{
		String query = "select name, id, date_created from " + table + " order by card, name";
		SqlParameterSource params = new MapSqlParameterSource();
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		ArrayList<DBProperty> props = new ArrayList<DBProperty>(0);
		while(rows.next())
		{
			DBProperty prop = new DBProperty();
			prop.setId(rows.getInt("id"));
			prop.setName(rows.getString("name"));
			prop.setDateCreated(rows.getDate("date_created"));
			
			props.add(prop);
		}
		
		return props;		
	}
	
	public int getCount(String table)
	{
		String query = "select count(id) from " + table;
		SqlParameterSource params = new MapSqlParameterSource();
	
		return getNamedParameterJdbcTemplate().queryForInt(query, params);
	}
	
	public int updateAppState(AppStateModel appState)
	{
		String query = "update app_state_details " +
			"set l3c5=:flag, ldap_user=:user, ldap_pass=:pass, last_started=:last " +
			"where id=0";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("flag", (appState.isL3c5Flag() ? 1 : 0))
			.addValue("last", appState.getLastStarted())
			.addValue("user", appState.getLdapUser())
			.addValue("pass", appState.getLdapPass());
		
		return getNamedParameterJdbcTemplate().update(query, params);		
	}
	
	public AppStateModel getAppState()
	{
		AppStateModel appState = null;
		
		String query = "select * from app_state_details where id=0";
		
		SqlRowSet row = getNamedParameterJdbcTemplate().queryForRowSet(query, new MapSqlParameterSource());
		
		if(row.next())
		{
			appState = new AppStateModel();
			
			if(row.getInt("l3c5") == 1)
				appState.setL3c5Flag(true);
			else
				appState.setL3c5Flag(false);
			
			appState.setLdapUser(row.getInt("ldap_user"));
			appState.setLdapPass(row.getString("ldap_pass"));
			
			appState.setLastStarted(row.getDate("last_started"));
		}
		
		return appState;
	}
}
