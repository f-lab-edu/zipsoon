package com.zipsoon.api.infrastructure.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "${mybatis.mapper-scan-packages}")
public class MyBatisConfig {
}
