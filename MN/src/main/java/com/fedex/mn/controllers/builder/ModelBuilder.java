package com.fedex.mn.controllers.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;

import com.fedex.mn.app.Application;
import com.fedex.mn.app.cache.MemCache;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.ApprovalModel;
import com.fedex.mn.models.impls.BasicDetailModel;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.EditTrans;
import com.fedex.mn.models.impls.EmailModel;
import com.fedex.mn.models.impls.LoginModel;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.models.impls.MNView;
import com.fedex.mn.models.impls.QueryResultsModel;
import com.fedex.mn.models.impls.ReleaseModel;
import com.fedex.mn.models.impls.ReportFieldModel;
import com.fedex.mn.models.impls.ReportModel;
import com.fedex.mn.models.impls.UserModel;
import com.fedex.mn.models.validations.MNValidation;
import com.fedex.mn.services.ApprovalService;
import com.fedex.mn.services.AttachmentService;
import com.fedex.mn.services.EditService;
import com.fedex.mn.services.MNService;
import com.fedex.mn.services.ReleaseService;
import com.fedex.mn.services.ReportService;
import com.fedex.mn.utils.Util;

@Component
public class ModelBuilder
{
	@Autowired
	private ReleaseService relServ;
	
	@Autowired
	private MNService mnServ;
	
	@Autowired
	private AttachmentService attachServ;
	
	@Autowired
	private ApprovalService approvalServ;
	
	@Autowired
	private ReportService reportServ;
	
	@Autowired
	private SessionModel userSession;
	
	@Autowired
	private MNValidation valid;
	
	@Autowired
	private EditService transServ;
	
	@Autowired
	private MemCache mem;
	
	@Autowired
	private Application app;
	
	@Value("${app.max.mns}")
	private String maxMNs; 
	
	@Value("${app.version}")
	private String version;
	
	@SuppressWarnings("unchecked")
	public void setFilterBarData(ModelMap model, SessionModel userSession, boolean filterCurrent)
	{
		model.addAttribute("rels", mem.getReleases());
		
		model.addAttribute("statuses", Util.getStatusToReleaseMap(mem.getMnViews(), mem.getProperties("status_types")));
		model.addAttribute("loads", mem.getProperties("load_types"));
		model.addAttribute("changes", mem.getProperties("change_types"));
		model.addAttribute("filters", userSession.getFilters());
		model.addAttribute("srs", mem.getProperties("symphony_profile_types"));
		model.addAttribute("dests", mem.getProperties("destination_types"));
		
		//Add a filter for the current release
		if(filterCurrent)
		{
			for(ReleaseModel rel : mem.getReleases())
			{
				if(rel.isCurrent())
					userSession.getFilters().put("release", rel.getName());
			}
		}
	}
	
	public void setLoginPageData(ModelMap model)
	{
		model.addAttribute("version", version);
	}
	
	public void setMainPageData(ModelMap model, SessionModel userSession)
	{		
		List<MNView> mnViews = mem.getMnViews();
		
		mnViews = Util.sortParents(Util.filterMNs(userSession.getFilters(), mnViews));				
		
		model.addAttribute("mns", mnViews);
		model.addAttribute("maxMNs", Integer.parseInt(maxMNs));
		model.addAttribute("rels", mem.getReleases());		
		model.addAttribute("locks", app.getLockedMNs());
		model.addAttribute("view", userSession.getView());
		if(userSession.getUser().isAdmin())
			model.addAttribute("sqa", Boolean.TRUE);
		else
			model.addAttribute("sqa", Boolean.FALSE);
		
		model.addAttribute("user", userSession.getUser());
		model.addAttribute("version", version);
	}
	
	public void setViewDisplayModel(ModelMap model, MNModel mnModel)
	{
		model.addAttribute("mnId", mnModel.getId());	
		
		BasicDetailModel basic = mnModel.getBasicDetail();
		model.addAttribute("testEnabled", valid.isTestEnabled(basic.getDestination().getId(), basic.getLoadType().getId()));
		model.addAttribute("buildEnabled", valid.isBuildEnabled(basic.getStatus().getId(), basic.getChangeType().getId()));
			
		model.addAttribute("email", new EmailModel());
		
		model.addAttribute("users", mem.getUsers());
		model.addAttribute("emails", mem.getEmails());
		model.addAttribute("version", version);
	}
	
