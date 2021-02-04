package org.study.account.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.sql.SqlIdentifier
import java.time.LocalDateTime
import java.util.*
import org.springframework.data.relational.core.query.Update as Query

enum class Gender {
    Male, Female, Neutral
}

sealed class User {
    data class CreateRequest(
        val username: String,
        val age: Int,
        val gender: Gender,
    ) {
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
    )

    data class UpdateRequest(
        val id: String,
        val username: String? = null,
        val age: Int? = null,
        val gender: Gender? = null,
        val version: Long,
    ) {
        private fun shouldBeUpdated() = username != null || age != null || gender != null
        fun toQuery(): Query {
            if (!shouldBeUpdated()) {
                throw RuntimeException("Parameter error, no data need to be modified")
            }
            val map = mutableMapOf<SqlIdentifier, Any>()

            map[SqlIdentifier.unquoted("version")] = version + 1
            map[SqlIdentifier.unquoted("update_time")] = LocalDateTime.now()
            if (username != null && username.isNotBlank()) {
                map[SqlIdentifier.unquoted("username")] = username
            }
            if (age != null) {
                map[SqlIdentifier.unquoted("age")] = age
            }
            if (gender != null) {
                map[SqlIdentifier.unquoted("gender")] = gender.name
            }
            return Query.from(map)
        }
    }
}