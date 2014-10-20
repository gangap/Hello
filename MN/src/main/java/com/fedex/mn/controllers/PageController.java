package com.fedex.mn.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.fedex.mn.app.Application;
import com.fedex.mn.controllers.builder.ModelBuilder;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.BasicDetailModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.services.MNService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/page")
public class PageController
{
	@Autowired
	private ModelBuilder builder;
	
	@Autowired
	private MNService mnServ;
	
	@Autowired
	private EmailService emailServ;
	
	@Autowired
	private SessionModel userSession;
	
	@Autowired
	private Application app;
	
	private static Logger log = Logger.getLogger(PageController.class);
	
	@RequestMapping(value="/view/{id}", method=RequestMethod.GET)
	public String getView(@PathVariable("id") int id, ModelMap model)
	{		
		log.info("/mn/edit - Switching MN view");		
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			return "redirect:/login";
		}		
		
		MNModel mnModel = mnServ.getMN(id);
		
		if(mnModel == null)
		{
			return "redirect:/main";			
		}
		else
		{
			builder.setViewDisplayModel(model, mnModel);
			model.addAttribute("view", "view");
			
			return "/main";
		}
	}
	
	@RequestMapping(value="/edit/{id}", method=RequestMethod.GET)
	public String getEdit(@PathVariable("id") int id, ModelMap model)
	{
		log.info("/mn/edit - Edit the MN " + id);		
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			return "redirect:/login";
		}
		
		userSession.setContentParam("Edit MN");
		userSession.setViewParam("");		
		
		//Check to see if the MN is locked...
		//If it is not locked, grant the lock to the user
		if(app.lockMN(id, userSession.getUser()))
		{
			log.debug("Locked the MN " + id);
			
			MNModel mnModel = mnServ.getMN(id);
			
			if(mnModel == null)
			{
				return "redirect:/main";
			}
			
			userSession.getEdittingMNs().add(mnModel);
			
			builder.setEditDisplayModel(model, mnModel);
			model.addAttribute("view", "edit");
			
			return "/main";
		}
		else
		{
			//Add an error
			log.info("Failed to get the lock for the MN");			
			model.addAttribute("type", "error");
			model.addAttribute("title", "MN Locked");
			model.addAttribute("msg", "Failed to get a lock for MN[" + id + "]");
			
			return "redirect:/msg";
		}		
	}
	
	@RequestMapping(value="/create", method=RequestMethod.GET)
	public String getAdd(ModelMap model)
	{
		log.info("/mn/add - Creating a new MN ");		
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			return "redirect:/login";
		}	
		
		userSession.setContentParam("Add MN");
		userSession.setViewParam("");
		
		model.addAttribute("type", "add");
		model.addAttribute("statusLevel", 0);
		
		MNModel mn = new MNModel();
		mn.setBasicDetail(new BasicDetailModel());
		
		builder.setBasicDisplayModel(model, mn);
		model.addAttribute("view", "create");
		
		return "/main";
	}
	
	@RequestMapping(value="/child/{mn}", method=RequestMethod.GET)
	public String postCreateChildMN(@PathVariable("mn") int mnId, ModelMap model)
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
			
		//We are not going to clear all locks for phase II	
		
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
			model.addAttribute("view", "create");
						
			return "/main";
		}
	}
	
	@RequestMapping(value="/reports", method=RequestMethod.GET)
	public String getReportMain(ModelMap model)
	{		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			return "redirect:/login";
		}			
		
		log.debug("/reports/default - Displaying the default report functionality");
		
		builder.setReportChooseDisplayModel(model);
		
		model.addAttribute("view", "report");
						
		return "/main";
	}
	
	@RequestMapping(value="/admin", method=RequestMethod.GET)
	public String getAdminMain(ModelMap model)
	{
		//Check login status
		if(!userSession.isLoggedIn())
		{
			return "redirect:/login";			
		}
		
		log.debug("/admin - Displaying the admin console");
		
		builder.setAdminModel(model);
		
		model.addAttribute("view", "admin");
		
		return "/main";
	}
	
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{
		emailServ.sendErrorEmail(e);
		return "/error/Sever-Exception";
	}
}
