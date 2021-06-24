package org.study.order

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.rsocket.metadata.WellKnownMimeType
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.springframework.util.MimeTypeUtils
import org.study.order.model.auth.Custom

@SpringBootTest
class ControllerSpec(val requester: RSocketRequester) : StringSpec({
    "create order"{
        requester
            .route("create.order")
            .metadata(BearerTokenMetadata("10001"), MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.string))
            .retrieveMono(Custom::class.java)
            .awaitSingleOrNull().shouldBeNull()
    }

})