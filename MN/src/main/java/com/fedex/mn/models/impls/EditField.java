package com.fedex.mn.models.impls;

public class EditField
{
	private int id;
	private int editTransaction;
	private int fieldEdited;
	private String from;
	private String to;
	
	public EditField()
	{}
	
	public EditField(int fieldEdited)
	{
		this.fieldEdited = fieldEdited;
	}
	
	public EditField(int fieldEdited, String from, String to)
	{
		this.fieldEdited = fieldEdited;
		this.from = from;
		this.to = to;		
	}		
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public int getEditTransaction()
	{
		return editTransaction;
	}
	public void setEditTransaction(int editTransaction)
	{
		this.editTransaction = editTransaction;
	}
	public int getFieldEdited()
	{
		return fieldEdited;
	}
	public void setFieldEdited(int fieldEdited)
	{
		this.fieldEdited = fieldEdited;
	}
	public String getFrom()
	{
		return from;
	}
	public void setFrom(String from)
	{
		this.from = from;
	}
	public String getTo()
	{
		return to;
	}
	public void setTo(String to)
	{
		this.to = to;
	}		
}
