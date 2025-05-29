package com.coditects.bar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@EnableDiscoveryClient
@SpringBootApplication
@RequestMapping("/api/")
public class BarApplication {

	public static void main(String[] args) {
        SpringApplication.run(BarApplication.class, args);
    }

//	 Bean for RestTemplate with LoadBalancer
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Endpoint to get bar details
    @GetMapping("/{barId}/stock")
    @CircuitBreaker(name = "stockService", fallbackMethod = "stockFallback")
    public String getStock(@PathVariable String barId) {
        //
        RestTemplate restTemplate = restTemplate();
        return restTemplate.getForObject("http://stock-service/api/stock/" + barId, String.class);
    }

    // Fallback method for circuit breaker
    public String stockFallback(String barId, Throwable t) {
        return "Stock service unavailable for bar " + barId + ". Try again later.";
    }

}
