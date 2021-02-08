package org.study.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.account.model.User
import org.study.account.service.UserService
import org.study.account.validation.validator.UserControllerValidator
import org.study.common.config.BusinessException
import org.study.common.config.GlobalExceptionHandler

@Controller
class UserController(
    val userService: UserService,
    val validator: UserControllerValidator,
    override val mapper: ObjectMapper
) : GlobalExceptionHandler(mapper) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("create.the.user")
    suspend fun create(request: User.CreateRequest) {
        val validatedRequest = validator.create(request)
        log.info("create a user, request parameters: {}", validatedRequest)
        throw BusinessException("custom unknown exception")
//        userService.create(validatedRequest.toEntity())
    }

    @MessageMapping("find.user.by.name")
    suspend fun findByName(username: String): User.Entity? = userService.findByName(username)

    @MessageMapping("find.all.users")
    suspend fun findAll(): Flow<User.Entity> = userService.findAll()

    @MessageMapping("update.user")
    suspend fun update(request: User.UpdateRequest) = userService.update(request)

    @MessageMapping("delete.user")
    suspend fun delete(id: String) = userService.delete(id)
}