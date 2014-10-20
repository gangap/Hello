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
import com.fedex.mn.models.impls.BasicDetailModel;
import com.fedex.mn.models.impls.BuildDetailModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.DeploymentDetailModel;
import com.fedex.mn.models.impls.EditField;
import com.fedex.mn.models.impls.EditTrans;
import com.fedex.mn.models.impls.EmailModel;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.models.impls.MNView;
import com.fedex.mn.models.impls.ReleaseModel;
import com.fedex.mn.models.impls.TestDetailModel;
import com.fedex.mn.models.validations.MNValidation;
import com.fedex.mn.services.ApprovalService;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.services.MNService;
import com.fedex.mn.services.ReleaseService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/mn/edit")
public class EditController
{
	@Autowired
	private ModelBuilder builder;
	
	@Autowired
	private Application app;
	
	@Autowired
	private MNService mnServ;
	
	@Autowired
	private EmailService emailServ;
	
	@Autowired
	private ReleaseService relServ;
	
	@Autowired
	private MNValidation valid;
	
	@Autowired
	private SessionModel userSession;
	
	@Autowired
	private MemCache mem;
	
	@Autowired
	private ApprovalService approvalServ;
	
	private static Logger log = Logger.getLogger(EditController.class);		
	
	/**
	 * GET				for AJAX requests to edit an MN
	 * @param mn		the MN to be editted
	 * @param model		the model to be used to populate the form
	 * @return 			the edit subview of the MN
	 */
	@RequestMapping(method=RequestMethod.GET)
	public String getEdit(@RequestParam("mn") int mn, ModelMap model)
	{
		log.info("/mn/edit - Edit the MN " + mn);		
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}
		
		userSession.setContentParam("Edit MN");
		userSession.setViewParam("");		
		
