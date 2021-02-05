package org.study.account.service

import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.study.account.model.User
import org.study.account.model.dao.UserDao

@Service
class UserService(val dao: UserDao) {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun create(entity: User.Entity) {
        log.info("create the user, dto: {}", entity)
        dao.insert(entity)
    }

    suspend fun findByName(username: String): User.Entity? = dao.findByUsername(username)

    suspend fun findAll(): Flow<User.Entity> = dao.findAll()

    suspend fun update(request: User.UpdateRequest) {
        dao.update(request)
    }

    suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}