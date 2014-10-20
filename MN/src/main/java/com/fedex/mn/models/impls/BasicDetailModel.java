package com.fedex.mn.models.impls;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fedex.mn.models.MNDetailModel;

public class BasicDetailModel
	extends MNDetailModel
{
	private DBProperty status;
	private DBProperty symphonyProfile;
	private DBProperty loadType;
	private UserModel originator;
	private DBProperty changeType;
	
	private String artifact;
	private DBProperty dependency;
	private List<Integer> dependencyIds;
	
	private ArrayList<DBProperty> loadTeam;
	private String summary;
	private String comments;		
	private DBProperty destination;
	private ReleaseModel release;
	
	private Date expectedLoadDate;
	private int elHour = 0, elMin = 0, elSec = 0;
	
	private Date actualLoadDate;
	private int alHour = 0, alMin = 0, alSec = 0;
	
	private SyncModel syncs;
	
	@SuppressWarnings("unchecked")
	public BasicDetailModel(BasicDetailModel basic)
	{				
		this.status = new DBProperty(basic.getStatus());
		this.symphonyProfile = new DBProperty(basic.getSymphonyProfile());
		this.loadType = new DBProperty(basic.getLoadType());
		this.originator = new UserModel(basic.getOriginator());
		this.changeType = new DBProperty(basic.getChangeType());
		this.artifact = new String("" + basic.getArtifact());
		this.dependency = new DBProperty(basic.getDependency());
		this.dependencyIds = new ArrayList<Integer>(basic.getDependencyIds());
		this.loadTeam = (ArrayList<DBProperty>) basic.getLoadTeam().clone();
		this.summary = new String("" + basic.getSummary());
		this.comments = new String("" + basic.getComments());
		this.destination = new DBProperty(basic.getDestination());
		this.release = new ReleaseModel(basic.getRelease());
		
		if(basic.getExpectedLoadDate() != null)
			this.expectedLoadDate = (Date) basic.getExpectedLoadDate().clone();
		if(basic.getActualLoadDate() != null)
			this.actualLoadDate = (Date) basic.getActualLoadDate().clone();	
		
		this.syncs = new SyncModel(basic.getSyncs());
	}
	
	public BasicDetailModel()
	{
		this.dependencyIds = new ArrayList<Integer>(0);
	}

	public DBProperty getStatus()
	{
		return status;
	}
	public void setStatus(DBProperty status)
	{
		this.status = status;
	}
	public DBProperty getSymphonyProfile()
	{
		return symphonyProfile;
	}
	public void setSymphonyProfile(DBProperty symphonyProfile)
	{
		this.symphonyProfile = symphonyProfile;
	}
	public DBProperty getLoadType()
	{
		return loadType;
	}
	public void setLoadType(DBProperty loadType)
	{
		this.loadType = loadType;
	}
	public UserModel getOriginator()
	{
		return originator;
	}
	public void setOriginator(UserModel originator)
	{
		this.originator = originator;
	}
	public DBProperty getChangeType()
	{
		return changeType;
	}
	public void setChangeType(DBProperty changeType)
	{
		this.changeType = changeType;
	}
	public String getArtifact()
	{
		return artifact;
	}
	public void setArtifact(String artifact)
	{
		this.artifact = artifact;
	}
	public DBProperty getDependency()
	{
		return dependency;
	}
	public void setDependency(DBProperty dependency)
	{
		this.dependency = dependency;
	}
	public List<Integer> getDependencyIds()
	{
		return dependencyIds;
	}
	public void setDependencyIds(List<Integer> dependencyIds)
	{
		this.dependencyIds = dependencyIds;
	}
	public ArrayList<DBProperty> getLoadTeam()
	{
		return loadTeam;
	}
	public void setLoadTeam(ArrayList<DBProperty> loadTeam)
	{
		this.loadTeam = loadTeam;
	}
	public String getSummary()
	{
		return summary;
	}
	public void setSummary(String summary)
	{
		this.summary = summary;
	}
	public String getComments()
	{
		return comments;
	}
	public void setComments(String comments)
	{
		this.comments = comments;
	}
	public DBProperty getDestination()
	{
		return destination;
	}
	public void setDestination(DBProperty destination)
	{
		this.destination = destination;
	}
	public ReleaseModel getRelease()
	{
		return release;
	}
	public void setRelease(ReleaseModel release)
	{
		this.release = release;
	}
	public Date getExpectedLoadDate()
	{
		return expectedLoadDate;
	}
	public void setExpectedLoadDate(Date expectedLoadDate)
	{
		this.expectedLoadDate = expectedLoadDate;
	}
	public Date getActualLoadDate()
	{
		return actualLoadDate;
	}
	public void setActualLoadDate(Date actualLoadDate)
	{
		this.actualLoadDate = actualLoadDate;
	}

	public int getElHour()
	{
		return elHour;
	}

	public void setElHour(int elHour)
	{
		this.elHour = elHour;
	}

	public int getElMin()
	{
		return elMin;
	}

	public void setElMin(int elMin)
	{
		this.elMin = elMin;
	}

	public int getElSec()
	{
		return elSec;
	}

	public void setElSec(int elSec)
	{
		this.elSec = elSec;
	}

	public int getAlHour()
	{
		return alHour;
	}

	public void setAlHour(int alHour)
	{
		this.alHour = alHour;
	}

	public int getAlMin()
	{
		return alMin;
	}

	public void setAlMin(int alMin)
	{
		this.alMin = alMin;
	}

	public int getAlSec()
	{
		return alSec;
	}

	public void setAlSec(int alSec)
	{
		this.alSec = alSec;
	}
	
	public SyncModel getSyncs()
	{
		return syncs;
	}
	
	public void setSyncs(SyncModel syncs)
	{
		this.syncs = syncs;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(super.equals(obj) && 
			obj.getClass().equals(this.getClass()))
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		// TODO Auto-generated method stub
		return "basic".hashCode() * super.hashCode();
	}
}
