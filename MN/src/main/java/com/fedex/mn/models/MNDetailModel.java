package com.fedex.mn.models;

public abstract class MNDetailModel
{
	private int id;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null &&
			obj.getClass().getSuperclass().getSimpleName().equals("MNDetailModel") &&
			((MNDetailModel)obj).getId() == this.id)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return "MNDetailModel".hashCode() + this.id;
	}
}
