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
import com.fedex.mn.daos.EditTransDaoInterface;
import com.fedex.mn.models.impls.EditField;
import com.fedex.mn.models.impls.EditTrans;

@Repository
@Transactional(rollbackFor=Exception.class)
public class EditTransDAO
	extends NamedParameterJdbcDaoSupport
	implements EditTransDaoInterface
{
	@Autowired
	@Qualifier("ds")
	private DataSource ds;
	
	@Autowired
	private UserDAO userDao;
	
	@Autowired
	private EditFieldDaoInterface editFieldDao;
	
	@PostConstruct
	public void init()
	{
		setDataSource(ds);
	}

	@Override
	public int add(EditTrans trans)
	{
		String query = "insert into edit_transactions " +
				"(mn_id, edited_by, date_edited) " +
				"values (:mnId, :editBy, :editDate)";
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("mnId", trans.getMnId())
			.addValue("editBy", trans.getEditedBy().getId())
			.addValue("editDate", trans.getDateEdited());
		KeyHolder key = new GeneratedKeyHolder();
		
		int modified = getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		
		trans.setId(key.getKey().intValue());
		for(EditField field : trans.getEditFields())
		{
			field.setEditTransaction(trans.getId());
			editFieldDao.add(field);
		}
		
		return modified;
	}

	@Override
	public EditTrans get(int id)
	{
		String query = "select mn_id, edited_by, date_edited " +
				"from edit_transactions " +
				"where id=:id";
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		EditTrans edit = null;
		
		if(rows.next())
		{
			edit = new EditTrans();
			
			edit.setId(id);
			edit.setMnId(rows.getInt("mn_id"));
			edit.setDateEdited(rows.getDate("date_edited"));
			edit.setEditedBy(userDao.getId(rows.getInt("edited_by")));
			
			edit.getEditFields().addAll(editFieldDao.getAll(id));
		}
		
		return edit;
	}

	@Override
	public List<EditTrans> getAll(int mnId)
	{
		String query = "select id, edited_by, date_edited " +
				"from edit_transactions " +
				"where mn_id=:id order by date_edited desc";
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", mnId);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		List<EditTrans> edits = new ArrayList<EditTrans>();
		
		while(rows.next())
		{
			EditTrans edit = new EditTrans();
			
			edit.setId(rows.getInt("id"));
			edit.setMnId(mnId);
			edit.setDateEdited(rows.getDate("date_edited"));
			edit.setEditedBy(userDao.getId(rows.getInt("edited_by")));
			
			edit.getEditFields().addAll(editFieldDao.getAll(edit.getId()));
			
			edits.add(edit);
		}
		
		return edits;
	}

}
