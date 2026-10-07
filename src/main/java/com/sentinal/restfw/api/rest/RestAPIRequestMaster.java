package com.sentinal.restfw.api.rest;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RestAPIRequestMaster
{

	private static final String REST_CONFIG_PATH = "/product_package/conf/app/rest";

	// HTTP Method -> List of all REST API requests
	private final Map<String, List<RestAPIRequest>> requestMap;

	public RestAPIRequestMaster()
	{
		requestMap = new HashMap<>();
	}

	/**
	 * Add a REST API request to the mapper.
	 */
	public void addRequest(RestAPIRequest request) {

		String httpMethod = request.getHttpMethod().toUpperCase();

		requestMap
			.computeIfAbsent(httpMethod, key -> new ArrayList<>())
			.add(request);
	}

	/**
	 * Find the REST API request matching the given
	 * HTTP method and URI.
	 */
	public RestAPIRequest findRequest(String httpMethod, String uri) {

		List<RestAPIRequest> requests =
			requestMap.get(httpMethod.toUpperCase());

		if (requests == null) {
			return null;
		}

		for (RestAPIRequest request : requests) {

			if (request.matches(uri)) {
				return request;
			}
		}

		return null;
	}

	/**
	 * Initialize REST API mappings by loading
	 * all XML configuration files.
	 */
	public void initialize(ServletContext servletContext) {

		String realPath =
			servletContext.getRealPath(REST_CONFIG_PATH);

		if (realPath == null) {
			throw new IllegalStateException(
				"Unable to locate REST configuration path: "
					+ REST_CONFIG_PATH
			);
		}

		File restDirectory = new File(realPath);

		if (!restDirectory.exists()) {
			throw new IllegalStateException(
				"REST configuration directory does not exist: "
					+ realPath
			);
		}

		if (!restDirectory.isDirectory()) {
			throw new IllegalStateException(
				"REST configuration path is not a directory: "
					+ realPath
			);
		}

		File[] xmlFiles = restDirectory.listFiles(
			file -> file.isFile()
				&& file.getName()
				.toLowerCase()
				.endsWith(".xml")
		);

		if (xmlFiles == null) {
			throw new IllegalStateException(
				"Unable to read REST configuration directory: "
					+ realPath
			);
		}

		for (File xmlFile : xmlFiles) {

			String httpMethod =
				getHttpMethod(xmlFile.getName());

			if (httpMethod == null) {
				continue;
			}

			parseXML(xmlFile, httpMethod);
		}
	}

	/**
	 * Parse one REST API XML file.
	 */
	private void parseXML(File xmlFile, String httpMethod) {

		try {

			if (xmlFile.length() == 0)
			{
				System.out.println(
					"Skipping empty REST configuration file: "
						+ xmlFile.getAbsolutePath()
				);

				return;
			}


			DocumentBuilderFactory factory =
				DocumentBuilderFactory.newInstance();

			DocumentBuilder builder =
				factory.newDocumentBuilder();

			Document document =
				builder.parse(xmlFile);

			document.getDocumentElement().normalize();

			NodeList requestNodes =
				document.getElementsByTagName("Request");

			for (int i = 0; i < requestNodes.getLength(); i++) {

				Element requestElement =
					(Element) requestNodes.item(i);

				RestAPIRequest request =
					new RestAPIRequest();

				// HTTP method
				request.setHttpMethod(httpMethod);

				// Request attributes
				request.setUri(
					requestElement.getAttribute("uri")
				);

				request.setBean(
					requestElement.getAttribute("bean")
				);

				request.setMethod(
					requestElement.getAttribute("method")
				);

				request.setEntity(
					requestElement.getAttribute("entity")
				);

				request.setAction(
					requestElement.getAttribute("action")
				);

				// Compile regex only once during startup
				String uri = request.getUri();

				if (isRegex(uri)) {
					request.setPattern(
						Pattern.compile(uri)
					);
				}

				// Parameters
				NodeList parameterNodes =
					requestElement.getElementsByTagName(
						"Parameter"
					);

				for (int j = 0;
				     j < parameterNodes.getLength();
				     j++) {

					Element parameterElement =
						(Element) parameterNodes.item(j);

					RestAPIRequestParameter parameter =
						new RestAPIRequestParameter();

					parameter.setName(
						parameterElement.getAttribute("name")
					);

					parameter.setSource(
						parameterElement.getAttribute("source")
					);

					parameter.setValue(
						parameterElement.getAttribute("value")
					);

					parameter.setType(
						parameterElement.getAttribute("type")
					);

					request.addParameter(parameter);
				}

				// Entity XML format
				NodeList entityXMLFormatNodes =
					requestElement.getElementsByTagName(
						"EntityXMLFormat"
					);

				if (entityXMLFormatNodes.getLength() > 0) {

					String entityXMLFormat =
						entityXMLFormatNodes
							.item(0)
							.getTextContent()
							.trim();

					request.setEntityXMLFormat(
						entityXMLFormat
					);
				}

				// Add request to mapper
				addRequest(request);
			}

		} catch (Exception e) {

			throw new RuntimeException(
				"Failed to parse REST API configuration: "
					+ xmlFile.getAbsolutePath(),
				e
			);
		}
	}

	/**
	 * Get HTTP method from XML file name.
	 *
	 * Examples:
	 *
	 * APIRequestByGet.xml    -> GET
	 * APIRequestByPost.xml   -> POST
	 * APIRequestByPut.xml    -> PUT
	 * APIRequestByDelete.xml -> DELETE
	 * APIRequestByPatch.xml  -> PATCH
	 */
	private String getHttpMethod(String fileName) {

		String name = fileName.toLowerCase();

		return switch (name)
		{
			case "apirequestbyget.xml" -> "GET";
			case "apirequestbypost.xml" -> "POST";
			case "apirequestbyput.xml" -> "PUT";
			case "apirequestbydelete.xml" -> "DELETE";
			case "apirequestbypatch.xml" -> "PATCH";
			default -> null;
		};

	}

	/**
	 * Check whether the URI contains regex characters.
	 */
	private boolean isRegex(String uri) {

		return uri.contains("(")
			|| uri.contains("[")
			|| uri.contains("\\")
			|| uri.contains("*")
			|| uri.contains("+")
			|| uri.contains("?")
			|| uri.contains("{");
	}
}
