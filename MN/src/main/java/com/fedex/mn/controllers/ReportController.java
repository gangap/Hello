package com.fedex.mn.controllers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fedex.mn.app.Application;
import com.fedex.mn.app.cache.MemCache;
import com.fedex.mn.controllers.builder.ModelBuilder;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.MNView;
import com.fedex.mn.models.impls.QueryFieldModel;
import com.fedex.mn.models.impls.QueryResultsModel;
import com.fedex.mn.models.impls.ReportFieldModel;
import com.fedex.mn.models.impls.ReportModel;
import com.fedex.mn.services.ReportService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/reports")
public class ReportController
{	
	@Autowired
	private MemCache mem;
	
	@Autowired
	private Application app;
	
	@Autowired
	private ModelBuilder builder;
	
	@Autowired
	private ReportService reportServ;
	
	@Autowired
	private SessionModel userSession;
	
	private static Logger log = Logger.getLogger(ReportController.class);
	
	@RequestMapping(value="/{view}", method=RequestMethod.GET)
	public String getReportMain(@PathVariable(value="view") String view, ModelMap model)
	{		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}	
				
		//Based on view
		if(view.equals("default"))
		{
			log.debug("/reports/default - Displaying the default report functionality");
			
			builder.setReportChooseDisplayModel(model);
		}
		else if(view.equals("create"))
		{
			log.debug("/reports/create - Displaying the create report functionality");
						
			builder.setReportCreateDisplayModel(model);	
		}
		else if(view.equals("email"))
		{
			log.debug("/reports/email - Displaying the email functionality");		
			
			List<MNView> mnViews = mem.getMnViews();
			
			mnViews = Util.sortParents(Util.filterMNs(userSession.getFilters(), mnViews));				
			
			model.addAttribute("mns", mnViews);
			builder.setReportEmailDisplayModel(model);
		}
						
