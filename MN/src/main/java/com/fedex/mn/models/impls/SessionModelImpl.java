package com.fedex.mn.models.impls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fedex.mn.app.Application;
import com.fedex.mn.models.SessionModel;
import com.fedex.mn.utils.Util;

@Component("userSession")
@Scope(value="session")
public class SessionModelImpl
	implements SessionModel, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5231752585279239231L;
	
	private boolean loggedIn = false;
	private String viewParam, contentParam, view;	
	private Map<String, String> filters;
	private List<MNModel> edittingMNs;
	private UserModel user;
	
	@Autowired
	private Application app;
	
	public SessionModelImpl()
	{
		filters = new HashMap<String, String>();
		
		//Set the filters
		filters.put("release", null);
		filters.put("orig", null);
		filters.put("mnid", null);
		filters.put("status", null);
		filters.put("load", null);
		filters.put("change", null);
		filters.put("srs", null);
		filters.put("originator", null);
		filters.put("test level", null);
		filters.put("artifact", null);
		filters.put("dest", null);
		filters.put("bExp", Util.df.format(new Date(Long.MAX_VALUE)));
		filters.put("aExp", Util.df.format(new Date(0)));		
		
		edittingMNs = new ArrayList<MNModel>(0);
	}
	
	@PreDestroy
	public void destroy()
	{
		setLoggedIn(false);
		app.unlockMNByUser(getUser());
		app.removeUser(this.user);
	}
	
	public boolean isLoggedIn() 
	{
		return loggedIn;
	}
	public void setLoggedIn(boolean loggedIn) 
	{
		this.loggedIn = loggedIn;
	}

	public String getContentParam()
	{
		return contentParam;
	}

	public void setContentParam(String contentParam)
	{
		this.contentParam = contentParam;
	}
	
	public String getViewParam()
	{
		return viewParam;
	}
	
	public void setViewParam(String viewParam)
	{
		this.viewParam = viewParam;
	}
	
	public Map<String, String> getFilters()
	{
		if(filters == null)
			filters = new HashMap<String, String>();
		
		return filters;
	}
	
	public void setFilters(Map<String, String> filters)
	{
		this.filters.putAll(filters);
	}		
	
	public void clearFilters()
	{
		Iterator<String> filterKeys = filters.keySet().iterator();
		
		while(filterKeys.hasNext())
		{
			String key = filterKeys.next();
			
			filters.put(key, null);
		}
	}
	
	public UserModel getUser()
	{
		return user;
	}

	@Override
	public void setUser(UserModel user)
	{
		this.user = user;
	}

	@Override
	public String getView()
	{
		return this.view;
	}

	@Override
	public void setView(String view)
	{		
		this.view = view;
	}

	@Override
	public void setFilterCookie(String filters)
	{
		if(filters != null)
		{
			this.filters = Util.parseFilters(filters);
		}
	}
	
	@Override
	public boolean hasFilters()
	{
		Iterator<String> values = filters.values().iterator();
		
		while(values.hasNext())
		{
			String value = values.next();
			
			if(value != null && !value.isEmpty())
				return true;
		}
		
		return false;
	}

	@Override
	public List<MNModel> getEdittingMNs()
	{
		if(edittingMNs == null)
			edittingMNs = new ArrayList<MNModel>(0);
		
		return edittingMNs;
	}
	
	@Override
	public MNModel getEdittingMN(int mn)
	{
		for(MNModel model : edittingMNs)
		{
			if(model.getId() == mn)
				return model;
		}
		
		return null;
	}
	
	@Override
	public void setEdittingMNs(List<MNModel> models)
	{
		this.edittingMNs = models;
	}
	
	@Override
	public void removeEdittingMN(int mn)
	{
		Iterator<MNModel> iter = edittingMNs.iterator();
		
		while(iter.hasNext())
		{
			MNModel model = iter.next();
			
			if(model.getId() == mn)
				iter.remove();
		}
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null &&
			obj.getClass().equals(this.getClass()) &&
			((SessionModelImpl)obj).getUser().equals(this.user))
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return "session".hashCode() * this.user.hashCode();
	}
}
