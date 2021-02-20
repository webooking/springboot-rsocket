package org.study.auth.model

import org.study.auth.util.RandomPasswordGenerator
import java.util.*

data class Client(
    val name: String, // account,order
    val id: String = UUID.randomUUID().toString(),
    val secret: String = RandomPasswordGenerator.generate(),
    val apiList: List<String> = listOf(
        API.GET_AUTHENTICATION,
    )
)

object API {
    const val GENERATE_TOKEN = "auth.generate.token"
    const val DELETE_TOKEN = "auth.delete.token"
    const val GET_AUTHENTICATION = "auth.get.authentication"
    const val UPDATE_AUTHENTICATION = "auth.update.authentication"
    const val REFRESH_TOKEN = "auth.refresh.token"
}