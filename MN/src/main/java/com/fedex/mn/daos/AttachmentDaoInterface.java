package com.fedex.mn.daos;

import java.util.List;

import com.fedex.mn.models.impls.AbinitioAttachment;
import com.fedex.mn.models.impls.Attachment;

public interface AttachmentDaoInterface
{
	int add(Attachment attachment);
	int delete(int id);
	Attachment get(int id);
	List<Attachment> getAll(int mnId);
	List<Attachment> getAllofTypes(int mnId, String type);
	AbinitioAttachment getAbinitioAttachment(int mnId);
	boolean hasAbinitioAttachment(int mnId);
}
