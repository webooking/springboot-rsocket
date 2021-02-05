package org.study.account.model.dto

data class TransferDto(
    val fromUserId: String,
    val toUserId: String,
    val amount: Int,
)