package com.zipsoon.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.zipsoon")
@MapperScan(basePackages = {
    "com.zipsoon.api.estate.mapper",
    "com.zipsoon.api.user.mapper",
    "com.zipsoon.api.user.repository",
    "com.zipsoon.common.mapper"
})
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}