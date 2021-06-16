package org.study.feign.util

import org.springframework.beans.factory.FactoryBean
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.security.rsocket.metadata.BearerTokenAuthenticationEncoder

class RSocketStrategiesFactoryBean: FactoryBean<RSocketStrategies> {
    override fun getObject(): RSocketStrategies = RSocketStrategies.builder()
        .encoders {
            it.add(BearerTokenAuthenticationEncoder())
            it.add(Jackson2CborEncoder())
        }
        .decoders {
            it.add(Jackson2CborDecoder())
        }
        .build()

    override fun getObjectType(): Class<*> = RSocketStrategies::class.java
}