package com.gritlab.buy01.productservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.gritlab.buy01.productservice.service.ProductService;

@SpringBootApplication
@EnableAsync
public class ProductserviceApplication {

  public static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(ProductserviceApplication.class, args);

    ProductService productService = context.getBean(ProductService.class);

    Thread processingThread = new Thread(productService::processEventsSequentially);
    processingThread.start();
  }

  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.setThreadNamePrefix("event-processor-");
    return executor;
  }
}
