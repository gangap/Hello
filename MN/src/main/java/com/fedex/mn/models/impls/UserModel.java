package com.fedex.mn.models.impls;

public class UserModel
{	
	private int id;
	private int fedExId;
	private String firstName;
	private String lastName;	
	private String phoneExt;
	private String pager;
	private String division;
	private String email;
	private boolean loadTeamUser = false;
	private boolean admin = false;
	
	public UserModel(){}
	
	public UserModel(UserModel user)
	{
		this.id = user.getId();
		this.fedExId = user.getFedExId();
		this.firstName = new String("" + user.getFirstName());
		this.lastName = new String("" + user.getLastName());
		this.phoneExt = new String("" + user.getPhoneExt());
		this.pager = new String("" + user.getPager());
		this.division = new String("" + user.getDivision());
		this.email = new String("" + user.getEmail());
		this.loadTeamUser = user.isLoadTeamUser();
		this.admin = user.isAdmin();
	}		
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public int getFedExId()
	{
		return fedExId;
	}
	public void setFedExId(int fedExId)
	{
		this.fedExId = fedExId;
	}
	public String getFirstName()
	{
		return firstName;
	}
	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}
	public String getLastName()
	{
		return lastName;
	}
	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}
	public String getName()
	{
		return firstName + " " + lastName;
	}
	
	//For Mapping purposes 
	public void setName(String name)
	{}
	
	public String getPhoneExt()
	{
		return phoneExt;
	}
	public void setPhoneExt(String phoneExt)
	{
		this.phoneExt = phoneExt;
	}
	public String getPager()
	{
		return pager;
	}
	public void setPager(String pager)
	{
		this.pager = pager;
	}
	public String getDivision()
	{
		return division;
	}
	public void setDivision(String division)
	{
		this.division = division;
	}
	public String getEmail()
	{
		return email;
	}
	public void setEmail(String email)
	{
		this.email = email;
	}
	public boolean isLoadTeamUser()
	{
		return loadTeamUser;
	}
	public void setLoadTeamUser(boolean loadTeamUser)
	{
		this.loadTeamUser = loadTeamUser;
	}
	
	public boolean isAdmin()
	{
		return admin;
	}
	
	public void setAdmin(boolean admin)
	{
		this.admin = admin;
	}
	
	@Override
	public String toString()
	{
		return this.getName() + "[" + this.fedExId + "]";
	}
	
	@Override
	public boolean equals(Object obj)
	{				
		if(obj != null && 
			obj.getClass().equals(this.getClass()) &&
			((UserModel)obj).getFedExId() == this.fedExId)
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return "user".hashCode() * this.fedExId;
	}
}
