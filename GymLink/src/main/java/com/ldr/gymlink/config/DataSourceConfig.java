package com.ldr.gymlink.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author 木子宸
 */
@Configuration
public class DataSourceConfig {


    @Value("${spring.datasource.primary.driver-class-name}")
    private String primaryDriverClassName;

    @Value("${spring.datasource.primary.jdbc-url}")
    private String primaryJdbcUrl;

    @Value("${spring.datasource.primary.username}")
    private String primaryUsername;

    @Value("${spring.datasource.primary.password}")
    private String primaryPassword;

    @Value("${spring.datasource.secondary.driver-class-name}")
    private String secondaryDriverClassName;

    @Value("${spring.datasource.secondary.jdbc-url}")
    private String secondaryJdbcUrl;

    @Value("${spring.datasource.secondary.username}")
    private String secondaryUsername;

    @Value("${spring.datasource.secondary.password}")
    private String secondaryPassword;


    /**
     * 配置主数据源：MySQL
     * 对应 application.yaml 中 spring.datasource.primary 配置项
     */
    @Primary
    @Bean(name = "primaryDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(primaryDriverClassName)
                .url(primaryJdbcUrl)
                .username(primaryUsername)
                .password(primaryPassword)
                .build();
    }

    /**
     * 配置从数据源：PostgreSQL（用于向量存储等 AI 相关功能）
     * 对应 application.yaml 中 spring.datasource.secondary 配置项
     */
    @Bean(name = "secondaryDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.secondary")
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create()
                .driverClassName(secondaryDriverClassName)
                .url(secondaryJdbcUrl)
                .username(secondaryUsername)
                .password(secondaryPassword)
                .build();
    }

    /**
     * 主数据源对应的 JdbcTemplate
     */
    @Primary
    @Bean(name = "primaryJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 次要数据源对应的 JdbcTemplate（用于 PgVector 等向量数据库操作）
     */
    @Bean(name = "vectorJdbcTemplate")
    public JdbcTemplate vectorStoreJdbcTemplate(@Qualifier("secondaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}