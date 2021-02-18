package org.study.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.study.account.model.Custom
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
    suspend fun create(@AuthenticationPrincipal(expression = "custom") operator: org.study.account.model.auth.Custom, request: Custom.CreateRequest) {
        val validatedRequest = validator.create(request)
        log.info("operator `{}` create a user, request parameters: {}", operator.username, validatedRequest)

        throw BusinessException("custom unknown exception")
//        userService.create(validatedRequest.toEntity())
    }

    @MessageMapping("find.user.by.name")
    suspend fun findByName(username: String): Custom.Entity? = userService.findByName(username)

    @MessageMapping("find.all.users")
    suspend fun findAll(): Flow<Custom.Entity> = userService.findAll()

    @MessageMapping("update.user")
    suspend fun update(request: Custom.UpdateRequest) = userService.update(request)

    @MessageMapping("delete.user")
    suspend fun delete(id: String) = userService.delete(id)
}