package com.fedex.mn.models.impls;

public class ClientMsg
{
	protected String type;
	protected String title;
	protected String msg;
	protected String tab;
	
	public ClientMsg()
	{}
	
	public ClientMsg(String type)
	{
		this.type = type;
		this.title = "";
		this.msg = "";
	}
	
	public ClientMsg(String type, String title, String msg)
	{
		this.type = type;
		this.title = title;
		this.msg = msg;
	}
	
	public ClientMsg(String type, String tab, String title, String msg)
	{
		this.type = type;
		this.tab = tab;
		this.title = title;
		this.msg = msg;
	}
	
	public String getType()
	{
		return type;
	}
	public void setType(String type)
	{
		this.type = type;
	}
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}
	public String getMsg()
	{
		return msg;
	}
	public void setMsg(String msg)
	{
		this.msg = msg;
	}
	public String getTab()
	{
		return tab;
	}
	public void setTab(String tab)
	{
		this.tab = tab;
	}
}
