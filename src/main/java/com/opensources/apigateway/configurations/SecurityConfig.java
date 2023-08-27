package com.opensources.apigateway.configurations;

import com.fasterxml.jackson.databind.JsonNode;
import com.opensources.apigateway.services.ApiScopeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApiScopeService apiScopeService;

    @Autowired
    public SecurityConfig(ApiScopeService apiScopeService) {
        this.apiScopeService = apiScopeService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JsonNode jsonNode = apiScopeService.loadApiScopes();

        for (JsonNode entry : jsonNode) {
            String path = entry.get("path").asText();
            String method = entry.get("method").asText();
            boolean authRequired = entry.get("auth_required").asBoolean();

            //TODO: handle authenticated
            if (authRequired) {
                http.authorizeHttpRequests(requests ->
                        requests.requestMatchers(HttpMethod.valueOf(method), path).authenticated()
                );
            } else {
                http.authorizeHttpRequests(requests ->
                        requests.requestMatchers(HttpMethod.valueOf(method), path).permitAll()
                );
            }
        }

        http.authorizeHttpRequests(requests ->
                requests.anyRequest().authenticated()
        );

        return http.build();
    }
}
