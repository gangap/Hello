package com.fedex.mn.models.impls;

import java.util.Date;

public class AppStateModel
{
	private boolean l3c5Flag;
	private Date lastStarted;
	private int ldapUser;
	private String ldapPass;
	
	public int getLdapUser()
	{
		return ldapUser;
	}
	public void setLdapUser(int ldapUser)
	{
		this.ldapUser = ldapUser;
	}
	public String getLdapPass()
	{
		return ldapPass;
	}
	public void setLdapPass(String ldapPass)
	{
		this.ldapPass = ldapPass;
	}	
	public boolean isL3c5Flag()
	{
		return l3c5Flag;
	}
	public void setL3c5Flag(boolean l3c5Flag)
	{
		this.l3c5Flag = l3c5Flag;
	}
	public Date getLastStarted()
	{
		return lastStarted;
	}
	public void setLastStarted(Date lastStarted)
	{
		this.lastStarted = lastStarted;
	}
}
