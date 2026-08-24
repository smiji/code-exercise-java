package com.tpx.urlshort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppStarter implements CommandLineRunner {

    @Value("${spring.application.name:URL Shortener}")
    private String appName;

    private static final Logger logger = LoggerFactory.getLogger(AppStarter.class);

    public static void main(String[] args) {
        SpringApplication.run(AppStarter.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Application **** {} **** started", appName);
    }
}