package org.study.order.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketRequester


@Configuration
class RSocketConfig {
    @Bean
    fun requester(
        builder: RSocketRequester.Builder,
        @Value("\${spring.rsocket.server.port}") port: Int
    ): RSocketRequester = builder.tcp("localhost", port)
}
