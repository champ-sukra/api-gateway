package com.opensources.apigateway.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensources.apigateway.controllers.response.Response;
import com.opensources.apigateway.exceptions.GeneralErrorException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Enumeration;

@Service
public class DynamicRoutingService {

    private final RestTemplate restTemplate;

    @Autowired
    public DynamicRoutingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<?> processRoutingRequest(String body, HttpMethod httpMethod, HttpServletRequest request) throws JsonProcessingException, GeneralErrorException {
        HttpHeaders httpHeaders = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            httpHeaders.set(headerName, request.getHeader(headerName));
        }

        HttpEntity<String> httpEntity = new HttpEntity<>(body, httpHeaders);
        try {
            return restTemplate.exchange(
                    request.getAttribute("redirect_uri").toString(),
                    httpMethod,
                    httpEntity,
                    Object.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Response<?> res = new ObjectMapper().readValue(e.getResponseBodyAsString(), Response.class);
            throw new GeneralErrorException(res.getCode());
        }
    }
}
