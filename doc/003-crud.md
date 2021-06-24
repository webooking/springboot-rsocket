---
typora-root-url: ./assets
---

# 1 ORM

## 1.1 什么是ORM

ORM全称是：Object Relational Mapping(对象关系映射)。

## 1.2 r2dbc

Reactive Relational Database Connectivity

# 2 配置开发环境

## 2.1 model
- DTO
Data Transfer Object

- DAO
Data access object

- Entity
- VO
View Object

![image-20210201023751700](/image-20210201023751700.png)



## 2.2 docker ce



## 2.3 postgresql

![preview](/v2-6ea814c44f127504e82a538c0d8d7237_r.jpg)

```
1. 下载postgresql镜像
docker pull postgres:13.2-alpine

2. 查看镜像文件
docker images --filter "reference=postgres"
REPOSITORY   TAG           IMAGE ID       CREATED      SIZE
postgres     13.2-alpine   e07060185412   3 days ago   160MB

3. 使用镜像
docker container run --name postgres -e POSTGRES_PASSWORD=mysecretpassword -d -p 5432:5432 postgres:13.2-alpine

```

## 2.4 配置PostgreSQL

```
1. 查看正在运行的容器
docker container ls -a
CONTAINER ID   IMAGE                    STATUS          PORTS      NAMES
487cb17f2e2a   postgres:13.2-alpine     Up 15 minutes   0.0.0.0:5432->5432/tcp   postgres

2. 进入容器
docker container exec -it postgres bash

3. 进入默认用户postgres
bash-5.1# psql -U postgres
psql (13.2)
Type "help" for help.

4. 查看数据库
postgres=# \l
                                 List of databases
   Name    |  Owner   | Encoding |  Collate   |   Ctype    |   Access privileges   
-----------+----------+----------+------------+------------+-----------------------
 postgres  | postgres | UTF8     | en_US.utf8 | en_US.utf8 | 
 template0 | postgres | UTF8     | en_US.utf8 | en_US.utf8 | =c/postgres          +
           |          |          |            |            | postgres=CTc/postgres
 template1 | postgres | UTF8     | en_US.utf8 | en_US.utf8 | =c/postgres          +
           |          |          |            |            | postgres=CTc/postgres
(3 rows)

5. 创建测试用的数据库rsocket
postgres=# create USER admin with PASSWORD '123456';
CREATE ROLE
postgres=# create database rsocket owner admin;
CREATE DATABASE
postgres=# grant all privileges on database rsocket to admin;
GRANT
postgres=# alter user admin with CREATEROLE;
ALTER ROLE

6. 切换database & user
postgres=# \q
bash-5.1# psql -d rsocket -U admin;
psql (12.5)
Type "help" for help.

rsocket=> select * from pg_user;
 usename  | usesysid | usecreatedb | usesuper | userepl | usebypassrls |  passwd  | valuntil | useconfig 
----------+----------+-------------+----------+---------+--------------+----------+----------+-----------
 postgres |       10 | t           | t        | t       | t            | ******** |          | 
 admin    |    16398 | f           | f        | f       | f            | ******** |          | 
(2 rows)

rsocket=> \du
                                   List of roles
 Role name |                         Attributes                         | Member of 
-----------+------------------------------------------------------------+-----------
 admin     | Create role                                                | {}
 postgres  | Superuser, Create role, Create DB, Replication, Bypass RLS | {}


7. 为account和order分别创建独立的schema
rsocket=> create user account with password '123456';
CREATE ROLE
rsocket=> create user orders with password '123456';
CREATE ROLE
rsocket=> create schema account;
CREATE SCHEMA
rsocket=> create schema orders;
CREATE SCHEMA
rsocket=> 
rsocket=> grant all privileges on schema account to account;
GRANT
rsocket=> grant all privileges on schema orders to orders;
GRANT
rsocket=> grant usage on schema account to orders;
GRANT
rsocket=> grant usage on schema orders to account;
GRANT

```

## 2.5 intellij idea database tools

![image-20210201045541194](/image-20210201045541194.png)



![image-20210201144510663](/image-20210201144510663.png)

## 2.6 timezone

### 2.6.1 database

