package org.study.auth.model

import org.study.auth.util.RandomPasswordGenerator
import java.util.*

data class Client(
    val name: String, // account,order
    val id: String = UUID.randomUUID().toString(),
    val secret: String = RandomPasswordGenerator.generate(),
    val apiList: List<String> = listOf(
        API.Get_Authentication,
    )
)

object API {
    const val Generate_Token = "auth.generate.token"
    const val Delete_Token = "auth.delete.token"
    const val Get_Authentication = "auth.get.authentication"
    const val Update_Authentication = "auth.update.authentication"
    const val Refresh_Token = "auth.refresh.token"
}