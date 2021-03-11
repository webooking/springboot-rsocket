package org.study.auth

import io.kotest.core.spec.style.StringSpec
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.auth.model.API
import org.study.auth.model.Admin
import org.study.auth.model.request.ClientRequest
import org.study.auth.model.request.GenerateTokenRequest
import org.study.auth.model.vo.UserAndToken
import reactor.kotlin.test.test

@SpringBootTest
class AuthControllerSpec(val requester: RSocketRequester) : StringSpec({
    "generate token"{
        requester
            .route(API.Generate_Token)
            .data(
                GenerateTokenRequest(
                    client = ClientRequest(
                        "account",
                        "e70c3dde-55fb-491a-bb3f-0dffbc632f2e",
                        "8I>y]?sDY7{4[a!+i90@vsh"
                    ),
                    user = Admin(
                        username = "admin001"
                    ),
                )
            )
            .retrieveMono(UserAndToken::class.java)
            .test()
            .expectNextMatches {
                log.info("retrieve value from RSocket mapping: {}", it)
                it is UserAndToken
            }
            .expectComplete()
            .verify()
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}