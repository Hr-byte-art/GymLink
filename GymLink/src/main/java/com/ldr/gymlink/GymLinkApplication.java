package com.ldr.gymlink;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
@EnableRabbit
public class GymLinkApplication {

    private static final Logger log = LoggerFactory.getLogger(GymLinkApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GymLinkApplication.class, args);
        log.info("SymLink 启动成功");
    }

}
