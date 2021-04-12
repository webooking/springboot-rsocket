/*
package org.study.feign

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.study.feign.proxy.proxy
import org.study.feign.client.UserService

class UserServiceSpec : StringSpec({
    "jdk dynamic proxy"{
        val greeting = "Hello Tom!"

        val instance = proxy(UserService::class.java){ method, args ->
            log.info("Invoked method: {}, args: {}", method.name, args)

            if ("sayHello" == method.name) {
                log.info("method: {}, return String: {}", method.name, greeting)
                greeting
            } else {

            }
        }


        instance.others().shouldBe(Unit)

        val message = instance.sayHello("yuri")

        message.shouldBe(greeting)
    }
    "launch"{
        val greeting = "Hello Tom!"

        val instance = proxy(UserService::class.java){ method, args ->
            delay(1000)
            log.info("Invoked method: {}, args: {}", method.name, args)

            if ("sayHello" == method.name) {
                log.info("method: {}, return String: {}", method.name, greeting)
                greeting
            } else {

            }
        }

        launch {
            instance.others().shouldBe(Unit)
        }
        launch {
            val message = instance.sayHello("yuri")

            message.shouldBe(greeting)
        }
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}*/
