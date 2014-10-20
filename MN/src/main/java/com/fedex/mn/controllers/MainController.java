package com.fedex.mn.controllers;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

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
import com.fedex.mn.controllers.builder.ModelBuilder;
import com.fedex.mn.models.*;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.services.StaticTableService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping({"/main", "/"})
public class MainController 
{
	//Start Fields
	private static Logger log = Logger.getLogger(MainController.class);
	
	@Autowired
	private Application app;
		
	@Autowired
	private ModelBuilder builder;
	
	@Autowired
	private SessionModel userSession;
	
	@Autowired
	private EmailService emailServ;		
	
	//End Fields
	
	//Start Routes
	
	@RequestMapping(method = RequestMethod.GET)
	public String getMain(ModelMap model)
	{
		log.debug("/main - Setting up the main page");				
		
		//Check login status
		if(!userSession.isLoggedIn())
			return "redirect:/login";
		
		userSession.setViewParam("main");
		userSession.setContentParam("");

		builder.setFilterBarData(model, userSession, true);
		builder.setMainPageData(model, userSession);		
		
		return "main";
	}
	
	@RequestMapping(value={"/clear/{mn}"}, method = RequestMethod.GET)
	public String getCleanMain(ModelMap model, @PathVariable(value="mn") int mnId)
	{		
		//Check login status
		if(!userSession.isLoggedIn())
			return "redirect:/login";		
		
		userSession.removeEdittingMN(mnId);
		
		if(mnId > 0)
			app.unlockMN(mnId);
		
		return "redirect:/main";
	}
	
	@RequestMapping(value={"/clear"}, method = RequestMethod.GET)
	public String getCleanMain(ModelMap model)
	{		
		//Check login status
		if(!userSession.isLoggedIn())
			return "redirect:/login";				
		
		return "redirect:/main";
	}
	
	@RequestMapping(value="/cancel", method = RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postCancel()
	{
		log.debug("/main/cancel - The user has cancelled an operation. Now displaying the main page");
		
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		userSession.setViewParam("main");
		userSession.setContentParam("default");
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}

		msgs.add(new ClientMsg("success", "Main Cancel", "Successfully cancelled an operation"));
		return msgs;
	}
	
	@RequestMapping(value="/ajax", method = RequestMethod.GET)
	public String ajaxMain(ModelMap model)
	{
		log.debug("/main/ajax - Getting the standard page through an AJAX request");
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
				
		builder.setMainPageData(model, userSession);
		
		if(userSession.getView().equals("std"))
			return "subviews/contents/main-contents/std-view-content";
		else
			return "subviews/contents/main-contents/tbl-view-content";
	}
	
	@RequestMapping(value="/switch/view", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postSwitchView(@RequestParam("view") String view,
		HttpServletResponse response)
	{
		log.debug("/main/switch/view - The user has requested the view to be switched");
		
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		Cookie cookie = new Cookie("view", view);
		cookie.setMaxAge(3600 * 24 * 60);
		cookie.setPath("/MN");
		
		response.addCookie(cookie);
		
		if(view.equals("std"))
			userSession.setView("std");
		else
			userSession.setView("tbl");
		
		msgs.add(new ClientMsg("success", "", ""));
		return msgs;
	}
	
	@RequestMapping(value="/ajax/lockcheck/{id}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postLockCheck(@PathVariable("id") int id)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
		
		if(app.lockMN(id, userSession.getUser()))
		{
			msgs.add(new ClientMsg("success"));
		}
		else
		{
			msgs.add(new ClientMsg("error", "MN is locked", "This MN is locked by: " + app.getLockedMNs().get(new Integer(id))));
		}
		
		return msgs;
	}
	
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{
		emailServ.sendErrorEmail(e);
		return "/error/Sever-Exception";
	}
	
	//End Routes
	
	//Start General Methods	
	

	
	//End General Methods
	
	//Start Getters/Setters

	
	//End Getters/Setters
}
