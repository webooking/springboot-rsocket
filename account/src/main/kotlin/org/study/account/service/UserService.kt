package org.study.account.service

import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.study.account.model.Custom
import org.study.account.model.dao.UserDao

@Service
class UserService(val dao: UserDao) {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun create(entity: Custom.Entity) {
        log.info("create the user, dto: {}", entity)
        dao.insert(entity)
    }

    suspend fun findByName(username: String): Custom.Entity? = dao.findByUsername(username)

    suspend fun findAll(): Flow<Custom.Entity> = dao.findAll()

    suspend fun update(request: Custom.UpdateRequest) {
        dao.update(request)
    }

    suspend fun delete(id: String) {
        dao.deleteById(id)
    }
}