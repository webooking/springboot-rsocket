package org.study.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication
import org.study.feign.EnableRSocketClients
import org.study.feign.FeignClientMapping
import java.util.*

@SpringBootApplication(exclude = [ReactiveUserDetailsServiceAutoConfiguration::class])
@EnableRSocketClients(
    FeignClientMapping(
        name = "account",
        host = "localhost",
        port = 7000,
        classes = [AccountClient::class]
    ),
)
class App

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<App>(*args)
}