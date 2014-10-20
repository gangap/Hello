package com.fedex.mn.daos;

import com.fedex.mn.models.impls.TestDetailModel;

public interface TestDetailDaoInterface
{
	int add(TestDetailModel test);
	TestDetailModel get(int id);
	int update(TestDetailModel test);
}
