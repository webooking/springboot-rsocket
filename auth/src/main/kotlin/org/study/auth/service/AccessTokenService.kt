package org.study.auth.service

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.deleteAndAwait
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.setIfAbsentAndAwait
import org.springframework.stereotype.Service
import org.study.auth.config.AuthTokenProperties
import org.study.auth.config.CacheKey
import org.study.auth.config.TokenType
import org.study.auth.model.Role
import org.study.auth.model.entity.TokenEntity
import org.study.auth.model.vo.TokenVo
import java.time.LocalDateTime

@Service
class AccessTokenService(
    val accessTokenRedisTemplate: ReactiveRedisTemplate<String, TokenEntity>,
    val properties: AuthTokenProperties,
) {
    suspend fun save(userId: String, role: Role, accessTokenId: String): TokenVo {
        val timeout = properties.findDuration(role, TokenType.AccessToken)
        val accessToken = TokenEntity(
            id = accessTokenId,
            userId = userId,
            role = role,
            createTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plus(timeout)
        )
        accessTokenRedisTemplate.opsForValue().setIfAbsentAndAwait(buildKey(accessTokenId), accessToken, timeout)

        return accessToken.toVo()
    }

    suspend fun find(id: String): TokenEntity? = accessTokenRedisTemplate.opsForValue().getAndAwait(buildKey(id))
    suspend fun delete(id: String) = accessTokenRedisTemplate.opsForValue().deleteAndAwait(buildKey(id))


    private fun buildKey(accessTokenId: String) = CacheKey.accessToken(accessTokenId)
}