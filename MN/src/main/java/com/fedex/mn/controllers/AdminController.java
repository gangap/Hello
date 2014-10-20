package com.fedex.mn.controllers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.fedex.mn.models.impls.ApprovalModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.EditField;
import com.fedex.mn.models.impls.EditTrans;
import com.fedex.mn.models.impls.ReleaseModel;
import com.fedex.mn.models.impls.UserModel;
import com.fedex.mn.services.ApprovalService;
import com.fedex.mn.services.EditService;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.services.LDAPService;
import com.fedex.mn.services.ReleaseService;
import com.fedex.mn.services.StaticTableService;
import com.fedex.mn.services.UserService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/admin")
public class AdminController
{
	@Autowired
	private EmailService emailServ;
		
	@Autowired
	private ModelBuilder builder;
	
	@Autowired
	private SessionModel userSession;	
	
	@Autowired
	private ReleaseService relServ;
	
	@Autowired
	private UserService userServ;
	
	@Autowired
	private ApprovalService approvalServ;
	
	@Autowired
	private LDAPService ldapServ;
	
	@Autowired
	private EditService editServ;
	
	@Autowired
	private Application app;
	
	@Autowired
	private StaticTableService stServ;
	
	@Autowired
	private MemCache mem;
	
	private static Logger log = Logger.getLogger(AdminController.class);
	
	@RequestMapping(method=RequestMethod.GET)
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
		
