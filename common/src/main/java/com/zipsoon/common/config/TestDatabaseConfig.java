package com.zipsoon.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
public class TestDatabaseConfig {

    @Bean
    public ResourceDatabasePopulator schemaPopulator() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.setContinueOnError(true);
        return populator;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource, ResourceDatabasePopulator schemaPopulator) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(schemaPopulator);
        return initializer;
    }
}