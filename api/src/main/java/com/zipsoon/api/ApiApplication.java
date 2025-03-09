package com.zipsoon.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.io.File;

@Slf4j
@SpringBootApplication(scanBasePackages = "com.zipsoon")
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(Environment environment) {
        return args -> {
            String port = environment.getProperty("server.port", "8080");
            String projectRoot = new File("").getAbsolutePath();
            String visualizerPath = projectRoot + "/zipsoon-visualizer/index.html";

            log.debug("✅ API 서버: http://localhost:" + port);
            log.debug("✅ Swagger 엔드포인트: http://localhost:" + port + "/swagger-ui/index.html");
            log.debug("✅ 디버깅용 프론트 웹앱: file://" + visualizerPath);
        };
    }

}