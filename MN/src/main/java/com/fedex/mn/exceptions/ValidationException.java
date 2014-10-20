package com.fedex.mn.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.ObjectError;

public class ValidationException
	extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -898061156992734959L;
	
	private List<ObjectError> errors;
	
	public ValidationException()
	{
		errors = new ArrayList<ObjectError>(0);
	}
	
	public List<ObjectError> getErrors()
	{
		return errors;
	}
}
