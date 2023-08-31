package com.opensources.apigateway.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class CacheControl {
//    private int ttl;
    private String pattern;

    public CacheControl(String pattern) {
//        this.ttl = ttl;
        this.pattern = pattern;
    }
}


