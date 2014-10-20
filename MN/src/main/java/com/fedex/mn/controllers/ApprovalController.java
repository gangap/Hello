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

import com.fedex.mn.app.cache.MemCache;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.ApprovalModel;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.EditField;
import com.fedex.mn.models.impls.EditTrans;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.models.validations.MNValidation;
import com.fedex.mn.services.ApprovalService;
import com.fedex.mn.services.AttachmentService;
import com.fedex.mn.services.EditService;
import com.fedex.mn.services.MNService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/mn/approvals")
public class ApprovalController
{		
	@Autowired
	private ApprovalService approvalServ;
	
	@Autowired
	private AttachmentService attachServ;
	
	@Autowired
	private MNValidation valid;
	
	@Autowired
	private MNService mnServ;
	
	@Autowired
	private EditService editServ;
	
	@Autowired
	private MemCache mem;
	
	@Autowired
	private SessionModel userSession;
	
	private static Logger log = Logger.getLogger(ApprovalController.class);
	
	@RequestMapping(value="/{type}/{param}/{mn}", method=RequestMethod.GET)
	public String getApprovals(@PathVariable("type") String type, 
		@PathVariable("param") String param, @PathVariable("mn") int mnId, 
		ModelMap model)
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
		
		//Get the list of current approvals
		List<ApprovalModel> approvals = new ArrayList<ApprovalModel>();
		approvals.addAll(approvalServ.getAll(mnId));
		MNModel mn = null;
		
		if(type.equals("edit"))
		{
			model.addAttribute("type", "edit");
			
			mn = userSession.getEdittingMN(mnId);			
		}
		else //if(type.equals("view"))
		{
			model.addAttribute("type", "view");
			
			mn = mnServ.getMN(mnId);
			
			if(mn == null)
			{
				model.addAttribute("type", "error");
				model.addAttribute("title", "Database Error");
				model.addAttribute("msg", "Could not retreive the MN[" + mnId + "] from the database");				
				
				return "redirect:/msg";
			}						
		}
		
		model.addAttribute("mnId", mnId);
		
