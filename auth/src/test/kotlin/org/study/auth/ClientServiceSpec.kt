package org.study.auth

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.study.auth.service.ClientService

@SpringBootTest
class ClientServiceSpec(val clientService: ClientService) : StringSpec({
    "save clients"{
        clientService
            .save()
            .onEach {
                it.shouldBeTrue()
            }
            .onCompletion {
                if (it == null) log.info("Completed successfully")
            }.collect()
    }

}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}