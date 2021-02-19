package org.study.auth

import io.kotest.core.spec.style.StringSpec
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import reactor.kotlin.test.test

@SpringBootTest
class AuthControllerSpec(val requester: RSocketRequester) : StringSpec({
    "generate token"{
        requester
            .route("auth.generate.token")
            .retrieveMono(String::class.java)
            .test()
            .expectNextMatches {
                log.info("retrieve value from RSocket mapping: {}", it)
                it is String
            }
            .expectComplete()
            .verify()
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}