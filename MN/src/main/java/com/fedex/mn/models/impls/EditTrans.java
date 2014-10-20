package com.fedex.mn.models.impls;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditTrans
{
	private int id;
	private int mnId;
	private UserModel editedBy;
	private Date dateEdited;
	
	private List<EditField> editFields;
	
	public EditTrans()
	{
		editFields = new ArrayList<EditField>();
	}

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

	public UserModel getEditedBy()
	{
		return editedBy;
	}

	public void setEditedBy(UserModel editedBy)
	{
		this.editedBy = editedBy;
	}

	public Date getDateEdited()
	{
		return dateEdited;
	}

	public void setDateEdited(Date dateEdited)
	{
		this.dateEdited = dateEdited;
	}

	public List<EditField> getEditFields()
	{
		return editFields;
	}		
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null &&
			obj.getClass().equals(this.getClass()) && 
			((EditTrans)obj).hashCode() == this.hashCode())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return "edit".hashCode() * this.id;
	}
}
