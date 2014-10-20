package com.fedex.mn.daos;

import java.util.List;

import com.fedex.mn.models.impls.ApprovalModel;

public interface ApprovalDaoInterface
{
	int add(ApprovalModel approval);
	int update(ApprovalModel approval);
	ApprovalModel get(int id);
	List<ApprovalModel> getAll(int mnId);
	int remove(int id);
}
