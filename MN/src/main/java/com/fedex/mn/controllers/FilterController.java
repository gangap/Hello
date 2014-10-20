package com.fedex.mn.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fedex.mn.controllers.builder.ModelBuilder;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/filter")
public class FilterController
{	
	@Autowired
	private EmailService emailServ;
	
	@Autowired
	private ModelBuilder builder;
	
	@Autowired
	private SessionModel userSession;
	
	private static Logger log = Logger.getLogger(FilterController.class);
	
	@RequestMapping(method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postFilter(@RequestParam("filterName") String filterName, 
		@RequestParam("filterVal") String filterVal, ModelMap model, HttpServletResponse response)
	{
		log.debug("/filter - Filtering the field: '" + filterName + "' on the value: '" + filterVal + "'");
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		//Check login status
		if(!userSession.isLoggedIn())
		{		
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		//Work-around 
		if(filterVal.equals("null") ||
			filterVal.equals("All Releases") ||
			filterVal.equals("All Statuses") ||
			filterVal.equals("All Destinations") ||
			filterVal.equals("All Change Types") ||
			filterVal.equals("All Load Types") ||
			filterVal.equals("All Symphony Profiles"))
			filterVal = null;
		
		userSession.getFilters().put(filterName, filterVal);	
		Cookie cookie = new Cookie("filters", Util.createFilter(userSession.getFilters()));
		cookie.setMaxAge(3600 * 24 * 60);
		cookie.setPath("/MN");
		
		response.addCookie(cookie);
		
		msgs.add(new ClientMsg("success", "filter", "Successfully added a filter"));
		return msgs;
	}
	
	@RequestMapping(value="/clear", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postClearFilter(ModelMap model, 
		HttpServletResponse response)
	{		
		log.debug("/filter/clear - Clearing all filters");
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		Cookie cookie = new Cookie("filters", "");
		cookie.setMaxAge(3600 * 24 * 60);
		cookie.setPath("/MN");
		
		response.addCookie(cookie);
		userSession.clearFilters();				
		
		msgs.add(new ClientMsg("success", "clear", "Successfully cleared all of the filters"));
		return msgs;
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public String postRefresh(ModelMap model)
	{
		log.debug("/filter/refresh - Refreshing the filter bar");
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
		
		builder.setFilterBarData(model, userSession, false);		
		return "subviews/contents/left-bars/filter-bar";
	}
	
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{
		emailServ.sendErrorEmail(e);
		return "/error/Sever-Exception";
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder)
	{
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat1.setLenient(false);
		
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM/dd/yyyy");
		dateFormat2.setLenient(false);
		
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat1, false));
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat2, false));
	}	
}
