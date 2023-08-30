package com.opensources.apigateway.configurations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensources.apigateway.controllers.response.Response;
import com.opensources.apigateway.models.DynamicRoute;
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
        List<DynamicRoute> routes = apiScopeService.getDynamicRoutes();
        for (DynamicRoute dynamicRoute : routes) {
            String path = dynamicRoute.getPath();
            String method = dynamicRoute.getMethod();
            boolean authRequired = dynamicRoute.isAuthRequired();

            //TODO: handle authenticated
            if (authRequired) {
                http.authorizeHttpRequests(requests ->
                        requests.requestMatchers(method, path).authenticated()
                );
            } else {
                http.authorizeHttpRequests(requests ->
                        requests.requestMatchers(method, path).permitAll()
                );
            }
        }

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

