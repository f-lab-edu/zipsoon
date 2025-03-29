package com.zipsoon.batch.infrastructure.processor.source.loader;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 테스트 전용 ResourceUtils - 테스트 환경에서 리소스 파일을 쉽게 찾을 수 있도록 설정
 */
public class TestResourceUtils {
    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
    private static final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    // 테스트 환경에서는 다른 경로 사용 (source/ 접두어 제거)
    private static final String BASE_PATH = "classpath:";

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