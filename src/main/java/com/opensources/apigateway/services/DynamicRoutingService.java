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
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@CacheConfig(cacheNames = "responses")
public class DynamicRoutingService {

    private final RestTemplate restTemplate;

    @Autowired
    public DynamicRoutingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<Response<Object>> processReversingProxy(DynamicRoute dynamicRoute, String body, HttpServletRequest request)
            throws GeneralErrorException, JsonProcessingException {
        HttpEntity<String> httpEntity = new HttpEntity<>(body, buildHttpHeaders(request));
        URI uri = buildUri(dynamicRoute, request);

        try {
            ResponseEntity<Response<Object>> responseEntity = restTemplate.exchange(
                    uri.toString(),
                    HttpMethod.valueOf(dynamicRoute.getMethod()),
                    httpEntity,
                    new ParameterizedTypeReference<>() {});
            return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            Response<?> res = new ObjectMapper().readValue(e.getResponseBodyAsString(), Response.class);
            throw new GeneralErrorException(res.getCode());
        }
    }

    @Cacheable(cacheNames = "responses", key = "#key")
    public ResponseEntity<?> processDynamicRouting(HttpServletRequest request, List<List<Route>> dynamicRoutes, String key, String body) {
        Map<String, Object> responseMap = new HashMap<>();
        for (List<Route> outerRoutes : dynamicRoutes) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Route innerRoute : outerRoutes) {
                HttpEntity<String> httpEntity = new HttpEntity<>(body, buildHttpHeaders(request));
                URI uri = buildUri(innerRoute, request);
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    Object responseData;
                    try {
                        responseData = request(uri.toString(), HttpMethod.valueOf(innerRoute.getMethod()), httpEntity);
                    } catch (GeneralErrorException | JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    responseMap.put(innerRoute.getKey(), responseData);
                });
                futures.add(future);
            }
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allOf.join();
        }

        Response<Map<String, Object>> response = new Response<>();
        response.setCode("success");
        response.setData(responseMap);
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

    private HttpHeaders buildHttpHeaders(HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            httpHeaders.set(headerName, request.getHeader(headerName));
        }
        return httpHeaders;
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
