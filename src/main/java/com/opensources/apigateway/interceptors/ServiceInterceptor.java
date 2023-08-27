package com.opensources.apigateway.interceptors;

import com.opensources.apigateway.configurations.ApplicationConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URI;
import java.util.Map;

@Component
public class ServiceInterceptor implements HandlerInterceptor {
    private final ApplicationConfig applicationConfig;

    @Autowired
    public ServiceInterceptor(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        String[] paths = httpServletRequest.getRequestURI().split("/");
        if (paths.length == 0) {
            return false;
        }

        ApplicationConfig.ServiceProperties serviceProperties = applicationConfig.getServices().get(paths[1]);
        if (serviceProperties == null) {
            return false;
        }
        String scheme = serviceProperties.getScheme();
        String host = serviceProperties.getHost();
        int port = serviceProperties.getPort();

        URI uri = new URI(scheme,
                null,
                host,
                port,
                httpServletRequest.getRequestURI(),
                httpServletRequest.getQueryString(), null);
        httpServletRequest.setAttribute("redirect_uri", uri);
        return true;
    }
}
