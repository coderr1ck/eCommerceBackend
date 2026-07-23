package com.coderrr1ck.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.Arrays;

@SpringBootApplication
@EnableAsync
@EnableMethodSecurity
@EnableJpaAuditing
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(BackendApplication.class);
		app.addListeners((ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
			event.getEnvironment().getPropertySources().forEach(source -> {
				if (source instanceof EnumerablePropertySource) {
					Arrays.stream(((EnumerablePropertySource<?>) source).getPropertyNames())
							.filter(name -> name.startsWith("spring."))
							.forEach(name -> System.out.println(" " + name + " : " + event.getEnvironment().getProperty(name) + ""));
				}

			});
		});
		app.run(args);
	}
}
