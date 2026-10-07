package com.sentinal.restfw.api.rest;

import javax.servlet.http.HttpServletRequest;

public class RestAPIUtil
{
	public static final int READ = 1;

	public static final int CREATE = 2;

	public static final int UPDATE = 3;

	public static final int DELETE = 4;

	public static final int PATCH = 5;

	private RestAPIUtil()
	{
	}

	public static int getRequestType(HttpServletRequest request)
	{
		String method = request.getMethod();

		if (method == null)
		{
			throw new IllegalArgumentException("HTTP request method cannot be null");
		}

		return switch (method.toUpperCase())
		{
			case "GET" -> READ;
			case "POST" -> CREATE;
			case "PUT" -> UPDATE;
			case "DELETE" -> DELETE;
			case "PATCH" -> PATCH;
			default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
		};
	}
}