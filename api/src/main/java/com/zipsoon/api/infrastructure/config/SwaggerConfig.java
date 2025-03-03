package com.zipsoon.api.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Zipsoon API")
                .version("v1.0")
                .description("자취생을 위한 부동산 매물 추천 서비스 API");

        String title = "JWT_ACCESS_TOKEN";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(title);
        Components components = new Components()
                .addSecuritySchemes(title, new SecurityScheme()
                        .name(title)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("access 토큰 값을 입력하세요 (Bearer 불필요)")
                );

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
