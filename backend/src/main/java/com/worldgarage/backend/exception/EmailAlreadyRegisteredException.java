package com.worldgarage.backend.exception;

public class EmailAlreadyRegisteredException extends RuntimeException {

  public EmailAlreadyRegisteredException(String email) {
    super("Email already exists: " + email);
  }
}
