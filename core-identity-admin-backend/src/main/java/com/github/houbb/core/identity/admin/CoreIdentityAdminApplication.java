package com.github.houbb.core.identity.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Core Identity Admin Backend Application.
 * Management BFF that calls Identity Backend via Internal API — never directly accesses the Identity database.
 */
@SpringBootApplication
public class CoreIdentityAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreIdentityAdminApplication.class, args);
    }
}