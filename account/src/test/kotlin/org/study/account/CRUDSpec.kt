package org.study.account

import io.kotest.core.spec.style.StringSpec
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.model.Gender
import org.study.account.model.Phone
import org.study.account.model.User
import reactor.kotlin.test.test
import java.util.*

@SpringBootTest
class CRUDSpec(val requester: RSocketRequester) : StringSpec({
    "create the user"{
        requester
            .route("create.the.user")
            .data(
                User.CreateRequest(
                    username = "yuri",
                    age = 18,
                    gender = Gender.Male,
                    phone = Phone(
                        countryCode = "+1",
                        number = "7785368920"
                    ),
                    legs = 2,
                    ageBracket = "Adolescent"
                )
            )
            .retrieveMono(Void::class.java)
            .test()
            .expectComplete()
            .verify()
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}

object RandomUtil {
    private val random = Random()
    fun generateRandom(max: Int) = random.nextInt(max)
}
