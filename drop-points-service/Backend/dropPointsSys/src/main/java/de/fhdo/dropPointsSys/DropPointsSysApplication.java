package de.fhdo.dropPointsSys;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication
public class DropPointsSysApplication {

	public static void main(String[] args) {
		SpringApplication.run(DropPointsSysApplication.class, args);
	}

}
