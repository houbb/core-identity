package com.coreplatform.starter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * OpenAPI 文档自动配置 — 提供默认的 OpenAPI 3 文档基础信息。
 *
 * <p>各模块可自定义 OpenAPI Bean 覆盖 title/description/version。</p>
 */
@AutoConfiguration
public class OpenApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Core Platform API")
                        .description("Core Platform REST API Documentation")
                        .version("1.0.0"));
    }
}