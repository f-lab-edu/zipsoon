package com.zipsoon.batch.source.util;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtils {
    private final static Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    public static Reader toReader(String path) throws IOException {
        InputStream is = ResourceUtils.class.getClassLoader().getResourceAsStream(path);
        if (is == null) throw new FileNotFoundException("Resource not found: " + path);
        return new BufferedReader(new InputStreamReader(is, DEFAULT_ENCODING));
    }

    public static Reader toReader(String path, Charset encoding) throws IOException {
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }

        InputStream is = ResourceUtils.class.getClassLoader().getResourceAsStream(path);
        if (is == null) throw new FileNotFoundException("Resource not found: " + path);
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
    
    public static Path getResourcePath(String resourcePath) throws IOException {
        URL url = ResourceUtils.class.getClassLoader().getResource(resourcePath);
        if (url == null) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        }
        return Paths.get(url.getPath());
    }
}