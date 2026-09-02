package com.worldgarage.backend.exception;

import com.worldgarage.backend.dto.ApiErrorResponse;
import com.worldgarage.backend.dto.ValidationErrorResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException exception) {

    Map<String, String> errors = new LinkedHashMap<>();

    exception
        .getBindingResult()
        .getFieldErrors()
        .forEach(
            error ->
                errors.putIfAbsent(
                    error.getField(), error.getDefaultMessage()));

    ValidationErrorResponse response = new ValidationErrorResponse(errors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
}
