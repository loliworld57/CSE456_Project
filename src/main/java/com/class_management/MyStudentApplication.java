package com.class_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;



@SpringBootApplication
@EnableWebSecurity
public class MyStudentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyStudentApplication.class, args);
	}

	@Bean
    public LayoutDialect layoutDialect() {
        return new LayoutDialect();
    }
	
}
