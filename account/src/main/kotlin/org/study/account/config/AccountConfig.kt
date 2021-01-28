package org.study.account.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.study.account.model.User

@Configuration
class AccountConfig {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun users(): List<User> {
        log.info("init users")
        return listOf(
            User(name = "peter", age = 18),
            User(name = "yuri", age = 28),
            User(name = "henry", age = 38),
        )
    }
}