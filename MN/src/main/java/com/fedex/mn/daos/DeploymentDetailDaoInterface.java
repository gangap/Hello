package com.fedex.mn.daos;

import com.fedex.mn.models.impls.DeploymentDetailModel;

public interface DeploymentDetailDaoInterface
{
	int add(DeploymentDetailModel model);
	DeploymentDetailModel get(int id);
	int update(DeploymentDetailModel model);
}
