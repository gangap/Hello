package com.fedex.mn.daos;

import java.util.List;

import com.fedex.mn.models.impls.EditTrans;

public interface EditTransDaoInterface
{
	int add(EditTrans trans);
	EditTrans get(int id);
	List<EditTrans> getAll(int mnId);
}
