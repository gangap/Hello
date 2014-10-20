package com.fedex.mn.daos;

import java.util.List;

import com.fedex.mn.models.impls.EditTrans;
import com.fedex.mn.models.impls.MNModel;
import com.fedex.mn.models.impls.MNView;

public interface MNDaoInterface
{
	int add(MNModel mn);
	MNModel get(int id);
	int update(MNModel mn, EditTrans edits);
	List<MNView> getAllMNViews();
	List<MNView> getAllMNViewsFilters(String name, String val);
	MNView getMNView(int id);	
	List<Integer> getChildMN(int mdId);
}
