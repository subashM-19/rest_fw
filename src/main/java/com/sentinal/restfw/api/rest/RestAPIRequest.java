package com.sentinal.restfw.api.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RestAPIRequest
{
	private String uri;
	private String bean;
	private String method;
	private String entity;
	private String action;
	private String entityXMLFormat;
	private String httpMethod;
	private List<RestAPIRequestParameter> parameters;
	private Pattern pattern;
	private String entityType;


	public RestAPIRequest() {
		parameters = new ArrayList<>();
	}

	public String getEntity()
	{
		return entity;
	}

	public void setEntity(String entity)
	{
		this.entity = entity;
	}

	public String getUri()
	{
		return uri;
	}

	public void setUri(String uri)
	{
		this.uri = uri;
	}

	public String getBean()
	{
		return bean;
	}

	public void setBean(String bean)
	{
		this.bean = bean;
	}

	public String getMethod()
	{
		return method;
	}

	public void setMethod(String method)
	{
		this.method = method;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public String getEntityXMLFormat()
	{
		return entityXMLFormat;
	}

	public void setEntityXMLFormat(String entityXMLFormat)
	{
		this.entityXMLFormat = entityXMLFormat;
	}

	public String getHttpMethod()
	{
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod)
	{
		this.httpMethod = httpMethod;
	}

	public List<RestAPIRequestParameter> getParameters()
	{
		return parameters;
	}

	public void setParameters(List<RestAPIRequestParameter> parameters)
	{
		this.parameters = parameters;
	}

	public void addParameter(RestAPIRequestParameter parameter)
	{
		this.parameters.add(parameter);
	}

	public Pattern getPattern() {
		return pattern;
	}

	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

	public String getEntityType()
	{
		return entityType;
	}

	public void setEntityType(String entityType)
	{
		this.entityType = entityType;
	}

	public boolean matches(String uri) {
		if (pattern != null) {
			return pattern.matcher(uri).matches();
		}

		return this.uri.equals(uri);
	}
}
