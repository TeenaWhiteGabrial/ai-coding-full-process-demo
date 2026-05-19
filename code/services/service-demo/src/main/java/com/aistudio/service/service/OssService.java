package com.aistudio.service.service;

import java.io.InputStream;

public interface OssService {

    String generateUpToken(String keyPrefix);

    String uploadFile(String key, InputStream inputStream, long contentLength, String contentType);

    default String generateUploadToken(String key) {
        return "";
    }

    default void deleteFile(String key) {
    }

    default void delete(String key) {
        deleteFile(key);
    }

    String getDomain();

    String getPublicUrl(String key);

    default String generatePresignedUrl(String key, int expireSeconds) {
        return getPublicUrl(key);
    }
}
