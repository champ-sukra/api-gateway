package com.opensources.apigateway.controllers;

import com.opensources.apigateway.controllers.response.Response;
import com.opensources.apigateway.exceptions.GeneralErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ExceptionHandlerController {

    @ResponseBody
    @ExceptionHandler(GeneralErrorException.class)
    public ResponseEntity<Response<Object>> handleDataNotFoundException(GeneralErrorException e) {
        Response<Object> responseObject = new Response<>();
        responseObject.setCode(e.getErrorCode());

        return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
    }
}
