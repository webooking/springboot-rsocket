package org.study.account

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.model.Gender
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
                    age = 34,
                    gender = Gender.Male
                )
            )
            .retrieveMono(Void::class.java)
            .test()
            .expectComplete()
            .verify()
    }
    "find user by name"{
        requester
            .route("find.user.by.name")
            .data("yuri")
            .retrieveMono(User.Entity::class.java)
            .test()
            .expectNextMatches {
                log.info("retrieve value from RSocket mapping: {}", it)
                it.age == 34
            }
            .expectComplete()
            .verify()
    }
    "update"{
        val old = requester
            .route("find.user.by.name")
            .data("yuri")
            .retrieveMono(User.Entity::class.java)
            .awaitFirst()

        val data = User.UpdateRequest(
            id = old.id,
            gender = Gender.Neutral,
            version = old.version
        )
        requester
            .route("update.user")
            .data(data)
            .retrieveMono(Void::class.java)
            .test()
            .expectComplete()
            .verify()
    }
    "delete the user by id"{
        val old = requester
            .route("find.user.by.name")
            .data("yuri")
            .retrieveMono(User.Entity::class.java)
            .awaitFirst()

        requester
            .route("delete.user")
            .data(old.id)
            .retrieveMono(Void::class.java)
            .test()
            .expectComplete()
            .verify()
    }
    "insert 30 records"{
        val usernameList = listOf(
            "Emma",
            "Olivia",
            "Ava",
            "Isabella",
            "Sophia",
            "Mia",
            "Charlotte",
            "Amelia",
            "Evelyn",
            "Abigail",
            "Harper",
            "Emily",
            "Elizabeth",
            "Avery",
            "Sofia",
            "Ella",
            "Madison",
            "Scarlett",
            "Victoria",
            "Aria",
            "Grace",
            "Chloe",
            "Camila",
            "Penelope",
            "Riley",
            "Layla",
            "Lillian",
            "Nora",
            "Zoey",
            "Mila",
        )
        usernameList.forEach { username ->
            requester
                .route("create.the.user")
                .data(
                    User.CreateRequest(
                        username = username,
                        age = RandomUtil.generateRandom(100),
                        gender = Gender.values()[RandomUtil.generateRandom(3)]
                    )
                )
                .retrieveMono(Void::class.java)
                .test()
                .expectComplete()
                .verify()
        }
    }
    "find all users"{
        requester
            .route("find.all.users")
            .retrieveFlux(User.Entity::class.java)
            .buffer(10)
            .test()
            .expectNextMatches { list ->
                log.info("top 10: {}", list)
                list.size == 10
            }.thenCancel()
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
