package org.example;


import org.apache.dubbo.config.spring.context.annotation.EnableDubboConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubboConfig
public class DubboprivderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DubboprivderApplication.class, args);
    }

}