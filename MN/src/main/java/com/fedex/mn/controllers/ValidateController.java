package com.fedex.mn.controllers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fedex.mn.app.Application;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.SyncModel;
import com.fedex.mn.models.impls.BasicDetailModel;
import com.fedex.mn.models.impls.BuildDetailModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.DeploymentDetailModel;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.models.impls.TestDetailModel;
import com.fedex.mn.models.validations.MNValidation;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/mn/validate")
public class ValidateController
{	
	@Autowired
	private Application app;
	
	@Autowired
	private EmailService emailServ;
	
	@Autowired
	private SessionModel userSession;
	
	@Autowired
	private MNValidation valid;
	
	@RequestMapping(value="/basic/{mn}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postEditValidateBasic(@ModelAttribute("basic") BasicDetailModel basic, 
		BindingResult result, ModelMap model, @RequestParam(value="loadTeam", required=false) List<Integer> loadTeams,
		@PathVariable("mn") int mnId, HttpServletRequest request)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		MNModel editMN = userSession.getEdittingMN(mnId);

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
		
		if(editMN == null)
		{
			msgs.add(new ClientMsg("error", "MN Does Not Exist", "The specified MN[" + mnId + "] does not exist"));
			return msgs;
		}
		
		//Ensure that the MN is still locked
		if(!app.hasLock(userSession.getUser(), editMN.getId()))
		{
			msgs.add(new ClientMsg("error", "Not Locked By User", "This MN is no longer locked by the user"));		
			return msgs;
		}
		
		ArrayList<DBProperty> loadList = new ArrayList<DBProperty>();
		if(loadTeams != null && !loadTeams.isEmpty())
		{
			for(Integer id : loadTeams)
			{
				DBProperty load = new DBProperty();
				load.setId(id);
				loadList.add(load);
			}
		}
		
		basic.setLoadTeam(loadList);
		msgs.addAll(valid.validateBasic(basic));
		
		if(msgs.size() > 0)
			return msgs;
		
		//Set the expected load times...
		Calendar exp = new GregorianCalendar();
		exp.setTime(basic.getExpectedLoadDate());
				
		exp.set(Calendar.HOUR_OF_DAY, basic.getElHour());
		exp.set(Calendar.MINUTE, basic.getElMin());
		exp.set(Calendar.SECOND, basic.getElSec());
		
		basic.setExpectedLoadDate(exp.getTime());
		
		//Set the actual load times...
		if(basic.getActualLoadDate() != null)
		{
			exp.setTime(basic.getActualLoadDate());
					
			exp.set(Calendar.HOUR_OF_DAY, basic.getAlHour());
			exp.set(Calendar.MINUTE, basic.getAlMin());
			exp.set(Calendar.SECOND, basic.getAlSec());
			
			basic.setActualLoadDate(exp.getTime());
		}
		
		basic.setId(editMN.getBasicDetail().getId());
		basic.setOriginator(editMN.getBasicDetail().getOriginator());
		
		SyncModel sync = new SyncModel();
		sync.getValues().put("all", ServletRequestUtils.getBooleanParameter(request, "all-cbox", false));
		sync.getValues().put("none", ServletRequestUtils.getBooleanParameter(request, "none-cbox", false));
		sync.getValues().put("2", ServletRequestUtils.getBooleanParameter(request, "2-cbox", false));
		sync.getValues().put("3", ServletRequestUtils.getBooleanParameter(request, "3-cbox", false));
		sync.getValues().put("4", ServletRequestUtils.getBooleanParameter(request, "4-cbox", false));
		sync.getValues().put("5", ServletRequestUtils.getBooleanParameter(request, "5-cbox", false));
		sync.getValues().put("6", ServletRequestUtils.getBooleanParameter(request, "6-cbox", false));
		
		basic.setSyncs(sync);
		
		editMN.setBasicDetail(basic);				
		msgs.add(new ClientMsg("success", "basic", "Successfully validated the basic details"));
		
