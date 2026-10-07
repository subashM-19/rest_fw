package com.sentinal.restfw.api.rest;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class RestFrameworkListener implements ServletContextListener {

	private static RestAPIRequestMaster restAPIRequestMaster;

	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		try
		{
			System.out.println("Initializing REST Framework...");

			restAPIRequestMaster = new RestAPIRequestMaster();

			restAPIRequestMaster.initialize(event.getServletContext());

			event.getServletContext().setAttribute("RestAPIRequestMaster", restAPIRequestMaster);

			System.out.println("REST Framework initialized successfully.");
		}
		catch (Exception e)
		{
			System.err.println("FAILED to initialize REST Framework.");

			e.printStackTrace();

			restAPIRequestMaster = null;

			/*
			 * Do not allow the application to start with
			 * an incorrectly initialized REST framework.
			 */
			throw new RuntimeException("REST Framework initialization failed", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent event)
	{

		System.out.println("Destroying REST Framework...");

		restAPIRequestMaster = null;
	}

	public static RestAPIRequestMaster getRequestMaster()
	{
		return restAPIRequestMaster;
	}
}