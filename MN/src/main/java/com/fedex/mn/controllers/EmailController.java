package com.fedex.mn.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fedex.mn.app.Application;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.EmailModel;
import com.fedex.mn.models.impls.QueryResultsModel;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/email")
public class EmailController
{
	@Autowired
	private EmailService emailServ;
	
	@Autowired
	private Application app;
	
	@Autowired
	private SessionModel userSession;
	
	@RequestMapping(value="/mn", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postMNEmail(@ModelAttribute("email") EmailModel emailModel,
		@RequestParam("mns") List<Integer> mns)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		if(emailServ.sendMNEmail(emailModel, mns, userSession.getUser()))
			msgs.add(new ClientMsg("success", "", ""));
		else
			msgs.add(new ClientMsg("error", "Failed to Send the Email", "The e-mail request failed"));					
		
		return msgs;
	}
	
	@RequestMapping(value="/report/{id}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postReportEmail(@ModelAttribute("email") EmailModel emailModel,
		@PathVariable("id") int id)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
		
		QueryResultsModel results = app.getResultList().get(new Integer(id));
				
		if(results != null)
		{
			if(emailServ.sendReportEmail(emailModel, app.getResultList().get(new Integer(id)), userSession.getUser()))
				msgs.add(new ClientMsg("success", "", ""));
			else
				msgs.add(new ClientMsg("error", "Failed to Send the Email", "The e-mail request failed"));
		}
		else
		{
			msgs.add(new ClientMsg("error", "System Error", "Could not retrieve the report requested for email!"));
		}
		
		return msgs;
	}
}
