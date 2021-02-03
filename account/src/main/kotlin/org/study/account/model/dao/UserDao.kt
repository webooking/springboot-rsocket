package org.study.account.model.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.allAndAwait
import org.springframework.data.r2dbc.core.flow
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import org.study.account.model.User

@Repository
class UserDao(val template: R2dbcEntityTemplate) {
    suspend fun create(dto: User.CreateDto): User.CreateDto? = template.insert(User.CreateDto::class.java).using(dto).awaitSingleOrNull()
    suspend fun findByName(username: String): User.Find? =
        template.selectOne(query(where("username").`is`(username)), User.Find::class.java).awaitFirstOrNull()

    suspend fun update(dto: User.UpdateDto): Int = template.update(User.UpdateDto::class.java).matching(
        query(
            where("id").`is`(dto.id)
                .and("version").`is`(dto.version)
        )
    ).apply(dto.toUpdate()).awaitFirst()

    suspend fun delete(id: String) = template.delete(User.Find::class.java).matching(
        query(where("id").`is`(id))
    ).allAndAwait()

    fun findAll(): Flow<User.Find> = template.select(User.Find::class.java).flow()
}