```
1. 进入容器
docker container exec -it postgres bash

2. 使用user account访问database rsocket
bash-5.1# psql -d rsocket -U account;

3. 查看默认的时区
rsocket=> show timezone;
 TimeZone 
----------
 UTC
(1 row)
```

### 2.6.2 docker container

以alpine linux为例：

```
bash-5.1# cat /proc/version
Linux version 4.19.121-linuxkit (root@buildkitsandbox) (gcc version 9.2.0 (Alpine 9.2.0)) #1 SMP Tue Dec 1 17:50:32 UTC 2020
bash-5.1# date -R
Tue, 02 Feb 2021 04:57:26 +0000
```


### 2.6.3 JVM

 ```
java -Duser.timezone=UTC -jar app.jar
 ```

### 2.6.4 springboot

```
TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
```

### 2.6.5 entity

| Database  | 描述           | 示例                        | Entity        | Vo     | DTO                          |
| --------- | -------------- | --------------------------- | ------------- | ------ | ---------------------------- |
| Timestamp | 日期+时间      | 2021-02-02T05:27:40.948881Z | LocalDateTime | String | datetimeFormat<br />timezone |
| Date      | 日期           | 2013-05-17                  | LocalDate     | String |                              |
| Time      | 一天内的时间点 | 12:52:51                    | LocalTime     | String |                              |



### 2.6.6 JSON encoder and decoder

```
val utcZone = ZoneId.of("UTC")
val beijingZone = ZoneId.of("Asia/Shanghai")
val vancouverZone = ZoneId.of("America/Vancouver")

TimeZone.setDefault(TimeZone.getTimeZone(utcZone))

val now = LocalDateTime.now()
val vancouver = now.atZone(utcZone)
			.withZoneSameInstant(vancouverZone)
			.format(DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss Z"))
println(vancouver) // 02/01/2021 - 22:46:29 -0800

val beijing = now.atZone(utcZone)
			.withZoneSameInstant(beijingZone)
			.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z"))
println(beijing) // 2021-02-02 14:46:29 +0800
```


# 3 单表操作

## 3.1 dependencies

```
implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
runtimeOnly("io.r2dbc:r2dbc-postgresql")
```

## 3.2 init database

```
1. 新建目录 resources/db/migration/V1_init.sql
2. SQL
DROP Type IF EXISTS gender;
DROP TABLE IF EXISTS t_user;

CREATE TYPE gender AS ENUM ('Male', 'Female', 'Neutral');
create table t_user (
    id varchar(36)  not null constraint t_user_pk primary key,
    username    varchar(50)  not null,
    age         smallint not null,
    gender      gender not null,
    create_time timestamp default CURRENT_TIMESTAMP not null,
    update_time timestamp
);
```

## 3.3 configuration

```
1. application-dev.yml
spring:
  r2dbc:
    url: r2dbc:pool:postgresql://127.0.0.1:5432/rsocket?currentSchema=account
    username: account
    password: 123456
    pool:
      enabled: true
      initial-size: 1
      max-size: 3

2. ConnectionFactory

package org.study.account.config

import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories


@Configuration
@EnableR2dbcRepositories
class DatabaseConfig(val properties: R2dbcProperties) {
    @Bean(destroyMethod = "dispose")
    @Primary
    fun connectionPool(): ConnectionPool {
        val connectionFactoryOptions = connectionFactoryOptions()
        val connectionFactory = ConnectionFactories.get(connectionFactoryOptions)

        val configuration = ConnectionPoolConfiguration.builder(connectionFactory)
            .maxIdleTime(properties.pool.maxIdleTime)
            .initialSize(properties.pool.initialSize)
            .maxSize(properties.pool.maxSize)
            .build()
        return ConnectionPool(configuration)
    }

    private fun connectionFactoryOptions(): ConnectionFactoryOptions {
        val optionsFromUrl = ConnectionFactoryOptions.parse(properties.url)
        return ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DATABASE, optionsFromUrl.getRequiredValue(ConnectionFactoryOptions.DATABASE))
            .option(ConnectionFactoryOptions.DRIVER, optionsFromUrl.getRequiredValue(ConnectionFactoryOptions.DRIVER))
            .option(ConnectionFactoryOptions.HOST, optionsFromUrl.getRequiredValue(ConnectionFactoryOptions.HOST))
            .option(ConnectionFactoryOptions.PORT, optionsFromUrl.getRequiredValue(ConnectionFactoryOptions.PORT))
            .option(ConnectionFactoryOptions.PROTOCOL, optionsFromUrl.getRequiredValue(ConnectionFactoryOptions.PROTOCOL))
            .option(ConnectionFactoryOptions.USER, properties.username)
            .option(ConnectionFactoryOptions.PASSWORD, properties.password)
            .build()
    }
}

```

