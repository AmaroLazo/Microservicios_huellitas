package com.servicio.codigoms_pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CodigomsPagosApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodigomsPagosApplication.class, args);
	}

}
