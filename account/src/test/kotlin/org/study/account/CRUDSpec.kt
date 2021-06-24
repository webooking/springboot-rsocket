package org.study.account

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.StringSpec
import io.rsocket.exceptions.CustomRSocketException
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.collect
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.retrieveFlow
import org.springframework.messaging.rsocket.retrieveMono
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.study.account.config.RSocketConfig
import org.study.account.model.Custom
import org.study.account.model.Custom.CreateRequest
import org.study.account.model.Gender
import org.study.account.model.Phone
import reactor.kotlin.test.test
import java.lang.reflect.ParameterizedType

@SpringBootTest
class CRUDSpec(val requester: RSocketRequester,val mapper:ObjectMapper) : StringSpec({
    "create the user"{
        repeat(10) { index ->
            launch {
                createUser(requester, "10000${index}", log)
            }
        }
//        createUser(requester, "100001", log)
    }
    "anonymous access api"{
        requester
            .route("signUp")
            .retrieveMono(Void::class.java)
            .test()
            .verifyComplete()
    }
    "stream"{
        log.info(Int::class.java.toString())
        requester
            .route("stream")
            .retrieveFlux(Int::class.java)
            .asFlow()
            .buffer(2)
            .collect {
                log.info("received ${it}")
            }
    }
    "list"{
        requester.route("find.all")
            .retrieveMono(List::class.java)
            .collect {
                it.forEach { custom ->
                    log.info((custom as Custom.CreateRequest).username)
                }
            }

    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}

suspend fun createUser(requester: RSocketRequester, tokenValue: String, log: Logger) = requester
    .route("create.the.user")
    .metadata(BearerTokenMetadata(tokenValue), RSocketConfig.MIME_TYPE)
    .data(
        buildUser(tokenValue)
    )
    .retrieveMono(Void::class.java)
    .test()
    .expectErrorMatches { ex ->
        ex is CustomRSocketException
    }
    .verify()
//.awaitFirstOrNull()


private fun buildUser(tokenValue: String) = CreateRequest(
    username = tokenValue,
    age = 18,
    gender = Gender.Male,
    phone = Phone(
        countryCode = "+1",
        number = "7785368920"
    ),
    legs = 1, //腿的个数必须是偶数
    ageBracket = "Adolescent"
)