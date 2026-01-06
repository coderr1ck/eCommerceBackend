package com.coderrr1ck.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
@EnableMongoAuditing
public class BackendApplication {

	public static void main(String[] args) {
		var ctx = SpringApplication.run(BackendApplication.class, args);
		var mongoClient = ctx.getBean(MongoTemplate.class);
		var mongoProperties = ctx.getBean(MongoProperties.class);
	}

}
