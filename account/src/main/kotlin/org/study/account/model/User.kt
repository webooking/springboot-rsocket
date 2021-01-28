package org.study.account.model

import java.time.LocalDateTime
import java.util.*

data class User(
    val id:String = UUID.randomUUID().toString(),
    val name:String,
    val age: Int,
    val createTime: LocalDateTime = LocalDateTime.now()
)
