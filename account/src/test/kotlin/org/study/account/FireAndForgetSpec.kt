package org.study.account

import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.model.User
import reactor.kotlin.test.test

@SpringBootTest
class FireAndForgetSpec(val requester: RSocketRequester) : StringSpec({
    "fire and forget"{
        requester
            .route("fireAndForget")
            .data(User(name = "frank", age = 34))
            .retrieveMono(Void::class.java)
            .test()
            .expectComplete()
            .verify()

        requester
            .route("request-response")
            .data("frank")
            .retrieveMono(User::class.java)
            .test()
            .expectNextMatches {
                it.age == 34
            }
            .expectComplete()
            .verify()
    }
})