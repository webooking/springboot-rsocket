package org.study.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.stereotype.Controller
import org.study.account.model.Custom
import org.study.account.service.UserService
import org.study.account.validation.validator.ArgumentValidator
import org.study.common.config.BusinessException
import org.study.common.config.GlobalExceptionHandler

@Controller
class UserController(
    val userService: UserService,
    val validator: ArgumentValidator,
    override val mapper: ObjectMapper
) : GlobalExceptionHandler(mapper) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("create.the.user")
    suspend fun create(@AuthenticationPrincipal(expression = "custom") operator: org.study.account.model.auth.Custom,
                       request: Custom.CreateRequest):String {
        validator.validate(request)

        log.info("operator `{}` create a user, request parameters: {}", operator.username, request)
        a(operator.username)
        return "ok"
//        throw BusinessException("custom unknown exception")
//        userService.create(validatedRequest.toEntity())
    }
    suspend fun fetchToken():String = ReactiveSecurityContextHolder.getContext().map {
        (it.authentication.credentials as OAuth2AccessToken).tokenValue
    }.awaitFirst()

    suspend fun a(username: String) {
        delay(100)
        log.info("--a-- $username -- ${fetchToken()}")
        b(username)
        coroutineScope {
            launch {
                c(username)
                d(username)
            }
            launch{
                e(username)
            }
        }
    }
    suspend fun b(username: String) {
        delay(100)
        log.info("--b-- $username --  ${fetchToken()}")
    }
    suspend fun c(username: String) {
        delay(100)
        log.info("--c-- $username --  ${fetchToken()}")
    }
    suspend fun d(username: String) {
        delay(100)
        log.info("--d-- $username --  ${fetchToken()}")
    }
    suspend fun e(username: String) {
        delay(100)
        log.info("--e-- $username --  ${fetchToken()}")
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