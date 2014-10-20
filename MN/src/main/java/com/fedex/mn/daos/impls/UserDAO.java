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

import com.fedex.mn.models.impls.UserModel;

@Repository
public class UserDAO
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
	
	public int add(UserModel user)
	{
		final String query = "insert into user_details (fed_id, first_name, last_name, division, " +
				"phone, pager, email, date_created) values (:fed, :first, :last, :div, :phone, :page, :email, :date)";

		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("fed", user.getFedExId())
			.addValue("first", user.getFirstName())
			.addValue("last", user.getLastName())
			.addValue("div", user.getDivision())
			.addValue("phone", user.getPhoneExt())
			.addValue("page", user.getPager())
			.addValue("email", user.getEmail())
			.addValue("date", Calendar.getInstance().getTime());
		
		KeyHolder key = new GeneratedKeyHolder();
		
		int rows = getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		
		user.setId(key.getKey().intValue());
		
		return rows;
	}	
	
	public UserModel getFedId(int fedId)
	{
		String query = "select id, first_name, last_name, division, phone, pager, email, date_created " +
				"from user_details where fed_id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", fedId);
		
		UserModel user = null;

		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(rows.next())
		{
			user = new UserModel();
			
			user.setId(rows.getInt("id"));
			user.setFedExId(fedId);
			user.setDivision(rows.getString("division"));
			user.setEmail(rows.getString("email"));			
			user.setFirstName(rows.getString("first_name"));
			user.setLastName(rows.getString("last_name"));
			user.setPager(rows.getString("pager"));
			user.setPhoneExt(rows.getString("phone"));
		}					
		
		return user;		
	}
	
	public UserModel getId(int id)
	{
		String query = "select fed_id, first_name, last_name, division, phone, pager, email, date_created " +
				"from user_details where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", id);
		
		UserModel user = null;
			
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(rows.next())
		{
			user = new UserModel();
			
			user.setId(id);
			user.setDivision(rows.getString("division"));
			user.setEmail(rows.getString("email"));
			user.setFedExId(rows.getInt("fed_id"));
			user.setFirstName(rows.getString("first_name"));
			user.setLastName(rows.getString("last_name"));
			user.setPager(rows.getString("pager"));
			user.setPhoneExt(rows.getString("phone"));
		}					
		
		return user;		
	}
	
	public UserModel get(String first, String last)
	{
		String query = "select id, fed_id, division, phone, pager, email, date_created " +
				"from user_details where first_name=:first and last_name=:last";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("first", first)
			.addValue("last", last);
		
		UserModel user = null;

		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(rows.next())
		{
			user = new UserModel();
			
			user.setId(rows.getInt("id"));
			user.setDivision(rows.getString("division"));
			user.setEmail(rows.getString("email"));
			user.setFedExId(rows.getInt("fed_id"));
			user.setFirstName(first);
			user.setLastName(last);
			user.setPager(rows.getString("pager"));
			user.setPhoneExt(rows.getString("phone"));
		}					
		
		return user;
	}

	public List<UserModel> getAll()
	{
		String query = "select id, fed_id, division, phone, pager, email, date_created, first_name, last_name " +
				"from user_details order by last_name, first_name";
		
		UserModel user = null;

		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, new MapSqlParameterSource());
		
		List<UserModel> users = new ArrayList<UserModel>(0);
		
		while(rows.next())
		{
			user = new UserModel();
			
			user.setId(rows.getInt("id"));
			user.setDivision(rows.getString("division"));
			user.setEmail(rows.getString("email"));
			user.setFedExId(rows.getInt("fed_id"));
			user.setFirstName(rows.getString("first_name"));
			user.setLastName(rows.getString("last_name"));
			user.setPager(rows.getString("pager"));
			user.setPhoneExt(rows.getString("phone"));
			
			users.add(user);
		}					
		
		return users;
	}
	
	public boolean has(int fedId)
	{
		String query = "select count(id) from user_details where fed_id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", fedId);
		
		int count = getNamedParameterJdbcTemplate().queryForInt(query, params);
		
		if(count > 0)
			return true;
		else
			return false;
	}
	
	public List<UserModel> getAllEmails()
	{
		String query = "select name, email " +
				"from email_view order by name";
		
		UserModel user = null;

		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, new MapSqlParameterSource());
		
		List<UserModel> users = new ArrayList<UserModel>(0);
		
		while(rows.next())
		{
			user = new UserModel();
						
			user.setEmail(rows.getString("email"));
			user.setFirstName(rows.getString("name"));
			
			users.add(user);
		}					
		
		return users;
	}
}