	public void setEditDisplayModel(ModelMap model, MNModel mnModel)
	{
		model.addAttribute("mnId", mnModel.getId());
		
		BasicDetailModel basic = mnModel.getBasicDetail();
		model.addAttribute("testEnabled", valid.isTestEnabled(basic.getDestination().getId(), basic.getLoadType().getId()));
		model.addAttribute("buildEnabled", valid.isBuildEnabled(basic.getStatus().getId(), basic.getChangeType().getId()));
		
		model.addAttribute("email", new EmailModel());
		
		model.addAttribute("users", mem.getUsers());
		model.addAttribute("emails", mem.getEmails());
		model.addAttribute("version", version);
	}
	
	public void setBasicDisplayModel(ModelMap model, MNModel mn)
	{
		model.addAttribute("basic", mn.getBasicDetail());
		model.addAttribute("form", "basic");
		
		model.addAttribute("releases", mem.getReleases());					
		model.addAttribute("parentId", mn.getParentID());
		
		//Only get the child MNs if there is not parent ID
		if(mn.getParentID() != 0 || mn.getParentID() != -1)
		{
			model.addAttribute("childMNs", mnServ.getChildMNs(mn.getId()));
		}

		model.addAttribute("destinations", mem.getProperties("destination_types"));
		
		if(mn.getBasicDetail().getDestination() == null || mn.getBasicDetail().getLoadType() == null)			
			model.addAttribute("statuses", valid.getAvailableStatuses("---", "---", approvalServ.getAll(mn.getId())));
		else
			model.addAttribute("statuses", valid.getAvailableStatuses(
				Util.getName(mem.getProperties("destination_types"), mn.getBasicDetail().getDestination().getId()), 
				Util.getName(mem.getProperties("load_types"), mn.getBasicDetail().getLoadType().getId()), 
				approvalServ.getAll(mn.getId())));
					
		if(mn.getBasicDetail().getStatus() != null)
			model.addAttribute("curStatus", mn.getBasicDetail().getStatus().getId());
		
		model.addAttribute("profiles", mem.getProperties("symphony_profile_types"));
		model.addAttribute("mns", mem.getMnViews());
		model.addAttribute("depMNs", mn.getBasicDetail().getDependencyIds());
		model.addAttribute("deps", mem.getProperties("dependency_types"));
		model.addAttribute("changes", mem.getProperties("change_types"));
		model.addAttribute("loadTeams", mem.getProperties("load_team_types"));
		model.addAttribute("loads", mem.getProperties("load_types"));
		
		model.addAttribute("version", version);
	}			
	
	public void setTestDisplayModel(ModelMap model, MNModel mn)
	{
		if(mn.getTestDetail().getSqaUser() == null)
			mn.getTestDetail().setSqaUser(new UserModel());
		
		model.addAttribute("test", mn.getTestDetail());
		model.addAttribute("form", "test");
		
		List<DBProperty> changes = mem.getProperties("change_types");
		for(DBProperty change : changes)
		{
			if(change.getId() == mn.getBasicDetail().getChangeType().getId())
				model.addAttribute("change", change.getName());
		}
		
		model.addAttribute("testTypes", mem.getProperties("test_types"));
		model.addAttribute("users", mem.getUsers());
	}
	
	public void setBuildDisplayModel(ModelMap model, MNModel mn)
	{
		model.addAttribute("build", mn.getBuildDetail());
		model.addAttribute("form", "build");
		
		//Because of changes to the OS-types we need to make sure that previous selections aren't changed
		String status = Util.getName(mem.getProperties("status_types"), mn.getBasicDetail().getStatus().getId());
		
		if(status.equals("Completed") || 
			status.equals("Cancelled"))
			model.addAttribute("completed", Boolean.TRUE);
		else
			model.addAttribute("completed", Boolean.FALSE);
		
		model.addAttribute("oss", mem.getProperties("os_types"));
		model.addAttribute("scms", mem.getProperties("scm_tool_types"));
		model.addAttribute("projects", mem.getProperties("projects"));	
		model.addAttribute("abinitio", attachServ.hasAbinitioAttachment(mn.getId()));
	}
	
	public void setDeployDisplayModel(ModelMap model, MNModel mn)
	{
		model.addAttribute("deploy", mn.getDeploymentDetail());
		model.addAttribute("form", "deploy");
		
		model.addAttribute("attachments", attachServ.getAllAttachments(mn.getId(), "deploy"));
	}
	
	public void setReportEmailDisplayModel(ModelMap model)
	{
		model.addAttribute("users", mem.getEmails());
		model.addAttribute("reportType", "email");		
		model.addAttribute("email", new EmailModel());
	}
	
