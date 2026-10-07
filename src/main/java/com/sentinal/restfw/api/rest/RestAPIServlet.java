package com.sentinal.restfw.api.rest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RestAPIServlet extends HttpServlet
{
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private RestAPIRequestMaster restAPIRequestMaster;

	@Override public void init() throws ServletException
	{
		super.init();

		restAPIRequestMaster = RestFrameworkListener.getRequestMaster();

		if (restAPIRequestMaster == null)
		{
			throw new ServletException("RestAPIRequestMaster is not initialized");
		}
	}

	@Override protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			String httpMethod = request.getMethod();

			String uri = getRequestURI(request);

			RestAPIRequest restAPIRequest = restAPIRequestMaster.findRequest(httpMethod, uri);

			if (restAPIRequest == null)
			{
				writeErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "REST API not found");
				return;
			}

			RestAPIContext context = createRestAPIContext(restAPIRequest, request);

			Object responseObject = invokeMethod(restAPIRequest, context);

			writeResponse(response, responseObject);
		}
		catch (Exception e)
		{
			e.printStackTrace();

			if (!response.isCommitted())
			{
				handleException(response, e);
			}
		}
	}

	private Object invokeMethod(RestAPIRequest restAPIRequest, RestAPIContext context) throws Exception
	{
		String beanName = restAPIRequest.getBean();
		String methodName = restAPIRequest.getMethod();

		if (methodName != null && !methodName.isEmpty())
		{
			Class<?> beanClass = Class.forName(beanName);
			Object bean = beanClass.getDeclaredConstructor().newInstance();

			Object[] arguments = context.getArguments();
			Method method = findMethod(beanClass, methodName, arguments);

			return method.invoke(bean, arguments);
		}

		return invokeAPIHandler(restAPIRequest, context);
	}

	private Method findMethod(Class<?> beanClass, String methodName, Object[] arguments)
	{
		for (Method method : beanClass.getMethods())
		{
			if (!method.getName().equals(methodName))
			{
				continue;
			}

			if (method.getParameterCount() != arguments.length)
			{
				continue;
			}

			if (matches(method.getParameterTypes(), arguments))
			{
				return method;
			}
		}

		throw new IllegalArgumentException("Method not found: " + beanClass.getName() + "." + methodName);
	}

	private boolean matches(Class<?>[] parameterTypes, Object[] arguments)
	{
		for (int i = 0; i < parameterTypes.length; i++)
		{
			if (arguments[i] == null)
			{
				if (parameterTypes[i].isPrimitive())
				{
					return false;
				}

				continue;
			}

			Class<?> expectedType = wrap(parameterTypes[i]);

			if (!expectedType.isAssignableFrom(arguments[i].getClass()))
			{
				return false;
			}
		}

		return true;
	}

	private Class<?> wrap(Class<?> type)
	{
		if (!type.isPrimitive())
		{
			return type;
		}

		if (type == int.class)
			return Integer.class;
		if (type == long.class)
			return Long.class;
		if (type == double.class)
			return Double.class;
		if (type == float.class)
			return Float.class;
		if (type == boolean.class)
			return Boolean.class;
		if (type == short.class)
			return Short.class;
		if (type == byte.class)
			return Byte.class;
		if (type == char.class)
			return Character.class;

		return type;
	}

	private Object invokeAPIHandler(RestAPIRequest apiRequest, RestAPIContext context) throws Exception
	{
		Class<?> beanClass = Class.forName(apiRequest.getBean());
		Object bean = beanClass.getDeclaredConstructor().newInstance();

		if (!(bean instanceof APIHandler handler))
		{
			throw new IllegalArgumentException("Bean " + apiRequest.getBean() + " must extend APIHandler");
		}

		return handler.handlePreProcess(context);
	}

	private RestAPIContext createRestAPIContext(RestAPIRequest apiRequest, HttpServletRequest request) throws Exception
	{
		String requestURI = getRequestURI(request);

		int requestType = RestAPIUtil.getRequestType(request);

		Map<Integer, String> pathParams = extractPathParams(apiRequest, requestURI);

		Map<String, String> queryParams = extractQueryParams(request);

		String payload = getPayload(request);

		return new RestAPIContext(requestURI, requestType, pathParams, queryParams, payload, apiRequest.getEntity(), apiRequest.getEntityType(), apiRequest);
	}

	private String getRequestURI(HttpServletRequest request)
	{
		String requestURI = request.getRequestURI();

		String contextPath = request.getContextPath();

		String servletPath = request.getServletPath();

		if (contextPath != null && !contextPath.isEmpty())
		{
			requestURI = requestURI.substring(contextPath.length());
		}

		if (servletPath != null && !servletPath.isEmpty() && requestURI.startsWith(servletPath))
		{
			requestURI = requestURI.substring(servletPath.length());
		}

		if (requestURI.isEmpty())
		{
			requestURI = "/";
		}

		return requestURI;
	}

	private Map<String, String> extractQueryParams(HttpServletRequest request)
	{
		Map<String, String> queryParams = new LinkedHashMap<>();

		Map<String, String[]> parameterMap = request.getParameterMap();

		for (Map.Entry<String, String[]> entry : parameterMap.entrySet())
		{
			String[] values = entry.getValue();

			if (values != null && values.length > 0)
			{
				queryParams.put(entry.getKey(), values[0]);
			}
		}

		return queryParams;
	}

	private String getPayload(HttpServletRequest request) throws Exception
	{
		String method = request.getMethod();

		if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))
		{
			return "";
		}
		StringBuilder payload = new StringBuilder();

		String line;

		BufferedReader reader = request.getReader();

		while ((line = reader.readLine()) != null)
		{
			payload.append(line);
		}

		return payload.toString();
	}

	private Map<Integer, String> extractPathParams(RestAPIRequest apiRequest, String requestURI)
	{
		Map<Integer, String> pathParams = new LinkedHashMap<>();

		Pattern pattern = apiRequest.getPattern();

		if (pattern == null)
		{
			return pathParams;
		}

		Matcher matcher = pattern.matcher(requestURI);

		if (!matcher.matches())
		{
			return pathParams;
		}

		for (int i = 1; i <= matcher.groupCount(); i++)
		{
			pathParams.put(i, matcher.group(i));
		}

		return pathParams;
	}

	private void writeResponse(HttpServletResponse response, Object responseObject) throws IOException
	{
		response.setContentType("application/json");

		response.setCharacterEncoding("UTF-8");

		String json = OBJECT_MAPPER.writeValueAsString(responseObject);

		response.getWriter().write(json);
	}

	private void handleException(HttpServletResponse response, Exception e) throws IOException
	{
		response.reset();

		response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

		response.setContentType("application/json");

		response.setCharacterEncoding("UTF-8");

		Map<String, String> error = new LinkedHashMap<>();

		error.put("error", "Internal server error");

		String json = OBJECT_MAPPER.writeValueAsString(error);

		response.getWriter().write(json);

	}

	private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException
	{
		response.setStatus(status);

		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		Map<String, String> error = new LinkedHashMap<>();

		error.put("error", message);

		String json = OBJECT_MAPPER.writeValueAsString(error);

		response.getWriter().write(json);
	}
}