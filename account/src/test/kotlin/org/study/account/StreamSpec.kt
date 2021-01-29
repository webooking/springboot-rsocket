package org.study.account

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.model.User
import reactor.kotlin.test.test

@SpringBootTest
class StreamSpec(val requester: RSocketRequester) : StringSpec({
    "stream"{
        requester
            .route("stream")
            .retrieveFlux(User::class.java)
            .buffer(20)
            .test()
            .expectNextMatches { list ->
                list.size shouldBe 20

                list.withIndex().all {
                    it.value.age == it.index
                }
            }.expectComplete()
            .verify()
    }
})