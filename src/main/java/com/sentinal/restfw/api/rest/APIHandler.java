package com.sentinal.restfw.api.rest;


public abstract class APIHandler
{
	public APIHandler()
	{
	}

	public Object handlePreProcess(RestAPIContext context) throws Exception
	{
		return null;
	}
}