		return msgs;
	}
	

	
	@RequestMapping(value="/test/{mn}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postEditValidateTest(@ModelAttribute("test") TestDetailModel test, 
		BindingResult result, ModelMap model, @PathVariable("mn") int mnId)
	{		
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		MNModel editMN = userSession.getEdittingMN(mnId);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}		
		
		
		if(editMN == null)
		{
			msgs.add(new ClientMsg("error", "MN Does Not Exist", "The specified MN[" + mnId + "] does not exist"));
			return msgs;
		}
		
		//Ensure that the MN is still locked
		if(!app.hasLock(userSession.getUser(), editMN.getId()))
		{
			msgs.add(new ClientMsg("error", "Not Locked By User", "This MN is no longer locked by the user"));		
			return msgs;
		}
		
		msgs.addAll(valid.validateTest(test, editMN.getBasicDetail()));
		
		if(result.hasErrors())
		{					
			for(FieldError err : result.getFieldErrors())
			{
				msgs.add(new ClientMsg("validation", "test", err.getField(), err.getDefaultMessage()));
			}		
		}
		
		if(msgs.size() > 0)
			return msgs;
		
		test.setId(editMN.getTestDetail().getId());
		
		editMN.setTestDetail(test);
		
		msgs.add(new ClientMsg("success", "test", "Successfully updated the test details"));
		return msgs;
	}
	
	@RequestMapping(value="/build/{mn}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postEditValidateBuild(@ModelAttribute("build") BuildDetailModel build, 
		BindingResult result, ModelMap model, @PathVariable("mn") int mnId)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		MNModel editMN = userSession.getEdittingMN(mnId);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}
			
		if(editMN == null)
		{
			msgs.add(new ClientMsg("error", "MN Does Not Exist", "The specified MN[" + mnId + "] does not exist"));
			return msgs;
		}
		
		//Ensure that the MN is still locked
		if(editMN == null || !app.hasLock(userSession.getUser(), editMN.getId()))
		{
			msgs.add(new ClientMsg("error", "Not Locked By User", "This MN is no longer locked by the user"));		
			return msgs;
		}
		
		msgs.addAll(valid.validateBuild(build, editMN.getBasicDetail()));
		
		if(result.hasErrors())
		{					
			for(FieldError err : result.getFieldErrors())
			{
				msgs.add(new ClientMsg("validation", "build", err.getField(), err.getDefaultMessage()));
			}		
		}
		
		if(msgs.size() > 0)
			return msgs;
		
		build.setId(editMN.getBuildDetail().getId());
		
		editMN.setBuildDetail(build);
		
		msgs.add(new ClientMsg("success", "build", "Successfully updated the build details"));
		return msgs;
	}
	
	@RequestMapping(value="/deploy/{mn}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postEditValidateDeploy(@ModelAttribute("deploy") DeploymentDetailModel deploy, 
		BindingResult result, ModelMap model, @PathVariable("mn") int mnId)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		MNModel editMN = userSession.getEdittingMN(mnId);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
						
		if(editMN == null)
		{
			msgs.add(new ClientMsg("error", "MN Does Not Exist", "The specified MN[" + mnId + "] does not exist"));
			return msgs;
		}
				
		//Ensure that the MN is still locked
		if(!app.hasLock(userSession.getUser(), editMN.getId()))
		{
			msgs.add(new ClientMsg("error", "Not Locked By User", "This MN is no longer locked by the user"));		
			return msgs;
		}
		
		msgs.addAll(valid.validateDeploy(deploy));
		
		if(result.hasErrors())
		{					
			for(FieldError err : result.getFieldErrors())
			{
				msgs.add(new ClientMsg("validation", "deploy", err.getField(), err.getDefaultMessage()));
			}		
		}
		
		if(msgs.size() > 0)
			return msgs;
		
		deploy.setId(editMN.getDeploymentDetail().getId());
		
		editMN.setDeploymentDetail(deploy);
		
		msgs.add(new ClientMsg("success", "deploy", "Successfully updated the deploy details"));
		return msgs;
	}
	
	@RequestMapping(value="/check/build/{mn}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postCheckBuild(@PathVariable("mn") int mnId)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		MNModel editMN = userSession.getEdittingMN(mnId);
		
		if(editMN == null)
		{
			msgs.add(new ClientMsg("", "", "false"));
			return msgs;
		}
		
		msgs = valid.validateBuild(editMN.getBuildDetail(), editMN.getBasicDetail());
		
		if(msgs.isEmpty())
		{
			msgs.clear();
			msgs.add(new ClientMsg("", "", "true"));
		}
		else
		{
			msgs.clear();
			msgs.add(new ClientMsg("", "", "false"));
		}
		
		return msgs;
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
