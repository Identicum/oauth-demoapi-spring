package com.identicum.iam.identity.oauth2;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.TokenIntrospectionResponse;
import com.nimbusds.oauth2.sdk.TokenIntrospectionSuccessResponse;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.Audience;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import static org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames.AUDIENCE;
import static org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames.CLIENT_ID;
import static org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames.EXPIRES_AT;
import static org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames.ISSUED_AT;
import static org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames.ISSUER;
import static org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames.NOT_BEFORE;
import static org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionClaimNames.SCOPE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom OpaqueTokenIntrospector implementation to support Token Introspection Bearer authentication
 * based on default Spring NimbusOpaqueTokenIntrospector
 * @author Martin Besozzi <mbesozzi@identicum.com>
 */

public class NimbusOpaqueTokenInstrospectionBearer implements OpaqueTokenIntrospector {
	private Converter<String, RequestEntity<?>> requestEntityConverter;
	private RestOperations restOperations;
	private String authorityPrefix = "SCOPE_";
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public NimbusOpaqueTokenInstrospectionBearer(String introspectionUri) {
		log.debug("Initializing OpaqueToken Introspection with URI {} ", introspectionUri);
		RestTemplate restTemplate = new RestTemplate();
		this.restOperations = restTemplate;
		this.requestEntityConverter = this.defaultRequestEntityConverter(URI.create(introspectionUri));
	}

	@Override
	public OAuth2AuthenticatedPrincipal introspect(String token) {
		RequestEntity<?> requestEntity = this.requestEntityConverter.convert(token);
		if (requestEntity == null) {
			throw new OAuth2IntrospectionException("requestEntityConverter returned a null entity");
		}
		log.debug("Calling introspection endpoint with token {} and Bearer authentication ", token);
		
		TokenIntrospectionResponse introspectionResponse;
		try {
			HTTPResponse httpResponse = adaptToNimbusResponse(this.restOperations.exchange(requestEntity, String.class));
			introspectionResponse = TokenIntrospectionResponse.parse(httpResponse);
			if (!introspectionResponse.indicatesSuccess()) {
				throw new OAuth2IntrospectionException("Token introspection failed");
		}
		} catch (ParseException e) {
			throw new OAuth2IntrospectionException(e.getMessage(), e);
		}

		TokenIntrospectionSuccessResponse introspectionSuccessResponse = (TokenIntrospectionSuccessResponse) introspectionResponse;

		if (!introspectionSuccessResponse.isActive()) {
			throw new OAuth2IntrospectionException("Provided token isn't active");
		}

		return convertClaimsSet(introspectionSuccessResponse);
    }

    private Converter<String, RequestEntity<?>> defaultRequestEntityConverter(URI introspectionUri) {
		return token -> {
			HttpHeaders headers = new HttpHeaders();
			// Addding Authorization Bearer
            headers.add("Authorization", "Bearer " + token);
		    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
			MultiValueMap<String, String> body = new LinkedMultiValueMap();
		    body.add("token", token);
			return new RequestEntity<>(body, headers, HttpMethod.POST, introspectionUri);
		};
	}
	
	private OAuth2AuthenticatedPrincipal convertClaimsSet(TokenIntrospectionSuccessResponse response) {
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		Map<String, Object> claims = response.toJSONObject();
		log.debug("Token Introspection response claims: {}", claims);

		if (response.getAudience() != null) {
			List<String> audiences = new ArrayList<>();
			for (Audience audience : response.getAudience()) {
				audiences.add(audience.getValue());
			}
			claims.put(AUDIENCE, Collections.unmodifiableList(audiences));
		}
		if (response.getClientID() != null) {
			claims.put(CLIENT_ID, response.getClientID().getValue());
		}
		if (response.getExpirationTime() != null) {
			Instant exp = response.getExpirationTime().toInstant();
			claims.put(EXPIRES_AT, exp);
		}
		if (response.getIssueTime() != null) {
			Instant iat = response.getIssueTime().toInstant();
			claims.put(ISSUED_AT, iat);
		}
		if (response.getIssuer() != null) {
			claims.put(ISSUER, issuer(response.getIssuer().getValue()));
		}
		if (response.getNotBeforeTime() != null) {
			claims.put(NOT_BEFORE, response.getNotBeforeTime().toInstant());
		}
		
		// Fix: if response.getScope() returns null, check is we could get it as a claim
		if (response.getScope() != null || claims.get("scope") != null) {
			log.debug("Response .getScope(): {}, Claims .get() {}", response.getScope() , claims.get("scope") );
			List<String> scopes;
			if(response.getScope() != null)
				scopes = Collections.unmodifiableList(response.getScope().toStringList());
			else
				scopes = Collections.unmodifiableList(getClaimAsList(claims.get("scope")));
			
			claims.put(SCOPE, scopes);

			for (String scope : scopes) {
				log.debug("Adding authority: {}", this.authorityPrefix + scope);
				authorities.add(new SimpleGrantedAuthority(this.authorityPrefix + scope));
			}
		}
		return new DefaultOAuth2AuthenticatedPrincipal(claims, authorities);
	}

	private HTTPResponse adaptToNimbusResponse(ResponseEntity<String> responseEntity) {
		HTTPResponse response = new HTTPResponse(responseEntity.getStatusCodeValue());
		response.setHeader(HttpHeaders.CONTENT_TYPE, responseEntity.getHeaders().getContentType().toString());
		response.setContent(responseEntity.getBody());

		if (response.getStatusCode() != HTTPResponse.SC_OK) {
			throw new OAuth2IntrospectionException(
					"Introspection endpoint responded with " + response.getStatusCode());
		}
		return response;
	}

    private URL issuer(String uri) {
		try {
			return new URL(uri);
		} catch (Exception ex) {
			throw new OAuth2IntrospectionException("Invalid " + ISSUER + " value: " + uri);
		}
	}

    private List<String> getClaimAsList(Object claimValue)
	{	
		List<String> claimValuesList = new ArrayList<String>();
        if(claimValue != null) {
            if( claimValue instanceof List ) {
                claimValuesList = (List<String>) claimValue;
            }
            else {
                claimValuesList.add((String)claimValue);
            }
        }
        return claimValuesList;
	}

}