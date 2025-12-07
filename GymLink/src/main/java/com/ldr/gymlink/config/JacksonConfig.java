package com.ldr.gymlink.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

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

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // 创建一个模块，将 Long 和 long 类型序列化为字符串
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        
        objectMapper.registerModule(simpleModule);
        
        return objectMapper;
    }
}
