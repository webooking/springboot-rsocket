package org.study.feign

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.verify
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class UserServiceSpec : StringSpec({
    "jdk dynamic proxy"{
        val greeting = "Hello Tom!"

        val handler = InvocationHandler { _, method, args ->
            log.info("Invoked method: {}, args: {}", method.name, args)

            if ("sayHello" == method.name) {
                log.info("method: {}, return String: {}", method.name, greeting)
                greeting
            } else {

            }
        }
        val instance = Proxy.newProxyInstance(
            this::class.java.classLoader,
            arrayOf(UserService::class.java),
            handler
        ) as UserService


        instance.others().shouldBe(Unit)

        val message = instance.sayHello("yuri")

        message.shouldBe(greeting)
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}