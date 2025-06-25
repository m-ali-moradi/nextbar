package de.fhdo.dropPointsSys;

import de.fhdo.dropPointsSys.dto.DropPointDto;
import de.fhdo.dropPointsSys.feign.EventPlannerClient;
import de.fhdo.dropPointsSys.feign.WarehouseClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.List;


@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages = "de.fhdo.dropPointsSys.feign")
public class DropPointsSysApplication implements EventPlannerClient, WarehouseClient {


	public static void main(String[] args) {

		SpringApplication.run(DropPointsSysApplication.class, args);
	}

	@Override
	public List<DropPointDto> getDropPoints() {
		return List.of();
	}

	@Override
	public String getConfirmation() {
		return "";
	}
}
