package com.x10.lambda.repository;


import java.util.Map;

import com.x10.lambda.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Component
public class UserRepository {

    private final DynamoDbClient dynamo;

    private final String tableName;


    public UserRepository(
            DynamoDbClient dynamo,
            @Value("${user.table-name}") String tableName
    ) {
        this.dynamo = dynamo;
        this.tableName = tableName;
    }

    private Map<String, AttributeValue> toAttributevalueMap(User user) {
        return Map.of(
                "name", AttributeValue.builder().s(user.getName()).build(),
                "id", AttributeValue.builder().s(user.getId().toString()).build()
        );
    }

    public void save(User user) {
        dynamo.putItem(builder -> builder.item(toAttributevalueMap(user))
                .tableName(tableName));
    }
}
