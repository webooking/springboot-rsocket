package org.study.account.model.auth

import org.springframework.security.oauth2.core.OAuth2AccessToken
import java.time.LocalDateTime
import java.time.ZoneOffset

enum class Role {
    Admin, Custom
}

data class AccessToken(
    val id: String,
    val userId: String,
    val role: Role,
    val createTime: LocalDateTime,
    val expiryTime: LocalDateTime,
){
    fun toOAuth2AccessToken() = OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER,
        id,
        createTime.toInstant(ZoneOffset.UTC),
        expiryTime.toInstant(ZoneOffset.UTC)
    )
}

data class RefreshToken(
    val id: String,
    val userId: String,
    val role: Role,
    val accessTokenId: String,
    val createTime: LocalDateTime,
    val expiryTime: LocalDateTime,
)