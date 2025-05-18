package com.ttn.nexuscart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class NexusCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusCartApplication.class, args);
    }

}
