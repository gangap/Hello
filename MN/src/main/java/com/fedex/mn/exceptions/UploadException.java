package com.fedex.mn.exceptions;

public class UploadException 
	extends Throwable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2325976009714149155L;
	
	private String msg;
	
	public UploadException()
	{}
	
	public UploadException(String msg)
	{
		this.msg = msg;
	}
	
	@Override
	public String getMessage()
	{
		if(msg == null)
			return "Failed to upload the file to the server";
		return msg;
	}

}
