package com.sentinal.restfw.api.rest;

import java.util.Map;

public class RestAPIContext
{
	private final String requestURI;
	private final int requestType;
	private final Map<Integer, String> pathParams;
	private final Map<String, String> requestParams;
	private final String payload;
	private final String entity;
	private final String entityType;
	private final Object[] arguments;


	public RestAPIContext(String requestURI, int requestType, Map<Integer, String> pathParams,
		Map<String, String> requestParams, String payload, String entity,
		String entityType, RestAPIRequest restAPIRequest)
	{
		this.requestURI = requestURI;
		this.requestType = requestType;
		this.pathParams = pathParams;
		this.requestParams = requestParams;
		this.payload = payload;
		this.entity = entity;
		this.entityType = entityType;
		this.arguments = buildArguments(restAPIRequest);
	}

	public String getRequestURI()
	{
		return requestURI;
	}

	public int getRequestType()
	{
		return requestType;
	}

	public Map<Integer, String> getPathParams()
	{
		return pathParams;
	}

	public String getPathParam(int index)
	{
		return pathParams.get(index);
	}

	public Map<String, String> getQueryParams()
	{
		return requestParams;
	}

	public String getQueryParam(String name)
	{
		return requestParams.get(name);
	}

	public String getPayload()
	{
		return payload;
	}

	public String getEntity()
	{
		return entity;
	}

	public String getEntityType()
	{
		return entityType;
	}

	public Object[] getArguments()
	{
		return arguments;
	}

	private Object[] buildArguments(RestAPIRequest restAPIRequest)
	{
		if (restAPIRequest.getParameters() == null || restAPIRequest.getParameters().isEmpty())
		{
			return new Object[0];
		}

		Object[] arguments = new Object[restAPIRequest.getParameters().size()];

		for (int i = 0; i < restAPIRequest.getParameters().size(); i++)
		{
			RestAPIRequestParameter parameter = restAPIRequest.getParameters().get(i);

			if ("body".equalsIgnoreCase(parameter.getSource()))
			{
				continue;
			}

			String value = resolveParameterValue(parameter);

			if (value == null)
			{
				throw new IllegalArgumentException(
					"Missing required parameter: " + parameter.getName()
				);
			}

			arguments[i] = convertParameter(
				value,
				parameter.getType(),
				parameter.getName()
			);
		}

		return arguments;
	}

	private String resolveParameterValue(RestAPIRequestParameter parameter)
	{
		String source = parameter.getSource();

		if ("request".equalsIgnoreCase(source))
		{
			return requestParams.get(parameter.getName());
		}

		if ("regex".equalsIgnoreCase(source))
		{
			String value = parameter.getValue();

			if (value == null || value.isEmpty())
			{
				throw new IllegalArgumentException(
					"Regex group is required for parameter: " + parameter.getName()
				);
			}

			return pathParams.get(Integer.parseInt(value));
		}

		throw new IllegalArgumentException(
			"Unsupported parameter source: " + source
		);
	}

	private Object convertParameter(String value, String type, String parameterName)
	{
		try
		{
			switch (type.toLowerCase())
			{
				case "string":
					return value;

				case "int":
				case "integer":
					return Integer.parseInt(value);

				case "long":
					return Long.parseLong(value);

				case "double":
					return Double.parseDouble(value);

				case "float":
					return Float.parseFloat(value);

				case "boolean":
					return Boolean.parseBoolean(value);

				case "string[]":
					return toStringArray(value);

				case "int[]":
				case "integer[]":
					return toIntArray(value);

				case "long[]":
					return toLongArray(value);

				case "double[]":
					return toDoubleArray(value);

				default:
					throw new IllegalArgumentException(
						"Unsupported type '" + type + "' for parameter: " + parameterName
					);
			}
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException(
				"Invalid value '" + value + "' for parameter: " + parameterName,
				e
			);
		}
	}

	private String[] toStringArray(String value)
	{
		String[] values = value.split(",");

		for (int i = 0; i < values.length; i++)
		{
			values[i] = values[i].trim();
		}

		return values;
	}

	private int[] toIntArray(String value)
	{
		String[] values = value.split(",");
		int[] result = new int[values.length];

		for (int i = 0; i < values.length; i++)
		{
			result[i] = Integer.parseInt(values[i].trim());
		}

		return result;
	}

	private long[] toLongArray(String value)
	{
		String[] values = value.split(",");
		long[] result = new long[values.length];

		for (int i = 0; i < values.length; i++)
		{
			result[i] = Long.parseLong(values[i].trim());
		}

		return result;
	}

	private double[] toDoubleArray(String value)
	{
		String[] values = value.split(",");
		double[] result = new double[values.length];

		for (int i = 0; i < values.length; i++)
		{
			result[i] = Double.parseDouble(values[i].trim());
		}

		return result;
	}
}