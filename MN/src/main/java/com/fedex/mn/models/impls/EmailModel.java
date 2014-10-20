package com.fedex.mn.models.impls;

public class EmailModel
{
	private String subject;
	private String to;
	private String cc;
	private String bcc;
	private String comment;
	
	public String getSubject()
	{
		return subject;
	}
	
	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	public String getTo()
	{
		return to;
	}

	public void setTo(String to)
	{
		this.to = to;
	}

	public String getCc()
	{
		return cc;
	}

	public void setCc(String cc)
	{
		this.cc = cc;
	}

	public String getBcc()
	{
		return bcc;
	}

	public void setBcc(String bcc)
	{
		this.bcc = bcc;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}		
}
