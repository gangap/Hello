package com.fedex.mn.models.impls;

import com.fedex.mn.models.MNDetailModel;

public class DeploymentDetailModel
	extends MNDetailModel
{
	private String prePatchText, postPatchText, deploymentText, alpsConfig, wlConfig;
	
	public DeploymentDetailModel(DeploymentDetailModel deploy)
	{
		this.alpsConfig = new String("" + deploy.getAlpsConfig());
		this.wlConfig = new String("" + deploy.getWlConfig());
		this.prePatchText = new String("" + deploy.getPrePatchText());
		this.postPatchText = new String("" + deploy.getPostPatchText());
		this.deploymentText = new String("" + deploy.getDeploymentText());
	}
	
	public DeploymentDetailModel()
	{}

	public String getPrePatchText()
	{
		return prePatchText;
	}
	public void setPrePatchText(String prePatchText)
	{
		this.prePatchText = prePatchText;
	}
	public String getPostPatchText()
	{
		return postPatchText;
	}
	public void setPostPatchText(String postPatchText)
	{
		this.postPatchText = postPatchText;
	}
	public String getDeploymentText()
	{
		return deploymentText;
	}
	public void setDeploymentText(String deploymentText)
	{
		this.deploymentText = deploymentText;
	}	
	public String getAlpsConfig()
	{
		return alpsConfig;
	}
	public String getWlConfig()
	{
		return wlConfig;
	}
	public void setAlpsConfig(String alpsConfig)
	{
		this.alpsConfig = alpsConfig;
	}
	public void setWlConfig(String wlConfig)
	{
		this.wlConfig = wlConfig;
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
		return "deploy".hashCode() * super.hashCode();
	}	
}
