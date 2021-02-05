package org.study.account.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.study.account.model.dao.AccountDao
import org.study.account.model.dto.TransferDto

@Service
class AccountService(val accountDao: AccountDao) {
    @Transactional
    suspend fun transfer(request: TransferDto) {
        accountDao.withdrawal(request.toUserId, request.amount)
        accountDao.deposit(request.fromUserId, request.amount)
    }

}