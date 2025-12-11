package com.ldr.gymlink.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置类
 * 解决 JavaScript 处理 Long 类型精度丢失问题
 * 
 * JavaScript 的 Number 类型最大安全整数是 2^53 - 1 = 9007199254740991
 * 当 Long 类型的 ID 超过这个值时，前端会出现精度丢失
 * 
 * 解决方案：将 Long 类型序列化为 String 类型
 */
@Configuration
public class JacksonConfig {

    /**
     * 使用 Jackson2ObjectMapperBuilderCustomizer 自定义 ObjectMapper
     * 这是 Spring Boot 推荐的方式，确保配置被正确应用
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 将 Long 和 long 类型序列化为字符串，避免前端精度丢失
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}
