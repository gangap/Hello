package com.fedex.mn.daos.impls;  

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List; 

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.lob.OracleLobHandler;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fedex.mn.daos.AttachmentDaoInterface;
import com.fedex.mn.models.impls.AbinitioAttachment;
import com.fedex.mn.models.impls.Attachment;

@Repository
@Transactional(rollbackFor = Exception.class)
public class AttachmentDAO 
	extends NamedParameterJdbcDaoSupport 
	implements AttachmentDaoInterface
{
	@Autowired
	@Qualifier("ds")
	private DataSource ds;
	
	@Autowired
	private UserDAO userDao;
	
	private static Logger log = Logger.getLogger(AttachmentDAO.class);
	
	@PostConstruct
	public void init()
	{
		setDataSource(ds);
	}

	@Override
	public int add(Attachment attachment)
	{
		final String query = "insert into attachments " +
			"(mn_id, upload_user, upload_date, display_name, upload_file, type, comments) " +
			"values (:mn, :user, :date, :disp, :up, :type, :comments)";		
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("mn", attachment.getMnId())
			.addValue("user", attachment.getUploadUser().getId())
			.addValue("date", Calendar.getInstance().getTime())
			.addValue("up", attachment.getUploadFile())
			.addValue("disp", attachment.getDisplayName())
			.addValue("type", attachment.getType())
			.addValue("comments", attachment.getComment());
		
		KeyHolder key = new GeneratedKeyHolder();
		
		int modified = getNamedParameterJdbcTemplate().update(query, params, key, new String[] {"id"});	
		
		attachment.setId(key.getKey().intValue());
		
		return modified;
	}

	@Override
	public Attachment get(int id)
	{		
		String query = "select id," +
			"mn_id, upload_user, upload_date, display_name, upload_file, type, comments " +
			"from attachments " +
			"where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		return getNamedParameterJdbcTemplate().queryForObject(query, params, new AttachmentMapper());
	}

	@Override
	public List<Attachment> getAll(int mnId)
	{	
		String query = "select " +
			"id, upload_user, upload_date, display_name, upload_file, type, comments " +
			"from attachments " +
			"where mn_id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", mnId);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		return getNamedParameterJdbcTemplate().query(query, params, new AttachmentMapper());
	}

	@Override
	public List<Attachment> getAllofTypes(int mnId, String type)
	{		
		String query = "select " +
			"* " +
			"from attachments " +
			"where mn_id=:id and type=:type";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", mnId)
			.addValue("type", type);
		
		return getNamedParameterJdbcTemplate().query(query, params, new AttachmentMapper());
	}

	@Override
	public int delete(int id)
	{
		String query = "delete from attachments where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
		
		return getNamedParameterJdbcTemplate().update(query, params);
	}
	
	public AbinitioAttachment getAbinitioAttachment(int mnId)
	{
		String query = "select * from abinitio_manifest_details where mn_id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", mnId);
		
		return getNamedParameterJdbcTemplate().queryForObject(query, params, new AbinitioMapper());
	}
	
	@Override
	public boolean hasAbinitioAttachment(int mnId)
	{
		String query = "select count(id) from abinitio_manifest_details where mn_id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", mnId);
		
		int rows = getNamedParameterJdbcTemplate().queryForInt(query, params);
		
		if(rows == 0)
			return false;
		else
			return true;
	}
	
	private final class AbinitioMapper
		implements RowMapper<AbinitioAttachment>
	{

		@Override
		public AbinitioAttachment mapRow(ResultSet rows, int rowNum)
			throws SQLException
		{
			AbinitioAttachment attachment = new AbinitioAttachment();
			
			attachment.setId(rows.getInt("id"));
			attachment.setMnId(rows.getInt("mn_id"));
			
			Blob blob = rows.getBlob("manifest_file");
			try
			{
				attachment.setFile((blob != null ? blob.getBytes(1, (int) blob.length()) : null));
			} 
			catch (SerialException e)
			{
				log.error(e);
			}
			
			return attachment;
		}
		
	}
	
	private final class AttachmentMapper
		implements RowMapper<Attachment>
	{

		@Override
		public Attachment mapRow(ResultSet rows, int rowNum) 
			throws SQLException
		{
			Attachment attachment = new Attachment();
			
			attachment.setId(rows.getInt("id"));
			attachment.setDisplayName(rows.getString("display_name"));
			attachment.setMnId(rows.getInt("mn_id"));
			
			SerialClob clob = (SerialClob)rows.getBlob("comments");
			
			try
			{
				if(clob != null)
					attachment.setComment(clob.getSubString(1, (int) clob.length()));
				else
					attachment.setComment("");
			}
			catch(SerialException e)
			{
				attachment.setComment("");
			}
			
			attachment.setType(rows.getString("type"));
			attachment.setUploadDate(rows.getDate("upload_date"));
			
			Blob blob = rows.getBlob("upload_file");
			try
			{
				attachment.setUploadFile((blob != null ? blob.getBytes(1, (int) blob.length()) : null));
			} 
			catch (SerialException e)
			{
				log.error(e);
			}
			catch(Exception e)
			{
				log.error(e);
			}
			
			attachment.setUploadUser(userDao.getId(rows.getInt("upload_user")));
			
			return attachment;
		}
		
	}
}
