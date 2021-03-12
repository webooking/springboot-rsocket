package org.study.account

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
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
        repeat(100) { index ->
            launch {
                createUser(requester, "10000${index}")
            }
        }
    }
    "repeat"{
        repeat(100) {
            log.info("""rsc --fnf --debug  --authBearer "$it" --data '{"username":"$it","age":18,"gender":"Male","phone":{"countryCode":"+1","number":"7785368920"},"legs":2,"ageBracket":"Adolescent"}' --route create.the.user tcp://localhost:7000 &""")
        }
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}

suspend fun createUser(requester: RSocketRequester, tokenValue: String): Void? = requester
    .route("create.the.user")
    .metadata(BearerTokenMetadata(tokenValue), RSocketConfig.MIME_TYPE)
    .data(
        buildUser(tokenValue)
    )
    .retrieveMono(Void::class.java).awaitFirstOrNull()


private fun buildUser(tokenValue: String) = Custom.CreateRequest(
    username = tokenValue,
    age = 18,
    gender = Gender.Male,
    phone = Phone(
        countryCode = "+1",
        number = "7785368920"
    ),
    legs = 2, //腿的个数必须是偶数
    ageBracket = "Adolescent"
)