## 3.4 CURD

### 3.4.1 Model

```
package org.study.account.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.sql.SqlIdentifier
import java.time.LocalDateTime
import java.util.*
import org.springframework.data.relational.core.query.Update as DBUpdate

enum class Gender {
    Male, Female, Neutral
}

sealed class User {
    data class CreateRequest(
        val username: String,
        val age: Int,
        val gender: Gender,
    ) {
        fun toDto() = CreateDto(
            username = username,
            age = age,
            gender = gender.name
        )
    }

    @Table("t_user")
    data class CreateDto(
        @Id val id: String = UUID.randomUUID().toString(),
        @Column("username") val username: String,
        @Column("age") val age: Int,
        @Column("gender") val gender: String,
    )

    @Table("t_user")
    data class Find(
        @Id val id: String,
        @Column("username") val username: String,
        @Column("age") val age: Int,
        @Column("gender") val gender: Gender,
        @Column("version") val version: Long,
        @Column("create_time") val createTime: LocalDateTime,
        @Column("update_time") val updateTime: LocalDateTime? = null,
    )

    data class UpdateRequest(
        val id: String,
        val username: String? = null,
        val age: Int? = null,
        val gender: Gender? = null,
        val version: Long,
    ) {
        private fun shouldBeUpdated() = username != null || age != null || gender != null
        fun toDto(): UpdateDto {
            if (!shouldBeUpdated()) {
                throw RuntimeException("Parameter error, no data need to be modified")
            }
            return UpdateDto(
                id = id,
                username = username,
                age = age,
                gender = gender?.name,
                version = version
            )
        }
    }

    @Table("t_user")
    data class UpdateDto(
        @Id val id: String,
        @Column("username") val username: String? = null,
        @Column("age") val age: Int? = null,
        @Column("gender") val gender: String? = null,
        @Column("version") val version: Long,
        @Column("update_time") val updateTime: LocalDateTime = LocalDateTime.now(),
    ) {
        fun toUpdate(): DBUpdate {
            val map = mutableMapOf<SqlIdentifier, Any>()

            map[SqlIdentifier.unquoted("version")] = version + 1
            map[SqlIdentifier.unquoted("update_time")] = updateTime
            if (username != null && username.isNotBlank()) {
                map[SqlIdentifier.unquoted("username")] = username
            }
            if (age != null) {
                map[SqlIdentifier.unquoted("age")] = age
            }
            if (gender != null && gender.isNotBlank()) {
                map[SqlIdentifier.unquoted("gender")] = gender
            }
            return DBUpdate.from(map)
        }
    }
}
```

### 3.4.2 Controller

- C, create a user
- R
  - read  a user
  - read 1..N users 
- U, update
- D, delete

```
package org.study.account.controller

import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import org.study.account.model.User
import org.study.account.service.UserService

@Controller
class UserController(val userService: UserService) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("create.the.user")
    suspend fun create(request: User.CreateRequest) {
        log.info("create a user, request parameters: {}", request)
        userService.create(request.toDto())
    }

    @MessageMapping("find.user.by.name")
    suspend fun findByName(username: String): User.Find? = userService.findByName(username)

    @MessageMapping("find.all.users")
    suspend fun findAll(): Flow<User.Find> = userService.findAll()

    @MessageMapping("update.user")
    suspend fun update(request: User.UpdateRequest) = userService.update(request.toDto())

    @MessageMapping("delete.user")
    suspend fun delete(id: String) = userService.delete(id)
}
```

