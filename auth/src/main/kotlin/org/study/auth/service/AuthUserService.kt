package org.study.auth.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.core.deleteAndAwait
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.setIfAbsentAndAwait
import org.springframework.stereotype.Service
import org.study.auth.config.AuthTokenProperties
import org.study.auth.config.CacheKey
import org.study.auth.config.ErrorCode
import org.study.auth.config.TokenType
import org.study.auth.model.*
import org.study.common.config.ErrorCodeException

@Service
class AuthUserService(
    val adminRedisTemplate: ReactiveRedisTemplate<String, Admin>,
    val customRedisTemplate: ReactiveRedisTemplate<String, Custom>,
    val properties: AuthTokenProperties,
    val mapper: ObjectMapper
) {
    suspend fun save(user: AuthUser) {
        val key = CacheKey.user(user.id, user.findRole())
        val timeout = properties.findDuration(user.findRole(), TokenType.RefreshToken)
        when (user.findRole()) {
            Role.Admin -> adminRedisTemplate.opsForValue().setIfAbsentAndAwait(
                key,
                user as Admin,
                timeout
            )
            else -> customRedisTemplate.opsForValue().setIfAbsentAndAwait(
                key,
                user as Custom,
                timeout
            )
        }
    }

    suspend fun delete(userId: String, role: Role) {
        val key = CacheKey.user(userId, role)

        when (role) {
            Role.Admin -> adminRedisTemplate.opsForValue().deleteAndAwait(key)
            else -> customRedisTemplate.opsForValue().deleteAndAwait(key)
        }
    }

    suspend fun findSecurityUser(userId: String, role: Role): SecurityUser {
        val entity = find(userId, role) ?: throw ErrorCodeException(ErrorCode.Expired_User_Or_RefreshToken, "user details have expired")
        return entity.toSecurity()
    }

    suspend fun find(userId: String, role: Role): AuthUser? {
        val key = CacheKey.user(userId, role)

        return when (role) {
            Role.Admin -> adminRedisTemplate.opsForValue().getAndAwait(key)
            else -> customRedisTemplate.opsForValue().getAndAwait(key)
        }
    }
}