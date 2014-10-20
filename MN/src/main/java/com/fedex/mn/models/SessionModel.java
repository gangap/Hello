package com.fedex.mn.models;

import java.util.List;
import java.util.Map;

import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.models.impls.UserModel;

public interface SessionModel
{
	/**
	 * Identifies if the user is logged in currently
	 * @return - True if logged in; false otherwise
	 */
	boolean isLoggedIn();
	
	/**
	 * Sets the user's login status
	 * @param loggedIn - True if the user has successfully logged in
	 */
	void setLoggedIn(boolean loggedIn);
	
	String getView();
	void setView(String view);
	
	String getContentParam();
	void setContentParam(String contentParam);
	
	String getViewParam();
	void setViewParam(String viewParam);
	
	Map<String, String> getFilters();
	void setFilters(Map<String, String> filters);
	
	List<MNModel> getEdittingMNs();
	MNModel getEdittingMN(int mn);
	void setEdittingMNs(List<MNModel> models);
	void removeEdittingMN(int mn);
	
	void clearFilters();
	
	UserModel getUser();
	void setUser(UserModel user);

	void setFilterCookie(String filters);

	boolean hasFilters();
}