		if(param.equals("approval"))
		{						
			//All of the needed signatures
			String dest = mem.getName("destination_types", mn.getBasicDetail().getDestination().getId());
			String load = mem.getName("load_types", mn.getBasicDetail().getLoadType().getId());
			String status = mem.getName("status_types", mn.getBasicDetail().getStatus().getId());
			model.addAttribute("needed", valid.getSignatures(dest, load, status, approvals));
			
			model.addAttribute("childMNs", mnServ.getChildMNs(mnId));
			model.addAttribute("attachments", attachServ.getAllAttachments(mnId, "approvals"));
			model.addAttribute("approvals", Util.getApprovalType(0, approvals));
			model.addAttribute("approval", new ApprovalModel());
			model.addAttribute("approvalValues", mem.getProperties("approval_types"));
			return "/subviews/contents/main-contents/forms/approval-form";
		}
		else //if(type.equals("confirm"))
		{			
			model.addAttribute("approvals", Util.getApprovalType(1, approvals));
			model.addAttribute("confirm", new ApprovalModel());
			model.addAttribute("confirmValues", mem.getProperties("load_confirmation_types"));
			
			return "/subviews/contents/main-contents/forms/load-confirm-form";
		}
	}
	
	@RequestMapping(value="/ajax/status/{id}", method=RequestMethod.GET)
	public String getStatusOptions(@PathVariable("id") int id,
		@RequestParam("dest") String dest, @RequestParam("load") String load, ModelMap model)
	{		
		model.addAttribute("statuses", valid.getAvailableStatuses(dest, load, approvalServ.getAll(id)));
		
		return "subviews/contents/main-contents/forms/components/basic-status-options";
	}
	
	//End Gets
	
	//Start Posts		
	@RequestMapping(value="/save/{type}/{mn}/{id}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postAddConfirm(@PathVariable("type") String type, @PathVariable("id") int id, 
		@PathVariable("mn") int mn, @RequestParam("comment") String comment, 
		@RequestParam(value="children[]", required=false) List<Integer> children)
	{		
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}		
		
		ApprovalModel approval = new ApprovalModel();
		approval.setComments(comment);
		approval.setApprovalType(new DBProperty());
		approval.getApprovalType().setId(id);
		approval.setDateSigned(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		approval.setUser(userSession.getUser());
		
		MNModel model = userSession.getEdittingMN(mn);
		if(model == null)
			model = mnServ.getMN(mn);			
		
		if(model == null)
		{
			msgs.add(new ClientMsg("error", "Database Error", "Could not retrieve MN[" + mn + "] from the database"));
		}
				
		if(type.equals("confirm"))
		{
			approval.setType(1);
			approval.setMnId(mn);
			
			String status = mem.getName("status_types", model.getBasicDetail().getStatus().getId());
			String dest = mem.getName("destination_types", model.getBasicDetail().getDestination().getId());
			
			//Only allow Load Confirmations if the status is 'Producation' for MNs with dest's of 'Producation'
			if(dest.equals("Production") && !status.equals("Production"))
			{
				msgs.add(new ClientMsg("error", "Cannot Confirm Load", "Load confirmations cannot be added until the MN is in 'Producation' status"));
				return msgs;
			}
			
			EditTrans edit = createEditTrans(model, type, id);
			
			//Make sure we do not already have this load confirmation
			List<ApprovalModel> approvalModels = approvalServ.getAll(mn);			
			for(ApprovalModel approvalModel : approvalModels)
			{
				if(approvalModel.getType() == 1
					&& approvalModel.getApprovalType().getId() == approval.getApprovalType().getId())
				{
					msgs.add(new ClientMsg("error", "Invalid Confirmation", "There is already an confirmation of this type."));
					return msgs;
				}
			}

			if(status.equals("Production"))
			{				
				edit.getEditFields().addAll(createStatusEditTrans(model, status).getEditFields());											
			}			
			
			if(!approvalServ.add(approval) || !editServ.add(edit))
			{
				msgs.add(new ClientMsg("error", "Database Error", "Could not submit the approval / confirmation"));
			}
			else
			{
				msgs.add(new ClientMsg("success", "Load Confirmation Successful", "Successfully added the Load Confirmation"));
			}
			
			return msgs;
		}
		else
		{
			approval.setType(0);
			
			List<ApprovalModel> approvalModels = approvalServ.getAll(approval.getMnId());
			for(ApprovalModel approvalModel : approvalModels)
			{
				if(approvalModel.getType() == 0 
					&& approvalModel.getApprovalType().getId() == approval.getApprovalType().getId())
				{
					msgs.add(new ClientMsg("error", "Invalid Approval", "There is already an approval of this type."));
				}
			}
			
			//Do the same for the children
			if(children != null)
			{
				for(Integer child : children)
				{
					approval.setMnId(child);
					MNModel childMN = mnServ.getMN(child.intValue());
					
					approvalModels = approvalServ.getAll(child);
					for(ApprovalModel approvalModel : approvalModels)
					{
						if(approvalModel.getType() == 0
							&& approvalModel.getApprovalType().getId() == approval.getApprovalType().getId())
						{
							msgs.add(new ClientMsg("error", "Invalid Approval", "There is already an approval of this type for the MN [" + child + "]."));	
						}					
					}
					
					if(!msgs.isEmpty())
						continue;
					
					EditTrans edit = createEditTrans(model, type, id);
					String status = mem.getName("status_types", childMN.getBasicDetail().getStatus().getId());
					String approvalName = mem.getName("approval_types", approval.getApprovalType().getId());
					
					if(approvalName.equals("PBS") && status.equals("Ready for Production"))
					{				
						edit.getEditFields().addAll(createStatusEditTrans(childMN, status).getEditFields());											
					}	
					
					if(!approvalServ.add(approval) || !editServ.add(edit))
					{
						msgs.add(new ClientMsg("error", "Database Error", "Could not submit the approval to the child MN [" + child + "]"));
						return msgs;	
					}
				}
			}	
			
			approval.setMnId(mn);
			
			EditTrans edit = createEditTrans(model, type, id);
			String status = mem.getName("status_types", model.getBasicDetail().getStatus().getId());
			String approvalName = mem.getName("approval_types", approval.getApprovalType().getId());
			
			if(approvalName.equals("PBS") && status.equals("Ready for Production"))
			{				
				edit.getEditFields().addAll(createStatusEditTrans(model, status).getEditFields());											
			}	
			
			if(!approvalServ.add(approval) || !editServ.add(createEditTrans(model, type, id)))
				msgs.add(new ClientMsg("error", "Database Error", "Could not submit the approval / confirmation"));
			else
				msgs.add(new ClientMsg("success", "Load Approval Successful", "Successfully added the Approval"));
			
			return msgs;
		}
	}
	
	@RequestMapping(value="/update/{id}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postUpdateApprovalComment(@PathVariable("id") int approvalId, 
			@RequestParam("comment") String comment)
	{
		List<ClientMsg> msgs = new ArrayList<ClientMsg>();
		log.debug("Updating the approval[" + approvalId + "]");
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
		
		ApprovalModel approval = approvalServ.get(approvalId);
		if(approval == null)
		{
			msgs.add(new ClientMsg("error", "Database Error", "Failed to get the approval from the database"));
			return msgs;
		}
		
		approval.setComments(comment);
		
		if(!approvalServ.update(approval))
		{
			msgs.add(new ClientMsg("error", "Database Error", "Failed to update the approval"));
			return msgs;
		}
		
		EditTrans edit = createApprovalUpdateEditTrans(approval.getMnId(), approval.getType(), approvalId);
		if(editServ.add(edit))
			msgs.add(new ClientMsg("successful"));
		else
			msgs.add(new ClientMsg("error", "Database Error", "Successfully updated the approval but the edit creation failed"));
		
		return msgs;
	}
	
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{
		return "/error/Sever-Exception";
	}
	
	private EditTrans createEditTrans(MNModel model, String type, int id)
	{
		if(type.equals("confirm"))
			return createConfirmEditTrans(model, type, id);
		else
			return createApprovalEditTrans(model, type, id);
	}
	
	private EditTrans createConfirmEditTrans(MNModel model, String type, int id)
	{
		EditTrans edit = new EditTrans();		
		edit.setMnId(model.getId());
		edit.setDateEdited(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		edit.setEditedBy(userSession.getUser());		
		
		edit.getEditFields().add(new EditField(mem.getPropId("field_types", "Load Confirmation"), mem.getName("load_confirmation_types", id), userSession.getUser().getName()));				
		
		return edit;
	}
	
	private EditTrans createApprovalEditTrans(MNModel model, String type, int id)
	{	
		EditTrans edit = new EditTrans();
		edit.setMnId(model.getId());
		edit.setDateEdited(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		edit.setEditedBy(userSession.getUser());
		
		edit.getEditFields().add(new EditField(mem.getPropId("field_types", "Approval"), mem.getName("approval_types", id), userSession.getUser().getName()));
		
		return edit;
	}	
	
	private EditTrans createApprovalUpdateEditTrans(int mnId, int type, int id)
	{
		EditTrans edit = new EditTrans();
		edit.setMnId(mnId);
		edit.setDateEdited(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		edit.setEditedBy(userSession.getUser());
		
		if(type == 1) //Confirm
			edit.getEditFields().add(new EditField(mem.getPropId("field_types", "Load Confirmation"), mem.getName("load_confirmation_types", id) + " Comment", " Modified"));
		else
			edit.getEditFields().add(new EditField(mem.getPropId("field_types", "Approval"), mem.getName("approval_types", id) + " Comment", " Modified "));
		
		return edit;
	}
	
	private EditTrans createStatusEditTrans(MNModel model, String status)
	{
		EditTrans edit = new EditTrans();
		edit.setMnId(model.getId());
		edit.setDateEdited(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		edit.setEditedBy(userSession.getUser());
		
		MNModel oldModel = userSession.getEdittingMN(model.getId());
		
		String curStatus = mem.getName("status_types", model.getBasicDetail().getStatus().getId());
		
		if(status.equals("Production"))
		{
			model.getBasicDetail().getStatus().setId(mem.getPropId("status_types", "Completed"));
			
			if(oldModel != null)
			{
				oldModel.getBasicDetail().getStatus().setId(mem.getPropId("status_types", "Completed"));
			}
			
			model.setLastModified(Calendar.getInstance().getTime());
			model.setLastModifiedBy(userSession.getUser());
			
			mnServ.updateMN(model, null);
		}
		else if(status.equals("Ready for Production"))
		{
			model.getBasicDetail().getStatus().setId(mem.getPropId("status_types", "Production"));
			
			if(oldModel != null)
			{
				oldModel.getBasicDetail().getStatus().setId(mem.getPropId("status_types", "Production"));
			}
			
			model.setLastModified(Calendar.getInstance().getTime());
			model.setLastModifiedBy(userSession.getUser());
			
			mnServ.updateMN(model, null);
		}
				
		edit.getEditFields().add(new EditField(mem.getPropId("field_types", "Status"), curStatus, status));
		
		return edit;
	}
}