		//Check to see if the MN is locked...
		//If it is not locked, grant the lock to the user
		if(app.lockMN(mn, userSession.getUser()))
		{
			log.debug("Locked the MN " + mn);
			
			MNModel mnModel = mnServ.getMN(mn);
			
			if(mnModel == null)
			{
				model.addAttribute("msg", new ClientMsg("error", "Database Error", "Could not retrieve the MN " + mn + " from the database!"));
				return "redirect:/msg";
			}
			
			userSession.getEdittingMNs().add(mnModel);
			
			model.addAttribute("mnId", mnModel.getId());	
			BasicDetailModel basic = mnModel.getBasicDetail();
			model.addAttribute("testEnabled", valid.isTestEnabled(basic.getDestination().getId(), basic.getLoadType().getId()));
			model.addAttribute("buildEnabled", valid.isBuildEnabled(basic.getStatus().getId(), basic.getChangeType().getId()));
			
			model.addAttribute("email", new EmailModel());
			
			model.addAttribute("users", mem.getUsers());
			
			return "/subviews/contents/main-contents/edit-mn-content";
		}
		else
		{
			//Add an error
			log.info("Failed to get the lock for the MN");			
			model.addAttribute("type", "error");
			model.addAttribute("title", "MN Locked");
			model.addAttribute("msg", "Failed to get a lock for MN[" + mn + "]");
			
			return "redirect:/msg";
		}		
	}
	
	/**
	 * GET - for AJAX requests to specific MN sub-forms
	 * @param view - The sub-form to display
	 * @param model - The model the controller will populate for the view
	 * @return - If the user is logged in it will send them to the correct view
	 */
	@RequestMapping(value="/{view}/{mn}", method=RequestMethod.GET)
	public String getEditForm(@PathVariable("view") String view, 
		@PathVariable("mn") int mnId, ModelMap model)
	{
		log.info("/mn/edit - Switching Edit MN[" + mnId + "] views ");
		MNModel editMN = userSession.getEdittingMN(mnId);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{
			ClientMsg login = Util.getSessionExpireMsg();
			model.addAttribute("type", login.getType());
			model.addAttribute("title", login.getTitle());
			model.addAttribute("msg", login.getMsg());
			return "redirect:/msg";
		}		
		
		if(editMN == null)
		{
			//In the case of a refresh on the page...
			//See if the MN is unlocked...if give it to the user
			if(app.lockMN(mnId, userSession.getUser()))
			{
				editMN = mnServ.getMN(mnId);
				
				if(editMN == null)
				{
					model.addAttribute("msg", new ClientMsg("error", "Database Error", "Could not retrieve the MN " + mnId + " from the database!"));
					return "redirect:/msg";
				}
				
				userSession.getEdittingMNs().add(editMN);
			}
			else
			{
				model.addAttribute("type", "error");
				model.addAttribute("title", "Lock Removed");
				model.addAttribute("msg", "The MN is no longer locked by the user");
				
				return "redirect:/msg";
			}
		}
		
		if(!app.hasLock(userSession.getUser(), editMN.getId()))
		{
			model.addAttribute("type", "lock");
			model.addAttribute("title", "Lock Removed");
			model.addAttribute("msg", "The MN is no longer locked by the user");
			
			return "redirect:/msg";
		}
		
		model.addAttribute("type", "edit");
		model.addAttribute("mnId", editMN.getId());		
		
		model.addAttribute("parentId", editMN.getParentID());
		
		if(view.equals("basic"))
		{			
			builder.setBasicDisplayModel(model, editMN);
			userSession.setViewParam("basic");
			
			return "/subviews/contents/main-contents/forms/basic-form";
		}
		else if(view.equals("test"))
		{			
			builder.setTestDisplayModel(model, editMN);
			userSession.setViewParam("test");
			
			return "/subviews/contents/main-contents/forms/test-form";
		}
		else if(view.equals("build"))
		{
			builder.setBuildDisplayModel(model, editMN);
			userSession.setViewParam("build");
			
			return "/subviews/contents/main-contents/forms/build-form";
		}
		else //if(view.equals("deploy"))
		{
			builder.setDeployDisplayModel(model, editMN);
			userSession.setViewParam("deploy");
			
			return "/subviews/contents/main-contents/forms/deploy-form";
		}
	}
	
	/**
	 * POST				for requests to unlock an MN that may or may not be locked
	 * @param mnId		The id of the MN to be unlocked
	 * @return 			A {@link List} of {@link ClientMsg} as a JSON-response
	 */
	@RequestMapping(value="/unlock/{mn}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postUnlockMN(@PathVariable("mn") int mnId)
	{
		log.info("Unlocking the MN[" + mnId + "]");
		
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		if(mnId > 0)
			msgs.add(new ClientMsg("successful"));
		else
			msgs.add(new ClientMsg("error"));
		
		//Check login status
		//If the user is not longer logged in then the MN is already unlocked
		if(!userSession.isLoggedIn())
		{			
			
			return msgs;
		}	
		
		userSession.removeEdittingMN(mnId);
		app.unlockMN(mnId);
		
		return msgs;
	}
	
	/**
	 * POST				updating/saving changes made to an MN in edit-mode	
	 * @param mnId 		the id of the MN being editted 
	 * @return			A {@link List} of {@link ClientMsg} as a JSON-response
	 */
	@RequestMapping(value="/save/{mn}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postEditSave(@PathVariable("mn") int mnId)
	{		
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}			
		
		MNModel changedMN = userSession.getEdittingMN(mnId);
		if(changedMN == null || !app.hasLock(userSession.getUser(), changedMN.getId()))
		{
			msgs.add(new ClientMsg("lock", "Lock Removed", "The MN is no longer locked by the user"));
			return msgs;
		}		
		
		MNModel oldMN = mnServ.getMN(changedMN.getId());
		
		//Check to see if the MN is edittable
		if(oldMN.getBasicDetail().getStatus().getName().equals("Production") &&
			oldMN.getBasicDetail().getDestination().getName().equals("Production") &&
			!Util.isPBSMember(mem.getProperties("load_team_users"), userSession.getUser().getFedExId()))
		{
			msgs.add(new ClientMsg("error", "Load Team Lock", "This MN is only edittable by the Load Team (PBS) from this point forward!"));
			return msgs;
		}
		
		msgs.addAll(valid.validateToUpdate(changedMN));
		
		if(msgs.size() > 0)
			return msgs;

		msgs.addAll(valid.validateSignatures(userSession.getEdittingMN(mnId).getBasicDetail().getDestination().getId(), 
			userSession.getEdittingMN(mnId).getBasicDetail().getLoadType().getId(), 
			userSession.getEdittingMN(mnId).getBasicDetail().getStatus().getId(), 
			approvalServ.getAll(changedMN.getId())));		
		
		changedMN.setLastModifiedBy(userSession.getUser());		
		EditTrans edits = createEditTrans(mnServ.getMN(changedMN.getId()), changedMN);
		
		if(!(edits.getEditFields().size() > 0))
		{
			msgs.add(new ClientMsg("error", "No Changes", "No fields were changed"));
			return msgs;
		}
		
		if(!mnServ.updateMN(changedMN, edits))
		{
			msgs.add(new ClientMsg("error", "Database Error", "Failed to update MN due to a database error!"));
		}
		else
		{			
			MNModel newMN = mnServ.getMN(oldMN.getId());
			
			if(newMN == null)
			{
				msgs.add(new ClientMsg("error", "Database Error", "Failed to retrieve the newly updated MN"));
			}
			else
			{
				//Remove the old MN and add the new one to the session edits and cache
				userSession.removeEdittingMN(mnId);
				userSession.getEdittingMNs().add(newMN);
				
				updateCache(newMN);
				msgs.add(new ClientMsg("success", "Edit Successful", "Succesfully updated the MN"));
			}								
		}
		
		return msgs;		
	}
	
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{
		emailServ.sendErrorEmail(e);
		return "/error/Sever-Exception";
	}
	
	public EditTrans createEditTrans(MNModel oldModel, MNModel newModel)
	{
		EditTrans edit = new EditTrans();
		
		edit.setMnId(oldModel.getId());
		edit.setDateEdited(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		edit.setEditedBy(newModel.getLastModifiedBy());
		
		List<EditField> editFields = new ArrayList<EditField>(0);
		
		List<DBProperty> fields = mem.getProperties("field_types");
		DBProperty field;
		
		//Go through the old MN and see what is different in the new MN
		//For each one note the change as a editField 
		//Basic
		BasicDetailModel oldBasic = oldModel.getBasicDetail();
		BasicDetailModel newBasic = newModel.getBasicDetail();
		
		if(oldBasic.getStatus().getId() != newBasic.getStatus().getId())
		{
			field = fields.get(Util.getIndex(fields, "Status"));
			editFields.add(new EditField(field.getId(), oldBasic.getStatus().getName(), mem.getName("status_types", newBasic.getStatus().getId())));
		}
		if(oldBasic.getSymphonyProfile().getId() != newBasic.getSymphonyProfile().getId())
		{
			field = fields.get(Util.getIndex(fields, "Symphony Profile"));
			editFields.add(new EditField(field.getId(), oldBasic.getSymphonyProfile().getName(), mem.getName("symphony_profile_types", newBasic.getSymphonyProfile().getId())));
		}
		if(oldBasic.getLoadType().getId() != newBasic.getLoadType().getId())
		{
			field = fields.get(Util.getIndex(fields, "Load Type"));
			editFields.add(new EditField(field.getId(), oldBasic.getLoadType().getName(), mem.getName("load_types", newBasic.getLoadType().getId())));
		}
		if(oldBasic.getChangeType().getId() != newBasic.getChangeType().getId())
		{
			field = fields.get(Util.getIndex(fields, "Change Type"));
			editFields.add(new EditField(field.getId(), oldBasic.getChangeType().getName(), mem.getName("change_types", newBasic.getChangeType().getId())));
		}
		if(!oldBasic.getArtifact().equals(newBasic.getArtifact()))
		{
			field = fields.get(Util.getIndex(fields, "Reference ID"));
			editFields.add(new EditField(field.getId()));
		}
		if(oldBasic.getDependency().getId() != newBasic.getDependency().getId()
			|| !oldBasic.getDependencyIds().equals(newBasic.getDependencyIds()))
		{
			field = fields.get(Util.getIndex(fields, "Dependency"));
			editFields.add(new EditField(field.getId()));
		}
		if(newBasic.getLoadTeam().size() != oldBasic.getLoadTeam().size())
		{
			field = fields.get(Util.getIndex(fields, "Load Team"));
			editFields.add(new EditField(field.getId()));
		}
		else
		{
			for(int index = 0; index < newBasic.getLoadTeam().size(); index++)
			{
				if(newBasic.getLoadTeam().get(index).getId() != oldBasic.getLoadTeam().get(index).getId())
				{
					field = fields.get(Util.getIndex(fields, "Load Team"));
					editFields.add(new EditField(field.getId()));
				}
			}
		}		
		if(!oldBasic.getSummary().equals(newBasic.getSummary()))
		{
			field = fields.get(Util.getIndex(fields, "Summary"));
			editFields.add(new EditField(field.getId()));
		}
		if(oldBasic.getDestination().getId() != newBasic.getDestination().getId())
		{
			field = fields.get(Util.getIndex(fields, "Destination"));
			editFields.add(new EditField(field.getId(), oldBasic.getDestination().getName(), mem.getName("destination_types", newBasic.getDestination().getId())));
		}
		if(oldBasic.getRelease().getId() != newBasic.getRelease().getId())			
		{
			ReleaseModel newRel = relServ.getRelease(newBasic.getRelease().getId());
			ReleaseModel oldRel = relServ.getRelease(oldBasic.getRelease().getId());
			if(newRel != null && oldRel != null)
			{
				field = fields.get(Util.getIndex(fields, "Release"));
				editFields.add(new EditField(field.getId(), oldRel.getName(), newRel.getName()));
			}
		}
		if(oldBasic.getExpectedLoadDate().compareTo(newBasic.getExpectedLoadDate()) != 0)
		{
			field = fields.get(Util.getIndex(fields, "Expected Load Date"));
			editFields.add(new EditField(field.getId(), oldBasic.getExpectedLoadDate().toString(), newBasic.getExpectedLoadDate().toString()));
		}
		if((oldBasic.getActualLoadDate() != null || newBasic.getActualLoadDate() != null) &&
			((oldBasic.getActualLoadDate() == null && newBasic.getActualLoadDate() != null) ||
			oldBasic.getActualLoadDate().compareTo(newBasic.getActualLoadDate()) != 0))
		{
			field = fields.get(Util.getIndex(fields, "Actual Load Date"));
			if(oldBasic.getActualLoadDate() == null)
				editFields.add(new EditField(field.getId(), "None", newBasic.getActualLoadDate().toString()));
			else if(newBasic.getActualLoadDate() == null)				
				editFields.add(new EditField(field.getId(), oldBasic.getActualLoadDate().toString(), "None"));
			else
				editFields.add(new EditField(field.getId(), oldBasic.getActualLoadDate().toString(), newBasic.getActualLoadDate().toString()));
		}
		for(String name : oldBasic.getSyncs().getValues().keySet())
		{
			if(!oldBasic.getSyncs().getValues().get(name).equals(newBasic.getSyncs().getValues().get(name)))
			{
				field = fields.get(Util.getIndex(fields, "Sync Environments"));
				editFields.add(new EditField(field.getId()));
				break;
			}
		}
		
		//Test
		TestDetailModel oldTest = oldModel.getTestDetail();
		TestDetailModel newTest = newModel.getTestDetail();
		
		if(!oldTest.getExpectedResults().equals(newTest.getExpectedResults()))
		{
			field = fields.get(Util.getIndex(fields, "Expected Results"));
			editFields.add(new EditField(field.getId()));
		}
		if(!oldTest.getSqaComments().equals(newTest.getSqaComments()))
		{
			field = fields.get(Util.getIndex(fields, "SQA Comments"));
			editFields.add(new EditField(field.getId()));
		}
		if((newTest.getSqaUser() != null) &&
			((oldTest.getSqaUser() == null && newTest.getSqaUser().getId() != -1) ||
				oldTest.getSqaUser() != null && oldTest.getSqaUser().getId() != newTest.getSqaUser().getId()))
		{
			field = fields.get(Util.getIndex(fields, "SQA Tester Assigned"));
			editFields.add(new EditField(field.getId()));
		}
		if(!oldTest.getSteps().equals(newTest.getSteps()))
		{
			field = fields.get(Util.getIndex(fields, "Steps"));
			editFields.add(new EditField(field.getId()));
		}		
		if(!oldTest.getTestableJustification().equals(newTest.getTestableJustification()))
		{
			field = fields.get(Util.getIndex(fields, "Not Testable Justification"));
			editFields.add(new EditField(field.getId()));
		}		
		if(oldTest.getTestData() != newTest.getTestData() 
			|| !oldTest.getTestDataText().equals(newTest.getTestDataText()))
		{
			field = fields.get(Util.getIndex(fields, "Test Data"));
			
			String from, to;
			if(oldTest.getTestData() == 0)
				from = "Yes";
			else if(oldTest.getTestData() == 1)
				from = "No";
			else
				from = "Other";
			
			if(newTest.getTestData() == 0)
				to = "Yes";
			else if(newTest.getTestData() == 1)
				to = "No";
			else
				to = "Other";
			
			editFields.add(new EditField(field.getId(), from, to));
		}		
		if((newTest.getTestType() != null) &&
			((oldTest.getTestType() == null && newTest.getTestType().getId() != -1) ||
				oldTest.getTestType() != null && oldTest.getTestType().getId() != newTest.getTestType().getId()))		
		{
			field = fields.get(Util.getIndex(fields, "Test Type"));
			editFields.add(new EditField(field.getId(), oldTest.getTestType().getName(), mem.getName("test_types", newTest.getTestType().getId())));
		}					
		
		//Build
		BuildDetailModel oldBuild = oldModel.getBuildDetail();
		BuildDetailModel newBuild = newModel.getBuildDetail();
		
		if(!oldBuild.getActualDeliverables().equals(newBuild.getActualDeliverables()))
		{
			field = fields.get(Util.getIndex(fields, "Actual Deliverables"));
			editFields.add(new EditField(field.getId()));
		}
		if((newBuild.getBuildProject() != null) &&
			((oldBuild.getBuildProject() == null && newBuild.getBuildProject().getId() != -1) ||
				oldBuild.getBuildProject() != null && oldBuild.getBuildProject().getId() != newBuild.getBuildProject().getId()))
		{
			field = fields.get(Util.getIndex(fields, "Build Project"));
			editFields.add(new EditField(field.getId(), oldBuild.getBuildProject().getName(), mem.getName("projects", newBuild.getBuildProject().getId())));
		}
		if(!oldBuild.getExpDeliverables().equals(newBuild.getExpDeliverables()))
		{
			field = fields.get(Util.getIndex(fields, "Expected Deliverable"));
			editFields.add(new EditField(field.getId()));
		}		
		if(!oldBuild.getFilePackaged().equals(newBuild.getFilePackaged()))
		{
			field = fields.get(Util.getIndex(fields, "Patch Names"));
			editFields.add(new EditField(field.getId()));
		}		
		if(oldBuild.getFml() != newBuild.getFml())
		{
			field = fields.get(Util.getIndex(fields, "New FML"));
			editFields.add(new EditField(field.getId()));
		}		
		if((newBuild.getOs() != null) &&
				((oldBuild.getOs() == null && newBuild.getOs().getId() != -1) ||
					oldBuild.getOs() != null && oldBuild.getOs().getId() != newBuild.getOs().getId()))
		{
			field = fields.get(Util.getIndex(fields, "OS Type"));
			editFields.add(new EditField(field.getId(), oldBuild.getOs().getName(), mem.getName("os_types", newBuild.getOs().getId())));
		}		
		if(!oldBuild.getPatchLocation().equals(newBuild.getPatchLocation()))
		{
			field = fields.get(Util.getIndex(fields, "Patch Location"));
			editFields.add(new EditField(field.getId()));
		}		
		if((newBuild.getScm() != null) &&
				((oldBuild.getScm() == null && newBuild.getScm().getId() != -1) ||
					oldBuild.getScm() != null && oldBuild.getScm().getId() != newBuild.getScm().getId()))
		{
			field = fields.get(Util.getIndex(fields, "SCM Tool"));
			editFields.add(new EditField(field.getId(), oldBuild.getScm().getName(), mem.getName("scm_types", newBuild.getScm().getId())));
		}		
		if(!oldBuild.getSourceCode().equals(newBuild.getSourceCode()))
		{
			field = fields.get(Util.getIndex(fields, "Source Code"));
			editFields.add(new EditField(field.getId()));
		}		
		if(oldBuild.getSpecialInstructions() != newBuild.getSpecialInstructions()
			|| !oldBuild.getSpecialInstructionsText().equals(newBuild.getSpecialInstructionsText()))
		{
			field = fields.get(Util.getIndex(fields, "Special Instructions"));
			editFields.add(new EditField(field.getId()));
		}				
		if(!oldBuild.getTrackerNum().equals(newBuild.getTrackerNum()))
		{
			field = fields.get(Util.getIndex(fields, "Build Tracker"));
			editFields.add(new EditField(field.getId(), oldBuild.getTrackerNum(), newBuild.getTrackerNum()));
		}		
		
		//Deploy
		DeploymentDetailModel oldDeploy = oldModel.getDeploymentDetail();
		DeploymentDetailModel newDeploy = newModel.getDeploymentDetail();
		
		if(!oldDeploy.getAlpsConfig().equals(newDeploy.getAlpsConfig()))
		{
			field = fields.get(Util.getIndex(fields, "Alps Configuration"));
			editFields.add(new EditField(field.getId()));			
		}
		if(!oldDeploy.getWlConfig().equals(newDeploy.getWlConfig()))
		{
			field = fields.get(Util.getIndex(fields, "WebLogic Configuration"));
			editFields.add(new EditField(field.getId()));			
		}
		if(!oldDeploy.getPrePatchText().equals(newDeploy.getPrePatchText()))
		{
			field = fields.get(Util.getIndex(fields, "Pre Deployment Instructions"));
			editFields.add(new EditField(field.getId()));
		}
		if(!oldDeploy.getPostPatchText().equals(newDeploy.getPostPatchText()))
		{
			field = fields.get(Util.getIndex(fields, "Post Deployment Instructions"));
			editFields.add(new EditField(field.getId()));
		}		
		
		edit.getEditFields().addAll(editFields);		
		
		return edit;		
	}
	
	private void updateCache(MNModel mn)
	{
		MNView mnView = null;
		
		for(MNView view : mem.getMnViews())
		{
			if(view.getMnId() == mn.getId())
				mnView = view;
		}
				
		if(mnView == null)
			return;
		
		mnView.setActDeliverables(mn.getBuildDetail().getActualDeliverables());
		mnView.setArtifact(mn.getBasicDetail().getArtifact());
		mnView.setChangeType(Util.getName(mem.getProperties("change_types"), mn.getBasicDetail().getChangeType().getId()));
		mnView.setComment(mn.getBasicDetail().getComments());
		mnView.setDest(Util.getName(mem.getProperties("destination_types"), mn.getBasicDetail().getDestination().getId()));
		mnView.setExpLoadDate(mn.getBasicDetail().getExpectedLoadDate());
		mnView.setLoadType(Util.getName(mem.getProperties("load_types"), mn.getBasicDetail().getLoadType().getId()));
		mnView.setProject(mn.getBuildDetail().getBuildProject().getName());		
		mnView.setSourceCode(mn.getBuildDetail().getSourceCode());
		mnView.setSrs(Util.getName(mem.getProperties("symphony_profile_types"), mn.getBasicDetail().getSymphonyProfile().getId()));
		mnView.setStatus(Util.getName(mem.getProperties("status_types"), mn.getBasicDetail().getStatus().getId()));
		mnView.setTracker(mn.getBuildDetail().getTrackerNum());		
		
		List<ReleaseModel> releases = relServ.getAllReleases();
		for(ReleaseModel rel : releases)
		{
			if(rel.getId() == mn.getBasicDetail().getRelease().getId())
				mnView.setRelease(rel.getName());
		}
	}
}
