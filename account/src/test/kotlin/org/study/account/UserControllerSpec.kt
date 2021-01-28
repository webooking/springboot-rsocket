package org.study.account

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.model.User
import reactor.kotlin.test.test

@SpringBootTest
class UserControllerSpec(val requester: RSocketRequester): StringSpec({
    "request-response"{
        requester
            .route("request-response")
            .data("yuri")
            .retrieveMono(User::class.java)
            .test()
            .expectNextMatches { it.age == 28 }
            .expectComplete()
            .verify()
    }
    "Table-Driven testing request-response"{
        forAll(
            row("peter", 18),
            row("yuri", 28),
            row("henry", 38),
        ){name, age ->
            requester
                .route("request-response")
                .data(name)
                .retrieveMono(User::class.java)
                .test()
                .expectNextMatches { it.age == age }
                .expectComplete()
                .verify()
        }
    }
})