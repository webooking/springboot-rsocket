package org.study.account.service

import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.study.account.model.User
import org.study.account.model.dao.UserDao

@Service
class UserService(val dao: UserDao) {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun create(dto: User.CreateDto) {
        log.info("create the user, dto: {}", dto)
        dao.create(dto)
    }

    suspend fun findByName(username: String): User.Find? = dao.findByName(username)
    suspend fun update(dto: User.UpdateDto) {
        dao.update(dto)
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }

    suspend fun findAll(): Flow<User.Find> = dao.findAll()
}