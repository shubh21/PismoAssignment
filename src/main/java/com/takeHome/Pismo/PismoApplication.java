package com.takeHome.Pismo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.takeHome.Pismo",
		"com.takeHome.Pismo.core",
		"com.takeHome.Pismo.infrastructure"
})
public class PismoApplication {

	public static void main(String[] args) {
		SpringApplication.run(PismoApplication.class, args);
	}

}
