package com.identicum.identity.demoapi.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SecurityConfiguration {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuerUri;
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);

   @Bean
        public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authorizeRequests -> {
                authorizeRequests
                    .requestMatchers("/api/v1/products").hasAuthority("SCOPE_api.identicum.com/products:read")
                    .anyRequest().authenticated();
            }).oauth2ResourceServer(oauth2ResourceServer -> {
                oauth2ResourceServer
                .jwt(jwt -> {
                    jwt.decoder(JwtDecoders.fromIssuerLocation(issuerUri));
                });
            });
            logger.debug("Authorization configuration set. {}", http);
            logger.debug("Resource server configuration set. {}", http);
    
            SecurityFilterChain filterChain = http.build();
            logger.debug("Security filter chain built.");
    
            return filterChain;
        }
}
