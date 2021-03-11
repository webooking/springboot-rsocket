package org.study.auth.controller

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.stereotype.Controller
import org.study.auth.config.ErrorCode
import org.study.auth.model.API
import org.study.auth.model.request.*
import org.study.auth.model.vo.UserAndToken
import org.study.auth.service.*
import org.study.common.config.ErrorCodeException
import java.util.*

@Controller
class AuthController(
    val clientService: ClientService,
    val authUserService: AuthUserService,
    val accessTokenService: AccessTokenService,
    val refreshTokenService: RefreshTokenService,
    val relationService: RelationService,
) {
    @MessageMapping(API.Generate_Token)
    suspend fun generateToken(request: GenerateTokenRequest): UserAndToken {
        clientService.verify(request.client, API.Generate_Token)

        authUserService.save(request.user)

        clearCache(request.user.id)

        val accessTokenId = UUID.randomUUID().toString()
        val refreshTokenId = UUID.randomUUID().toString()

        return coroutineScope {
            val accessToken = async { accessTokenService.save(request.user.id, request.user.findRole(), accessTokenId) }
            val refreshToken = async { refreshTokenService.save(request.user.id, request.user.findRole(), refreshTokenId) }
            launch {
                relationService.save(request.user.id, request.user.findRole(), accessTokenId, refreshTokenId)
            }

            UserAndToken(
                request.user,
                accessToken.await(),
                refreshToken.await(),
            )
        }
    }

    private suspend fun clearCache(userId: String) {
        coroutineScope{
            launch {
                relationService.findAccessTokenIdByUserId(userId)?.let {
                    accessTokenService.delete(it)
                }
            }
            launch {
                relationService.findRefreshTokenIdByUserId(userId)?.let {
                    refreshTokenService.delete(it)
                    relationService.deleteAll(userId, it)
                }
            }
        }
    }

    @MessageMapping(API.Delete_Token)
    suspend fun deleteToken(request: DeleteTokenRequest) {
        clientService.verify(request.client, API.Delete_Token)

        accessTokenService.find(request.accessToken)?.let { accessToken ->
            accessTokenService.delete(accessToken.id)
            val refreshTokenId = relationService.findRefreshTokenIdByUserId(accessToken.userId) ?: throw ErrorCodeException(
                ErrorCode.Expired_User_Or_RefreshToken,
                "The association between User and RefreshToken expired"
            )
            coroutineScope {
                launch {
                    refreshTokenService.delete(refreshTokenId)
                }
                launch {
                    authUserService.delete(accessToken.userId, accessToken.role)
                }
                launch {
                    relationService.deleteAll(accessToken.userId, refreshTokenId)
                }
            }
        }
    }

    @MessageMapping(API.Get_Authentication)
    suspend fun getAuthentication(request: GetAuthenticationRequest): BearerTokenAuthentication {
        clientService.verify(request.client, API.Get_Authentication)

        val accessTokenEntity = accessTokenService.find(request.accessToken) ?: throw ErrorCodeException(
            ErrorCode.Expired_AccessToken,
            "accessToken has expired"
        )
        val user = authUserService.findSecurityUser(accessTokenEntity.userId, accessTokenEntity.role)
        return BearerTokenAuthentication(
            user,
            accessTokenEntity.toSecurity(),
            emptyList()
        )
    }

    @MessageMapping(API.Update_Authentication)
    suspend fun updateAuthentication(request: UpdateAuthenticationRequest) {
        clientService.verify(request.client, API.Update_Authentication)

        authUserService.save(request.user)

        val accessTokenId = relationService.findAccessTokenIdByUserId(request.user.id)
        accessTokenId?.let {
            accessTokenService.delete(it)
        }

        relationService.deleteAccessToken(request.user.id)
    }

    @MessageMapping(API.Refresh_Token)
    suspend fun refreshToken(request: RefreshTokenRequest): UserAndToken {
        clientService.verify(request.client, API.Refresh_Token)

        val refreshToken = refreshTokenService.find(request.refreshToken) ?: throw ErrorCodeException(
            ErrorCode.Expired_User_Or_RefreshToken,
            "refreshToken have expired"
        )
        val user = authUserService.find(refreshToken.userId, refreshToken.role) ?: throw ErrorCodeException(
            ErrorCode.Expired_User_Or_RefreshToken,
            "user details have expired"
        )

        relationService.findAccessTokenIdByUserId(user.id)?.let { accessTokenId ->
            accessTokenService.delete(accessTokenId)
            relationService.deleteAccessToken(user.id)
        }
        val accessTokenId = UUID.randomUUID().toString()
        val accessToken = accessTokenService.save(user.id, user.findRole(), accessTokenId)
        relationService.save(user.id, accessToken, refreshToken)

        return UserAndToken(
            user,
            accessToken,
            refreshToken.toVo(),
        )
    }
}