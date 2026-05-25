package com.gugugaga.jsmedicine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.gugugaga.jsmedicine.**.mapper")
@SpringBootApplication
public class JsmedicineApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsmedicineApplication.class, args);
    }

}
