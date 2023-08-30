package com.opensources.apigateway.interceptors;

import com.opensources.apigateway.configurations.ApplicationConfig;
import com.opensources.apigateway.models.DynamicRoute;
import com.opensources.apigateway.services.ApiScopeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URI;

@Component
public class ServiceInterceptor implements HandlerInterceptor {
    private final ApiScopeService apiScopeService;

    @Autowired
    public ServiceInterceptor(ApiScopeService apiScopeService) {
        this.apiScopeService = apiScopeService;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        DynamicRoute route = apiScopeService.getMatchedAPI(httpServletRequest.getServletPath(), httpServletRequest.getMethod());
        if (route == null) {
            return false;
        }

        httpServletRequest.setAttribute("dynamic_route", route);
        return true;
    }
}
