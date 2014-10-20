package com.fedex.mn.exceptions;

public class DatabaseException 
	extends Throwable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4432628421482747354L;
	
	private String msg;
	
	public DatabaseException()
	{}
	
	public DatabaseException(String msg)
	{
		this.msg = msg;
	}
	
	@Override
	public String getMessage()
	{
		if(msg == null)
			return "An exception occurred in a database operation";
		else
			return msg;
	}
}
