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
class RefreshTokenService(
    val refreshTokenRedisTemplate: ReactiveRedisTemplate<String, TokenEntity>,
    val properties: AuthTokenProperties,
) {
    suspend fun save(userId: String, role: Role, refreshTokenId: String): TokenVo {
        val timeout = properties.findDuration(role, TokenType.RefreshToken)
        val refreshToken = TokenEntity(
            id = refreshTokenId,
            userId = userId,
            role = role,
            createTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plus(timeout)
        )
        refreshTokenRedisTemplate.opsForValue().setIfAbsentAndAwait(buildKey(refreshTokenId), refreshToken, timeout)

        return refreshToken.toVo()
    }

    suspend fun delete(id: String) = refreshTokenRedisTemplate.opsForValue().deleteAndAwait(buildKey(id))
    suspend fun find(refreshTokenId: String): TokenEntity? = refreshTokenRedisTemplate.opsForValue().getAndAwait(buildKey(refreshTokenId))

    private fun buildKey(refreshTokenId: String) = CacheKey.refreshToken(refreshTokenId)
}