package com.identicum.iam.identity.spring;

import javax.servlet.http.HttpServletRequest;

import com.identicum.iam.identity.oauth2.NimbusOpaqueTokenInstrospectionBearer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.util.StringUtils;


@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${app.scope.read}")
    private String scopeRead;

    @Value("${app.scope.write}")
    private String scopeWrite;
    
    @Value("${spring.security.oauth2.resourceserver.opaquetoken.introspection-uri}")
    private String instrospectUri;
    
    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-id:}")
    private String clientId;

     @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-secret:")
    private String clientSecret;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
            .and()
              .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/**")
                  .hasAuthority("SCOPE_" + scopeRead)
                .antMatchers(HttpMethod.POST, "/api/**")
                  .hasAuthority("SCOPE_" + scopeWrite)
                .anyRequest()
                  .authenticated()
            .and()
              .oauth2ResourceServer(oauth2 -> oauth2
              .authenticationManagerResolver(this.tokenAuthenticationManagerResolver()));
            //  .oauth2ResourceServer(oauth2 -> oauth2
            //    .opaqueToken(opaqueToken -> opaqueToken
            //      .introspector(new NimbusOpaqueTokenInstrospectionBearer(instrospectUri))  
            //    )
            // );
            //.opaqueToken();
    }
    
    @Bean
    AuthenticationManagerResolver<HttpServletRequest> tokenAuthenticationManagerResolver() 
    {
      OpaqueTokenAuthenticationProvider opaqueTokenAuthProvider;
      // If client_id is empty, we asume that token introspector support Bearer authentication
      // Otherwise, we use the default NimbusOpaqueTokenIntrospector that supprot Basic authentication
      if(StringUtils.isEmpty(this.clientId)) {
        opaqueTokenAuthProvider = new OpaqueTokenAuthenticationProvider(new NimbusOpaqueTokenInstrospectionBearer(instrospectUri));
      }
      else {
        opaqueTokenAuthProvider = new OpaqueTokenAuthenticationProvider(new NimbusOpaqueTokenIntrospector(instrospectUri,clientId, clientSecret));
      }
      
      return request -> {
            return opaqueTokenAuthProvider::authenticate;
      };
    }  
    
}
