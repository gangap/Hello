package com.fedex.mn.models.impls;

public class ReportFieldModel
	implements Comparable<ReportFieldModel>
{
	private int id;
	private String fieldName;
	private int dataType;
	private String displayName;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public String getFieldName()
	{
		return fieldName;
	}
	public void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}
	public int getDataType()
	{
		return dataType;
	}
	public void setDataType(int dataType)
	{
		this.dataType = dataType;
	}
	public String getDisplayName()
	{
		return displayName;
	}
	public void setDisplayName(String displayName)
	{
		this.displayName = displayName;
	}
	
	@Override
	public int compareTo(ReportFieldModel rpt)
	{
		return this.displayName.compareTo(rpt.getDisplayName());
	}		
}
