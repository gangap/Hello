package com.fedex.mn.daos;

import java.util.List;

import com.fedex.mn.models.impls.EditField;

public interface EditFieldDaoInterface
{
	void add(EditField edit);
	EditField get(int id);
	List<EditField> getAll(int editTransId);
}
