package com.example.status;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongoClient(Environment env) {
        String uri = env.getProperty("SPRING_DATA_MONGODB_URI");

        if (uri == null || uri.isEmpty()) {
            throw new RuntimeException(
                "SPRING_DATA_MONGODB_URI is NOT set. Cannot start application."
            );
        }

        return MongoClients.create(uri);
    }
}
