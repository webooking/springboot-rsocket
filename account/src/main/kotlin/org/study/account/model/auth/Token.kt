package org.study.account.model.auth

import java.time.LocalDateTime

data class AccessToken(
    val id: String,
    val userId: String,
    val createTime: LocalDateTime,
    val expiryTime: LocalDateTime,
)

data class RefreshToken(
    val id: String,
    val userId: String,
    val accessTokenId: String,
    val createTime: LocalDateTime,
    val expiryTime: LocalDateTime,
)