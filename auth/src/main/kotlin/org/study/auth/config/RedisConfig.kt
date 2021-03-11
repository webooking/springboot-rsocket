package org.study.auth.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.study.auth.model.Admin
import org.study.auth.model.Client
import org.study.auth.model.Custom
import org.study.auth.model.entity.TokenEntity


@Configuration
class RedisConfig(val mapper: ObjectMapper, val factory: ReactiveRedisConnectionFactory) {
    @Bean("clientRedisTemplate")
    fun clientRedisTemplate() = redisTemplate<Client>()

    @Bean("adminRedisTemplate")
    fun adminRedisTemplate() = redisTemplate<Admin>()

    @Bean("customRedisTemplate")
    fun customRedisTemplate() = redisTemplate<Custom>()

    @Bean("tokenRedisTemplate")
    fun tokenRedisTemplate() = redisTemplate<TokenEntity>()

    private inline fun <reified T> redisTemplate() = ReactiveRedisTemplate(
        factory,
        RedisSerializationContext
            .newSerializationContext<String, T>(StringRedisSerializer()) // keySerializer
            .value(Jackson2JsonRedisSerializer(T::class.java).apply {
                setObjectMapper(mapper)
            }) //valueSerializer
            .build()
    )
}

