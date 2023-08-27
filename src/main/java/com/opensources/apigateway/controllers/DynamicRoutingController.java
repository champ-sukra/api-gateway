package com.opensources.apigateway.controllers;

import com.opensources.apigateway.exceptions.GeneralErrorException;
import com.opensources.apigateway.services.DynamicRoutingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class DynamicRoutingController {
    private final DynamicRoutingService dynamicRoutingService;

    @Autowired
    public DynamicRoutingController(DynamicRoutingService dynamicRoutingService) {
        this.dynamicRoutingService = dynamicRoutingService;
    }

    @RequestMapping("/**")
    public ResponseEntity<?> routeRequest(@RequestBody(required = false) String body,
                                                 HttpMethod httpMethod,
                                                 HttpServletRequest httpServletRequest,
                                                 HttpServletResponse httpServletResponse) throws IOException, GeneralErrorException {
        ResponseEntity<?> responseEntity = dynamicRoutingService.processRoutingRequest(body, httpMethod, httpServletRequest);
        return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
    }
}

