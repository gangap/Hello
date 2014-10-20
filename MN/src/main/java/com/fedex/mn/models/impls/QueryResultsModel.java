package com.fedex.mn.models.impls;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueryResultsModel
{	
	private int id;
	private Date dateCreated;
	private List<String> resultTypes;
	private List<List<Object>> results;
	private ReportModel report;
	
	public QueryResultsModel()
	{
		resultTypes = new ArrayList<String>();
		results = new ArrayList<List<Object>>();
	}

	public List<String> getResultTypes()
	{
		return resultTypes;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public void setResultTypes(List<String> resultTypes)
	{
		this.resultTypes = resultTypes;
	}

	public List<List<Object>> getResults()
	{
		return results;
	}

	public void setResults(List<List<Object>> results)
	{
		this.results = results;
	}

	public ReportModel getReport()
	{
		return report;
	}
	
	public void setReport(ReportModel report)
	{
		this.report = report;
	}
	
	public Date getDateCreated()
	{
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}
}
