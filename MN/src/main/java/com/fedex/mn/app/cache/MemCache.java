package com.fedex.mn.app.cache;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fedex.mn.models.impls.AppStateModel;
import com.fedex.mn.models.impls.DBProperty;
import com.fedex.mn.models.impls.MNView;
import com.fedex.mn.models.impls.ReleaseModel;
import com.fedex.mn.models.impls.ReportFieldModel;
import com.fedex.mn.models.impls.UserModel;
import com.fedex.mn.services.MNService;
import com.fedex.mn.services.ReleaseService;
import com.fedex.mn.services.ReportService;
import com.fedex.mn.services.StaticTableService;
import com.fedex.mn.services.UserService;

@Component
public class MemCache
{
	private Map<String, List<DBProperty>> stValues;
	
	private List<UserModel> users, emails;
	
	private List<MNView> mnViews;
	
	@Autowired
	private StaticTableService stServ;	
	
	@Autowired
	private UserService userServ;
	
	@Autowired
	private MNService mnServ;
	
	@Autowired
	private ReportService reportServ;
	
	@Autowired
	private ReleaseService relServ;
	
	@Value("${app.db.st}")
	private String tables;

	private List<ReportFieldModel> reportFields;
	
	private List<ReleaseModel> releases;
	
	public void init()
	{
		stValues = new Hashtable<String, List<DBProperty>>();
		reportFields = reportServ.getAllReportFieldTypes();
		
		String[] parsedTables = tables.split(",");
		for(String table : parsedTables)
		{
			table = table.trim();
			stValues.put(table, stServ.getAllProperties(table));
		}
		
		users = userServ.getAllUsers();
		emails = userServ.getAllEmails();
		releases = relServ.getAllReleases();
		
		mnViews = mnServ.getAllMNs();	
	}
	
	public List<DBProperty> getProperties(String table)
	{
		if(stValues.get(table) != null)
			return stValues.get(table);
		return new ArrayList<DBProperty>();
	}
	
	public void setStValues(Map<String, List<DBProperty>> stValues)
	{
		this.stValues = stValues;
	}
	
	public Map<String, List<DBProperty>> getStValues()
	{
		return stValues;
	}
	
	public String getTables()
	{
		return tables;
	}
	
	public List<ReleaseModel> getReleases()
	{
		return releases;
	}
	
	public void setReleases(List<ReleaseModel> releases)
	{
		this.releases = releases;
	}
	
	public String getName(String table, int id)
	{
		List<DBProperty> props = getProperties(table);
		
		for(DBProperty prop : props)
		{
			if(prop.getId() == id)
				return prop.getName();
		}
		
		return "";
	}	
	
	public int getPropId(String table, String name)
	{
		List<DBProperty> props = getProperties(table);
		
		for(DBProperty prop : props)
		{
			if(prop.getName().toLowerCase().equals(name.trim().toLowerCase()))
				return prop.getId();
		}
		
		return -1;
	}
	
	public List<UserModel> getUsers()
	{
		return users;
	}
	
	public List<UserModel> getEmails()
	{
		return emails;
	}
	
	public void setUsers(List<UserModel> users)
	{
		this.users = users;
	}

	public List<MNView> getMnViews()
	{
		return mnViews;
	}
	
	public void setMnViews(List<MNView> mnViews)
	{
		this.mnViews = mnViews;
	}

	public void setEmails(List<UserModel> emails)
	{
		this.emails = emails;
	}

	public List<ReportFieldModel> getReportFieldTypes()
	{
		return this.reportFields;
	}
}
