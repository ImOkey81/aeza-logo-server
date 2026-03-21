package org.aeza.aezaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AezaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AezaServerApplication.class, args);
    }
}
