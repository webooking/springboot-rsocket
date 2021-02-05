package org.study.account.controller

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.account.model.dto.TransferDto
import org.study.account.service.AccountService

@Controller
class AccountController(val accountService: AccountService) {
    @MessageMapping("transfer")
    suspend fun transfer(request: TransferDto): Unit = accountService.transfer(request)
}