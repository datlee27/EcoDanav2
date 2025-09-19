package com.ecodana.evodanavn1.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;



@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadDotenv() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")  // Load từ thư mục gốc
                    .ignoreIfMissing()  // Không lỗi nếu .env không tồn tại
                    .load();
            dotenv.entries().forEach(entry ->
                    System.setProperty(entry.getKey(), entry.getValue())
            );
            System.out.println("✅ Loaded .env variables for EvoDanavn1");
        } catch (Exception e) {
            System.err.println("❌ Failed to load .env: " + e.getMessage());
        }
    }
}