package org.study.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.study.auth.model.Client


@Configuration
class RedisConfig {
    @Bean("clientRedisTemplate")
    fun clientRedisTemplate(factory: ReactiveRedisConnectionFactory) = ReactiveRedisTemplate(
        factory,
        RedisSerializationContext
            .newSerializationContext<String, Client>(StringRedisSerializer()) // keySerializer
            .value(Jackson2JsonRedisSerializer(Client::class.java)) //valueSerializer
            .build()
    )
}