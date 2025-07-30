package org.ek.cloud_storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DbConnectionLogger implements CommandLineRunner {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Override
    public void run(String... args) {
        System.out.println("🔍 Connecting to DB: " + dbUrl + " with user: " + dbUser);
    }
}
