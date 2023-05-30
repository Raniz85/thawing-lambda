package com.x10.lambda

import com.x10.lambda.model.User
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import spock.lang.Specification

import javax.validation.ConstraintViolationException
import java.util.function.Consumer

@MicronautTest
@Property(name = "user.table-name", value = "test")
class UserRequestHandlerSpec extends Specification {

    private DynamoDbClient dynamo = Mock()

    @MockBean(DynamoDbClient)
    DynamoDbClient dynamo() {
        dynamo
    }

    @Inject
    UserRequestHandler requestHandler

    def "Invalid names"() {
        given: "A user to create"
        def user = new User.CreateUser(
                name: name
        )

        when: "The user is handled"
        requestHandler.execute(user)

        then: "The correct action is taken"
        thrown(ConstraintViolationException)

        where:
        name << ["", null]
    }

    def "Valid names"() {
        given: "A user to create"
        def user = new User.CreateUser(
                name: name
        )

        when: "The user is handled"
        requestHandler.execute(user)

        then: "The user is saved"
        1 * dynamo.putItem(_ as Consumer)

        where:
        name << ["a", "1", "John Doe", "🙂"]
    }
}
