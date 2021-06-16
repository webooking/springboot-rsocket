package org.study.account

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication
import org.study.feign.EnableRSocketClients
import java.util.*

@SpringBootApplication(exclude = [ReactiveUserDetailsServiceAutoConfiguration::class])
@EnableRSocketClients
class App

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    runApplication<App>(*args)
}