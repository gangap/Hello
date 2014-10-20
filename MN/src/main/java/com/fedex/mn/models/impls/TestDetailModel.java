package com.fedex.mn.models.impls;

import com.fedex.mn.models.MNDetailModel;

public class TestDetailModel
	extends MNDetailModel
{
	private DBProperty testType;
	private String testableJustification;
	private int testData;
	private String testDataText;
	private String expectedResults;
	private String steps;
	private UserModel sqaUser;
	private String sqaComments;
	
	public TestDetailModel(TestDetailModel test)
	{
		this.testType = new DBProperty(test.getTestType());
		this.testableJustification = new String("" + test.getTestableJustification());
		this.testData = test.getTestData();
		this.testDataText = new String("" + test.getTestDataText());
		this.expectedResults = new String("" + test.getExpectedResults());
		this.steps = new String("" + test.getSteps());
		
		if(test.getSqaUser() != null)
			this.sqaUser = new UserModel(test.getSqaUser());
		
		this.sqaComments = new String("" + test.getSqaComments());
	}
	
	public TestDetailModel()
	{}

	public DBProperty getTestType()
	{
		return testType;
	}
	public void setTestType(DBProperty testType)
	{
		this.testType = testType;
	}
	public String getTestableJustification()
	{
		return testableJustification;
	}
	public void setTestableJustification(String testableJustification)
	{
		this.testableJustification = testableJustification;
	}
	public int getTestData()
	{
		return testData;
	}
	public void setTestData(int testData)
	{
		this.testData = testData;
	}
	public String getTestDataText()
	{
		return testDataText;
	}
	public void setTestDataText(String testDataText)
	{
		this.testDataText = testDataText;
	}
	public String getExpectedResults()
	{
		return expectedResults;
	}
	public void setExpectedResults(String expectedResults)
	{
		this.expectedResults = expectedResults;
	}
	public String getSteps()
	{
		return steps;
	}
	public void setSteps(String steps)
	{
		this.steps = steps;
	}
	public UserModel getSqaUser()
	{
		return sqaUser;
	}
	public void setSqaUser(UserModel sqaUser)
	{
		this.sqaUser = sqaUser;
	}
	public String getSqaComments()
	{
		return sqaComments;
	}
	public void setSqaComments(String sqaComments)
	{
		this.sqaComments = sqaComments;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(super.equals(obj) && 
			obj.getClass().equals(this.getClass()))
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		// TODO Auto-generated method stub
		return "test".hashCode() * super.hashCode();
	}
}
