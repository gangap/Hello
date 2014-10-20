package com.fedex.mn.controllers;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fedex.mn.exceptions.DownloadException;
import com.fedex.mn.exceptions.UploadException;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.models.impls.AbinitioAttachment;
import com.fedex.mn.models.impls.Attachment;
import com.fedex.mn.models.impls.ClientMsg;
import com.fedex.mn.services.AttachmentService;
import com.fedex.mn.services.EmailService;
import com.fedex.mn.utils.Util;

@Controller
@RequestMapping("/mn/attach")
public class AttachmentController
{	
	@Autowired
	private AttachmentService attachServ;
	
	@Autowired
	private EmailService emailServ;
	
	@Autowired
	private SessionModel userSession;
	
	@Value("${app.env}")
	private String appEnv;
	
	private Logger log = Logger.getLogger(AttachmentController.class);
	
	@RequestMapping(value="/{type}/{mn}", method=RequestMethod.POST)
	public void postAddAttach(@PathVariable("type") String type, @PathVariable("mn") int mnId,
		MultipartHttpServletRequest request, HttpServletResponse response, @RequestParam("comment") String comment)
	{		
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		response.setContentType("text/html");
		
		try
		{		
			//Check login status
			if(!userSession.isLoggedIn())
			{			
				msgs.add(Util.getSessionExpireMsg());
				response.getWriter().print("{error: 'login'}");
			}	
			
			Iterator<String> fileNames = request.getFileNames();
			MultipartFile file = null;
			
			if(fileNames.hasNext())
			{
				String fileName = fileNames.next();
				
				file = request.getFile(fileName);
			}		
			
			if(file == null)
			{
				msgs.add(new ClientMsg("error", "Attachment Failed", "Failed to upload the attachment"));
				
				emailServ.sendErrorEmail(new UploadException("Uploading the attachment to the server failed for the /mn/attach/{type}/{mn} (POST) action"));
				response.getWriter().print("{error: 'Upload Error'}");
				return;
			}	
			
			Attachment attachment = new Attachment();
			
			attachment.setDisplayName(file.getOriginalFilename());
			attachment.setMnId(mnId);
			attachment.setType(type);
			attachment.setUploadDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
			attachment.setUploadFile(file.getBytes());
			attachment.setComment(comment);
			attachment.setUploadUser(userSession.getUser());
			
			if(!attachServ.addAttachment(attachment))
			{
				msgs.add(new ClientMsg("error", "Attachment Failed", "Failed to upload the attachment"));
				response.getWriter().print("{error: 'Database Error'}");
			}
						
			log.info("The user " + userSession.getUser().getName() + " has uploaded a file for the MN[" + mnId + "] of type[" + type + "]");
			
			msgs.add(new ClientMsg("success", "Upload Successful", "The attachment was successfully uploaded"));
			response.getWriter().print("{success: true}");
		}
		catch(IOException e)
		{log.error("Failed to write the response", e);}
	}
	
	@RequestMapping(value="/download/{id}", method=RequestMethod.GET)
	public @ResponseBody byte[] getAttachment(@PathVariable("id") int id, HttpServletResponse response)
	{				
		try
		{
			Attachment attachment = attachServ.get(id);
			
			if(attachment == null)
			{
				response.getWriter().println("The file[" + id + "] requested is no longer available");
				return new byte[0];
			}
			
			if(attachment.getUploadFile() != null)
			{
								
				//Figure out the response's content-type
				response.setHeader("Content-Disposition", "attachment;filename=" + attachment.getDisplayName());				
				response.setContentType("application/force-download");
				response.setContentLength(attachment.getUploadFile().length);		
				
				return attachment.getUploadFile();
			}
		}
		catch(IOException e)
		{
			log.error("Error transmitting the file to the client", e);
			emailServ.sendErrorEmail(new DownloadException("Failed to download the file [" + id + "] for the /mn/attach/download/{id} (GET) action"));
		}
		
		return new byte[0];
	}
	
	@RequestMapping(value="/abinitio/{id}", method=RequestMethod.GET)
	public @ResponseBody byte[] getAbinitioAttachment(@PathVariable("id") int id, HttpServletResponse response)
	{				
		try
		{
			AbinitioAttachment attachment = attachServ.getAbinitioAttachment(id);
			
			if(attachment == null)
			{
				response.getWriter().println("The file[" + id + "] requested is no longer available");
				return new byte[0];
			}
			
			if(attachment.getFile() != null)
			{
								
				//Figure out the response's content-type
				response.setHeader("Content-Disposition", "attachment;filename=AbinitioManifest" + id + ".txt");				
				response.setContentType("application/force-download");
				response.setContentLength(attachment.getFile().length);		
				
				return attachment.getFile();
			}
		}
		catch(IOException e)
		{
			log.error("Error transmitting the file to the client", e);
			emailServ.sendErrorEmail(new DownloadException("Failed to download the file [" + id + "] for the /mn/attach/abinitio/" + id + " (GET) action"));
		}
		
		return new byte[0];
	}	
	
	@RequestMapping(value="/delete/{id}", method=RequestMethod.POST)
	public @ResponseBody List<ClientMsg> postDelete(@PathVariable("id") int id)
	{		
		List<ClientMsg> msgs = new ArrayList<ClientMsg>(0);
		
		//Check login status
		if(!userSession.isLoggedIn())
		{			
			msgs.add(Util.getSessionExpireMsg());
			return msgs;
		}	
		
		if(attachServ.deleteAttachment(id))
		{
			msgs.add(new ClientMsg("success", "", ""));
		}
		else
		{
			msgs.add(new ClientMsg("error", "Database Error", "Failed to delete the file"));
		}
		
		return msgs;
	}
		
	@ExceptionHandler(Exception.class)
	public String handleException(Exception e)
	{		
		emailServ.sendErrorEmail(e);
		return "/error/Sever-Exception";
	}
}
