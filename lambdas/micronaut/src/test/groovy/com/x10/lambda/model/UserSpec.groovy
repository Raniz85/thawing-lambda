package com.x10.lambda.model

import spock.lang.Specification

class UserSpec extends Specification {

    def "That users get an ID"() {
        given: "A user"
        def user = new User("John Doe")

        expect: "That the user has an ID"
        user.id
    }

    def "That CreateUser creates a user correctly"() {
        given: "A user to create"
        def createUser = new User.CreateUser().with {
            name = "John Doe"
            it
        }

        when: "It is converted to a real user"
        def user = createUser.toUser()

        then: "The user has the correct name and an ID"
        user.id
        user.name == createUser.name
    }
}
