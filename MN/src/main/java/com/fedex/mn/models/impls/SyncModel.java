package com.fedex.mn.models.impls;

import java.util.HashMap;
import java.util.Map;

public class SyncModel
{
	private int id;
	private Map<String, Boolean> values;
	
	public SyncModel()
	{}
	
	public SyncModel(SyncModel bl)
	{
		this.id = bl.getId();		
		this.values = new HashMap<String, Boolean>(bl.getValues());
	}
	
	public int getId()
	{
		return id;
	}
	
	public void setId(int id)
	{
		this.id = id;
	}
	
	public Map<String, Boolean> getValues()
	{
		if(values == null)
			values = new HashMap<String, Boolean>();
		
		return values;
	}
	
	public void setValues(Map<String, Boolean> values)
	{
		this.values = values;
	}
}