		return "/subviews/contents/main-contents/reports-content";
	}
	
	@RequestMapping(value="/edit/{id}", method=RequestMethod.GET)
	public String getEdit(@PathVariable("id") int id, ModelMap model)
	{		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}	
		
		log.debug("/reports/edit - Editing a report that has not been saved");
		
		QueryResultsModel results = app.getResultList().get(new Integer(id));
		if(results != null)
		{
			builder.setReportEditDisplayModel(model, results.getReport());
		}
		else
		{
			model.addAttribute("type", "error");
			model.addAttribute("title", "Cannot Edit Query");
			model.addAttribute("msg", "The query specified is unable to be editted at this time");
			return "redirect:/msg";
		}
		
		return "/subviews/contents/main-contents/reports-content";
	}
	
	@RequestMapping(value="/choose/query/{id}", method=RequestMethod.GET)
	public String getChooseQuery(@PathVariable("id") int id, ModelMap model)
	{		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
		
		ReportModel report = reportServ.getReport(id);
		
		if(report == null)
		{
			model.addAttribute("type", "error");
			model.addAttribute("title", "Database Error");
			model.addAttribute("msg", "There was an error retrieving the report [" + id + "] from the DB");
		}
		
		QueryResultsModel results = reportServ.generateReport(createReportQuery(report));
		results.setReport(report);
		
		//Add the results to the Application's List
		int key = Util.randomInt(1, Integer.MAX_VALUE);
		app.getResultList().put(key, results);
		
		//Save the report
		reportServ.addReportData(results);
		
		builder.setReportQueryModel(results, model);
		List<String> headers = new ArrayList<String>();
		
		for(QueryFieldModel field : report.getSelects())
		{
			headers.add(getReportDisplayName(field.getFieldId()));
		}
		
		model.addAttribute("headers", headers);
		model.addAttribute("queryKey", key);
		model.addAttribute("reportDate", Calendar.getInstance().getTime());
		
		return "/subviews/contents/main-contents/reports-content";		
	}
	
	@RequestMapping(value="/choose/edit/{id}", method=RequestMethod.GET)
	public String getChooseEdit(@PathVariable("id") int id, ModelMap model)
	{		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
		
		ReportModel report = reportServ.getReport(id);
		
		if(report == null)
		{
			model.addAttribute("type", "error");
			model.addAttribute("title", "Database Error");
			model.addAttribute("msg", "There was an error retrieving the report [" + id + "] from the DB");
		}
		
		builder.setReportEditDisplayModel(model, report);
		
		return "/subviews/contents/main-contents/reports-content";		
	}		
	
	@RequestMapping(value="/create/query", method=RequestMethod.GET)
	public String getCreateQuery(@RequestParam(value="report-name", defaultValue="Default", required=false) String reportName,
		@RequestParam("select-query-fields") List<Integer> selectFields, 
		@RequestParam(value="where-query-fields", required=false) List<String> whereFields, 
		ModelMap model)
	{	
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
		
		ReportModel report = new ReportModel();
		
		report.setName(reportName);
		report.setId(0);
		report.setCreated(Calendar.getInstance().getTime());
		report.getQueryFields().addAll(createSelectModels(selectFields));
		report.getQueryFields().addAll(createWhereModels(whereFields));			
		
		String query = createReportQuery(report);
		
		QueryResultsModel results = reportServ.generateReport(query);		
		results.setReport(report);
			
		//Add the results to the Application's List
		int key = Util.randomInt(1, Integer.MAX_VALUE);
		app.getResultList().put(key, results);
		
		builder.setReportQueryModel(results, model);
		List<String> headers = new ArrayList<String>();
		
		for(QueryFieldModel field : report.getSelects())
		{
			headers.add(getReportDisplayName(field.getFieldId()));
		}
		
		model.addAttribute("reportDate", Calendar.getInstance().getTime());
		model.addAttribute("headers", headers);
		model.addAttribute("queryKey", key);
		
		return "/subviews/contents/main-contents/reports-content";
	}	
	
	@RequestMapping(value="/edit/query/{id}", method=RequestMethod.GET)
	public String getEditQuery(@RequestParam(value="report-name") String reportName,
		@RequestParam("select-query-fields") List<Integer> selectFields, 
		@RequestParam(value="where-query-fields", required=false) List<String> whereFields, 
		@PathVariable("id") int id, ModelMap model)
	{		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
		
		ReportModel report = new ReportModel();
		
		report.setName(reportName);
		report.setId(id);
		report.getQueryFields().addAll(createSelectModels(selectFields));
		report.getQueryFields().addAll(createWhereModels(whereFields));
		
		String query = createReportQuery(report);
		
		QueryResultsModel results = reportServ.generateReport(query);
		results.setReport(report);
		
		//Add the results to the Application's List
		int key = Util.randomInt(1, Integer.MAX_VALUE);
		app.getResultList().put(key, results);
		
		builder.setReportQueryModel(results, model);
		List<String> headers = new ArrayList<String>();
		
		for(QueryFieldModel field : report.getSelects())
		{
			headers.add(getReportDisplayName(field.getFieldId()));
		}
		
		model.addAttribute("reportDate", results.getReport().getCreated());
		model.addAttribute("headers", headers);
		model.addAttribute("queryKey", key);
		
		return "/subviews/contents/main-contents/reports-content";
	}
	
	@RequestMapping(value="/archive/{id}", method=RequestMethod.GET)
	public String getArchive(@PathVariable("id") int id, ModelMap model)
	{		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
		
		if(id <= 0)
		{
			model.addAttribute("type", "error");
			model.addAttribute("title", "Invalid Archive");
			model.addAttribute("msg", "The archive selected is invalid");
			return "redirect:/msg";
		}
		
		QueryResultsModel results = reportServ.getArchive(id);
		
		//Add the results to the Application's List
		int key = Util.randomInt(1, Integer.MAX_VALUE);
		app.getResultList().put(key, results);
		
		builder.setReportQueryModel(results, model);
			
		List<String> headers = new ArrayList<String>();
		
		for(QueryFieldModel field : results.getReport().getSelects())
		{
			headers.add(getReportDisplayName(field.getFieldId()));
		}						
		
		model.addAttribute("reportDate", results.getDateCreated());
		model.addAttribute("headers", headers);
		model.addAttribute("archiveView", true);
		model.addAttribute("queryKey", key);
		
		return "/subviews/contents/main-contents/reports-content";
	}
	
	@RequestMapping(value="/ajax/comparisons/{fieldId}", method=RequestMethod.GET)
	public String getComparisons(@PathVariable("fieldId") int fieldId, ModelMap model)
	{				
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
				
		builder.setReportWhereClauseModel(fieldId, model);		
		 
		return "/subviews/contents/main-contents/forms/components/report-where-opt";
	}
	
	@RequestMapping(value="/ajax/archives/{id}", method=RequestMethod.GET)
	public String getArchiveList(@PathVariable("id") int id, ModelMap model)
	{
		List<DBProperty> props = reportServ.getReportArchives(id);
		model.addAttribute("archives", props);
		
		return "/subviews/contents/main-contents/forms/components/report-archive-select";
	}

	@RequestMapping(value="/create/save", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postCreateSave(@RequestParam(value="report-name") String reportName,
		@RequestParam("select-query-fields") List<Integer> selectFields, 
		@RequestParam(value="where-query-fields", required=false) List<String> whereFields, 
		ModelMap model)
	{		
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		if(reportName == null || reportName.trim().isEmpty())
		{
			msgs.add(new ClientMsg("error", "Invalid Report Name", "Please select a report name"));
			return msgs;
		}	
		
		if(nameTaken(reportServ.getAllReports(), reportName, -1))
		{
			msgs.add(new ClientMsg("error", "Cannot use this name", "This name is currently in use. Please try another"));
			return msgs;
		}
		
		ReportModel report = new ReportModel();
		report.setName(reportName);
		report.setCreated(Calendar.getInstance().getTime());
		report.setLastModified(report.getCreated());
		report.setUserCreated(userSession.getUser());
		report.getQueryFields().addAll(createSelectModels(selectFields));
		report.getQueryFields().addAll(createWhereModels(whereFields));
		
		if(reportServ.addReport(report))
			msgs.add(new ClientMsg("success", "Save Successful", "The report was successfully saved"));
		else
			msgs.add(new ClientMsg("error", "Database Error", "There was an error saving the report to the Database"));
		
		return msgs;
	}
	
	
	
	@RequestMapping(value="/edit/update/{id}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postEditUpdate(@RequestParam(value="report-name") String reportName,
		@RequestParam("select-query-fields") List<Integer> selectFields, 
		@RequestParam(value="where-query-fields", required=false) List<String> whereFields,
		@PathVariable("id") int id, ModelMap model)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		if(reportName == null || reportName.trim().isEmpty())
		{
			msgs.add(new ClientMsg("error", "Invalid Report Name", "Please select a report name"));
			return msgs;
		}	
		
		//Make sure that we can update this particular report
		//Division must be the same as the originator's 
		//Name must not be taken
		ReportModel dbReport = reportServ.getReport(id);
		
		if(dbReport == null 
			|| !dbReport.getUserCreated().getDivision().equalsIgnoreCase(userSession.getUser().getDivision()))
		{
			msgs.add(new ClientMsg("error", "Cannot update this report", "You are not in the same team as the originator"));
			return msgs;
		}
		else if(nameTaken(reportServ.getAllReports(), reportName, id))
		{
			msgs.add(new ClientMsg("error", "Cannot use this name", "This name is currently in use. Please try another"));
			return msgs;
		}				
		
		ReportModel report = new ReportModel();
		report.setName(reportName);
		report.setId(id);
		report.setLastModified(Calendar.getInstance().getTime());
		report.getQueryFields().addAll(createSelectModels(selectFields));
		report.getQueryFields().addAll(createWhereModels(whereFields));
		
		if(reportServ.updateReport(report))
			msgs.add(new ClientMsg("success", "Update Successful", "The report was successfully updated"));
		else
			msgs.add(new ClientMsg("error", "Database Error", "There was an error updating the report in the Database"));
		
		
		return msgs;
	}

	@RequestMapping(value="/download", method=RequestMethod.GET)
	public ModelAndView postDownloadReport(@RequestParam("id") int id, 
		HttpServletResponse response, HttpServletRequest request, ModelMap model)
	{
		QueryResultsModel results = app.getResultList().get(new Integer(id));
		
		if(results == null)
			return null;
		
		ReportModel report = results.getReport();		
		
		List<String> headers = new ArrayList<String>();
		
		for(QueryFieldModel field : report.getSelects())
		{
			headers.add(getReportDisplayName(field.getFieldId()));
		}
		
		model.addAttribute("headers", headers);
		model.addAttribute("report", report);
		model.addAttribute("data", results.getResults());
		model.addAttribute("dataTypes", results.getResultTypes());
		
		return new ModelAndView("ReportDownload", "MN Report", model);
	}		
	
	private List<QueryFieldModel> createSelectModels(List<Integer> selects)
	{
		List<QueryFieldModel> selectModels = new ArrayList<QueryFieldModel>();
		
		for(int index = 0; index < selects.size(); index++)
		{
			Integer select = selects.get(index);
			QueryFieldModel selectField = new QueryFieldModel();
			selectField.setCard(index + 1);
			selectField.setSelectField(true);
			selectField.setFieldId(select.intValue());
			selectModels.add(selectField);					
		}
		
		return selectModels;				
	}
	
	private List<QueryFieldModel> createWhereModels(List<String> wheres)
	{		
		List<QueryFieldModel> whereModels = new ArrayList<QueryFieldModel>();
		
		if(wheres == null || wheres.size() == 0)
			return whereModels;
		
		for(int index = 0; index < wheres.size(); index++)
		{
			String where[] = wheres.get(index).split(";");
			if(where.length != 3)
			{
				log.error("There was an error in the where clause of the report query");
				return whereModels;
			}
			
			QueryFieldModel whereField = new QueryFieldModel();
			whereField.setSelectField(false);
			whereField.setComparison(Integer.parseInt(where[1]));
			whereField.setFieldId(Integer.parseInt(where[0]));
			whereField.setValue(where[2]);	
			
			whereModels.add(whereField);
		}
		
		Collections.sort(whereModels);
		
		return whereModels;				
	}
	
	private String createReportQuery(ReportModel report)
	{
		StringBuilder query = new StringBuilder();
					
		StringBuilder selectClause = new StringBuilder("select ");
		
		List<QueryFieldModel> selects = report.getSelects();
		for(int index = 0; index < selects.size(); index++)
		{
			String selectField = getReportFieldName(selects.get(index).getFieldId());
			if(getFieldDataType(selects.get(index).getFieldId()) == 3)
			{
				selectField = "to_char(" + selectField + ", 'DD-MM-YYYY HH24:MI:SS')";  
			}
			
			if(index == selects.size() - 1)
			{				
				selectClause.append(selectField + " ");
			}
			else
			{
				selectClause.append(selectField + ", ");
			}
		}
		
		StringBuilder fromClause = new StringBuilder();
		boolean joinApprovals = false;
		
		List<QueryFieldModel> fields = report.getQueryFields();
		for(QueryFieldModel queryField : fields)
		{
			if(getReportFieldName(queryField.getFieldId()).toLowerCase().contains("approval"))
			{
				joinApprovals = true;
				break;
			}			
		}
		
		if(joinApprovals)
			fromClause.append(" from full_mn_view left join full_approval_view on full_mn_view.mn_id=full_approval_view.approval_mn_id ");
		else
			fromClause.append(" from full_mn_view ");
		
		StringBuilder whereClause = new StringBuilder();		
		
		List<QueryFieldModel> wheres = report.getWheres();
		
		if(wheres.size() > 0)
		{
			whereClause.append(" where ");
		}
		
		for(int index = 0; index < wheres.size(); index++)
		{
			QueryFieldModel curWhere, lastWhere, nextWhere;
			curWhere = wheres.get(index);
			
			if(index != 0)
				lastWhere = wheres.get(index - 1);
			else
				lastWhere = null;
			
			if(index + 1 < wheres.size())
				nextWhere = wheres.get(index + 1);
			else
				nextWhere = null;
			
			//See if we should be in a OR-string
			if(nextWhere != null && nextWhere.getFieldId() == curWhere.getFieldId())
			{
				//See if we are already in an OR-string
				if(lastWhere != null && lastWhere.getFieldId() == curWhere.getFieldId())
				{
					//We are in an OR-string...
					whereClause.append(getWhereCondition(curWhere) + shouldAndOr(curWhere));
				}
				else
				{
					//We are not in an OR-string yet...
					whereClause.append("( " + getWhereCondition(curWhere) + shouldAndOr(curWhere));
				}
			}
			//See if we were in an OR-string
			else if(lastWhere != null && lastWhere.getFieldId() == curWhere.getFieldId())
			{
				//If we were the last part of an OR-string...
				if(nextWhere != null && nextWhere.getFieldId() == curWhere.getFieldId())
				{
					//We are not the last part of an OR-string...
					whereClause.append(getWhereCondition(curWhere) + shouldAndOr(curWhere));
				}
				else if(nextWhere != null)
				{
					//We are the lastl part of an OR-string... 
					whereClause.append(getWhereCondition(curWhere) + " ) AND ");
				}
				else
				{
					whereClause.append(getWhereCondition(curWhere) + " ) ");
				}
			}
			//Just a normal AND-string
			else if(nextWhere != null)
			{
				whereClause.append(getWhereCondition(curWhere) + " AND ");
			}
			else
			{
				whereClause.append(getWhereCondition(curWhere));
			}
				
		}
		
		query.append(selectClause.toString() + fromClause.toString() + whereClause.toString());
		log.info(query.toString());
		
		return query.toString();
	}
	
	private String getReportFieldName(int fieldId)
	{
		List<ReportFieldModel> fields = mem.getReportFieldTypes();
		
		for(ReportFieldModel field : fields)
		{
			if(field.getId() == fieldId)
				return field.getFieldName();
		}
		
		return "";
	}
	
	private String getReportDisplayName(int fieldId)
	{
		List<ReportFieldModel> fields = mem.getReportFieldTypes();
		
		for(ReportFieldModel field : fields)
		{
			if(field.getId() == fieldId)
				return field.getDisplayName();
		}
		
		return "";
	}
	
	private int getFieldDataType(int fieldId)
	{
		List<ReportFieldModel> fields = mem.getReportFieldTypes();
		
		for(ReportFieldModel field : fields)
		{
			if(field.getId() == fieldId)
				return field.getDataType();
		}
		
		return 0;
	}
	
	private String getComparisonName(int compId)
	{
		List<DBProperty> comparisons = mem.getProperties("comparison_types");
		
		for(DBProperty comparison : comparisons)
		{
			if(comparison.getId() == compId)
			{
				if(comparison.getName().toLowerCase().equals("contains"))
					return "like";
				else if(comparison.getName().toLowerCase().equals("equals"))
					return "=";
				else if(comparison.getName().toLowerCase().equals("before"))
					return "<";
				else if(comparison.getName().toLowerCase().equals("after"))
					return ">";
				else //Not Equals
					return "!=";
			}			
		}
		
		return "";
	}
	
	private String getWhereCondition(QueryFieldModel where)
	{		
		//Make sure that it is not a select
		if(!where.isSelectField())
		{
			String comp = getComparisonName(where.getComparison());
			
			if(comp.equals(">") || comp.equals("<"))
			{
				if(((String)where.getValue()).length() > 12)
					return " " + getReportFieldName(where.getFieldId()) + " " + comp + " to_date('" + where.getValue() + "', 'mm/dd/yyyy hh24:mi:ss') ";
				else
					return " " + getReportFieldName(where.getFieldId()) + " " + comp + " to_date('" + where.getValue() + "', 'mm/dd/yyyy') ";
			}	
			else
			{
				String reportFieldName = getReportFieldName(where.getFieldId());
				Object val = null;
				
				if(reportFieldName.equals("load_teams"))
				{
					List<DBProperty> srsProfiles = mem.getStValues().get("symphony_profile_types");
					for(DBProperty prop : srsProfiles)
					{
						if(((String)where.getValue()).contains(prop.getName()))
						{
							if(!comp.equals("like"))
								val = prop.getId();
							else
								val = "%" + prop.getId() + "%";
						}
					}
				}
				else
				{
					val = where.getValue();
				}
				
				return " " + reportFieldName + " " + comp + " '" + val + "' ";
			}
		}		
		
		return "";
	}
	
	private String shouldAndOr(QueryFieldModel where)
	{		
		//If the comparison is < or > then it has to be an AND otherwise OR
		if(!where.isSelectField())
		{
			String comp = getComparisonName(where.getComparison());
			
			if(comp.equals(">") || comp.equals("<") || comp.equals("!="))
				return " AND ";
			else
				return " OR ";
		}	
		
		return "";
	}
	
	private boolean nameTaken(List<DBProperty> allReports, String reportName, int id)
	{
		for(DBProperty prop : allReports)
		{
			if(prop.getName().equalsIgnoreCase(reportName) && prop.getId() != id)
				return true;
		}
		
		return false;
	}
}
