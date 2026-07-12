package com.example.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 后台管理系统启动类
 *
 * @author example
 */
@SpringBootApplication
// 扫描 MyBatis Mapper 接口
@MapperScan("com.example.admin.mapper")
// 启用 Spring 异步方法支持
@EnableAsync
// 启用 Spring 定时任务支持
@EnableScheduling
public class SpringbootAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootAdminApplication.class, args);
    }
}
