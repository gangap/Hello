package com.fedex.mn.controllers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fedex.mn.app.Application;
import com.fedex.mn.app.cache.MemCache;
import com.fedex.mn.controllers.builder.ModelBuilder;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.ApprovalModel;
import com.fedex.mn.models.impls.BasicDetailModel;
import com.fedex.mn.models.impls.BuildDetailModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.DeploymentDetailModel;
import com.fedex.mn.models.impls.EditField;
import com.fedex.mn.models.impls.EditTrans;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.models.impls.MNView;
import com.fedex.mn.models.impls.ReleaseModel;
import com.fedex.mn.models.impls.SyncModel;
import com.fedex.mn.models.impls.TestDetailModel;
import com.fedex.mn.models.validations.MNValidation;
import com.fedex.mn.services.EditService;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.services.MNService;
import com.fedex.mn.services.ReleaseService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/mn/create")
public class CreateController
{	
	@Autowired
	private ModelBuilder builder;
	
	@Autowired
	private Application app;
	
	@Autowired
	private MNService mnServ;
	
	@Autowired
	private EmailService emailServ;
	
	@Autowired
	private MNValidation mnValidation;
	
	@Autowired
	private SessionModel userSession;
	
	@Autowired
	private EditService editServ;
	
	@Autowired
	private ReleaseService relServ;
	
	@Autowired
	private MemCache mem;
	
	private static Logger log = Logger.getLogger(CreateController.class);
	/**
	 * GET - for AJAX requests to create an MN
	 * @param model - The model to be used to populate the form
	 * @return - The create subview of the MN
	 */
	@RequestMapping(method=RequestMethod.GET)
	public String getAdd(ModelMap model)
	{
		log.info("/mn/add - Creating a new MN ");		
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
		
		userSession.setContentParam("Add MN");
		userSession.setViewParam("");
		
		model.addAttribute("type", "add");
		model.addAttribute("statusLevel", 0);
		
		MNModel mn = new MNModel();
		mn.setBasicDetail(new BasicDetailModel());
		
		builder.setBasicDisplayModel(model, mn);
		
		return "subviews/contents/main-contents/add-mn-content";
	}
	
	@RequestMapping(value="/child/{view}/{mn}", method=RequestMethod.GET)
	public String postCreateChildMN(@PathVariable("view") String view,
		@PathVariable("mn") int mnId, ModelMap model)
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
			
		//Do not clear locks for phase II changes		
		MNModel mn = mnServ.getMN(mnId);
		
