package org.study.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.study.auth.model.Role
import java.time.Duration

@Component
@ConfigurationProperties("auth-token")
class AuthTokenProperties {
    lateinit var expiryPolicies: List<ExpiryPolicy>

    fun findDuration(role: Role, tokenType: TokenType): Duration {
        val expiryPolicy = expiryPolicies.first {
            it.role == role
        }
        return when (tokenType) {
            TokenType.AccessToken -> expiryPolicy.accessToken
            else -> expiryPolicy.refreshToken
        }
    }
}

class ExpiryPolicy {
    lateinit var role: Role
    lateinit var accessToken: Duration
    lateinit var refreshToken: Duration
}

enum class TokenType {
    AccessToken, RefreshToken
}