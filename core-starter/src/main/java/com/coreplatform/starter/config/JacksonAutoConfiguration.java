package com.coreplatform.starter.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * Jackson 序列化自动配置 — 统一所有 core-* 模块的 JSON 序列化行为。
 *
 * <ul>
 *   <li>snake_case 字段命名</li>
 *   <li>null 值不序列化</li>
 *   <li>ISO 8601 日期格式</li>
 *   <li>未知属性不报错</li>
 *   <li>Java 8 时间类型支持</li>
 * </ul>
 */
@AutoConfiguration
public class JacksonAutoConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
            builder.featuresToDisable(
                    SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
            );
            builder.modules(new JavaTimeModule());
        };
    }
}