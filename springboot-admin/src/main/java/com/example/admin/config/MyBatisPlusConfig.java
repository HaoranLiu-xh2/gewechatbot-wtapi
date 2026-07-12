package com.example.admin.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.LocalDateTime;
import java.util.Properties;

/**
 * MyBatis Plus 配置类
 *
 * @author example
 */
@Slf4j
@Configuration
public class MyBatisPlusConfig {

    @Autowired
    private Environment environment;

    /**
     * 配置 MyBatis Plus 插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 根据驱动动态选择分页方言
        DbType dbType = resolveDbType();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        log.info("MyBatis Plus 分页插件已启用，数据库类型：{}", dbType);
        return interceptor;
    }

    /**
     * 配置 DatabaseIdProvider，用于 XML 中区分 MySQL/SQLite 语法
     */
    @Bean
    public DatabaseIdProvider databaseIdProvider() {
        VendorDatabaseIdProvider provider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        properties.setProperty("MySQL", "mysql");
        properties.setProperty("SQLite", "sqlite");
        provider.setProperties(properties);
        return provider;
    }

    private DbType resolveDbType() {
        // 优先读取手动配置的数据库类型
        String configuredType = environment.getProperty("app.database.type", "").toLowerCase();
        if ("sqlite".equals(configuredType)) {
            return DbType.SQLITE;
        }
        if ("mysql".equals(configuredType)) {
            return DbType.MYSQL;
        }
        // 未配置时根据驱动自动识别
        String driverClassName = environment.getProperty("spring.datasource.driver-class-name", "");
        if (driverClassName.contains("sqlite")) {
            return DbType.SQLITE;
        }
        return DbType.MYSQL;
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                log.debug("执行插入自动填充...");
                LocalDateTime now = now();
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                log.debug("执行更新自动填充...");
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, now());
            }
        };
    }

    /**
     * 获取当前时间：SQLite 去掉纳秒，避免 JDBC 解析失败
     */
    private LocalDateTime now() {
        LocalDateTime now = LocalDateTime.now();
        if (resolveDbType() == DbType.SQLITE) {
            return now.withNano(0);
        }
        return now;
    }
}
