package com.example.codigosms_boleta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
@EnableFeignClients
@SpringBootApplication
public class CodigosmsBoletaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CodigosmsBoletaApplication.class, args);
	}

}
