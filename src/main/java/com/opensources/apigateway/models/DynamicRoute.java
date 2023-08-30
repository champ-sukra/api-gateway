package com.opensources.apigateway.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.util.List;

@Setter
@Getter
public class DynamicRoute extends Route {
    private boolean authRequired;
    private CacheControl cacheControl;
    private List<List<Route>> routes;

    public DynamicRoute(String scheme,
                        String host,
                        int port,
                        String path,
                        String method,
                        boolean authRequired,
                        CacheControl cacheControl,
                        List<List<Route>> routes) {
        super(scheme, host, port, path, method, null);

        this.authRequired = authRequired;
        this.cacheControl = cacheControl;
        this.routes = routes;
    }
}