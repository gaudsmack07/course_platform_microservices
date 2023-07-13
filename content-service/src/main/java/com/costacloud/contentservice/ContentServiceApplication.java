package com.costacloud.contentservice;

import io.minio.MinioClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ContentServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(ContentServiceApplication.class, args);
	}
}
