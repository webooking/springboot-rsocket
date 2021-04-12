package org.study.feign

import io.kotest.core.spec.style.StringSpec
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import reactor.kotlin.test.test

@SpringBootTest
class UserControllerSpec(val requester: RSocketRequester):StringSpec({
    "test controller"{
        requester
            .route("say.hello")
            .data("yuri")
            .retrieveMono(String::class.java)
            .test()
            .expectNext("Hello Tom!")
            .expectComplete()
            .verify()
    }
}){
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}