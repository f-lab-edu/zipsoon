package com.zipsoon.api.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = {
    "com.zipsoon.api.estate.mapper",
    "com.zipsoon.api.user.mapper",
    "com.zipsoon.api.user.repository",
    "com.zipsoon.common.mapper"
})
public class MyBatisConfig {
}