### 3.4.3 Service

```
package org.study.account.service

import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.study.account.model.User
import org.study.account.model.dao.UserDao

@Service
class UserService(val dao: UserDao) {
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun create(dto: User.CreateDto) {
        log.info("create the user, dto: {}", dto)
        dao.create(dto)
    }

    suspend fun findByName(username: String): User.Find? = dao.findByName(username)
    suspend fun update(dto: User.UpdateDto) {
        dao.update(dto)
    }

    suspend fun delete(id: String) {
        dao.delete(id)
    }

    suspend fun findAll(): Flow<User.Find> = dao.findAll()
}
```

### 3.4.4 Dao

```
package org.study.account.model.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.allAndAwait
import org.springframework.data.r2dbc.core.flow
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query.query
import org.springframework.stereotype.Repository
import org.study.account.model.User

@Repository
class UserDao(val template: R2dbcEntityTemplate) {
    suspend fun create(dto: User.CreateDto): User.CreateDto? = template.insert(User.CreateDto::class.java).using(dto).awaitSingleOrNull()
    suspend fun findByName(username: String): User.Find? =
        template.selectOne(query(where("username").`is`(username)), User.Find::class.java).awaitFirstOrNull()

    suspend fun update(dto: User.UpdateDto): Int = template.update(User.UpdateDto::class.java).matching(
        query(
            where("id").`is`(dto.id)
                .and("version").`is`(dto.version)
        )
    ).apply(dto.toUpdate()).awaitFirst()

    suspend fun delete(id: String) = template.delete(User.Find::class.java).matching(
        query(where("id").`is`(id))
    ).allAndAwait()

    fun findAll(): Flow<User.Find> = template.select(User.Find::class.java).flow()
}
```

### 3.4.5 Testing

```
package org.study.account

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.retrieveFlow
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
            .retrieveMono(User.Find::class.java)
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
            .retrieveMono(User.Find::class.java)
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
            .retrieveMono(User.Find::class.java)
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
                        gender = Gender.values()[RandomUtil.generateRandom(2)]
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
            .retrieveFlux(User.Find::class.java)
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

```

## 3.5 Refactoring Model

```
package org.study.account.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.relational.core.sql.SqlIdentifier
import java.time.LocalDateTime
import java.util.*
import org.springframework.data.relational.core.query.Update as Query

enum class Gender {
    Male, Female, Neutral
}

sealed class User {
    data class CreateRequest(
        val username: String,
        val age: Int,
        val gender: Gender,
    ) {
        fun toEntity() = Entity(
            id = UUID.randomUUID().toString(),
            username = username,
            age = age,
            gender = gender.name,
            version = 0L,
        )
    }

    @Table("t_user")
    data class Entity(
        @Id val id: String,
        @Column("username") val username: String,
        @Column("age") val age: Int,
        @Column("gender") val gender: String,
        @Column("version") val version: Long,
        @Column("create_time") val createTime: LocalDateTime? = null,
        @Column("update_time") val updateTime: LocalDateTime? = null,
    )

    data class UpdateRequest(
        val id: String,
        val username: String? = null,
        val age: Int? = null,
        val gender: Gender? = null,
        val version: Long,
    ) {
        private fun shouldBeUpdated() = username != null || age != null || gender != null
        fun toQuery(): Query {
            if (!shouldBeUpdated()) {
                throw RuntimeException("Parameter error, no data need to be modified")
            }
            val map = mutableMapOf<SqlIdentifier, Any>()

            map[SqlIdentifier.unquoted("version")] = version + 1
            map[SqlIdentifier.unquoted("update_time")] = LocalDateTime.now()
            if (username != null && username.isNotBlank()) {
                map[SqlIdentifier.unquoted("username")] = username
            }
            if (age != null) {
                map[SqlIdentifier.unquoted("age")] = age
            }
            if (gender != null) {
                map[SqlIdentifier.unquoted("gender")] = gender.name
            }
            return Query.from(map)
        }
    }
}
```

# 4 多表操作

1. 关系

- 一对一，一个用户只有一个token

- 一对多，一个用户可以绑定多张银行卡

2. ~~外键~~

# 5 复杂查询



