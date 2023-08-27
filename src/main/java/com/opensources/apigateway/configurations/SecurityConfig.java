package com.opensources.apigateway.configurations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensources.apigateway.controllers.response.Response;
import com.opensources.apigateway.services.ApiScopeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

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
        JsonNode apiScopes = apiScopeService.loadApiScopes();

        for (JsonNode scope : apiScopes) {
            String path = scope.get("path").asText();
            String method = scope.get("method").asText();
            boolean authRequired = scope.get("auth_required").asBoolean();

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

        List<JsonNode> dynamicApiScopes = apiScopeService.loadDynamicApiScopes();
        System.out.println(dynamicApiScopes);

        // Deny access to all unmatched requests
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint());
        return http.build();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            Response<?> res = new Response<>();
            res.setCode("unauthorized_api");
            ObjectMapper mapper = new ObjectMapper();
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            mapper.writeValue(response.getOutputStream(), res);
        };
    }
}

