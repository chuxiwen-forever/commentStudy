package com.liu.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.liu.mapper")
public class MybatisPlusConfig {

}
