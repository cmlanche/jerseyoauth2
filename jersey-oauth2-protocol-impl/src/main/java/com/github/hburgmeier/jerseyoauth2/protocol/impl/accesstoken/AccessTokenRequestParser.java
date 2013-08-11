package com.github.hburgmeier.jerseyoauth2.protocol.impl.accesstoken;

import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.github.hburgmeier.jerseyoauth2.api.protocol.IHttpRequest;
import com.github.hburgmeier.jerseyoauth2.api.protocol.OAuth2Exception;
import com.github.hburgmeier.jerseyoauth2.api.types.GrantType;
import com.github.hburgmeier.jerseyoauth2.api.types.ParameterStyle;
import com.github.hburgmeier.jerseyoauth2.protocol.impl.ClientSecretExtractor;
import com.github.hburgmeier.jerseyoauth2.protocol.impl.ScopeParser;
import com.github.hburgmeier.jerseyoauth2.protocol.impl.extractor.CombinedExtractor;
import com.github.hburgmeier.jerseyoauth2.protocol.impl.oauth2.Constants;

public class AccessTokenRequestParser {

	private static final EnumSet<ParameterStyle> supportedStyles = EnumSet.of(ParameterStyle.BODY, ParameterStyle.QUERY);
	
	private final ScopeParser scopeParser = new ScopeParser();
	
	private final CombinedExtractor grantTypeExtractor = new CombinedExtractor(Constants.GRANT_TYPE, supportedStyles);
	private final CombinedExtractor clientIdExtractor = new CombinedExtractor(Constants.CLIENT_ID, supportedStyles);
	private final CombinedExtractor codeExtractor = new CombinedExtractor(Constants.CODE, supportedStyles);
	private final CombinedExtractor redirectUriExtractor = new CombinedExtractor(Constants.REDIRECT_URI, supportedStyles);
	private final CombinedExtractor refreshTokenExtractor = new CombinedExtractor(Constants.REFRESH_TOKEN, supportedStyles);
	private final CombinedExtractor scopeExtractor = new CombinedExtractor(Constants.SCOPE, supportedStyles);
	
	public AccessTokenRequest parse(IHttpRequest request, boolean enableAuthorizationHeader) throws OAuth2Exception
	{
		String grantTypeString = grantTypeExtractor.extractValue(request);
		if (StringUtils.isEmpty(grantTypeString))
			throw new OAuth2Exception();
		GrantType grantType = GrantType.parse(grantTypeString);
		
		String clientId = clientIdExtractor.extractValue(request);
		String code = codeExtractor.extractValue(request);
		String redirectUri = redirectUriExtractor.extractValue(request);
		
		ClientSecretExtractor clientSecretExtractor = new ClientSecretExtractor(enableAuthorizationHeader);
		String clientSecret = clientSecretExtractor.extractValue(request);
		
		String refreshToken = null;
		Set<String> scopes = null;
		if (grantType == GrantType.REFRESH_TOKEN)
		{
			refreshToken = refreshTokenExtractor.extractValue(request);
			String scope = scopeExtractor.extractValue(request);
			scopes = scopeParser.parseScope(scope);
		}
		
		return new AccessTokenRequest(grantType, clientId, clientSecret, code, redirectUri, refreshToken, scopes);
	}
	
}
