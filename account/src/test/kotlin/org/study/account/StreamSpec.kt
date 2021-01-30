package org.study.account

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.slf4j.LoggerFactory
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
            .buffer(4)
            .test()
            .expectNextMatches { list ->
                list.size shouldBe 4

                list.withIndex().all {
                    it.value.age == it.index
                }
            }.expectComplete()
            .verify()
    }

    "read on demand"{
        requester
            .route("stream")
            .retrieveFlux(User::class.java)
            .buffer(2)
            .test()
            .expectNextMatches { list ->
                log.info("Received from the server: {}", list)
                list.size shouldBe 2

                list.withIndex().all {
                    it.value.age == it.index
                }
            }.expectNextMatches { list ->
                log.info("Received from the server: {}", list)
                list.size shouldBe 2

                list.withIndex().all {
                    it.value.age == it.index + 2
                }
            }.expectComplete()
            .verify()
    }

    "cancel"{
        requester
            .route("stream")
            .retrieveFlux(User::class.java)
            .buffer(2)
            .test()
            .expectNextMatches { list ->
                log.info("Received from the server: {}", list)
                list.size shouldBe 2

                list.withIndex().all {
                    it.value.age == it.index
                }
            }.thenCancel()
            .verify()
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}