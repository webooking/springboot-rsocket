package org.study.order

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ClientSpec(val client: AccountClient) : StringSpec({
    "singUp"{
        client.signUp()
    }

    "stream"{
        client.receive().buffer(2).collect {
            log.info("received $it")
        }
    }

    "list"{
        client.findAll().forEach {
            log.info(it.username)
        }
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
