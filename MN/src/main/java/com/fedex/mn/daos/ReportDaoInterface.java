package com.fedex.mn.daos;

import java.util.List;

import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.QueryResultsModel;
import com.fedex.mn.models.impls.ReportFieldModel;
import com.fedex.mn.models.impls.ReportModel;

public interface ReportDaoInterface
{
	int addReport(ReportModel report);
	int updateReport(ReportModel report);
	
	ReportModel getReport(int id);
	ReportModel getReport(String name);
	
	List<ReportFieldModel> getAllReportsFieldTypes();
	
	List<DBProperty> getAllReports();
	List<DBProperty> getReportArchives(int id);
	
	int saveReportData(QueryResultsModel results);
	QueryResultsModel getReportArchiveData(int id);
	
	QueryResultsModel queryDB(String query);
}
