package com.opensources.apigateway.controllers;

import com.opensources.apigateway.controllers.response.Response;
import com.opensources.apigateway.exceptions.GeneralErrorException;
import com.opensources.apigateway.models.DynamicRoute;
import com.opensources.apigateway.services.DynamicRoutingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Enumeration;

@RestController
public class DynamicRoutingController {
    private final DynamicRoutingService dynamicRoutingService;

    @Autowired
    public DynamicRoutingController(DynamicRoutingService dynamicRoutingService) {
        this.dynamicRoutingService = dynamicRoutingService;
    }

    @RequestMapping("/**")
    public ResponseEntity<?> routeRequest(@RequestBody(required = false) String body,
                                                 HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws IOException, GeneralErrorException {
        DynamicRoute dynamicRoute = (DynamicRoute) httpServletRequest.getAttribute("dynamic_route");

        if (dynamicRoute.getRoutes() == null) {
            return dynamicRoutingService.processReversingProxy(dynamicRoute, body, httpServletRequest);
        } else {
            return dynamicRoutingService.processDynamicRouting(
                    httpServletRequest,
                    dynamicRoute.getRoutes(),
                    dynamicRoute.getCacheControl().getPattern(),
                    body);
        }
    }
}

