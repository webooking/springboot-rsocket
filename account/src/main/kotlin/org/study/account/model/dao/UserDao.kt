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
    suspend fun insert(entity: User.Entity): User.Entity? = template.insert(entity).awaitSingleOrNull()
    suspend fun findByUsername(username: String): User.Entity? =
        template.selectOne(query(where("username").`is`(username)), User.Entity::class.java).awaitFirstOrNull()

    suspend fun update(request: User.UpdateRequest): Int = template.databaseClient
        .sql("update t_user ${request.toSetString()} where id=:id and version=:version")
        .bind("id", request.id)
        .bind("version", request.version)
        .fetch().rowsUpdated().awaitFirst()

    suspend fun deleteById(id: String) = template.delete(User.Entity::class.java).matching(
        query(where("id").`is`(id))
    ).allAndAwait()

    fun findAll(): Flow<User.Entity> = template.select(User.Entity::class.java).flow()
}