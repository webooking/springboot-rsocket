package org.study.order.model.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User
import java.util.*

data class AuthUser(
    private val attributes: Map<String, Any>
) : OAuth2User {
    override fun getName(): String = attributes["username"] as String

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities(): List<out GrantedAuthority> = emptyList()

    fun getAdmin() = Admin(
        id = attributes["id"] as String,
        username = attributes["username"] as String,
        attributes["language"] as String,
    )

    fun getCustom() = Custom(
        id = attributes["id"] as String,
        username = attributes["username"] as String,
        phone = attributes["phone"] as String,
        email = attributes["email"] as String,
        attributes["language"] as String,
    )

    fun getLocale(): Locale = Locale(attributes["language"] as String)
}

data class Admin(
    val id: String,
    val username: String,
    val language: String,
) {
    fun toAuthUser() = AuthUser(
        mapOf(
            "id" to id,
            "username" to username,
            "language" to language,
        )
    )
}

data class Custom(
    val id: String,
    val username: String,
    val phone: String,
    val email: String,
    val language: String,
) {
    fun toAuthUser() = AuthUser(
        mapOf(
            "id" to id,
            "username" to username,
            "phone" to phone,
            "email" to email,
            "language" to language,
        )
    )
}
