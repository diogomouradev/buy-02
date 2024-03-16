package com.gritlab.buy01.mediaservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MediaserviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(MediaserviceApplication.class, args);
  }
}
