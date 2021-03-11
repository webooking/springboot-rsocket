package org.study.auth.model.request

import org.study.auth.model.AuthUser

data class ClientRequest(
    val name: String,
    val id: String,
    val secret: String,
)

data class GenerateTokenRequest(
    val client: ClientRequest,
    val user: AuthUser,
)

data class DeleteTokenRequest(
    val client: ClientRequest,
    val accessToken: String,
)

typealias GetAuthenticationRequest = DeleteTokenRequest
typealias UpdateAuthenticationRequest = GenerateTokenRequest

data class RefreshTokenRequest(
    val client: ClientRequest,
    val refreshToken: String,
)