package org.study.auth.model.entity

import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.study.auth.model.Role
import org.study.auth.model.vo.TokenVo
import java.time.LocalDateTime
import java.time.ZoneOffset

data class TokenEntity(
    val id: String,
    val userId: String,
    val role: Role,
    val createTime: LocalDateTime,
    val expiryTime: LocalDateTime,
) {
    fun toVo() = TokenVo(id, createTime, expiryTime)

    fun toSecurity() = OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER,
        id,
        createTime.toInstant(ZoneOffset.UTC),
        expiryTime.toInstant(ZoneOffset.UTC)
    )
}