	public void setPrintViewModel(ModelMap model, MNModel mn)
	{
		List<ApprovalModel> approvals = approvalServ.getAll(mn.getId());
		
		model.addAttribute("children", mnServ.getChildMNs(mn.getId()));
		
		//Approvals
		String dest = mn.getBasicDetail().getDestination().getName();
		String load = mn.getBasicDetail().getLoadType().getName();
		String status = mn.getBasicDetail().getStatus().getName();
		model.addAttribute("needed", valid.getSignatures(dest, load, status, approvals));
		
		model.addAttribute("approvals", Util.getApprovalType(0, approvals));
		model.addAttribute("approvalValues", mem.getProperties("approval_types"));
		
		model.addAttribute("confirms", Util.getApprovalType(1, approvals));
		model.addAttribute("confirmValues", mem.getProperties("load_confirmation_types"));
		
		BasicDetailModel basic = mn.getBasicDetail();
		model.addAttribute("testEnabled", valid.isTestEnabled(basic.getDestination().getId(), basic.getLoadType().getId()));
		model.addAttribute("buildEnabled", valid.isBuildEnabled(basic.getStatus().getId(), basic.getChangeType().getId()));
		
		List<EditTrans> trans = transServ.getAll(mn.getId());
		model.addAttribute("transactions", trans);
		model.addAttribute("fields", mem.getProperties("field_types"));
		
		model.addAttribute("mn", mn);
		
		//Only get the child MNs if there is not parent ID
		if(mn.getParentID() != 0 || mn.getParentID() != -1)
		{
			model.addAttribute("childMNs", mnServ.getChildMNs(mn.getId()));
		}
		
		if(!userSession.isLoggedIn())
		{
			model.addAttribute("logged", "false");
			model.addAttribute("login", new LoginModel());
		}
		
		model.addAttribute("version", version);
	}

	public void setReportCreateDisplayModel(ModelMap model)
	{		
		model.addAttribute("reportType", "create");
		model.addAttribute("comparisons", mem.getProperties("comparison_types"));
		
		List<ReportFieldModel> rptFields = new ArrayList<ReportFieldModel>(mem.getReportFieldTypes());
		Collections.sort(rptFields);
		
		model.addAttribute("fieldTypes", rptFields);
	}
	
	public void setReportEditDisplayModel(ModelMap model, ReportModel report)
	{		
		model.addAttribute("reportType", "edit");
		model.addAttribute("reportId", report.getId());
		model.addAttribute("selects", report.getSelects());
		model.addAttribute("wheres", report.getWheres());
		model.addAttribute("reportName", report.getName());
		model.addAttribute("comparisons", mem.getProperties("comparison_types"));
		
		List<ReportFieldModel> rptFields = new ArrayList<ReportFieldModel>(mem.getReportFieldTypes());
		Collections.sort(rptFields);
		
		model.addAttribute("fieldTypes", rptFields);
	}
	
	public void setReportChooseDisplayModel(ModelMap model)
	{
		model.addAttribute("reportType", "default");
		model.addAttribute("reports", reportServ.getAllReports());
		model.addAttribute("version", version);
	}

	public void setReportQueryModel(QueryResultsModel results, ModelMap model)
	{		
		model.addAttribute("email", new EmailModel());
		model.addAttribute("emails", mem.getEmails());
		model.addAttribute("reportType", "query");
		model.addAttribute("savedReport", (results.getReport().getId() > 0 ? true : false));
		model.addAttribute("name", results.getReport().getName());
		model.addAttribute("values", results.getResults());
		model.addAttribute("valueTypes", results.getResultTypes());		
	}
	
