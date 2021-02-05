---
typora-root-url: ./assets
---

# 1 什么是事务 Transaction

数据库事务有ACID4个特性：

- A：Atomic，原子性。若干SQL，要么全部执行，要么全部不执行
- C：Consistent，一致性。事务完成后，所有数据的状态都是一致的，即A账户只要减去了100，B账户则必定加上了100；
- I：Isolation，隔离性。多个事务之间，互相独立
- D：Duration，持久性。持久化存储

# 2 story

```
BEGIN;
UPDATE t_account SET balance = balance - 100 WHERE user_id = 'user001';
UPDATE t_account SET balance = balance + 100 WHERE user_id = 'user002';
COMMIT; # 如果抛异常，则，ROLLBACK;
```

# 3 source code

## 3.1 Database

```
DROP TABLE IF EXISTS t_account;

create table t_account (
    id varchar(36)  not null primary key,
    user_id varchar(36) not null,
    balance    int4  not null CHECK(balance >= 0),
    version     bigint default 0,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp
);

insert into t_account(id,user_id,balance) values
 (uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'user001', 90),
 (uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'user002', 0),
 (uuid_in(md5(random()::text || clock_timestamp()::text)::cstring), 'user003', 10);

```

## 3.2 Controller

```
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
```

## 3.3 Service

```
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
```

## 3.4 Dao

```
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
```

## 3.5 Testing

```
package org.study.account

import io.kotest.core.spec.style.StringSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.model.dto.TransferDto
import reactor.kotlin.test.test

@SpringBootTest
class TransactionSpec(val requester: RSocketRequester) :StringSpec({
    "transfer 100"{
        requester
            .route("transfer")
            .data(TransferDto(
                fromUserId = "user001",
                toUserId = "user002",
                amount = 100
            ))
            .retrieveMono(Void::class.java)
            .test()
            .expectComplete()
            .verify()
    }

    "transfer 10"{
        requester
            .route("transfer")
            .data(TransferDto(
                fromUserId = "user001",
                toUserId = "user002",
                amount = 10
            ))
            .retrieveMono(Void::class.java)
            .test()
            .expectComplete()
            .verify()
    }
})
```



