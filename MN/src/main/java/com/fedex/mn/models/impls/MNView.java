package com.fedex.mn.models.impls;

import java.util.Date;

public class MNView
	implements Comparable<MNView>
{
	private int mnId;
	private int parentId;
	private boolean parent;
	private String status;
	private String srs;
	private String release;
	private String name;
	private String loadType;
	private String changeType;
	private String project;
	private String comment;
	private String dest;
	private Date created;
	private Date expLoadDate;
	private String artifact;
	private String patchNames;
	private String sourceCode;
	private String actDeliverables;
	private String tracker;
	
	public MNView(){}
	
	public MNView(MNModel mn)
	{
		this.mnId = mn.getId();
		this.parentId = mn.getParentID();
		if(mn.getParentID() != 0)
			this.parent = true;
		else
			this.parent = false;
		this.status = new String(mn.getBasicDetail().getStatus().getName());
		this.srs = new String(mn.getBasicDetail().getSymphonyProfile().getName());
		this.release = new String(mn.getBasicDetail().getRelease().getName());
		this.name = new String(mn.getBasicDetail().getOriginator().getName());
		this.loadType = new String(mn.getBasicDetail().getLoadType().getName());
		this.changeType = new String(mn.getBasicDetail().getChangeType().getName());
		this.project = new String(mn.getBuildDetail().getBuildProject().getName());
		this.comment = new String(mn.getBasicDetail().getComments());
		this.dest = new String(mn.getBasicDetail().getDestination().getName());
		
		if(mn.getDateCreated() != null)
			this.created = (Date) mn.getDateCreated().clone();
		
		if(mn.getBasicDetail().getExpectedLoadDate() != null)
			this.expLoadDate = (Date) mn.getBasicDetail().getExpectedLoadDate().clone();
		
		this.artifact = new String(mn.getBasicDetail().getArtifact());
		this.patchNames = new String(mn.getBuildDetail().getFilePackaged());
		this.sourceCode = new String(mn.getBuildDetail().getSourceCode());
		this.actDeliverables = new String(mn.getBuildDetail().getActualDeliverables());
		this.tracker = mn.getBuildDetail().getTrackerNum();
	}
	
	public MNView(MNView view)
	{
		this.mnId = view.getMnId();
		this.parentId = view.getParentId();
		this.parent = view.isParent();
		this.status = new String(view.getStatus());
		this.srs = new String(view.getSrs());
		this.release = new String(view.getRelease());
		this.name = new String(view.getName());
		this.loadType = new String(view.getLoadType());
		this.changeType = new String(view.getChangeType());
		this.project = new String(view.getProject());
		this.comment = new String(view.getComment());
		this.dest = new String(view.getDest());
		
		if(view.getCreated() != null)
			this.created = (Date) view.getCreated().clone();
		
		if(view.getExpLoadDate() != null)
			this.expLoadDate = (Date) view.getExpLoadDate().clone();
		
		this.artifact = new String(view.getArtifact());
		this.patchNames = new String(view.getPatchNames());
		this.sourceCode = new String(view.getSourceCode());
		this.actDeliverables = new String(view.getActDeliverables());
		this.tracker = view.getTracker();
	}
	
	public int getMnId()
	{
		return mnId;
	}
	public void setMnId(int mnId)
	{
		this.mnId = mnId;
	}
	public String getStatus()
	{
		return status;
	}
	public void setStatus(String status)
	{
		this.status = status;
	}
	public String getSrs()
	{
		return srs;
	}
	public void setSrs(String srs)
	{
		this.srs = srs;
	}
	public String getRelease()
	{
		return release;
	}
	public void setRelease(String release)
	{
		this.release = release;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getProject()
	{
		return project;
	}
	public void setProject(String project)
	{
		this.project = project;
	}
	public String getArtifact()
	{
		return artifact;
	}
	public void setArtifact(String artifact)
	{
		this.artifact = artifact;
	}
	public String getComment()
	{
		return comment;
	}
	public void setComment(String comment)
	{
		this.comment = comment;
	}
	public Date getCreated()
	{
		return created;
	}
	public void setCreated(Date created)
	{
		this.created = created;
	}
	public String getLoadType()
	{
		return loadType;
	}
	public void setLoadType(String loadType)
	{
		this.loadType = loadType;
	}	
	public String getChangeType()
	{
		return changeType;
	}
	public void setChangeType(String changeType)
	{
		this.changeType = changeType;
	}
	public String getPatchNames()
	{
		return patchNames;
	}
	public void setPatchNames(String patchNames)
	{
		this.patchNames = patchNames;
	}
	public String getTracker()
	{
		return tracker;
	}
	public void setTracker(String tracker)
	{
		this.tracker = tracker;
	}

	public String getSourceCode()
	{
		return sourceCode;
	}

	public void setSourceCode(String sourceCode)
	{
		this.sourceCode = sourceCode;
	}

	public String getActDeliverables()
	{
		return actDeliverables;
	}

	public void setActDeliverables(String actDeliverables)
	{
		this.actDeliverables = actDeliverables;
	}	
	
	public Date getExpLoadDate()
	{
		return expLoadDate;
	}
	
	public void setExpLoadDate(Date expLoadDate)
	{
		this.expLoadDate = expLoadDate;
	}
	
	public String getDest()
	{
		return dest;
	}
	
	public void setDest(String dest)
	{
		this.dest = dest;
	}
	
	public int getParentId()
	{
		return parentId;
	}
	
	public void setParentId(int parentId)
	{
		this.parentId = parentId;
	}

	public boolean isParent()
	{
		return parent;
	}

	public void setParent(boolean parent)
	{
		this.parent = parent;
	}

	@Override
	public int compareTo(MNView view)
	{
		if(this.mnId > view.getMnId())
			return 1;
		else if(this.mnId < view.getMnId())
			return -1;
		else
			return 0;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null && 
			obj.getClass().equals(this.getClass()) &&
			((MNView)obj).getMnId() == this.mnId)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return "MNView".hashCode() * this.mnId;
	}
}
