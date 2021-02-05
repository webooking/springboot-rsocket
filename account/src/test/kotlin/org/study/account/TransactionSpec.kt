package org.study.account

import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.model.dto.TransferDto
import reactor.kotlin.test.test

@SpringBootTest
class TransactionSpec(val requester: RSocketRequester) : StringSpec({
    "transfer 100".config(enabled = false) {
        requester
            .route("transfer")
            .data(
                TransferDto(
                    fromUserId = "user001",
                    toUserId = "user002",
                    amount = 100
                )
            )
            .retrieveMono(Void::class.java)
            .test()
            .expectComplete()
            .verify()
    }

    "transfer 10"{
        requester
            .route("transfer")
            .data(
                TransferDto(
                    fromUserId = "user001",
                    toUserId = "user002",
                    amount = 10
                )
            )
            .retrieveMono(Void::class.java)
            .test()
            .expectComplete()
            .verify()
    }
})