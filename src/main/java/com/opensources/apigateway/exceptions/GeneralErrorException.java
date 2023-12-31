package com.opensources.apigateway.exceptions;

import lombok.Getter;

@Getter
public class GeneralErrorException extends Throwable {
    private final String errorCode;

    public GeneralErrorException(String errorCode) {
        this.errorCode = errorCode;
    }
}
