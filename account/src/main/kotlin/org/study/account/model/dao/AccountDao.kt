package org.study.account.model.dao

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository

@Repository
class AccountDao(val template: R2dbcEntityTemplate) {
    suspend fun deposit(fromUserId: String, amount: Int) {
        template.databaseClient.sql("UPDATE t_account SET balance = balance - :amount WHERE user_id = :userId")
            .bind("amount", amount)
            .bind("userId", fromUserId)
            .fetch().rowsUpdated().awaitFirst()
    }

    suspend fun withdrawal(toUserId: String, amount: Int) {
        template.databaseClient.sql("UPDATE t_account SET balance = balance + :amount WHERE user_id = :userId")
            .bind("amount", amount)
            .bind("userId", toUserId)
            .fetch().rowsUpdated().awaitFirst()
    }

}