package com.fedex.mn.models.impls;

import java.util.Date;


public class Attachment
{
	protected int id;
	protected int mnId;
	protected String type;
	protected String displayName;
	protected UserModel uploadUser;
	protected Date uploadDate;	
	protected byte[] uploadFile;
	protected String comment;
	
	//private static Logger log = Logger.getLogger(Attachment.class);
	
	public Attachment(){}
	
	public Attachment(Attachment attachment)
	{
		this.id = attachment.getId();
		this.mnId = attachment.getMnId();
		this.type = new String(attachment.getType());
		this.displayName = new String(attachment.getDisplayName());
		this.uploadUser = new UserModel(attachment.getUploadUser());
		this.uploadDate = (Date) attachment.getUploadDate().clone();
		this.uploadFile = attachment.getUploadFile();
		this.comment = new String(attachment.getComment());
	}
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}	
	public UserModel getUploadUser()
	{
		return uploadUser;
	}
	public void setUploadUser(UserModel uploadUser)
	{
		this.uploadUser = uploadUser;
	}
	public Date getUploadDate()
	{
		return uploadDate;
	}
	public void setUploadDate(Date uploadDate)
	{
		this.uploadDate = uploadDate;
	}
	public byte[] getUploadFile()
	{
		return uploadFile;
	}
	public void setUploadFile(byte[] bs)
	{
		this.uploadFile = bs;
	}
	public String getType()
	{
		return type;
	}
	public void setType(String type)
	{
		this.type = type;
	}
	public String getDisplayName()
	{
		return displayName;
	}
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
	public int getMnId()
	{
		return mnId;
	}
	public void setMnId(int mnId)
	{
		this.mnId = mnId;
	}
	public String getComment()
	{
		return comment;
	}
	public void setComment(String comment)
	{
		this.comment = comment;
	}
}
