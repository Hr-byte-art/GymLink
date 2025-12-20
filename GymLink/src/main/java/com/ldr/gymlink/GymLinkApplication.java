package com.ldr.gymlink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author 王哈哈
 */
@SpringBootApplication
@EnableAsync
public class GymLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymLinkApplication.class, args);
    }

}
