package com.fedex.mn.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fedex.mn.app.Application;
import com.fedex.mn.app.cache.MemCache;
import com.fedex.mn.controllers.builder.ModelBuilder;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.EditTrans;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.models.impls.MNView;
import com.fedex.mn.models.impls.ReleaseModel;
import com.fedex.mn.models.validations.MNValidation;
import com.fedex.mn.services.ApprovalService;
import com.fedex.mn.services.EditService;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.services.MNService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/mn/general")
public class MNController
{
	//Start Fields
	private static Logger log = Logger.getLogger(MNController.class);
	
	@Autowired
	private Application app;
	
	@Autowired
	private MemCache mem;
	
	@Autowired
	private MNService mnServ;
	
	@Autowired
	private ApprovalService approvalServ;
	
	@Autowired
	private EmailService emailServ;
	
	@Autowired
	private EditService transServ;
	
	@Autowired
	private ModelBuilder builder;
	
	@Autowired
	private SessionModel userSession;
	
	@Autowired
	private MNValidation valid;
	//End Fields
	
	//Start Methods
	//Start Mappings
	//Start Gets
	
	@RequestMapping(value="/print/{mn}", method=RequestMethod.GET)
	public String getPrintScreen(@PathVariable("mn") int mnId, ModelMap model)
	{		
		MNModel mn = mnServ.getMN(mnId);
		
		if(!userSession.isLoggedIn())
		{
			return "redirect:/login";
		}
				
		if(mn == null)
		{
			//TODO: Create an error page for this
			return "";
		}
		else
		{
			builder.setPrintViewModel(model, mn);

			return "print";
		}
	}
	
	@RequestMapping(value="/dd/{mn}", method=RequestMethod.GET)
	public String getDDView(@PathVariable("mn") int mn, ModelMap model)
	{
		log.debug("/mn/genera/dd/" + mn + " - Getting the DD view");
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
		
		MNModel mnValue = mnServ.getMN(mn);
		
		if(mnValue == null)
		{
			model.addAttribute("type", "error");
			model.addAttribute("title", "Database Error");
			model.addAttribute("msg", "Failed to get the edit history of the MN");
			return "redirect:/msg";
		}
		
		model.addAttribute("mn", mnValue);
		model.addAttribute("approvals", Util.getApprovalType(0, approvalServ.getAll(mn)));
		model.addAttribute("approvalValues", mem.getProperties("approval_types"));
		
		return "/subviews/dialogs/dd";
	}		
		
	@RequestMapping(value="/history/{type}/{mn}", method=RequestMethod.GET)
	public String getEditHistory(@PathVariable("type") String type, 
		@PathVariable("mn") int mn, ModelMap model)
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
		
		List<EditTrans> trans = new ArrayList<EditTrans>();
				
		if(type.equals("edit"))			
			model.addAttribute("type", "edit");			
		else //if(type.equals("view")
			model.addAttribute("type", "view");
		 
		trans = transServ.getAll(mn);
		
		if(trans == null)
		{
			model.addAttribute("type", "error");
			model.addAttribute("title", "Database Error");
			model.addAttribute("msg", "Failed to get the edit history of the MN");
			return "redirect:/msg";
		}
		
		model.addAttribute("edits", trans);
		model.addAttribute("fields", mem.getProperties("field_types"));
		model.addAttribute("mnId", mn);
		
		return "/subviews/contents/main-contents/forms/history-form";
	}	
	
	@RequestMapping(value="/ajax/dep", method=RequestMethod.GET)
	public String getMNinRelease(@RequestParam("rel") int releaseId, 
		@RequestParam(value="mnId", required=false, defaultValue="0") int mnId, ModelMap model)
	{
		List<MNView> mns = new ArrayList<MNView>();
		List<ReleaseModel> releases = mem.getReleases();
		ReleaseModel curRel = null;
		
		for(ReleaseModel rel: releases)
		{
			if(rel.getId() == releaseId)
				curRel = rel;
		}				
		
		for(MNView mn : mem.getMnViews())
		{
			if(mn.getRelease().equals(curRel.getName()))
				mns.add(mn);
		}
		
		MNModel mn = null;
		
		if(mnId > 0)
		{
			mn = mnServ.getMN(mnId);
			
			if(mn != null)
			{
				model.addAttribute("deps", mn.getBasicDetail().getDependencyIds());
			}
		}
		
		model.addAttribute("mns", mns);
		
		return "/subviews/contents/main-contents/forms/components/mn-dep-select";
	}
	
	@RequestMapping(value="/{type}/cancel", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postCancelOperation(@PathVariable("type") String type)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		if(type.equals("add"))
		{
			msgs.add(new ClientMsg("success", "Add Cancel", "Successfully cancelled the add operation"));
		}
		//Removing the 'edit' cancel operation
		else if(type.equals("view"))
		{
			msgs.add(new ClientMsg("success", "View Cancel", "Successfully cancelled the view operation"));
		}
		else if(type.equals("report"))
		{			
			msgs.add(new ClientMsg("success", "Report Cancel", "Successfully cancelled the report operation"));
		}
		else
		{
			msgs.add(new ClientMsg("error", "Invalid Cancel Operation", "This cancel operation type is unknown"));
		}
		
		return msgs;
	}
	
	@RequestMapping(value="/{type}/enabled", method=RequestMethod.POST)
	public @ResponseBody ClientMsg postIsTabEnabled(@PathVariable("type") String type, 
		@RequestParam(value="dest", required=false) int dest, @RequestParam(value="status", required=false) int status,
		@RequestParam(value="change", required=false) int change, @RequestParam(value="load", required=false) int load)
	{		
		if(type.equals("test"))
		{
			if(valid.isTestEnabled(dest, load))
				return Util.getTrueMsg();
			else
				return Util.getFalseMsg();
		}
		else //if(type.equals("build"))
		{
			if(valid.isBuildEnabled(status, change))
				return Util.getTrueMsg();
			else
				return Util.getFalseMsg();
		}
	}
	
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{
		emailServ.sendErrorEmail(e);
		return "/error/Sever-Exception";
	}
	
	//End Posts
	//End Requests
	//End Methods
}
