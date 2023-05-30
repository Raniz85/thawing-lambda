package com.x10.lambda.repository;


import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.x10.lambda.model.User;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Singleton
public class UserRepository {

    private final DynamoDbClient dynamo;

    private final String tableName;

    @Inject
    public UserRepository(
            DynamoDbClient dynamo,
            @Property(name = "user.table-name") String tableName
    ) {
        this.dynamo = dynamo;
        this.tableName = tableName;
    }

    private Map<String, AttributeValue> toAttributeValueMap(User user) {
        return ImmutableMap.of(
                "name", AttributeValue.builder().s(user.getName()).build(),
                "id", AttributeValue.builder().s(user.getId().toString()).build()
        );
    }

    public void save(User user) {
        dynamo.putItem(builder -> builder.item(toAttributeValueMap(user))
                .tableName(tableName));
    }
}
