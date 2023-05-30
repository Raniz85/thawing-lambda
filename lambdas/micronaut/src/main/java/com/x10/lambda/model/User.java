package com.x10.lambda.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.validation.constraints.NotBlank;
import java.util.Map;
import java.util.UUID;

@Introspected
public class User {

    private final UUID id;

    private final String name;

    public User(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Introspected
    public static class CreateUser {

        @NonNull
        @NotBlank
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public User toUser() {
            return new User(name);
        }
    }
}
