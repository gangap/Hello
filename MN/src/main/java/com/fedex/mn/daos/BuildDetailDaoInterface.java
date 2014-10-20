package com.fedex.mn.daos;

import com.fedex.mn.models.impls.BuildDetailModel;

public interface BuildDetailDaoInterface
{
	int add(BuildDetailModel build);
	BuildDetailModel get(int id);
	int update(BuildDetailModel build);
}
