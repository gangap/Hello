package com.fedex.mn.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.fedex.mn.app.cache.MemCache;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.AppStateModel;
import com.fedex.mn.models.impls.QueryResultsModel;
import com.fedex.mn.models.impls.UserModel;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.services.StaticTableService;
import com.fedex.mn.utils.Util;

/**
 * Initializes the application and holds values that are critical across the application.
 * <p>
 * @author Paul Marks
 *
 */
@Component("app")
public class Application
	implements Serializable
{
	private static final long serialVersionUID = 5614139431490729039L;
	
	private static Logger log = Logger.getLogger(Application.class);
	
	private AppStateModel appState;
	
	@Value("${app.env:dev}")
	private String env;
	
	@Value("${app.admin.uid:893008}")
	private String adminUserId;
	private UserModel adminUser;
	
	private Map<Integer, UserModel> lockedMNs;
	private Map<Integer, QueryResultsModel> resultList;
	private List<UserModel> loggedUsers;
	
	@Autowired
	private MemCache mem;
	
	@Autowired
	private StaticTableService stServ;
	
	@Autowired
	private EmailService emailServ;
	
	@Value("${app.error.email}")
	private String errorEmailAddress;
	
	/**
	 * Initialize the MN System appliction by idenifying the instance type and initializing the cache
	 */
	@PostConstruct
	public void init()
	{
		log.info("Initializing the MN System");
		
		if(env.equals("dev"))
			log.info("Development Instance");
		else if(env.equals("test"))
			log.info("Internal Test Instance");
		else			
			log.info("Production Instance");
		
		mem.init();
		
		int adminId = 0;
		try
		{
			 adminId = Integer.parseInt(adminUserId);
		}
		catch(NumberFormatException e)
		{
			adminId = 893008;
		}
		
		adminUser = Util.getUser(mem.getUsers(), adminId);
		
		lockedMNs = new HashMap<Integer, UserModel>();
		resultList = new HashMap<Integer, QueryResultsModel>();				
		
		appState = stServ.getAppState();		
		appState.setLastStarted(Calendar.getInstance().getTime());
		stServ.updateAppState(appState);			
		
		loggedUsers = new ArrayList<UserModel>();
		
		log.info("The MN System has successfully loaded");
	}
	
	public void addUser(UserModel model)
	{		
		log.info("Attempting to add the user[" + model + "] to the session list");
		
		for(UserModel user : loggedUsers)
		{
			if(user != null && user.equals(model))				
				return;
		}
				
		loggedUsers.add(model);
		log.info("Adding the user[" + model + "] to the session list (" + loggedUsers.size() + " users)");
	}
	
	public void removeUser(UserModel model)
	{	
		log.info("Attempting to remove the user[" + model + "] from the session list");
		
		Iterator<UserModel> iter = loggedUsers.iterator();
		while(iter.hasNext())
		{
			UserModel user = iter.next();
			
			if(user == null || user == null)
			{
				iter.remove();
			}			
			else if(user.equals(model))
			{
				log.info("Removed user[" + user + "] from the app's session list (" + (loggedUsers.size() - 1) + " users)");
				iter.remove();
				break;
			}
		}	
	}
	
	public synchronized boolean lockMN(int mn, UserModel user)
	{		
		log.debug("Attempting to obtain a lock on the MN[" + mn + "] for user: " + user.getName());
		
		//Make sure that the MN isn't already locked
		if(lockedMNs.containsKey(mn))
		{
			//The MN is locked... 
			//If the MN is locked by the user attempting to access the lock...
			//Allow the user to access the MN for editting
			
			if(lockedMNs.get(mn).equals(user))
			{				
				return true;				
			}
			else
			{
				return false;
			}
		}

		//For phase II...allow the user to have multiple MNs open at the same time for editting
		//No removing of the locks if the user has one already
		
		//Give the user the lock on the MN
		lockedMNs.put(mn, user);
		
		return true;
	}
	
	public void unlockMN(int mn)
	{
		log.debug("Attempting to unlock MN[" + mn + "]");
		
		if(lockedMNs.containsKey(mn))
		{
			log.debug("The MN[" + mn + "] has been unlocked");
			lockedMNs.remove(mn);	
		}		
		else
		{
			log.debug("The MN was not locked");
		}
	}
	
	public void unlockMNByUser(UserModel user)
	{
		if(user != null)
		{
			log.debug("Attempting to unlock the MN belonging to user " + user.getName());
			
			while(lockedMNs.values().remove(user)){}
		}
	}
	
	public boolean isLocked(int mn)
	{
		if(lockedMNs.containsKey(mn))
			return true;
		
		return false;
	}
	
	public boolean hasLock(UserModel user, int mnId)
	{
		if(lockedMNs.containsKey(mnId))
			if(lockedMNs.get(mnId).equals(user))
				return true;
		
		return false;
	}
	
	public void unlockAllMNs()
	{
		log.debug("Attempting to unlock all MNs");
		Iterator<Integer> lockedKeys = lockedMNs.keySet().iterator();
		
		while(lockedKeys.hasNext())
		{
			log.debug("Unlocking MN[" + lockedKeys.next() + "]");
			lockedKeys.remove();
		}
		
		log.debug("Attempting to unlock all MNs");
	}		
	
	public Map<Integer, UserModel> getLockedMNs()
	{
		return lockedMNs;
	}
	
	public Map<Integer, QueryResultsModel> getResultList()
	{
		return resultList;
	}
	
	public AppStateModel getAppState()
	{
		return appState;
	}
	
	public UserModel getAdminUser()
	{
		return adminUser;
	}
	
	public void setAppState(AppStateModel appState)
	{
		this.appState = appState;
	}
	
	public List<UserModel> getLoggedUsers()
	{
		return loggedUsers;
	}
	
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{
		emailServ.sendErrorEmail(e, errorEmailAddress, "Application", 0);
		return "/error/Sever-Exception";
	}	
}
