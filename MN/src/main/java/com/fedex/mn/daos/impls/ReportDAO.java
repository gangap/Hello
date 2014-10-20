package com.fedex.mn.daos.impls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fedex.mn.daos.ReportDaoInterface;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.QueryFieldModel;
import com.fedex.mn.models.impls.QueryResultsModel;
import com.fedex.mn.models.impls.ReportFieldModel;
import com.fedex.mn.models.impls.ReportModel;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.utils.Util;

@Repository
@Transactional(rollbackFor = Exception.class)
public class ReportDAO 
	extends NamedParameterJdbcDaoSupport
	implements ReportDaoInterface
{
	@Autowired
	@Qualifier("ds")
	private DataSource ds;
	
	@Autowired
	private UserDAO userDao;
	
	@Autowired
	private StaticTableDAO stDao;
	
	@Autowired
	private EmailService emailServ;
	
	private SimpleDateFormat df;
	private static Logger log = Logger.getLogger(ReportDAO.class);
	
	@PostConstruct
	public void init()
	{
		df = new SimpleDateFormat("EEE, MMM d, yyyy h:mm a ");
		setDataSource(ds);
	}		

	@Override
	public int addReport(ReportModel report)
	{
		String query = "insert into report_details " +
				"(created_by, date_created, last_modified, name) " +
				"values(:by, :created, :last, :name)";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("by", report.getUserCreated().getId())
			.addValue("created", report.getCreated())
			.addValue("last", report.getLastModified())
			.addValue("name", report.getName());
		
		KeyHolder key = new GeneratedKeyHolder();
		
		int modified = getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		
		int keyVal = key.getKey().intValue();		
		
		for(QueryFieldModel field : report.getQueryFields())
		{
			field.setReportId(keyVal);
			addReportQueryDetails(field);
		}
		
		report.setId(keyVal);
		
		return modified;
	}
	
	private void addReportQueryDetails(QueryFieldModel field)
	{
		String query = "insert into report_query_details " +
				"(report_id, report_field_id, card, comparison_id, value) " +
				"values(:report, :field, :card, :comp, :val)";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("report", field.getReportId())
			.addValue("field", field.getFieldId())
			.addValue("card", field.getCard())
			.addValue("comp", field.getComparison())
			.addValue("val", field.getValue());
		
		KeyHolder key = new GeneratedKeyHolder();
		
		getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		
		field.setId(key.getKey().intValue());
	}

	@Override
	public int updateReport(ReportModel report)
	{
		String query = "update report_details " +
				"set last_modified=:last, name=:name " +
				"where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("last", Calendar.getInstance().getTime())
			.addValue("name", report.getName())
			.addValue("id", report.getId());
		
		int modified = getNamedParameterJdbcTemplate().update(query,  params);
		
		List<QueryFieldModel> oldFields = getQueryFields(report.getId());
		
		for(QueryFieldModel oldField : oldFields)
		{
			boolean deleteField = true;
			
			for(QueryFieldModel newField : report.getQueryFields())
			{
				if(oldField.getId() == newField.getId())
				{
					deleteField = false;
					updateQueryField(newField);
					break;
				}
			}
			
			if(deleteField)
				deleteQueryField(oldField.getId());
		}
		
		for(QueryFieldModel newField : report.getQueryFields())
		{
			boolean addField = true;
			
			for(QueryFieldModel oldField : oldFields)
			{
				if(oldField.getId() == newField.getId())
				{
					addField = false;
					break;
				}
			}
			
			if(addField)
			{
				newField.setReportId(report.getId());
				addReportQueryDetails(newField);
			}
		}
		
		return modified;
	}
	
	private void updateQueryField(QueryFieldModel field)
	{
		String query = "update report_query_details " +
				"set card=:card, comparison_id:comp, value=:val " +
				"where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("card", field.getCard())
			.addValue("comparison_id", field.getComparison())
			.addValue("value", field.getValue())
			.addValue("id", field.getId());
		
		getNamedParameterJdbcTemplate().update(query, params);
	}
	
	@Override
	public ReportModel getReport(int id)
	{
		ReportModel report = new ReportModel();
		
		String query = "select created_by, date_created, last_modified, name " +
			"from report_details where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", id);
		
		SqlRowSet row = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(row.next())
		{
			report.setId(id);
			
			report.setCreated(row.getDate("date_created"));
			report.setUserCreated(userDao.getId(row.getInt("created_by")));
			report.setLastModified(row.getDate("last_modified"));
			report.setName(row.getString("name"));
		}
		
		report.getQueryFields().addAll(getQueryFields(report.getId()));
		
		return report;
	}
	
	@Override
	public ReportModel getReport(String name)
	{
		ReportModel report = new ReportModel();
		
		String query = "select created_by, date_created, last_modified, id " +
			"from report_details where name=:name";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("name", name);
		
		SqlRowSet row = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(row.next())
		{
			report.setName(name);
			
			report.setId(row.getInt("id"));			
			report.setCreated(row.getDate("date_created"));
			report.setUserCreated(userDao.getId(row.getInt("created_by")));
			report.setLastModified(row.getDate("last_modified"));			
		}
		
		report.getQueryFields().addAll(getQueryFields(report.getId()));
		
		return report;
	}
	
	private List<QueryFieldModel> getQueryFields(int reportId)
	{
		List<QueryFieldModel> fields = new ArrayList<QueryFieldModel>(0);
		
		String query = "select id, report_field_id, card, comparison_id, value " +
			"from report_query_details where report_id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", reportId);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		while(rows.next())
		{
			QueryFieldModel field = new QueryFieldModel();
			
			field.setId(rows.getInt("id"));
			field.setReportId(reportId);
			field.setFieldId(rows.getInt("report_field_id"));
			
			int card = rows.getInt("card");			
			if(card > 0)
			{
				field.setCard(card);
				field.setSelectField(true);
				field.setComparison(0);
				field.setValue(null);
			}
			else
			{
				field.setCard(0);
				field.setSelectField(false);
				field.setComparison(rows.getInt("comparison_id"));
				field.setValue(rows.getString("value"));
			}
			
			fields.add(field);
		}
		
		return fields;
	}
	
	private void deleteQueryField(int id)
	{
		String query = "delete from report_query_details where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
		
		getNamedParameterJdbcTemplate().update(query, params);
	}

	@Override
	public List<ReportFieldModel> getAllReportsFieldTypes()
	{
		List<ReportFieldModel> fields = new ArrayList<ReportFieldModel>(0);
		
		String query = "select id, field_name, data_type, display_name from report_field_types";
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, new MapSqlParameterSource());
		
		while(rows.next())
		{
			ReportFieldModel field = new ReportFieldModel();
			
			field.setId(rows.getInt("id"));
			field.setFieldName(rows.getString("field_name"));
			field.setDataType(rows.getInt("data_type"));
			field.setDisplayName(rows.getString("display_name"));
			
			fields.add(field);
		}
		
		return fields;
	}

	@Override
	public QueryResultsModel queryDB(String query)
	{
		QueryResultsModel fullResults = new QueryResultsModel();
		List<List<Object>> results = new ArrayList<List<Object>>();
		
		SqlRowSet rows = getJdbcTemplate().queryForRowSet(query);
		
		SqlRowSetMetaData metaData = rows.getMetaData();
		List<String> columnTypes = new ArrayList<String>();
		List<String> columnNames = new ArrayList<String>();
		
		for(int index = 1; index <= metaData.getColumnCount(); index++)
		{
			columnTypes.add(metaData.getColumnTypeName(index).toLowerCase());
			columnNames.add(metaData.getColumnName(index).toLowerCase());
		}
		
		SerialClob clob = null;
		
		while(rows.next())
		{
			List<Object> result = new ArrayList<Object>();
			
			for(int index = 0; index < columnTypes.size(); index++)
			{
				String columnType = columnTypes.get(index);
				String columnName = columnNames.get(index);
				
				if(columnType.contains("varchar"))
				{
					if(columnName.equals("load_teams"))
					{
						List<DBProperty> loadTeams = Util.makeListofProperties(rows.getString(index + 1), stDao.getProperties("symphony_profile_types"));
						StringBuilder teamString = new StringBuilder();
						
						for(int teamIndex = 0; teamIndex < loadTeams.size(); teamIndex++)
						{
							String team = loadTeams.get(teamIndex).getName();
							
							if(teamIndex < loadTeams.size() - 1)
								teamString.append(team + ", ");
							else
								teamString.append(team);
						}
						
						result.add(teamString.toString());
					}
					else
					{
						result.add(rows.getString(index + 1));
					}
				}
				else if(columnType.equals("number"))
					result.add(rows.getInt(index + 1));
				else if(columnType.equals("date"))
					result.add(rows.getDate(index + 1));
				else if(columnType.equals("timestamp"))
					result.add(Util.convertToTimestamp(rows.getObject(index + 1)));
				else //if(columnType.toLowerCase().equals("clob"))
				{
					clob = (SerialClob)rows.getObject(index + 1);
					try
					{
						if(clob != null)
							result.add(clob.getSubString(1, (int) clob.length()));
						else
							result.add("");
					}
					catch(SerialException e)
					{
						result.add("");
					}
				}
			}
			
			results.add(result);
		}
		
		fullResults.setResults(results);
		fullResults.setResultTypes(columnTypes);
		
		return fullResults;
	}

	@Override
	public List<DBProperty> getAllReports()
	{
		List<DBProperty> allReports = new ArrayList<DBProperty>(0);
		
		String query = "select id, name " +
			"from report_details order by name";
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, new MapSqlParameterSource());
		
		while(rows.next())
		{
			DBProperty prop = new DBProperty();
			
			prop.setId(rows.getInt("id"));
			prop.setName(rows.getString("name"));
			
			allReports.add(prop);
		}
		
		return allReports;
	}

	@Override
	public List<DBProperty> getReportArchives(int id)
	{
		List<DBProperty> allReports = new ArrayList<DBProperty>(0);
		
		String query = "select id, date_created " +
			"from report_archive where report_id=:id order by date_created desc";
		
		SqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
		
		SqlRowSet rows = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		while(rows.next())
		{
			DBProperty prop = new DBProperty();
			
			prop.setId(rows.getInt("id"));
			prop.setName(df.format(rows.getDate("date_created")));
			
			allReports.add(prop);
		}
		
		return allReports;
	}

	@Override
	public QueryResultsModel getReportArchiveData(int id)
	{
		QueryResultsModel results = null;
		ObjectMapper mapper = new ObjectMapper();
		
		String query = "select data, date_created " +
			"from report_archive " +
			"where id=:id";
		
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", id);
		
		SqlRowSet row = getNamedParameterJdbcTemplate().queryForRowSet(query, params);
		
		if(row.next())
		{
			try
			{
				
				SerialClob clob = (SerialClob)row.getObject("data");
				
				if(clob != null)
					results = mapper.readValue(clob.getSubString(1, (int)clob.length()), QueryResultsModel.class);
				
				if(results != null)
					results.setDateCreated(row.getDate("date_created"));
			}
			catch(Exception e)
			{
				log.debug(e);
			}				
		}
		
		return results;
	}

	@Override
	public int saveReportData(QueryResultsModel results)
	{
		ObjectMapper mapper = new ObjectMapper();
		String data = "";
		
		try
		{
			data = mapper.writeValueAsString(results);			
		}
		catch(Exception e)		
		{
			emailServ.sendErrorEmail(e);
		}
						
		String query = "insert into report_archive " +
			"(data, date_created, report_id) " +
			"values (:data, :date, :report)";
	
		SqlParameterSource params = new MapSqlParameterSource()
			.addValue("data", data)
			.addValue("date", Calendar.getInstance().getTime())
			.addValue("report", results.getReport().getId());
			
		KeyHolder key = new GeneratedKeyHolder();		
		int modified = getNamedParameterJdbcTemplate().update(query, params, key, new String[]{"id"});
		
		results.setId(key.getKey().intValue());
		
		return modified;
	}

}
