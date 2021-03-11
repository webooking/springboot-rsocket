package org.study.auth

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContain
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.study.auth.model.API
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
    "find all clients"{
        clientService.findAll().onEach {
            log.info("client values: {}", it)
            it.apiList.shouldContain(API.Get_Authentication)
        }.onCompletion {
            log.info("Completed successfully")
        }.collect()
    }
    "clear all clients"{
        clientService
            .clear()
            .onEach {
                it.shouldBeTrue()
            }
            .onCompletion {
                log.info("Completed successfully")
            }.collect()
    }

}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}