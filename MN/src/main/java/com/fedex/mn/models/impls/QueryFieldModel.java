package com.fedex.mn.models.impls;

public class QueryFieldModel
	implements Comparable<QueryFieldModel>
{
	private int id;
	private int reportId;
	private int fieldId;
	private boolean selectField;
	private int card;
	private int comparison;
	private Object value;
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public int getReportId()
	{
		return reportId;
	}
	public void setReportId(int reportId)
	{
		this.reportId = reportId;
	}
	public int getFieldId()
	{
		return fieldId;
	}
	public void setFieldId(int fieldId)
	{
		this.fieldId = fieldId;
	}
	public boolean isSelectField()
	{
		return selectField;
	}
	public void setSelectField(boolean selectField)
	{
		this.selectField = selectField;
	}
	public int getCard()
	{
		return card;
	}
	public void setCard(int card)
	{
		this.card = card;
	}
	public int getComparison()
	{
		return comparison;
	}
	public void setComparison(int comparison)
	{
		this.comparison = comparison;
	}
	public Object getValue()
	{
		return value;
	}
	public void setValue(Object value)
	{
		this.value = value;
	}
	@Override
	public int compareTo(QueryFieldModel o)
	{
		if(o.getFieldId() > this.getFieldId())
			return 1;
		else if(o.getFieldId() < this.getFieldId())
			return -1;
		else
			return 0;
	}		
}
