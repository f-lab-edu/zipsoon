package com.zipsoon.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.zipsoon.common.repository")
public class MyBatisConfig {
}