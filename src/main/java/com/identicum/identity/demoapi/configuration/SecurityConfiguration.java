package com.identicum.identity.demoapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.jboss.logging.Logger;

@Configuration
public class SecurityConfiguration {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuerUri;
    private static final Logger logger = Logger.getLogger(SecurityConfiguration.class);

   @Bean
        public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
            RequestMatcher productMatcher = new AntPathRequestMatcher("/api/v1/products");

            http.authorizeHttpRequests(authorizeRequests -> {
                authorizeRequests
                    .requestMatchers(productMatcher).hasAuthority("SCOPE_products")
                    .anyRequest().authenticated();
            }).oauth2ResourceServer(oauth2ResourceServer -> {
                oauth2ResourceServer
                .jwt(jwt -> {
                    jwt.decoder(JwtDecoders.fromIssuerLocation(issuerUri));
                });
            });
            logger.debugv("Authorization configuration set.{0} ", http);
            logger.debugv("Resource server configuration set. {0}", http);
    
            SecurityFilterChain filterChain = http.build();
            logger.debug("Security filter chain built.");
    
            return filterChain;
        }
}

   
  