		return "/subviews/contents/main-contents/admin-content";
	}
	
	@RequestMapping(value="/signature/get/{mnId}", method=RequestMethod.GET)
	public String getMNSignatures(@PathVariable("mnId") int mnId, ModelMap model)
	{
		//Check login status
		if(!userSession.isLoggedIn())
		{
			return "redirect:/login";			
		}
		
		log.debug("/signature/get/" + mnId + " - Getting the signatures");
		
		model.addAttribute("signatures", approvalServ.getAll(mnId));
		
		return "/subviews/contents/main-contents/forms/components/admin-remove-signature";
	}
	
	@RequestMapping(value="/signature/remove/{id}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postRemoveSignature(@PathVariable("id") int approvalId)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		log.debug("/signature/remove/" + approvalId + " - Posting removing the approval");
		
		ApprovalModel approval = approvalServ.get(approvalId);
		
		if(!approvalServ.remove(approvalId))
		{
			msgs.add(new ClientMsg("error", "Database Error", "Failed to remove the approval"));
			return msgs;
		}
		
		if(editServ.add(removeApprovalEditTrans(approval)))
			msgs.add(new ClientMsg("successful"));
		else
			msgs.add(new ClientMsg("error", "Database Error", "Successfully removed the approval, but the edit transaction failed"));
		
		return msgs;
	}
	
	@RequestMapping(value="/ldap/creds", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postUpdateLDAPCreds(@RequestParam("user") int user,
		@RequestParam("pass") String pass)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		app.getAppState().setLdapUser(user);
		app.getAppState().setLdapPass(pass);
		if(stServ.updateAppState(app.getAppState()))
			msgs.add(new ClientMsg("successful"));
		else
			msgs.add(new ClientMsg("error"));
		
		return msgs;
	}
	
	@RequestMapping(value="/release/add", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postAdd(@RequestParam("relName") String relName, 
		ModelMap model)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();

		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
		
		//Make sure that we dont take a name already in use
		for(ReleaseModel release : relServ.getAllReleases())
		{
			if(release.getName().equals(relName))
			{
				//Make an error...
				//This error will probably have to go in the user-session...
				log.debug("There is already a release with this name!");
				msgs.add(new ClientMsg("error", "Release Name Duplication", "There is already a release with this name!"));
			}
		}
		
		ReleaseModel rel = new ReleaseModel();
		rel.setName(relName);
		rel.setDateCreated(Calendar.getInstance().getTime());
		
		if(!relServ.addRelease(rel))
		{
			msgs.add(new ClientMsg("error", "Database Error", "There was an error during the creation of a new release in the database"));
		}
		else
		{			
			log.debug("Added a new release [" + relName + "]");
			
			msgs.add(new ClientMsg("success", "Release Creation", "Successfullyl created a new release"));
		}
		
		return msgs;
	}
	
	@RequestMapping(value="/release/delete", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postDelete(@RequestParam("relName") String relName, 
		ModelMap model)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();

		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
		
		if(!relServ.deleteRelease(relName))
		{
			log.debug("The release [" + relName + "] has been successfully deleted");
			msgs.add(new ClientMsg("success", "Release Deletiona", "Successfully deleted the release: " + relName));			
		}
		else
		{
			msgs.add(new ClientMsg("error", "Database Error", "There was an error during the creation of a new release in the database"));
		}
		
		return msgs;
	}
	
	@RequestMapping(value="/add/user/{uid}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postAddUser(@PathVariable(value="uid") int uid)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
		
		//Make sure the user is not already entered
		if(userServ.hasUser(uid))
		{
			msgs.add(new ClientMsg("error", "User Already Entered", "This user[" + uid + "] is already in the database"));
			return msgs;
		}
							
		UserModel newUser = ldapServ.getUser(uid);
		
		if(newUser == null)
		{
			msgs.add(new ClientMsg("error", "LDAP Error", "Failed to get the user from LDAP"));
			return msgs;
		}
		
		if(!userServ.addUser(newUser))
		{
			msgs.add(new ClientMsg("error", "Database Error", "Failed to add the user[" + uid + "] to the database"));
			return msgs;
		}
		
		mem.getUsers().add(newUser);
		msgs.add(new ClientMsg("success"));
		
		return msgs;
	}
	
	@RequestMapping(value="/pbsuser/add", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postAddPBSUser(@RequestParam(value="id") int id)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
		
		//First check to see if the id is in the Load User list already		
		if(mem.getPropId("load_team_users", id + "") > 0)
		{
			msgs.add(new ClientMsg("error", "", "The user[" + id + "] is already in the group"));
			return msgs;
		}
		
		//Then make sure it is in the user list
		UserModel newUser = null;
		if(!userServ.hasUser(id))
		{
			//If not check to make sure it is a valid user			
			newUser = ldapServ.getUser(id);
			
			if(newUser == null)
			{
				msgs.add(new ClientMsg("error", "LDAP Error", "Failed to get the user from LDAP"));
				return msgs;
			}
			
			if(!userServ.addUser(newUser))
			{
				msgs.add(new ClientMsg("error", "Database Error", "Failed to add the user[" + id + "] to the database"));
				return msgs;
			}
			
			mem.getUsers().add(newUser);
			msgs.add(new ClientMsg("successful", "", "Added the user[" + newUser.getName() + "] to the user list"));
		}
		else
		{
			newUser = userServ.getUserFed(id);
		}
		
		//Finally add the user to the list
		DBProperty prop = new DBProperty();
		prop.setName(id + "");

		if(stServ.add("load_team_users", prop))
		{
			mem.getProperties("load_team_users").add(prop);
			msgs.add(new ClientMsg("successful", "", "Successfully added the user[" + newUser.getName() + "] to the PBS team"));
			msgs.add(new ClientMsg(newUser.getName(), newUser.getFedExId() + "", ""));
		}
		
		return msgs;
	}
	
	@RequestMapping(value="/pbsuser/remove", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postRemovePBSUser(@RequestParam(value="ids[]") List<Integer> ids)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		for(Integer id : ids)
		{
			DBProperty prop = mem.getProperties("load_team_users").get(Util.getIndex(mem.getProperties("load_team_users"), id.intValue() + ""));
			if(stServ.remove("load_team_users", prop))
			{
				msgs.add(new ClientMsg("successful", prop.getName(), ""));
				mem.getProperties("load_team_users").remove(prop);
			}
		}		
		
		if(msgs.size() > 0)
			msgs.add(0, new ClientMsg("successful", "", "Successfully removed user(s) from the PBS Team"));
		else
			msgs.add(0, new ClientMsg("error", "", "Failed to remove any user(s) from the PBS Team"));
						
		return msgs;
	}
	
	@RequestMapping(value="/adminuser/add", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postAddAdminUser(@RequestParam(value="id") int id)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
		
		//First check to see if the id is in the Load User list already		
		if(mem.getPropId("admin_users", id + "") > 0)
		{
			msgs.add(new ClientMsg("error", "", "The user[" + id + "] is already in an admin"));
			return msgs;
		}
		
		//Then make sure it is in the user list
		UserModel newUser = null;
		if(!userServ.hasUser(id))
		{
			newUser = ldapServ.getUser(id);
			
			if(newUser == null)
			{
				msgs.add(new ClientMsg("error", "LDAP Error", "Failed to get the user from LDAP"));
				return msgs;
			}
			
			if(!userServ.addUser(newUser))
			{
				msgs.add(new ClientMsg("error", "Database Error", "Failed to add the user[" + id + "] to the database"));
				return msgs;
			}
			
			mem.getUsers().add(newUser);
			msgs.add(new ClientMsg("successful", "", "Added the user[" + newUser.getName() + "] to the admin list"));
		}
		else
		{
			newUser = userServ.getUserFed(id);
		}
		
		//Finally add the user to the list
		DBProperty prop = new DBProperty();
		prop.setName(id + "");

		if(stServ.add("admin_users", prop))
		{
			mem.getProperties("admin_users").add(prop);
			msgs.add(new ClientMsg("successful", "", "Successfully added the user[" + newUser.getName() + "] to the admin list"));
			msgs.add(new ClientMsg(newUser.getName(), newUser.getFedExId() + "", ""));
		}
		
		return msgs;
	}
	
	@RequestMapping(value="/adminuser/remove", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postRemoveAdminUser(@RequestParam(value="ids[]") List<Integer> ids)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		for(Integer id : ids)
		{
			DBProperty prop = mem.getProperties("admin_users").get(Util.getIndex(mem.getProperties("admin_users"), id.intValue() + ""));
			if(stServ.remove("admin_users", prop))
			{
				msgs.add(new ClientMsg("successful", prop.getName(), ""));
				mem.getProperties("admin_users").remove(prop);
			}
		}		
		
		if(msgs.size() > 0)
			msgs.add(0, new ClientMsg("successful", "", "Successfully removed user(s) from the admin list"));
		else
			msgs.add(0, new ClientMsg("error", "", "Failed to remove any user(s) from the admin list"));
						
		return msgs;
	}	
	
	@RequestMapping(value="/unlock/{mn}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postUnlockSingle(@PathVariable(value="mn") int mn)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
		
		app.unlockMN(mn);		
		msgs.add(new ClientMsg("success", "", ""));
		
		return msgs;
	}
	
	@RequestMapping(value="/l3c5", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postL3C5Flag()
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
				
		app.getAppState().setL3c5Flag(!app.getAppState().isL3c5Flag());
		if(stServ.updateAppState(app.getAppState()))
		{
			msgs.add(new ClientMsg("successful"));
		}
		else
		{
			msgs.add(new ClientMsg("error"));
		}
		
		return msgs;
	}
	
	@RequestMapping(value="/unlock/all", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postUnlockAll()
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
		
		app.unlockAllMNs();		
		msgs.add(new ClientMsg("success", "", ""));
		
		return msgs;
	}	
	
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{
		emailServ.sendErrorEmail(e);
		return "/error/Sever-Exception";
	}
	
	private EditTrans removeApprovalEditTrans(ApprovalModel approval)
	{
		EditTrans edit = new EditTrans();
		edit.setMnId(approval.getMnId());
		edit.setDateEdited(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		edit.setEditedBy(userSession.getUser());
		
		if(approval.getType() == 1) //Confirm
			edit.getEditFields().add(new EditField(mem.getPropId("field_types", "Load Confirmation"), mem.getName("load_confirmation_types", approval.getApprovalType().getId()), " Removed"));
		else
			edit.getEditFields().add(new EditField(mem.getPropId("field_types", "Approval"), mem.getName("approval_types", approval.getApprovalType().getId()), " Removed"));
		
		return edit;
	}
}
