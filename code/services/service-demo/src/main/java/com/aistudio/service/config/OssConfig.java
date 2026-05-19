package com.aistudio.service.config;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(OssConfig.OssProperties.class)
public class OssConfig {

    @Data
    @ConfigurationProperties(prefix = "oss")
    public static class OssProperties {
        private String endpoint;
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private String downloadUrl;
        private String region = "sd-jn-scyd-gyhlwzq-icp";
    }

    @Bean
    public S3Client s3Client(OssProperties props) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                props.getAccessKey(), props.getSecretKey());

        S3Configuration s3Config = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(props.getEndpoint()))
                .region(Region.of(props.getRegion()))
                .serviceConfiguration(s3Config)
                .build();
    }
}
