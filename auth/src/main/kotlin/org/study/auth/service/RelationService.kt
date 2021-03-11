package org.study.auth.service

import org.springframework.data.redis.core.*
import org.springframework.stereotype.Service
import org.study.auth.config.AuthTokenProperties
import org.study.auth.config.CacheKey
import org.study.auth.config.ErrorCode
import org.study.auth.config.TokenType
import org.study.auth.model.Role
import org.study.auth.model.entity.TokenEntity
import org.study.auth.model.vo.TokenVo
import org.study.common.config.ErrorCodeException
import java.time.Duration
import java.time.temporal.ChronoUnit

@Service
class RelationService(
    val strRedisTemplate: ReactiveStringRedisTemplate,
    val properties: AuthTokenProperties,
) {
    suspend fun save(userId: String, role: Role, accessTokenId: String, refreshTokenId: String) {
        val accessTokenTimeout = properties.findDuration(role, TokenType.AccessToken)
        val refreshTokenTimeout = properties.findDuration(role, TokenType.RefreshToken)

        save(userId, accessTokenId, accessTokenTimeout, refreshTokenId, refreshTokenTimeout)
    }
    suspend fun save(userId: String, accessToken: TokenVo, refreshToken: TokenEntity) {
        val accessTokenTimeout = Duration.ofSeconds(accessToken.createTime.until(accessToken.expiryTime, ChronoUnit.SECONDS))
        val refreshTokenTimeout = Duration.ofSeconds(accessToken.createTime.until(refreshToken.expiryTime, ChronoUnit.SECONDS))

        save(userId, accessToken.id, accessTokenTimeout, refreshToken.id, refreshTokenTimeout)
    }

    private suspend fun save(userId: String,accessTokenId: String, accessTokenTimeout:Duration, refreshTokenId: String,refreshTokenTimeout:Duration)=strRedisTemplate.opsForValue().let {
        it.setIfAbsentAndAwait(CacheKey.userAndAccessToken(userId), accessTokenId, accessTokenTimeout)
        it.setIfAbsentAndAwait(CacheKey.userAndRefreshToken(userId), refreshTokenId, refreshTokenTimeout)
        it.setIfAbsentAndAwait(CacheKey.refreshTokenAndAccessToken(refreshTokenId), accessTokenId, accessTokenTimeout)
    }

    suspend fun findRefreshTokenIdByUserId(userId: String): String? =
        strRedisTemplate.opsForValue().getAndAwait(CacheKey.userAndRefreshToken(userId))

    suspend fun deleteAll(userId: String, refreshTokenId: String){
        deleteUserAndAccessToken(userId)
        deleteUserAndRefreshToken(userId)
        deleteRefreshTokenAndAccessToken(refreshTokenId)
    }

    suspend fun findAccessTokenIdByUserId(userId: String): String? = strRedisTemplate.opsForValue().getAndAwait(CacheKey.userAndAccessToken(userId))
    suspend fun deleteAccessToken(userId: String) {
        val refreshTokenId = findRefreshTokenIdByUserId(userId) ?: throw ErrorCodeException(
            ErrorCode.Expired_User_Or_RefreshToken,
            "The association between User and RefreshToken expired"
        )

        deleteUserAndAccessToken(userId)
        deleteRefreshTokenAndAccessToken(refreshTokenId)
    }

    private suspend fun deleteUserAndAccessToken(userId: String) = delete(CacheKey.userAndAccessToken(userId))
    private suspend fun deleteUserAndRefreshToken(userId: String) = delete(CacheKey.userAndRefreshToken(userId))
    private suspend fun deleteRefreshTokenAndAccessToken(refreshTokenId: String) = delete(CacheKey.refreshTokenAndAccessToken(refreshTokenId))

    private suspend fun delete(key:String) = strRedisTemplate.opsForValue().deleteAndAwait(key)

}