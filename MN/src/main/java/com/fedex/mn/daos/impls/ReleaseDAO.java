package com.fedex.mn.daos.impls;

import java.util.ArrayList;
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

import com.fedex.mn.models.impls.ReleaseModel;

@Repository
public class ReleaseDAO
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
	
	public int add(ReleaseModel rel)
	{
		String query = "insert into releases (name, date_created, current_release) " +
				"values (:name, :date, :current)";
		SqlParameterSource map = new MapSqlParameterSource()
			.addValue("name", rel.getName())
			.addValue("date", rel.getDateCreated())
			.addValue("current", rel.isCurrent());
		
		KeyHolder key = new GeneratedKeyHolder();

		int modified = getNamedParameterJdbcTemplate().update(query, map, key, new String[]{"id"});
		
		rel.setId(key.getKey().intValue());
		
		return modified;
	}
	
	public int delete(String name)
	{
		String query = "delete from releases where name=:name";
		SqlParameterSource map = new MapSqlParameterSource()
			.addValue("name", name);
		
		return getNamedParameterJdbcTemplate().update(query, map);
	}	
	
	public List<ReleaseModel> getAllReleases()
	{
		String query = "select id, name, date_created, current_release from releases order by date_created asc";
		SqlParameterSource params = new MapSqlParameterSource();
		List<ReleaseModel> releases = new ArrayList<ReleaseModel>(0);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);			
		
		while(rows.next())
		{
			ReleaseModel rel = new ReleaseModel();
			
			rel.setId(rows.getInt("id"));
			rel.setName(rows.getString("name"));
			rel.setDateCreated(rows.getDate("date_created"));
			rel.setCurrent(rows.getBoolean("current_release"));
			
			releases.add(rel);
		}
		
		return releases;
	}
	
	public ReleaseModel getRelease(int id)
	{
		String query = "select name, date_created, current_release from releases where id=:id";
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", id);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);			
		
		ReleaseModel rel = null;
		
		if(rows.next())
		{
			rel = new ReleaseModel();
			
			rel.setId(id);
			rel.setName(rows.getString("name"));
			rel.setDateCreated(rows.getDate("date_created"));		
			rel.setCurrent(rows.getBoolean("current_release"));
		}
		
		return rel;		
	}

	public ReleaseModel getRelease(String name)
	{
		String query = "select id, date_created, current_release from releases where name=:name";
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("name", name);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);			
		
		ReleaseModel rel = null;
		
		if(rows.next())
		{
			rel = new ReleaseModel();
			
			rel.setId(rows.getInt("id"));
			rel.setName(name);
			rel.setDateCreated(rows.getDate("date_created"));	
			rel.setCurrent(rows.getBoolean("current"));
		}
		
		return rel;		
	}
}
