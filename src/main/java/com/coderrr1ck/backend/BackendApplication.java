package com.coderrr1ck.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableAsync
@EnableMethodSecurity
@EnableJpaAuditing
public class BackendApplication {

	public static void main(String[] args) {
		var ctx = SpringApplication.run(BackendApplication.class, args);
	}

}
