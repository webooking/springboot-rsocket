package org.study.account

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flow
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import reactor.kotlin.test.test
import java.lang.Thread.sleep
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
@SpringBootTest
class ChannelSpec(val requester: RSocketRequester) : StringSpec({
    "channel"{
        val clientFlow = flow {
            while (true) {
                emit("Send by the client: ${UUID.randomUUID()}")
            }
        }
        val seconds = requester
            .route("channel")
            .data(clientFlow)
            .retrieveFlux(String::class.java)
            .buffer(5)
            .test()
            .expectNextMatches { list ->
                log.info("Received from the server: {}", list)
                list.size == 5
            }.thenCancel()
            .verify().toSeconds()

        sleep(10000)

        seconds shouldBe 5
    }
}){
    companion object{
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}