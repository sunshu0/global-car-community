package com.worldgarage.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

  @GetMapping({
      "/garage",
      "/submit",
      "/account",
      "/admin",
      "/cars/{id}",
      "/users/{id}"
  })
  public String forwardToFrontend() {
    return "forward:/index.html";
  }
}
