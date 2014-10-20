package com.fedex.mn.models.impls;

public class AbinitioAttachment
{
	private int id, mnId;
	private byte[] file;
	
	public AbinitioAttachment()
	{}
	
	public AbinitioAttachment(int id, int mnId, byte[] file)
	{
		this.id = id;
		this.mnId = mnId;
		this.file = file;
	}
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	public int getMnId()
	{
		return mnId;
	}
	public void setMnId(int mnId)
	{
		this.mnId = mnId;
	}
	public byte[] getFile()
	{
		return file;
	}
	public void setFile(byte[] file)
	{
		this.file = file;
	}
	
	
}
