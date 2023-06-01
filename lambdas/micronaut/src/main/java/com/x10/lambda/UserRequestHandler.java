package com.x10.lambda;

import javax.validation.Valid;

import com.x10.lambda.model.User;
import com.x10.lambda.repository.UserRepository;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.crac.OrderedResource;
import io.micronaut.function.aws.MicronautRequestHandler;
import jakarta.inject.Inject;
import org.crac.Context;
import org.crac.Core;
import org.crac.Resource;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Introspected
public class UserRequestHandler
        extends MicronautRequestHandler<User.CreateUser, User>
        implements Resource {

    @Inject
    protected UserRepository userRepository;

    @Inject
    protected DynamoDbClient dynamoDbClient;

    public UserRequestHandler() {
        Core.getGlobalContext().register(this);
    }

    public UserRequestHandler(ApplicationContext applicationContext) {
        super(applicationContext);
        this.userRepository = applicationContext.getBean(UserRepository.class);
    }

    @Override
    public User execute(@Valid User.CreateUser input) {
        final User user = input.toUser();
        userRepository.save(user);
        return user;
    }

    @Override
    protected @NonNull ApplicationContextBuilder newApplicationContextBuilder() {
        var builder = super.newApplicationContextBuilder();
        builder.eagerInitSingletons(true);
        builder.eagerInitConfiguration(true);
        return builder;
    }

    @Override
    public void beforeCheckpoint(Context<? extends Resource> context) throws Exception {
        try {
            dynamoDbClient.listTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterRestore(Context<? extends Resource> context) throws Exception {

    }
}
