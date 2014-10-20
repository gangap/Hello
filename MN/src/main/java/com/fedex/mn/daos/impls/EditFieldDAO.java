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
import org.springframework.transaction.annotation.Transactional;

import com.fedex.mn.daos.EditFieldDaoInterface;
import com.fedex.mn.models.impls.EditField;

@Repository
@Transactional(rollbackFor=Exception.class)
public class EditFieldDAO
	extends NamedParameterJdbcDaoSupport
	implements EditFieldDaoInterface
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
	public void add(EditField edit)
	{
		String query = "insert into edit_fields " +
				"(edit_trans, field_id, from_value, to_value)" +
				"values (:editId, :fieldId, :from, :to)";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("editId", edit.getEditTransaction())
			.addValue("fieldId", edit.getFieldEdited())
			.addValue("from", edit.getFrom())
			.addValue("to", edit.getTo());
		KeyHolder key = new GeneratedKeyHolder();
		
		getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		
		edit.setId(key.getKey().intValue());
	}

	@Override
	public EditField get(int id)
	{
		String query = "select edit_trans, field_id, from_value, to_value " +
				"from edit_fields where id=:id";
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
		
		EditField edit = null;
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(rows.next())
		{
			edit = new EditField();
			
			edit.setId(id);
			edit.setEditTransaction(rows.getInt("edit_trans"));
			edit.setFieldEdited(rows.getInt("field_id"));
			edit.setFrom(rows.getString("from_value"));
			edit.setTo(rows.getString("to_value"));
		}
		
		return edit;
	}

	@Override
	public List<EditField> getAll(int editTransId)
	{
		String query = "select id, field_id, from_value, to_value " +
				"from edit_fields where edit_trans=:id";
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", editTransId);
		
		List<EditField> edits = new ArrayList<EditField>();
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		while(rows.next())
		{
			EditField edit = new EditField();
			
			edit.setId(rows.getInt("id"));
			edit.setEditTransaction(editTransId);
			edit.setFieldEdited(rows.getInt("field_id"));
			edit.setFrom(rows.getString("from_value"));
			edit.setTo(rows.getString("to_value"));
			
			edits.add(edit);
		}
		
		return edits;
	}

}
