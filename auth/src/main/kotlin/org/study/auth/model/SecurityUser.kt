package org.study.auth.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.LocalDateTime
import java.util.*

@JsonIgnoreProperties(value = ["name", "authorities", "admin", "custom"])
data class SecurityUser(
    private val attributes: Map<String, Any>
) : OAuth2User {
    override fun getName(): String = attributes["username"] as String

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities(): List<GrantedAuthority> = emptyList()

    fun getAdmin() = Admin(
        id = attributes["id"] as String,
        username = attributes["username"] as String,
        createTime = attributes["createTime"] as LocalDateTime,
        updateTime = attributes["updateTime"]?.let {
            it as LocalDateTime
        }
    )

    fun getCustom() = Custom(
        id = attributes["id"] as String,
        username = attributes["username"] as String,
        phone = attributes["phone"] as String,
        email = attributes["email"] as String,
        createTime = attributes["createTime"] as LocalDateTime,
        updateTime = attributes["updateTime"]?.let {
            it as LocalDateTime
        }
    )
}

enum class Role {
    Admin, Custom
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
sealed class AuthUser(open val id: String) {
    abstract fun toSecurity(): SecurityUser
    abstract fun toMap(): Map<String, Any>
    abstract fun findRole(): Role
}

data class Admin(
    override val id: String = UUID.randomUUID().toString(),
    val username: String,
    val createTime: LocalDateTime = LocalDateTime.now(),
    val updateTime: LocalDateTime? = null,
) : AuthUser(id) {
    override fun findRole() = Role.Admin

    override fun toMap(): Map<String, Any> = mutableMapOf(
        "id" to id,
        "username" to username,
        "role" to findRole(),
        "createTime" to createTime,
    ).apply {
        updateTime?.let { put("updateTime", updateTime) }
    }.toMap()

    override fun toSecurity() = SecurityUser(toMap())
}

data class Custom(
    override val id: String = UUID.randomUUID().toString(),
    val username: String,
    val phone: String,
    val email: String,
    val createTime: LocalDateTime = LocalDateTime.now(),
    val updateTime: LocalDateTime? = null,
) : AuthUser(id) {
    override fun findRole() = Role.Custom
    override fun toMap(): Map<String, Any> = mutableMapOf(
        "id" to id,
        "username" to username,
        "role" to findRole(),
        "phone" to phone,
        "email" to email,
        "createTime" to createTime,
    ).apply {
        updateTime?.let { put("updateTime", updateTime) }
    }.toMap()

    override fun toSecurity() = SecurityUser(toMap())
}
