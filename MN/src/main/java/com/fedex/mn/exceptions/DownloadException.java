package com.fedex.mn.exceptions;

public class DownloadException 
	extends Throwable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 467293491472244347L;
	
	private String msg;
	
	public DownloadException()
	{}
	
	public DownloadException(String msg)
	{
		this.msg = msg;
	}
	
	@Override
	public String getMessage()
	{
		if(msg == null)
			return "Failed to download the file";
		else 
			return msg;
	}
}
