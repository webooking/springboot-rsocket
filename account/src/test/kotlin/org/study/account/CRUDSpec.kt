package org.study.account

import io.kotest.core.spec.style.StringSpec
import io.rsocket.exceptions.CustomRSocketException
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.study.account.config.RSocketConfig
import org.study.account.model.Custom
import org.study.account.model.Gender
import org.study.account.model.Phone
import reactor.kotlin.test.test

@SpringBootTest
class CRUDSpec(val requester: RSocketRequester) : StringSpec({
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


private fun buildUser(tokenValue: String) = Custom.CreateRequest(
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