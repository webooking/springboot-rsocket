package org.study.account

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication
import org.study.account.controller.AccountController
import org.study.account.controller.UserController
import org.study.feign.EnableRSocketClients
import org.study.feign.FeignClientMapping
import java.util.*

@SpringBootApplication(exclude = [ReactiveUserDetailsServiceAutoConfiguration::class])
@EnableRSocketClients(
    FeignClientMapping(
        name = "account",
        host = "localhost",
        port = 7001,
        classes = [UserController::class]
    ),
    FeignClientMapping(
        name = "auth",
        host = "localhost",
        port = 7001,
        classes = [AccountController::class]
    ),
)
class App

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<App>(*args)
}