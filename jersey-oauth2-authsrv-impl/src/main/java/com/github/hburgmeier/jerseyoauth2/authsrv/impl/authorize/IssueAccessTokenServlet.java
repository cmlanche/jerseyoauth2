package com.github.hburgmeier.jerseyoauth2.authsrv.impl.authorize;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hburgmeier.jerseyoauth2.api.protocol.IAccessTokenRequest;
import com.github.hburgmeier.jerseyoauth2.api.protocol.IRequestFactory;
import com.github.hburgmeier.jerseyoauth2.api.protocol.OAuth2ErrorCode;
import com.github.hburgmeier.jerseyoauth2.api.protocol.OAuth2ParseException;
import com.github.hburgmeier.jerseyoauth2.api.protocol.OAuth2ProtocolException;
import com.github.hburgmeier.jerseyoauth2.api.protocol.ResponseBuilderException;
import com.github.hburgmeier.jerseyoauth2.authsrv.api.IConfiguration;
import com.github.hburgmeier.jerseyoauth2.authsrv.api.token.ITokenService;
import com.github.hburgmeier.jerseyoauth2.authsrv.api.ui.AuthorizationFlowException;
import com.github.hburgmeier.jerseyoauth2.protocol.impl.HttpHeaders;
import com.github.hburgmeier.jerseyoauth2.protocol.impl.HttpRequestAdapter;
import com.google.inject.Singleton;

@Singleton
public class IssueAccessTokenServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IssueAccessTokenServlet.class);
	
	private final ITokenService tokenService;
	private final IConfiguration configuration;
	private final IRequestFactory requestFactory;
	
	@Inject
	public IssueAccessTokenServlet(final ITokenService tokenService, final IConfiguration configuration, final IRequestFactory requestFactory) {
		this.tokenService = tokenService;
		this.configuration = configuration;
		this.requestFactory = requestFactory;
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
			IOException {
		if (configuration.getStrictSecurity() && !request.isSecure())
		{
			LOGGER.error("Strict security switch on but insecure request received");
			response.sendError(HttpURLConnection.HTTP_BAD_REQUEST);
		} else {
			
			try {
				IAccessTokenRequest oauthRequest = null;
				try {
					oauthRequest = requestFactory.parseAccessTokenRequest(new HttpRequestAdapter(request), 
							configuration.getEnableAuthorizationHeaderForClientAuth());
					LOGGER.debug("Parsing OAuthTokenRequest successful");

					tokenService.handleRequest(request, response, getServletContext(), oauthRequest);
				} catch (OAuth2ParseException e) {
					LOGGER.error("Token request problem", e);
					tokenService.sendErrorResponse(response, e);
				} catch (OAuth2ProtocolException e) {
					LOGGER.error("Token request problem", e);
					if (e.getErrorCode() == OAuth2ErrorCode.INVALID_CLIENT &&
							oauthRequest.hasUsedAuhorizationHeader())
						{
							sendUnauthorizedResponse(response);
						} else {
							tokenService.sendErrorResponse(response, e);
						}
					
				}
			} catch (AuthorizationFlowException | ResponseBuilderException e) {
				LOGGER.error("OAuth2 system exception", e);
				throw new ServletException(e);
			}
		}
	}
	
	protected void sendUnauthorizedResponse(HttpServletResponse response)
	{
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.addHeader(HttpHeaders.AUTHENTICATE, "Basic");
	}

}
