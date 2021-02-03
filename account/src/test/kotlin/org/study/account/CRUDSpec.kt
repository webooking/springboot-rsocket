package org.study.account

import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.model.Gender
import org.study.account.model.User
import reactor.kotlin.test.test

@SpringBootTest
class CRUDSpec(val requester: RSocketRequester) :StringSpec({
    "create the user"{
        requester
            .route("create.the.user")
            .data(User.Create(
                username = "yuri",
                age = 34,
                gender = Gender.Male
            ))
            .retrieveMono(Void::class.java)
            .test()
            .expectComplete()
            .verify()
    }
})