package org.study.account.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime
import java.util.*

enum class Gender {
    Male, Female, Neutral
}

sealed class User {
    data class Create(
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
        @Column("gender")
        val gender: String,
    )

    data class Find(
        val id: String,
        val username: String,
        val age: Int,
        val gender: Gender,
        val createTime: LocalDateTime,
        val updateTime: LocalDateTime? = null,
    )
}