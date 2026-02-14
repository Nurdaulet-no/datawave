package org.dw.datawave;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatawaveApplication {
    public static void main(String[] args) {
        SpringApplication.run(DatawaveApplication.class, args);
    }
}
