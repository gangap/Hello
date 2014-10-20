package com.fedex.mn.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fedex.mn.app.Application;
import com.fedex.mn.controllers.builder.ModelBuilder;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.services.MNService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/mn/view")
public class ViewController
{	
	@Autowired
	private MNService mnServ;
	
	@Autowired
	private EmailService emailServ;
	
	@Autowired
	private ModelBuilder builder;
	
	@Autowired
	private SessionModel userSession;
	
	private static Logger log = Logger.getLogger(ViewController.class);		
	
	@RequestMapping(method=RequestMethod.GET)
	public String getView(@RequestParam("mn") int mn, ModelMap model)
	{		
		log.info("/mn/view - Switching MN view");		
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}		
		
		MNModel mnModel = mnServ.getMN(mn);
		
		if(mnModel == null)
		{
			model.addAttribute("type", "error");
			model.addAttribute("title", "Database Error");
			model.addAttribute("msg", "Could not retrieve the MN " + mn + " from the database!");
			return "redirect:/msg";			
		}
		else
		{
			builder.setViewDisplayModel(model, mnModel);
			
			return "/subviews/contents/main-contents/view-mn-content";
		}
	}
	
	/**
	 * GET - for AJAX requests to specific MN sub-forms
	 * @param view - The sub-form to display
	 * @param model - The model the controller will populate for the view
	 * @return - If the user is logged in it will send them to the correct view
	 */
	@RequestMapping(value="/{view}/{mn}", method=RequestMethod.GET)
	public String getViewForm(@PathVariable("view") String view, 
		@PathVariable("mn") int mnId, ModelMap model)
	{
		log.info("/mn/edit - Switching View MN views ");		
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}		
		
		model.addAttribute("type", "view");
		model.addAttribute("mnId", mnId);
		
		MNModel mn = mnServ.getMN(mnId);
		
		if(mn == null)
		{
			model.addAttribute("type", "error");
			model.addAttribute("title", "Database Error");
			model.addAttribute("msg", "Could not retrieve the MN currently being viewed");
			return "redirect:/msg";
		}
		
		model.addAttribute("parentId", mn.getParentID());
		
		if(view.equals("basic"))
		{			
			builder.setBasicDisplayModel(model, mn);
			userSession.setViewParam("basic");
			
			return "/subviews/contents/main-contents/forms/basic-form";
		}
		else if(view.equals("test"))
		{			
			builder.setTestDisplayModel(model, mn);
			userSession.setViewParam("test");
			
			return "/subviews/contents/main-contents/forms/test-form";
		}
		else if(view.equals("build"))
		{
			builder.setBuildDisplayModel(model, mn);
			userSession.setViewParam("build");
			
			return "/subviews/contents/main-contents/forms/build-form";
		}
		else //if(view.equals("deploy"))
		{
			builder.setDeployDisplayModel(model, mn);
			userSession.setViewParam("deploy");
			
			return "/subviews/contents/main-contents/forms/deploy-form";
		}
	}
	
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{
		emailServ.sendErrorEmail(e);
		return "/error/Sever-Exception";
	}
}
