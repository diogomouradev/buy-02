package com.gritlab.buy01.mediaservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class HealthController {

  @GetMapping("/mediaHealth")
  public ResponseEntity<HttpStatus> healthCheck() {
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
