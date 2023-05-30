package com.x10.lambda;

import com.x10.lambda.model.User;
import com.x10.lambda.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.function.Function;

@Component
@SpringBootApplication
public class UserRequestHandler
        implements Function<User.CreateUser, User> {

    @Autowired
    private UserRepository userRepository;


    @Override
    public User apply(@Validated User.CreateUser input) {
        final User user = input.toUser();
        userRepository.save(user);
        return user;
    }

    public static void main(String... args) {
        SpringApplication.run(UserRequestHandler.class, args);
    }
}
