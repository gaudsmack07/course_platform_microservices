package com.costacloud.contentservice;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}")
    private String url;
    @Value("${minio.access.key}")
    private String accessKey;
    @Value("${minio.secret.key}")
    private String secretKey;

//    @Bean
//    public MinioClient createMinioClient() {
//        return MinioClient.builder()
//                .endpoint(url)
//                .credentials(accessKey, secretKey)
//                .build();
//    }

    @Bean
    public S3Client getS3Client() {
        return S3Client.builder()
                .region(Region.US_EAST_1) // Replace with the appropriate region if needed
                .endpointOverride(URI.create(url))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
