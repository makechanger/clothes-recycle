package com.recycle;

import org.mybatis.spring.annotation.MapperScan; 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 衣物回收平台 - 启动类
 */
@SpringBootApplication
@MapperScan("com.recycle.mapper") // 让 MyBatis-Plus 去这个包下找你的 Mapper 接口
public class RecycleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecycleApplication.class, args);
    }
}