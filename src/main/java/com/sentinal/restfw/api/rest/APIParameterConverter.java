package com.sentinal.restfw.api.rest;

public final class APIParameterConverter
{
	private APIParameterConverter()
	{
	}

	public static Object convert(String value, String type)
	{
		if (value == null)
		{
			return null;
		}

		if (type == null || type.trim().isEmpty())
		{
			return value;
		}

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

			case "float[]":
				return toFloatArray(value);

			case "boolean[]":
				return toBooleanArray(value);

			default:
				throw new IllegalArgumentException("Unsupported parameter type: " + type);
		}
	}

	private static String[] toStringArray(String value)
	{
		String[] values = value.split(",");

		for (int i = 0; i < values.length; i++)
		{
			values[i] = values[i].trim();
		}

		return values;
	}

	private static int[] toIntArray(String value)
	{
		String[] values = value.split(",");
		int[] result = new int[values.length];

		for (int i = 0; i < values.length; i++)
		{
			result[i] = Integer.parseInt(values[i].trim());
		}

		return result;
	}

	private static long[] toLongArray(String value)
	{
		String[] values = value.split(",");
		long[] result = new long[values.length];

		for (int i = 0; i < values.length; i++)
		{
			result[i] = Long.parseLong(values[i].trim());
		}

		return result;
	}

	private static double[] toDoubleArray(String value)
	{
		String[] values = value.split(",");
		double[] result = new double[values.length];

		for (int i = 0; i < values.length; i++)
		{
			result[i] = Double.parseDouble(values[i].trim());
		}

		return result;
	}

	private static float[] toFloatArray(String value)
	{
		String[] values = value.split(",");
		float[] result = new float[values.length];

		for (int i = 0; i < values.length; i++)
		{
			result[i] = Float.parseFloat(values[i].trim());
		}

		return result;
	}

	private static boolean[] toBooleanArray(String value)
	{
		String[] values = value.split(",");
		boolean[] result = new boolean[values.length];

		for (int i = 0; i < values.length; i++)
		{
			result[i] = Boolean.parseBoolean(values[i].trim());
		}

		return result;
	}
}