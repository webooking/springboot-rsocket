package org.study.auth.model.vo

import java.time.LocalDateTime

data class TokenVo(
    val id: String,
    val createTime: LocalDateTime,
    val expiryTime: LocalDateTime,
)