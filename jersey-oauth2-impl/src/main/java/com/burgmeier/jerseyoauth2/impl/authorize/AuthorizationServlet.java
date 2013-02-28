package com.burgmeier.jerseyoauth2.impl.authorize;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.burgmeier.jerseyoauth2.api.client.IAuthorizationService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AuthorizationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final IAuthorizationService authService;
	
	private ServletContext servletContext;
	
	
	@Inject
	public AuthorizationServlet(final IAuthorizationService authService, ServletContext servletContext)
	{
		this.authService = authService;
		this.servletContext = servletContext;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		authService.evaluateAuthorizationRequest(request, response, servletContext);
	}

}