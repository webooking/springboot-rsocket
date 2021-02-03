package org.study.account.controller

import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.account.model.User
import org.study.account.service.UserService

@Controller
class UserController(val userService: UserService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("create.the.user")
    suspend fun create(request: User.CreateRequest) {
        log.info("create a user, request parameters: {}", request)
        userService.create(request.toDto())
    }

    @MessageMapping("find.user.by.name")
    suspend fun findByName(username: String): User.Find? = userService.findByName(username)

    @MessageMapping("find.all.users")
    suspend fun findAll(): Flow<User.Find> = userService.findAll()

    @MessageMapping("update.user")
    suspend fun update(request: User.UpdateRequest) = userService.update(request.toDto())

    @MessageMapping("delete.user")
    suspend fun delete(id: String) = userService.delete(id)
}