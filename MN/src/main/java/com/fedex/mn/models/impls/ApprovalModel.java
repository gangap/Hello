package com.fedex.mn.models.impls;

import java.util.Date;

public class ApprovalModel
{
	private int id;
	private int mnId;
	private int type;
	private DBProperty approvalType;
	private UserModel user;
	private Date dateSigned;
	private String comments;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public int getMnId()
	{
		return mnId;
	}
	public void setMnId(int mnId)
	{
		this.mnId = mnId;
	}
	public DBProperty getApprovalType()
	{
		return approvalType;
	}
	public void setApprovalType(DBProperty approvalType)
	{
		this.approvalType = approvalType;
	}
	public UserModel getUser()
	{
		return user;
	}
	public void setUser(UserModel user)
	{
		this.user = user;
	}
	public Date getDateSigned()
	{
		return dateSigned;
	}
	public void setDateSigned(Date dateSigned)
	{
		this.dateSigned = dateSigned;
	}	
	public String getComments()
	{
		return comments;
	}
	public void setComments(String comments)
	{
		this.comments = comments;
	}
	public int getType()
	{
		return this.type;
	}
	public void setType(int type)
	{
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null &&
			obj.getClass().equals(this.getClass()) && 
			((ApprovalModel)obj).hashCode() == this.hashCode())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return "approval".hashCode() * this.id;
	}
}
