package com.opensources.apigateway.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensources.apigateway.configurations.ApplicationConfig;
import com.opensources.apigateway.controllers.response.Response;
import com.opensources.apigateway.exceptions.GeneralErrorException;
import com.opensources.apigateway.models.DynamicRoute;
import com.opensources.apigateway.models.Route;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DynamicRoutingService {

    private final RestTemplate restTemplate;

    @Autowired
    public DynamicRoutingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<?> processRoutingRequest(DynamicRoute dynamicRoute, String body, HttpServletRequest request) throws JsonProcessingException, GeneralErrorException {
        HttpHeaders httpHeaders = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            httpHeaders.set(headerName, request.getHeader(headerName));
        }

        if (dynamicRoute.getRoutes() == null) {
            return processReversingProxy(dynamicRoute, httpHeaders, body, request);
        } else {
            return processDynamicRouting(dynamicRoute, httpHeaders, body, request);
        }
    }

    private ResponseEntity<?> processReversingProxy(DynamicRoute dynamicRoute, HttpHeaders httpHeaders, String body, HttpServletRequest request)
            throws GeneralErrorException, JsonProcessingException {
        HttpEntity<String> httpEntity = new HttpEntity<>(body, httpHeaders);
        URI uri = buildUri(dynamicRoute, request);

        try {
            return restTemplate.exchange(
                    uri.toString(),
                    HttpMethod.valueOf(dynamicRoute.getMethod()),
                    httpEntity,
                    Object.class);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Response<?> res = new ObjectMapper().readValue(e.getResponseBodyAsString(), Response.class);
            throw new GeneralErrorException(res.getCode());
        }
    }

    private ResponseEntity<?> processDynamicRouting(DynamicRoute dynamicRoute, HttpHeaders httpHeaders, String body, HttpServletRequest request) throws JsonProcessingException, GeneralErrorException {
        List<List<Route>> routes = dynamicRoute.getRoutes();
        Map<String, Object> response = new HashMap<>();
        for (List<Route> outerRoutes : routes) {
            Route interRoute = outerRoutes.get(0);
            HttpEntity<String> httpEntity = new HttpEntity<>(body, httpHeaders);
            URI uri = buildUri(interRoute, request);
            Object responseData = request(uri.toString(), HttpMethod.valueOf(interRoute.getMethod()), httpEntity);
            response.put(interRoute.getKey(), responseData);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private URI buildUri(Route route, HttpServletRequest request) {
        try {
            return new URI(route.getScheme(),
                    null,
                    route.getHost(),
                    route.getPort(),
                    route.getPath(),
                    request.getQueryString(), null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Object request(String uri, HttpMethod method, HttpEntity<String> httpEntity) throws GeneralErrorException, JsonProcessingException {
        try {
            ResponseEntity<Response<Object>> responseEntity = restTemplate.exchange(
                    uri,
                    method,
                    httpEntity,
                    new ParameterizedTypeReference<>() {});
            Response<Object> responseBody = responseEntity.getBody();

            if (responseEntity.getStatusCode() != HttpStatus.OK || responseBody == null) {
                throw new GeneralErrorException("unknown_error");
            }

            String code = responseBody.getCode();
            if (code == null) {
                throw new GeneralErrorException("unknown_error");
            }
            if (!"success".equals(code)) {
                throw new GeneralErrorException(code);
            }

            return responseBody.getData();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Response<?> res = new ObjectMapper().readValue(e.getResponseBodyAsString(), Response.class);
            throw new GeneralErrorException(res.getCode());
        } catch (GeneralErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
