package com.jinmu.xinda;

import com.jinmu.xinda.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
//@ComponentScan(basePackages = "com.jinmu.xinda.entity")
public class XindaApplication {

    @Value("${jwt.secret}")
    private String secretKey;
    //创建JwtUtil
    @Bean
    public JwtUtil jwtUtil(){
        JwtUtil jwtUtil = new JwtUtil(secretKey);
        return jwtUtil;
    }

    public static void main(String[] args) {
        SpringApplication.run(XindaApplication.class, args);
    }

}
