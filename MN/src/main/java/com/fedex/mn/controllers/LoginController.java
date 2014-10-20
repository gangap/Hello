package com.fedex.mn.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fedex.mn.app.Application;
import com.fedex.mn.app.cache.MemCache;
import com.fedex.mn.controllers.builder.ModelBuilder;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.LoginModel;
import com.fedex.mn.models.impls.UserModel;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.services.LDAPService;
import com.fedex.mn.services.UserService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/login")
public class LoginController
{	
	private static Logger log = Logger.getLogger(LoginController.class);
	
	@Autowired
	private Application app;
	
	@Autowired
	private UserService userServ;
	
	@Autowired
	private SessionModel userSession;
	
	@Autowired
	private LDAPService ldapServ;
	
	@Autowired
	private MemCache mem;
	
	@Autowired
	private ModelBuilder mBuilder;
	
	@Autowired
	private EmailService emailServ;
		
	@Value("${app.error.email}")
	private String errorEmailAddress;
	
	@Value("${app.env}")
	private String env;
	
	@Value("${app.admin.uid}")
	private String adminUid;
	
	@Value("${sso.url:http://sso.secure.fedex.com/MN/login}")
	private String ssoURL;
	
	@RequestMapping(method=RequestMethod.GET)
	public String getLogin(@ModelAttribute("login") LoginModel login,
			@CookieValue(value="view", defaultValue="std", required=false) String view, 
			@CookieValue(value="filters", defaultValue="", required=false) String filters,
			@RequestHeader(value="oblix_uid", required=false, defaultValue="-1") int uid, 
			HttpServletRequest request, ModelMap model)
	{		
		log.debug("/login(GET) - Getting the Login page");
		
		userSession.setContentParam("");
		userSession.setViewParam("login");
		
		if(!env.equals("prod") )
		{
			if(login.getUsername() == null || login.getUsername().trim().isEmpty())
			{
				mBuilder.setLoginPageData(model);
				return "/login";
			}
			else
			{
				uid = Integer.parseInt(login.getUsername());
			}
		}
		else if(uid < 0)
		{
			//We are in production			
			return "redirect:http://sso.secure.fedex.com/MN/login";
		}
		
		log.debug("/login(GET) - Logging in the user[" + uid + "]");
		
		UserModel user = ldapServ.getUser(uid);
		
		if(user != null)
		{						
			//Ensure that the user is part of the DB
			if(userServ.hasUser(uid))
			{
				log.debug("The user is already in the DB");
			}	
			else
			{
				if(userServ.addUser(user))
					log.debug("The user has been added to the database");
				else
					log.error("Failed to add the user[" + user.getId() + "] to the database");				
			}
			
			user = Util.getUser(mem.getUsers(), uid);
						
			if(Util.getId(mem.getProperties("load_team_users"), new String("" + user.getFedExId())) > 0)
				user.setLoadTeamUser(true);
			else
				user.setLoadTeamUser(false);
			
			if(Util.getId(mem.getProperties("admin_users"), new String("" + user.getFedExId())) > 0)
				user.setAdmin(true);
			else
				user.setAdmin(false);			
			
			userSession.setLoggedIn(true);
			userSession.setUser(user);
			userSession.setView(view);
			userSession.setFilters(Util.parseFilters(filters));
			
			app.addUser(userSession.getUser());
		}
		
		if(userSession.isLoggedIn())
		{
			if(env.equals("dev") || env.equals("test"))
				return "redirect:/main";
			else
				return "redirect:https://sso.secure.fedex.com/MN/main";
		}
		else
		{
			if(env.equals("prod"))
			{
				return "redirect:https://sso.secure.fedex.com/MN/login";
			}
			else
			{
				mBuilder.setLoginPageData(model);
				return "login";
			}
		}
	}
	
	@RequestMapping(value={"/logout"}, method=RequestMethod.GET)
	public String postLogout()
	{		
		if(userSession.getUser() != null)
			log.debug("/login/logout - The user " + userSession.getUser().getName() + " has logged out");
		else
			log.debug("/login/logout - The user has logged out");
		
		//Log the user's session out		
		userSession.clearFilters();
		userSession.setEdittingMNs(null);
				
		userSession.setViewParam(null);
		userSession.setContentParam(null);
		
		app.unlockMNByUser(userSession.getUser());
		app.removeUser(userSession.getUser());
		
		userSession.setLoggedIn(false);		
		
		if(env.equals("dev"))
		{
			log.debug("The dev user has logged out...redirecting back to the login page");
			return "redirect:/login";
		}
		else
		{
			return "redirect:https://sso.secure.fedex.com/wsso/logout.html";	
		}		
	}
	
	@RequestMapping(value="/check", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postLoginCheck()
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		if(userSession.isLoggedIn())
			msgs.add(new ClientMsg("success", "", "valid"));
		else
			msgs.add(new ClientMsg("success", "", "invalid"));
		
		return msgs;
	}
	
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{		
		if(userSession.isLoggedIn())
			emailServ.sendErrorEmail(e);
		else
			emailServ.sendErrorEmail(e, errorEmailAddress, "Login", 0);
		
		return "/error/Sever-Exception";
	}
}