	public void setReportWhereClauseModel(int fieldId, ModelMap model)
	{
		List<DBProperty> comparisons = new ArrayList<DBProperty>();
		List<ReportFieldModel> fields = mem.getReportFieldTypes();		
		ReportFieldModel selected = null;		
		DBProperty dataType = null;
		String type = null;
		
		
		for(ReportFieldModel fieldTypes : fields)
		{
			if(fieldTypes.getId() == fieldId)
			{
				selected = fieldTypes;
			}
		}
				
		for(DBProperty prop : mem.getProperties("data_types"))
		{
			if(selected.getDataType() == prop.getId())
				dataType = prop;
		}
		 
		//Add the value types
		String field = selected.getDisplayName();
			
		if(field.equalsIgnoreCase("status"))
		{
			model.addAttribute("props", mem.getProperties("status_types"));
			type = "select";
		}
		else if(field.equalsIgnoreCase("destination"))
		{
			model.addAttribute("props", mem.getProperties("destination_types"));
			type = "select";
		}
		else if(field.equalsIgnoreCase("release"))
		{
			model.addAttribute("props", mem.getReleases());
			type = "select";
		}
		else if(field.equalsIgnoreCase("load type"))
		{
			model.addAttribute("props", mem.getProperties("load_types"));
			type = "select";
		}
		else if(field.equalsIgnoreCase("change type"))
		{
			model.addAttribute("props", mem.getProperties("change_types"));
			type = "select";
		}
		else if(field.equalsIgnoreCase("change type"))
		{
			model.addAttribute("props", mem.getProperties("change_types"));
			type = "select";
		}
		else if(field.equalsIgnoreCase("symphony profile") ||
				field.equalsIgnoreCase("load team"))
		{
			model.addAttribute("props", mem.getProperties("symphony_profile_types"));
			type = "select";
		}
		else if(field.equalsIgnoreCase("load team"))
		{
			model.addAttribute("props", mem.getProperties("load_team_types"));
			model.addAttribute("type", "select");
		}
		else if(field.equalsIgnoreCase("test type"))
		{
			model.addAttribute("props", mem.getProperties("test_types"));
			type = "select";
		}
		else if(field.equalsIgnoreCase("operating system"))
		{
			model.addAttribute("props", mem.getProperties("os_types"));
			type = "select";
		}
		else if(field.equalsIgnoreCase("scm tool"))
		{
			model.addAttribute("props", mem.getProperties("scm_tool_types"));
			type = "select";
		}
		else if(field.equalsIgnoreCase("expected load date") 
			|| field.equalsIgnoreCase("acutal load date") 
			|| field.equalsIgnoreCase("date created")
			|| field.equalsIgnoreCase("date last modified")
			|| field.equalsIgnoreCase("date signed"))
		{
			type = "date";
		}
		else if(field.equalsIgnoreCase("originator's name") 
			|| field.equalsIgnoreCase("assigned sqa user name")
			|| field.equalsIgnoreCase("approval's user name"))
		{
			model.addAttribute("props", mem.getUsers());
			type = "select";
		}
		else if(field.equalsIgnoreCase("originator's email") 
			|| field.equalsIgnoreCase("assigned sqa user email")
			|| field.equalsIgnoreCase("approval's user email"))
		{
			model.addAttribute("props", mem.getEmails());
			type = "email";
		}
		else if(field.equalsIgnoreCase("mn number") 
			|| field.equalsIgnoreCase("parent mn number")
			|| field.equalsIgnoreCase("dependency mn number"))
		{
			model.addAttribute("props", mem.getMnViews());
			type = "mn";
		}
		
		model.addAttribute("type", type);	
		
		for(DBProperty prop : mem.getProperties("comparison_types"))
		{
			if(dataType.getName().equalsIgnoreCase("date") ||
				dataType.getName().equalsIgnoreCase("timestamp"))
			{
				if(prop.getName().equalsIgnoreCase("before") ||
					prop.getName().equalsIgnoreCase("after"))
					comparisons.add(prop);
			}
			else if (dataType.getName().equalsIgnoreCase("clob"))
			{
				if(prop.getName().equalsIgnoreCase("contains"))
					comparisons.add(prop);
			}
			else
			{
				if(prop.getName().equalsIgnoreCase("contains") ||
					prop.getName().equalsIgnoreCase("equals") ||
					prop.getName().equalsIgnoreCase("not"))
					comparisons.add(prop);
			}
		}
		
		model.addAttribute("comparisons", comparisons);
	}

	public void setAdminModel(ModelMap model)
	{
		Map<Integer, UserModel> lockedMNs = app.getLockedMNs();
		
		model.addAttribute("lockedMNs", lockedMNs);
		if(lockedMNs.isEmpty())
			model.addAttribute("hasLocks", false);
		else
			model.addAttribute("hasLocks", true);		
		
		model.addAttribute("loggedUsers", app.getLoggedUsers());
		model.addAttribute("releases", mem.getReleases());
		model.addAttribute("mns", mem.getMnViews());
		model.addAttribute("appState", app.getAppState());
		
		List<UserModel> loadUsers = new ArrayList<UserModel>(0);
		
		for(DBProperty prop : mem.getProperties("load_team_users"))
		{			
			for(UserModel user : mem.getUsers())
			{
				try
				{
					if(user.getFedExId() == Integer.parseInt(prop.getName()))
						loadUsers.add(user);
				}
				catch(NumberFormatException e)
				{
					//Dont do anything...
				}
			}
		}
		
		List<UserModel> adminUsers = new ArrayList<UserModel>(0);
		
		for(DBProperty prop : mem.getProperties("admin_users"))
		{			
			for(UserModel user : mem.getUsers())
			{
				try
				{
					if(user.getFedExId() == Integer.parseInt(prop.getName()))
						adminUsers.add(user);
				}
				catch(NumberFormatException e)
				{
					//Dont do anything...
				}
			}
		}
		
		model.addAttribute("loadUsers", loadUsers);
		model.addAttribute("adminUsers", adminUsers);
		model.addAttribute("version", version);
	}
}
