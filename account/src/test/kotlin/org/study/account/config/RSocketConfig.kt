package org.study.account.config

import io.rsocket.metadata.WellKnownMimeType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.springframework.util.MimeTypeUtils


@Configuration
class RSocketConfig {

    @Bean
    fun requester(
        builder: RSocketRequester.Builder,
        @Value("\${spring.rsocket.server.port}") port: Int
    ): RSocketRequester{
        return builder.tcp("localhost", port)
    }

    companion object {
        val TOKEN = BearerTokenMetadata("123456")
        val MIME_TYPE = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.string)
    }
}