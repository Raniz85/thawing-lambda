package com.x10.lambda;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
public class DynamoDbConfiguration {

    @Bean
    @Lazy
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .build();
    }
}
