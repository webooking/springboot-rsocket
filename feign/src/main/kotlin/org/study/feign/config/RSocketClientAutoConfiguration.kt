package org.study.feign.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.study.feign.util.RSocketClientBuilder

@Configuration
open class RSocketClientAutoConfiguration {
    @Bean
    open fun rsocketClientBuilder() = RSocketClientBuilder()
}