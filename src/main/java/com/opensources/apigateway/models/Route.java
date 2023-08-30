package com.opensources.apigateway.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class Route {
    private String scheme;
    private String host;
    private int port;
    private String path;
    private String method;
    private String key;

    public Route(String scheme, String host, int port, String path, String method, String key) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
        this.method = method;
        this.key = key;
    }
}


