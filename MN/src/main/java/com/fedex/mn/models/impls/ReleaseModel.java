package com.fedex.mn.models.impls;

import java.util.ArrayList;
import java.util.List;


public class ReleaseModel
	extends DBProperty
{
	private List<MNModel> mns;
	private boolean current;		
	
	public ReleaseModel()
	{
		mns = new ArrayList<MNModel>(0);
	}
	
	public ReleaseModel(ReleaseModel release)
	{
		super(release);
		
		this.current = release.isCurrent();
		
		this.mns = new ArrayList<MNModel>();
		for(MNModel mn : release.getMns())
		{
			MNModel temp = new MNModel(mn);
			mns.add(temp);
		}
	}
	
	public List<MNModel> getMns()
	{
		return mns;
	}
	public void setMns(List<MNModel> mns)
	{
		this.mns = mns;
	}

	public boolean isCurrent()
	{
		return current;
	}

	public void setCurrent(boolean current)
	{
		this.current = current;
	}	
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null &&
			obj.getClass().equals(this.getClass()) &&
			((ReleaseModel)obj).getId() == this.id)
			return true;
		else
			return false;		
	}
	
	@Override
	public int hashCode()
	{
		return "release".hashCode() * this.id;
	}
}
