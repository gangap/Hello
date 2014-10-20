package com.fedex.mn.models.impls;

public class CalEvent
{
	private String title, description, type;
	private long start, end;
	
	public CalEvent()
	{}
	
	public CalEvent(MNView view)
	{
		this.start = view.getExpLoadDate().getTime();		
		this.end = this.start;
		
		this.title = view.getMnId() + "";
		this.description = view.getComment();
		this.type = view.getDest();
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public long getStart()
	{
		return start;
	}

	public void setStart(long start)
	{
		this.start = start;
	}

	public long getEnd()
	{
		return end;
	}

	public void setEnd(long end)
	{
		this.end = end;
	}
}
