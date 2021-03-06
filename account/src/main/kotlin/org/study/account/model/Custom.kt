package org.study.account.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.study.account.validation.constraints.AgeBracket
import org.study.account.validation.constraints.Odd
import java.time.LocalDateTime
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.Min

enum class Gender {
    Male, Female, Neutral
}

sealed class Custom {
    @AgeBracket
    data class CreateRequest(
        val username: String,
        @get:Min(18) val age: Int,
        val gender: Gender,
        @get:Valid
        val phone: Phone,
        @get:Odd
        val legs: Int,
        val ageBracket: String,
        val hobbies: List<String> = listOf("swimming", "basketball", "football")
    ) : Custom() {
        fun toEntity() = Entity(
            id = UUID.randomUUID().toString(),
            username = username,
            age = age,
            gender = gender.name,
            version = 0L,
        )
    }

    @Table("t_user")
    data class Entity(
        @Id val id: String,
        @Column("username") val username: String,
        @Column("age") val age: Int,
        @Column("gender") val gender: String,
        @Column("version") val version: Long,
        @Column("create_time") val createTime: LocalDateTime? = null,
        @Column("update_time") val updateTime: LocalDateTime? = null,
    ) : Custom()

    data class UpdateRequest(
        val id: String,
        val username: String? = null,
        val age: Int? = null,
        val gender: Gender? = null,
        val version: Long,
    ) : Custom() {
        private fun shouldBeUpdated() = username != null || age != null || gender != null
        fun toSetString(): String {
            if (!shouldBeUpdated()) {
                throw RuntimeException("Parameter error, no data need to be modified")
            }
            val buffer = StringBuffer()
            buffer.append("SET version = ${version + 1}, update_time='${LocalDateTime.now()}'")

            if (username != null && username.isNotBlank()) {
                buffer.append(", username = '${username}'")
            }
            if (age != null) {
                buffer.append(", age=${age}")
            }
            if (gender != null) {
                buffer.append(", gender='${gender.name}'")
            }
            return buffer.toString()
        }
    }
}