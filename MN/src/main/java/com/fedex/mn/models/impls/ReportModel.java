package com.fedex.mn.models.impls;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportModel
{
	private int id;
	private String name;
	private Date created;
	private Date lastModified;
	private UserModel userCreated;
	private List<QueryFieldModel> queryFields;
	
	public ReportModel()
	{
		queryFields = new ArrayList<QueryFieldModel>(0);
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
	public Date getCreated()
	{
		return created;
	}
	public void setCreated(Date created)
	{
		this.created = created;
	}
	public Date getLastModified()
	{
		return lastModified;
	}
	public void setLastModified(Date lastModified)
	{
		this.lastModified = lastModified;
	}
	public UserModel getUserCreated()
	{
		return userCreated;
	}
	public void setUserCreated(UserModel userCreated)
	{
		this.userCreated = userCreated;
	}	
	public List<QueryFieldModel> getQueryFields()
	{
		return queryFields;
	}
	public List<QueryFieldModel> getSelects()
	{
		List<QueryFieldModel> selects = new ArrayList<QueryFieldModel>();
		for(QueryFieldModel field : queryFields)
		{
			if(field.isSelectField())
				selects.add(field);
		}
		
		return selects;
	}
	public List<QueryFieldModel> getWheres()
	{
		List<QueryFieldModel> wheres = new ArrayList<QueryFieldModel>();
		for(QueryFieldModel field : queryFields)
		{
			if(!field.isSelectField())
				wheres.add(field);
		}
		
		return wheres;
	}
}
