package com.worldgarage.backend.exception;

import com.worldgarage.backend.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EmailAlreadyRegisteredException.class)
  public ResponseEntity<ApiErrorResponse> handleEmailAlreadyRegistered(
      EmailAlreadyRegisteredException exception) {

    ApiErrorResponse response = new ApiErrorResponse(exception.getMessage());

    return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
  }
}
