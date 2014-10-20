package com.fedex.mn.models.impls;

import com.fedex.mn.models.MNDetailModel;

public class BuildDetailModel
	extends MNDetailModel
{

	private String trackerNum;
	private DBProperty os;
	private DBProperty buildProject;
	private DBProperty scm;
	private int fml;
	private int specialInstructions;
	private String specialInstructionsText;
	private String sourceCode;
	private String expDeliverables;
	private String actualDeliverables;
	private String filePackaged;
	private String patchLocation;
	
	public BuildDetailModel(BuildDetailModel build)
	{
		this.trackerNum = build.getTrackerNum();
		this.os = new DBProperty(build.getOs());
		this.buildProject = new DBProperty(build.getBuildProject());
		this.scm = new DBProperty(build.getScm());
		this.fml = build.getFml();
		this.specialInstructions = build.getSpecialInstructions();
		this.specialInstructionsText = new String("" + build.getSpecialInstructionsText());
		this.sourceCode = new String("" + build.getSourceCode());
		this.expDeliverables = new String("" + build.getExpDeliverables());
		this.actualDeliverables = new String("" + build.getActualDeliverables());
		this.filePackaged = new String("" + build.getFilePackaged());
		this.patchLocation = new String("" + build.getPatchLocation());
	}
	
	public BuildDetailModel()
	{}

	public String getTrackerNum()
	{
		if(trackerNum == null)
			return "";
		else
			return trackerNum;
	}
	public void setTrackerNum(String trackerNum)
	{
		this.trackerNum = trackerNum;
	}
	public DBProperty getOs()
	{
		return os;
	}
	public void setOs(DBProperty os)
	{
		this.os = os;
	}
	public DBProperty getBuildProject()
	{
		return buildProject;
	}
	public void setBuildProject(DBProperty buildProject)
	{
		this.buildProject = buildProject;
	}
	public DBProperty getScm()
	{
		return scm;
	}
	public void setScm(DBProperty scm)
	{
		this.scm = scm;
	}
	public int getFml()
	{
		return fml;
	}
	public void setFml(int fml)
	{
		this.fml = fml;
	}
	public int getSpecialInstructions()
	{
		return specialInstructions;
	}
	public void setSpecialInstructions(int specialInstructions)
	{
		this.specialInstructions = specialInstructions;
	}
	public String getSpecialInstructionsText()
	{
		return specialInstructionsText;
	}
	public void setSpecialInstructionsText(String specialInstructionsText)
	{
		this.specialInstructionsText = specialInstructionsText;
	}
	public String getSourceCode()
	{
		return sourceCode;
	}
	public void setSourceCode(String sourceCode)
	{
		this.sourceCode = sourceCode;
	}
	public String getExpDeliverables()
	{
		return expDeliverables;
	}
	public void setExpDeliverables(String expDeliverables)
	{
		this.expDeliverables = expDeliverables;
	}
	public String getActualDeliverables()
	{
		return actualDeliverables;
	}
	public void setActualDeliverables(String actualDeliverables)
	{
		this.actualDeliverables = actualDeliverables;
	}
	public String getFilePackaged()
	{
		return filePackaged;
	}
	public void setFilePackaged(String filePackaged)
	{
		this.filePackaged = filePackaged;
	}
	public String getPatchLocation()
	{
		return patchLocation;
	}
	public void setPatchLocation(String patchLocation)
	{
		this.patchLocation = patchLocation;
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
		return "build".hashCode() * super.hashCode();
	}
}
