package com.github.houbb.core.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Core Identity Backend Application.
 * The single source of truth for all identity data.
 */
@SpringBootApplication
public class CoreIdentityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreIdentityApplication.class, args);
    }
}