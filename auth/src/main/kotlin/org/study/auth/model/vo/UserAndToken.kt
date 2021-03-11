package org.study.auth.model.vo

import org.study.auth.model.AuthUser

data class UserAndToken(
    val user: AuthUser,
    val accessToken: TokenVo,
    val refreshToken: TokenVo,
)