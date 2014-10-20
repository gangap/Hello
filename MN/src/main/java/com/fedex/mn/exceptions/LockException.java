package com.fedex.mn.exceptions;

public class LockException 
	extends Throwable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3619098471486964117L;
	
	private String msg;
		
	public LockException()
	{}
	
	public LockException(String msg)
	{
		this.msg = msg;
	}
	
	@Override
	public String getMessage()
	{
		if(msg == null)
			return "An exception occurred during the handling of a lock";
		return msg;
	}
}
