package com.fedex.mn.daos;

import com.fedex.mn.models.impls.BasicDetailModel;

public interface BasicDetailDaoInterface
{
	int add(BasicDetailModel basic);
	BasicDetailModel get(int id);
	int update(BasicDetailModel basic);
}
