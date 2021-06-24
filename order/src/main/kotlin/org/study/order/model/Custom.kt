package org.study.order.model

import java.time.LocalDateTime

enum class Gender {
    Male, Female, Neutral
}

sealed class Custom {
    data class CreateRequest(
        val username: String,
        val age: Int,
        val gender: Gender,
        val phone: Phone,
        val legs: Int,
        val ageBracket: String,
        val hobbies: List<String> = listOf("swimming", "basketball", "football")
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