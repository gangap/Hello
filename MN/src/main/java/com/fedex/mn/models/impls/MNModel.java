package com.fedex.mn.models.impls;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MNModel
{
	private int id;
	private int parentID;
	private Date lastModified;
	private Date dateCreated;
	
	private BasicDetailModel basicDetail;
	private BuildDetailModel buildDetail;
	private DeploymentDetailModel deploymentDetail;
	private TestDetailModel testDetail;
	private UserModel lastModifiedBy;
	
	//private static Logger log = Logger.getLogger(MNModel.class);
	
	private List<Attachment> attachments;
	
	public MNModel()
	{
		attachments = new ArrayList<Attachment>(0);
	}
	
	public MNModel(MNModel mn)
	{		
		this.id = mn.getId();
		this.parentID = mn.getParentID();				
		
		this.basicDetail = new BasicDetailModel(mn.getBasicDetail());
		this.testDetail = new TestDetailModel(mn.getTestDetail());
		this.buildDetail = new BuildDetailModel(mn.getBuildDetail());
		this.deploymentDetail = new DeploymentDetailModel(mn.getDeploymentDetail());	
		
		if(mn.getLastModifiedBy() != null)
			this.lastModifiedBy = new UserModel(mn.getLastModifiedBy());
		
		if(mn.getLastModified() != null)
			this.lastModified = (Timestamp) mn.getLastModified().clone();
		
		if(mn.getDateCreated() != null)
			this.dateCreated = (Timestamp) mn.getDateCreated().clone();			
		
		this.attachments = new ArrayList<Attachment>(0);			
		for(Attachment attach: mn.getAttachments())
		{
			Attachment temp = new Attachment(attach);
			this.attachments.add(temp);
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

	public int getParentID()
	{
		return parentID;
	}

	public void setParentID(int parentID)
	{
		this.parentID = parentID;
	}

	public Date getDateCreated()
	{
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}

	public Date getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(Date lastModified)
	{
		this.lastModified = lastModified;
	}

	public BasicDetailModel getBasicDetail()
	{
		return basicDetail;
	}

	public void setBasicDetail(BasicDetailModel basicDetail)
	{
		this.basicDetail = basicDetail;
	}

	public BuildDetailModel getBuildDetail()
	{
		return buildDetail;
	}

	public void setBuildDetail(BuildDetailModel buildDetail)
	{
		this.buildDetail = buildDetail;
	}

	public DeploymentDetailModel getDeploymentDetail()
	{
		return deploymentDetail;
	}

	public void setDeploymentDetail(DeploymentDetailModel deploymentDetail)
	{
		this.deploymentDetail = deploymentDetail;
	}

	public TestDetailModel getTestDetail()
	{
		return testDetail;
	}

	public void setTestDetail(TestDetailModel testDetail)
	{
		this.testDetail = testDetail;
	}

	public UserModel getLastModifiedBy()
	{
		return lastModifiedBy;
	}

	public void setLastModifiedBy(UserModel lastModifiedBy)
	{
		this.lastModifiedBy = lastModifiedBy;
	}

	public List<Attachment> getAttachments()
	{
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments)
	{
		this.attachments = attachments;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null &&
			obj.getClass().equals(this.getClass()) &&
			((MNModel)obj).getId() == this.id)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return "MNModel".hashCode() * id;
	}
}
