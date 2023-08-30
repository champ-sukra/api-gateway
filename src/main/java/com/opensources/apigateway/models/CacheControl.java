package com.opensources.apigateway.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
public class CacheControl {
    private int ttl;
    private String pattern;

    public CacheControl(int ttl, String pattern) {
        this.ttl = ttl;
        this.pattern = pattern;
    }
}


