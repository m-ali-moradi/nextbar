package com.nextbar.bar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages = "com.nextbar.bar.feign")
//@RequestMapping("/api/")
public class BarApplication {

	public static void main(String[] args) {
        SpringApplication.run(BarApplication.class, args);
    }
}
