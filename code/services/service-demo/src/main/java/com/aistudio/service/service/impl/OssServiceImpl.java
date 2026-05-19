package com.aistudio.service.service.impl;

import com.aistudio.service.config.OssConfig.OssProperties;
import com.aistudio.service.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final S3Client s3Client;
    private final OssProperties ossProperties;

    @Override
    public String generateUpToken(String keyPrefix) {
        return "";
    }

    @Override
    public String uploadFile(String key, InputStream inputStream, long contentLength, String contentType) {
        try {
            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                    .bucket(ossProperties.getBucketName())
                    .key(key)
                    .acl("public-read");

            if (contentType != null && !contentType.isEmpty()) {
                requestBuilder.contentType(contentType);
            }

            PutObjectRequest request = requestBuilder.build();
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));

            log.info("uploaded file to oss: {}", key);
            return key;
        } catch (Exception exception) {
            throw new IllegalStateException("upload failed: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void deleteFile(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(ossProperties.getBucketName())
                    .key(key)
                    .build();
            s3Client.deleteObject(request);
            log.info("deleted oss file: {}", key);
        } catch (Exception exception) {
            log.warn("delete oss file failed, ignored. key={}, error={}", key, exception.getMessage());
        }
    }

    @Override
    public String getDomain() {
        return ossProperties.getDownloadUrl();
    }

    @Override
    public String getPublicUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return ossProperties.getDownloadUrl().replaceAll("/$", "") + "/" + key;
    }

    @Override
    public String generatePresignedUrl(String key, int expireSeconds) {
        if (key == null || key.isBlank()) {
            return null;
        }
        try (S3Presigner presigner = createPresigner()) {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(req -> req
                            .bucket(ossProperties.getBucketName())
                            .key(key)
                            .build())
                    .signatureDuration(Duration.ofSeconds(expireSeconds))
                    .build();
            return presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception exception) {
            log.error("generate presigned url failed: key={}, error={}", key, exception.getMessage());
            return getPublicUrl(key);
        }
    }

    private S3Presigner createPresigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                ossProperties.getAccessKey(), ossProperties.getSecretKey());

        return S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(ossProperties.getEndpoint()))
                .region(Region.of(ossProperties.getRegion()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
