package org.study.account.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.sql.SqlIdentifier
import java.time.LocalDateTime
import java.util.*
import org.springframework.data.relational.core.query.Update as DBUpdate

enum class Gender {
    Male, Female, Neutral
}

sealed class User {
    data class CreateRequest(
        val username: String,
        val age: Int,
        val gender: Gender,
    ) {
        fun toDto() = CreateDto(
            username = username,
            age = age,
            gender = gender.name
        )
    }

    @Table("t_user")
    data class CreateDto(
        @Id val id: String = UUID.randomUUID().toString(),
        @Column("username") val username: String,
        @Column("age") val age: Int,
        @Column("gender") val gender: String,
    )

    @Table("t_user")
    data class Find(
        @Id val id: String,
        @Column("username") val username: String,
        @Column("age") val age: Int,
        @Column("gender") val gender: Gender,
        @Column("version") val version: Long,
        @Column("create_time") val createTime: LocalDateTime,
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
        fun toDto(): UpdateDto {
            if (!shouldBeUpdated()) {
                throw RuntimeException("Parameter error, no data need to be modified")
            }
            return UpdateDto(
                id = id,
                username = username,
                age = age,
                gender = gender?.name,
                version = version
            )
        }
    }

    @Table("t_user")
    data class UpdateDto(
        @Id val id: String,
        @Column("username") val username: String? = null,
        @Column("age") val age: Int? = null,
        @Column("gender") val gender: String? = null,
        @Column("version") val version: Long,
        @Column("update_time") val updateTime: LocalDateTime = LocalDateTime.now(),
    ) {
        fun toUpdate(): DBUpdate {
            val map = mutableMapOf<SqlIdentifier, Any>()

            map[SqlIdentifier.unquoted("version")] = version + 1
            map[SqlIdentifier.unquoted("update_time")] = updateTime
            if (username != null && username.isNotBlank()) {
                map[SqlIdentifier.unquoted("username")] = username
            }
            if (age != null) {
                map[SqlIdentifier.unquoted("age")] = age
            }
            if (gender != null && gender.isNotBlank()) {
                map[SqlIdentifier.unquoted("gender")] = gender
            }
            return DBUpdate.from(map)
        }
    }
}