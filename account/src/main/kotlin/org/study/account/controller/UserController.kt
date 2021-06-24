package org.study.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.stereotype.Controller
import org.study.account.model.Custom
import org.study.account.model.Gender
import org.study.account.model.Phone
import org.study.account.model.auth.AuthUser
import org.study.account.service.UserService
import org.study.account.validation.validator.ArgumentValidator
import org.study.common.config.GlobalExceptionHandler
import java.util.*

@Controller
class UserController(
    val userService: UserService,
    val validator: ArgumentValidator,
    override val mapper: ObjectMapper,
    val messageSource: MessageSource
) : GlobalExceptionHandler(mapper) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("signUp")
    suspend fun signUp(): Unit {
        log.info("sign up")
//        return "greet"
    }

    @MessageMapping("find.user")
    suspend fun findUser(@AuthenticationPrincipal(expression = "custom") user: org.study.account.model.auth.Custom) = null

    @MessageMapping("stream")
    suspend fun receive(): Flow<Int> = flow {
        (1..3).forEach {
            log.info("current value: $it")
            emit(it)
        }
    }

    @MessageMapping("find.all")
    suspend fun findAll2(): List<Custom.CreateRequest> = (1..10).map { buildUser("1000$it") }
    private fun buildUser(tokenValue: String) = Custom.CreateRequest(
        username = tokenValue,
        age = 18,
        gender = Gender.Male,
        phone = Phone(
            countryCode = "+1",
            number = "7785368920"
        ),
        legs = 1, //腿的个数必须是偶数
        ageBracket = "Adolescent",
        hobbies = listOf("a", "b", "c")
    )

    @MessageMapping("create.the.user")
    suspend fun create(
        @AuthenticationPrincipal(expression = "custom") user: org.study.account.model.auth.Custom,
        request: Custom.CreateRequest
    ) {
        log.info(
            "`{}` create a user, request parameters: {}, message: {}",
            user.username,
            request,
            messageSource.getMessage("custom.error", null, ReactiveSecurityContextHolder.getContext().map {
                (it.authentication.principal as AuthUser).getLocale()
            }.awaitFirst())
        )
        validator.validate(Locale(user.language), request)

        /*
        a(operator.username)
        throw BusinessException(messageSource.getMessage("custom.error", null, ReactiveSecurityContextHolder.getContext().map {
            (it.authentication.principal as AuthUser).getLocale()
        }.awaitFirst()))*/
//        userService.create(validatedRequest.toEntity())
    }

    suspend fun fetchToken(): String = ReactiveSecurityContextHolder.getContext().map {
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
            launch {
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