package org.study.auth.config

import org.study.auth.model.Role

object CacheKey {
    /**
     * key: userId
     * value: org.study.auth.model.AuthUser
     * expiration: refreshToken's timeout
     */
    fun user(userId: String, role: Role) = "auth:user:${role}:${userId}"

    /**
     * key: accessTokenId
     * value: org.study.auth.model.entity.TokenEntity
     * expiration: accessToken's timeout
     */
    fun accessToken(id: String) = "auth:accessToken:${id}"

    /**
     * key: refreshTokenId
     * value: org.study.auth.model.entity.TokenEntity
     * expiration: refreshToken's timeout
     */
    fun refreshToken(id: String) = "auth:refreshToken:${id}"

    /**
     * key: userId
     * value: accessTokenId
     * expiration: accessToken's timeout
     */
    fun userAndAccessToken(userId: String) = "auth:user:accessToken:${userId}"

    /**
     * key: userId
     * value: refreshTokenId
     * expiration: refreshToken's timeout
     */
    fun userAndRefreshToken(userId: String) = "auth:user:refreshToken:${userId}"

    /**
     * key: refreshTokenId
     * value: accessTokenId
     * expiration: accessToken's timeout
     */
    fun refreshTokenAndAccessToken(refreshTokenId:String) = "auth:refreshToken:accessToken:${refreshTokenId}"

}