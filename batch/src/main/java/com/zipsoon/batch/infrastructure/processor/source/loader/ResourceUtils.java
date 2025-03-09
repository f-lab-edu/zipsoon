package com.zipsoon.batch.infrastructure.processor.source.loader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class ResourceUtils {
    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
    private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    // 환경변수나 시스템 프로퍼티로 경로 설정 가능
    private static final String BASE_PATH = System.getProperty("resource.path", "classpath:source/");

    public static Reader toReader(String path) throws IOException {
        InputStream is = resolver.getResource(BASE_PATH + path).getInputStream();
        return new BufferedReader(new InputStreamReader(is, DEFAULT_ENCODING));
    }

    public static Reader toReader(String path, Charset encoding) throws IOException {
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        InputStream is = resolver.getResource(BASE_PATH + path).getInputStream();
        return new BufferedReader(new InputStreamReader(is, encoding));
    }

    public static String toString(String path) throws IOException {
        try (Reader reader = toReader(path)) {
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
            return sb.toString();
        }
    }

    public static LocalDateTime getLastModifiedTime(String path) throws IOException {
        Resource resource = resolver.getResource(BASE_PATH + path);
        if (!resource.exists()) {
            throw new FileNotFoundException("Resource not found: " + path);
        }
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(resource.lastModified()),
            ZoneId.systemDefault()
        );
    }
}