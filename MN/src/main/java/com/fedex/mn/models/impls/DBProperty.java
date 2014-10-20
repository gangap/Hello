package com.fedex.mn.models.impls;

import java.util.Date;

public class DBProperty
{
	protected int id;
	protected String name;
	protected Date dateCreated;
	
	public DBProperty(){}
	
	public DBProperty(DBProperty p)
	{
		if(p == null)
		{
			this.id = -1;
			this.name = "";
		}
		else
		{
			this.id = p.getId();
			this.name = new String("" + p.getName());
			
			if(p.getDateCreated() != null)
				this.dateCreated = (Date)p.getDateCreated().clone();
		}
	}
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public Date getDateCreated()
	{
		return dateCreated;
	}
	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}		
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null &&
			obj.getClass().equals(this.getClass()) && 
			((DBProperty)obj).hashCode() == this.hashCode())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return "DBProperty".hashCode() * id * name.hashCode();
	}
}
