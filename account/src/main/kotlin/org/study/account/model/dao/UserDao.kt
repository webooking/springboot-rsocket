package org.study.account.model.dao

import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository
import org.study.account.model.User

@Repository
class UserDao(val template: R2dbcEntityTemplate) {
    suspend fun create(dto: User.CreateDto): User.CreateDto? = template.insert(User.CreateDto::class.java).using(dto).awaitSingleOrNull()
}