		if(mn == null)
		{
			model.addAttribute("type", "error");
			model.addAttribute("title", "Database Error");
			model.addAttribute("msg", "Failed to get an MN for child creation");
			return "redirect:/msg";
		}
		else
		{
			model.addAttribute("type", "add");
			
			//Set the status of the new MN to null
			mn.getBasicDetail().setStatus(new DBProperty());
			mn.setParentID(mn.getId());
			builder.setBasicDisplayModel(model, mn);
			model.addAttribute("mnId", mn.getId());
			model.addAttribute("isChild", true);
						
			return "subviews/contents/main-contents/add-mn-content";
		}
	}
	
	@RequestMapping(value="/save", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postAddSave(@ModelAttribute("basic") BasicDetailModel basic, 
			BindingResult result, @RequestParam(value = "parentId", required = false) int parentId, 
			@RequestParam(value="loadTeam", required=false, defaultValue="") List<Integer> loadTeam,
			@RequestParam(value="elHour", required=false, defaultValue="0") String elHour,
			@RequestParam(value="elMin", required=false, defaultValue="0") String elMin,
			@RequestParam(value="elSec", required=false, defaultValue="0") String elSec,
			HttpServletRequest request)	
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();

		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}			
		
		if(basic.getDependencyIds() == null || basic.getDependencyIds().contains(new Integer(-1)))
		{
			basic.setDependencyIds(new ArrayList<Integer>(0));
		}
		
		ArrayList<DBProperty> loadList = new ArrayList<DBProperty>();
		for(Integer id : loadTeam)
		{
			DBProperty load = new DBProperty();
			load.setId(id);
			loadList.add(load);
		}
		
		msgs.addAll(mnValidation.validateBasic(basic));
		
		basic.setLoadTeam(loadList);
		if(basic.getLoadTeam().isEmpty())
		{
			msgs.add(new ClientMsg("validation", "basic", "loadTeam", "Please select at least one (1) Load Team"));
		}
		
		if(result.hasErrors())
		{						
			for(FieldError err : result.getFieldErrors())
			{
				if(err.getField().equals("expectedLoadDate"))
				{
					msgs.add(new ClientMsg("validation", "basic", "expectedLoadDate", "Invalid entry for the Expected Load Date"));
				}
				if(err.getField().equals("artifact"))
				{
					msgs.add(new ClientMsg("validation", "basic", "artifact", "Invalid entry for the artifact"));
				}
			}									
		}
		
		if(msgs.size() > 0)
			return msgs;
		
		basic.setOriginator(userSession.getUser());	
		
		//Set the expected load times...
		Calendar exp = new GregorianCalendar();
		exp.setTime(basic.getExpectedLoadDate());
				
		try
		{
			exp.set(Calendar.HOUR_OF_DAY, Integer.parseInt(elHour));
			exp.set(Calendar.MINUTE, Integer.parseInt(elMin));
			exp.set(Calendar.SECOND, Integer.parseInt(elSec));
		}
		catch(NumberFormatException e)
		{
			msgs.add(new ClientMsg("validation", "basic", "expectedLoadHour", "Invalid time entry"));
		}
		
		basic.setExpectedLoadDate(exp.getTime());
		
		SyncModel sync = new SyncModel();
		sync.getValues().put("all", ServletRequestUtils.getBooleanParameter(request, "all-cbox", false));
		sync.getValues().put("none", ServletRequestUtils.getBooleanParameter(request, "none-cbox", false));
		sync.getValues().put("2", ServletRequestUtils.getBooleanParameter(request, "2-cbox", false));
		sync.getValues().put("3", ServletRequestUtils.getBooleanParameter(request, "3-cbox", false));
		sync.getValues().put("4", ServletRequestUtils.getBooleanParameter(request, "4-cbox", false));
		sync.getValues().put("5", ServletRequestUtils.getBooleanParameter(request, "5-cbox", false));
		sync.getValues().put("6", ServletRequestUtils.getBooleanParameter(request, "6-cbox", false));
		
		basic.setSyncs(sync);
		
		//Create a blank MNModel
		MNModel mn = new MNModel();
		mn.setParentID(parentId);
		mn.setBasicDetail(basic);
		mn.setBuildDetail(new BuildDetailModel());
		mn.setTestDetail(new TestDetailModel());
		mn.setDeploymentDetail(new DeploymentDetailModel());
		
		//Get all of the signatures that will need to be made
		
		if(!mnServ.addMN(mn) || !editServ.add(createEditTrans(mn)))
		{
			msgs.add(new ClientMsg("error", "Database Error", "Could not add the MN to the database"));
			return msgs;
		}		
		else
		{			
			int mnId = mn.getId();
			
			updateCache(mn);
			msgs.add(new ClientMsg("success", "Creation Successful", "The MN " + mnId + " was successfully created"));
			msgs.addAll(mnValidation.validateSignatures(mn.getBasicDetail().getDestination().getId(), 
					mn.getBasicDetail().getLoadType().getId(), 
					mn.getBasicDetail().getStatus().getId(), 
					new ArrayList<ApprovalModel>()));
			msgs.add(new ClientMsg("mn", mnId + "", ""));
			return msgs;			
		}
	}
		
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{
		emailServ.sendErrorEmail(e);
		return "/error/Sever-Exception";
	}
	
	public EditTrans createEditTrans(MNModel mn)
	{
		EditTrans edit = new EditTrans();
		
		edit.setMnId(mn.getId());
		edit.setDateEdited(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		edit.setEditedBy(userSession.getUser());
		
		List<EditField> editFields = new ArrayList<EditField>(0);
		
		List<DBProperty> fields = mem.getProperties("field_types");
		DBProperty field;
		
		//Go through the old MN and see what is different in the new MN
		//For each one note the change as a editField 
		//Basic
		BasicDetailModel basic = mn.getBasicDetail();
		
		field = fields.get(Util.getIndex(fields, "Status"));
		editFields.add(new EditField(field.getId(), "-Empty-", mem.getName("status_types", basic.getStatus().getId())));

		field = fields.get(Util.getIndex(fields, "Symphony Profile"));
		editFields.add(new EditField(field.getId(), "-Empty-", mem.getName("symphony_profile_types", basic.getSymphonyProfile().getId())));

		field = fields.get(Util.getIndex(fields, "Load Type"));
		editFields.add(new EditField(field.getId(), "-Empty-", mem.getName("load_types", basic.getLoadType().getId())));

		field = fields.get(Util.getIndex(fields, "Change Type"));
		editFields.add(new EditField(field.getId(), "-Empty-", mem.getName("change_types", basic.getChangeType().getId())));

		field = fields.get(Util.getIndex(fields, "Reference ID"));
		editFields.add(new EditField(field.getId()));

		field = fields.get(Util.getIndex(fields, "Dependency"));
		editFields.add(new EditField(field.getId()));

		field = fields.get(Util.getIndex(fields, "Load Team"));
		editFields.add(new EditField(field.getId()));

		field = fields.get(Util.getIndex(fields, "Load Team"));
		editFields.add(new EditField(field.getId()));

		field = fields.get(Util.getIndex(fields, "Summary"));
		editFields.add(new EditField(field.getId()));

		field = fields.get(Util.getIndex(fields, "Destination"));
		editFields.add(new EditField(field.getId(), "-Empty-", mem.getName("destination_types", basic.getDestination().getId())));
				
		ReleaseModel rel = relServ.getRelease(basic.getRelease().getId());
		if(rel != null)
		{
			field = fields.get(Util.getIndex(fields, "Release"));
			editFields.add(new EditField(field.getId(), "-Empty-", rel.getName()));	
		}
		
		field = fields.get(Util.getIndex(fields, "Expected Load Date"));
		editFields.add(new EditField(field.getId(), "-Empty-", basic.getExpectedLoadDate().toString()));
		
		edit.getEditFields().addAll(editFields);
		
		return edit;
	}
	
	private void updateCache(MNModel mn)
	{
		MNView mnView = new MNView();
		
		mnView.setMnId(mn.getId());
		mnView.setActDeliverables("");
		mnView.setArtifact(mn.getBasicDetail().getArtifact());
		mnView.setChangeType(Util.getName(mem.getProperties("change_types"), mn.getBasicDetail().getChangeType().getId()));
		mnView.setComment(mn.getBasicDetail().getComments());
		mnView.setDest(Util.getName(mem.getProperties("destination_types"), mn.getBasicDetail().getDestination().getId()));
		mnView.setExpLoadDate(mn.getBasicDetail().getExpectedLoadDate());
		mnView.setLoadType(Util.getName(mem.getProperties("load_types"), mn.getBasicDetail().getLoadType().getId()));
		mnView.setProject("");
		mnView.setSourceCode("");
		mnView.setSrs(Util.getName(mem.getProperties("symphony_profile_types"), mn.getBasicDetail().getSymphonyProfile().getId()));
		mnView.setStatus(Util.getName(mem.getProperties("status_types"), mn.getBasicDetail().getStatus().getId()));
		mnView.setTracker("");
		mnView.setName(mn.getBasicDetail().getOriginator().getName());
		mnView.setCreated(mn.getDateCreated());
		
		List<ReleaseModel> releases = relServ.getAllReleases();
		for(ReleaseModel rel : releases)
		{
			if(rel.getId() == mn.getBasicDetail().getRelease().getId())
				mnView.setRelease(rel.getName());
		}
		
		mem.getMnViews().add(mnView);
	}
}
