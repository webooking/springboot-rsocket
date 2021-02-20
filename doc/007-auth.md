---
typora-root-url: ./assets
---

# 1 What is Security

- 你是谁
- 能做什么

## 1.1 你是谁

如何识别“你是谁”？

| 方法                           | 验证流程 |
| ------------------------------ | ---- |
| 用户名 + 密码                  | 1. 用户注册时，密码会被加密保存。比如，BCrypt64算法，很难暴力破解<br />2. 用户登录时，填写：用户名+密码（明文，也只能是明文）<br />3. 通过加密通道，比如https传输数据到后台<br />4. 验证用户名和密码<br />5. 验证通过，获得用户的身份信息（authentication）<br />6. 存储`Access Token + authentication` |
| 手机号（邮箱） + 验证码 + 密码 |      |
| google，facebook等第三方登录  | 1. 服务端（后台）预先在这些平台注册<br />2. google，facebook等第三方服务，会返回：认证URL，client_id，client_secret<br />3. 用户页面会被导向google，facebook等第三方服务提供的页面<br />4. 用户如果信任App，则会点击“授权通过”，页面会再次跳回App，<br />同时，拿到`Access Token`<br />5. 后台使用第三方平台发放的认证URL，将<br />`client_id + client_secret + Access Token`发送出去<br />6. 验证通过，则可以获取到用户的身份信息（authentication）<br />7. 在后台存储`client_id + AccessToken + authentication` |

## 1.2 能做什么

- 前端页面展示的菜单，按钮，图片，下拉框等资源
- 后台开放的接口

如何识别用户是否有权限操作这些资源？

1. 权限，就是用户与资源之间的连线。连在一起，就有权限

2. 权限的规则是管理员配置好的，用户认证通过后，可以将`Access Token` 看作`key`，对应的value有两块儿：

   - authentication，用户的身份信息
   - authorization，用户与资源之间的连线

于是，根据`Access Token`就可以判断用户的权限了

## 1.3 什么是token



## 1.4 什么是RBAC模型

![1536889025557.png](/1536889025557.png)

- 用户：真实存在的人
- 角色
  - 从业务的角度，对用户分组。比如：管理员、代理、会员、游客等；
  - 从功能的角度，也可以对用户分组，比如：readOnly、write等
- 资源 ：后端提供的接口、网页、图片、文件等，都是资源

## 1.5 OAuth2

为了方便描述OAuth的授权流程，我将系统拆分成以下几个模块：

- App，iOS/Android/WAP应用程序
- auth，授权服务器
- account，管理用户的数据，开放注册，登录，修改用户信息等接口
- order。订单的CRUD；调用account的接口，展示订单关联的用户信息

![运行流程](/1460000013467127.png)



| 概念/专有名词        | 描述                 | 备注                                                         |
| -------------------- | -------------------- | ------------------------------------------------------------ |
| Resource Owner       | 资源持有者，即：用户 |                                                              |
| Authorization Server | 授权服务器，即：auth | 1. 为了安全与性能，auth的数据直接放缓存里<br />2. 也可以使用第三方的授权服务器，比如google，facebook，github等 |
| Resource Server      | 资源提供者           | account，order                                               |
| Client               | 资源调用者           | App，account，order                                          |
| Authentication       | 用户身份信息         | 用户名，密码，手机号，验证码，邮箱，一次性密钥等             |
| Access Token         |                      | 默认生效时间：1周                                            |
| Refresh Token        |                      | 默认生效时间：1月                                            |

![image-20210208174011178](/image-20210208174011178.png)



# 2 Access Protected API

安全是比较独立的一套知识体系，先改造account，要求：

- 登录，注册，刷新token，允许直接访问
- 其他接口，没有token，不允许访问

## 2.1 dependencies

```
implementation("org.springframework.boot:spring-boot-starter-security"){
    exclude(module = "spring-security-web")
}
implementation("org.springframework.security:spring-security-messaging")
implementation("org.springframework.security:spring-security-rsocket")
implementation("org.springframework.security:spring-security-oauth2-resource-server"){
    exclude(module = "spring-security-web")
    exclude(module = "spring-web")
}
```

## 2.2 server 

###  2.2.1 security configuration

```
package org.study.account.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity.AuthorizePayloadsSpec
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import reactor.core.publisher.Mono


@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
class RSocketSecurityConfig {
    @Bean
    fun mapReactiveUserDetailsService(): MapReactiveUserDetailsService {
        fun buildUser(username: String, password: String, role: String) =
            User.withUsername(username).password(password).roles(role).build()

        return MapReactiveUserDetailsService(
            User.withUsername("user").password("user").roles("USER").build(),
            User.withUsername("admin").password("admin").roles("USER").build()
        )
    }

    @Bean
    fun messageHandler(strategies: RSocketStrategies) = RSocketMessageHandler().apply {
        argumentResolverConfigurer.addCustomResolver(AuthenticationPrincipalArgumentResolver())
        rSocketStrategies = strategies
    }

    @Bean
    fun authorization(security: RSocketSecurity): PayloadSocketAcceptorInterceptor {
        security.authorizePayload { authorize: AuthorizePayloadsSpec ->
            authorize
                .route("signin").permitAll()
                .route("create.the.user").hasRole("USER")
                .anyRequest().authenticated()
                .anyExchange().permitAll()
        }
            .simpleAuthentication { simple ->
                simple.authenticationManager { authentication ->
                    val accessToken = authentication.name
                    val user = User.withUsername("user123456").password("").roles("USER").build()

                    Mono.just(
                        UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.authorities
                        )
                    )
                }
            }
        return security.build()
    }

}
```

### 2.2.2 Protected API

```
package org.study.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.study.account.model.Custom
import org.study.account.service.UserService
import org.study.account.validation.validator.UserControllerValidator
import org.study.common.config.BusinessException
import org.study.common.config.GlobalExceptionHandler

@Controller
class UserController(
    val userService: UserService,
    val validator: UserControllerValidator,
    override val mapper: ObjectMapper
) : GlobalExceptionHandler(mapper) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("create.the.user")
    suspend fun create(@AuthenticationPrincipal operator: UserDetails, request: Custom.CreateRequest) {
        val validatedRequest = validator.create(request)
        log.info("operator `{}` create a user, request parameters: {}", operator.username, validatedRequest)
        throw BusinessException("custom unknown exception")
//        userService.create(validatedRequest.toEntity())
    }
}
```



## 2.3 client 

### 2.3.1 security configuration

```
package org.study.account.config

import io.rsocket.metadata.WellKnownMimeType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.cbor.Jackson2CborDecoder
import org.springframework.http.codec.cbor.Jackson2CborEncoder
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.security.rsocket.metadata.BearerTokenAuthenticationEncoder
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.springframework.util.MimeTypeUtils


@Configuration
class RSocketConfig {

    @Bean
    fun requester(
        builder: RSocketRequester.Builder,
        @Value("\${spring.rsocket.server.port}") port: Int
    ): RSocketRequester = builder.tcp("localhost", port)

    @Bean
    fun rSocketStrategies(): RSocketStrategies {
        return RSocketStrategies.builder()
            .encoders {
                it.add(BearerTokenAuthenticationEncoder())
                it.add(Jackson2CborEncoder())
            }
            .decoders {
                it.add(Jackson2CborDecoder())
            }
            .build()
    }

    companion object {
        val TOKEN = BearerTokenMetadata("123456")
        val MIME_TYPE = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.string)
    }
}
```

### 2.3.2 Access Protected API

```
package org.study.account

import io.kotest.core.spec.style.StringSpec
import io.rsocket.exceptions.CustomRSocketException
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import org.study.account.config.RSocketConfig
import org.study.account.model.Custom
import org.study.account.model.Gender
import org.study.account.model.Phone
import reactor.kotlin.test.test

@SpringBootTest
class CRUDSpec(val requester: RSocketRequester) : StringSpec({
    "create the user"{
        requester
            .route("create.the.user")
            .metadata(RSocketConfig.TOKEN, RSocketConfig.MIME_TYPE)
            .data(
                Custom.CreateRequest(
                    username = "yuri",
                    age = 18,
                    gender = Gender.Male,
                    phone = Phone(
                        countryCode = "+1",
                        number = "7785368920"
                    ),
                    legs = 2, //腿的个数必须是偶数
                    ageBracket = "Adolescent"
                )
            )
            .retrieveMono(Void::class.java)
            .test()
//            .expectErrorMatches { ex ->
//                ex is RejectedSetupException
//            }
            .expectError(CustomRSocketException::class.java)
//            .expectComplete()
            .verify()
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
```



# 3 Refactoring

针对权限管理的特殊性，个性化定制`UserDetails`

- ```
  authorities = emptyList()
  ```

- 去掉role。取而代之的是，每个角色一个model，比如：Admin，Custom

于是，配置接口的权限时，就不需要判断角色了，而是，直接拿model。从token到Model (Admin/Custom)的转换过程，是本节重构的重点

## 3.1 security configuration

```
package org.study.account.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity.AuthorizePayloadsSpec
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor
import org.study.account.model.auth.Custom
import reactor.core.publisher.Mono
import java.time.Instant


@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
class RSocketSecurityConfig {

    @Bean
    fun messageHandler(strategies: RSocketStrategies) = RSocketMessageHandler().apply {
        argumentResolverConfigurer.addCustomResolver(AuthenticationPrincipalArgumentResolver())
        rSocketStrategies = strategies
    }

    @Bean
    fun authorization(security: RSocketSecurity): PayloadSocketAcceptorInterceptor {
        security.authorizePayload { authorize: AuthorizePayloadsSpec ->
            authorize
                .route("signin").permitAll()
                .anyRequest().authenticated()
                .anyExchange().permitAll()
        }
            .simpleAuthentication { simple ->
                simple.authenticationManager { authentication ->
                    val accessToken = authentication.name
                    val user = Custom(
                        "user001",
                        "user123456",
                        "1374567890",
                        "yuri@qq.com"
                    ).toAuthUser()

                    Mono.just(
                        BearerTokenAuthentication(
                            user,
                            OAuth2AccessToken(
                                OAuth2AccessToken.TokenType.BEARER,
                                accessToken,
                                Instant.now(),
                                Instant.now().plusSeconds(100)
                            ),
                            emptyList(),
                        )
                    )
                }
            }
        return security.build()
    }

}
```



## 3.2 Protected API

```
package org.study.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.study.account.model.Custom
import org.study.account.service.UserService
import org.study.account.validation.validator.UserControllerValidator
import org.study.common.config.BusinessException
import org.study.common.config.GlobalExceptionHandler

@Controller
class UserController(
    val userService: UserService,
    val validator: UserControllerValidator,
    override val mapper: ObjectMapper
) : GlobalExceptionHandler(mapper) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @MessageMapping("create.the.user")
    suspend fun create(@AuthenticationPrincipal(expression = "custom") operator: org.study.account.model.auth.Custom, request: Custom.CreateRequest) {
        val validatedRequest = validator.create(request)
        log.info("operator `{}` create a user, request parameters: {}", operator.username, validatedRequest)

        throw BusinessException("custom unknown exception")
//        userService.create(validatedRequest.toEntity())
    }
}
```

## 3.3 Custom UserDetails

```
package org.study.account.model.auth

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

data class AuthUser(
    private val attributes: Map<String, Any>
) : OAuth2User {
    override fun getName(): String = attributes["username"] as String

    override fun getAttributes(): Map<String, Any> = attributes

    override fun getAuthorities(): List<out GrantedAuthority> = emptyList()

    fun getAdmin() = Admin(
        id = attributes["id"] as String,
        username = attributes["username"] as String,
    )

    fun getCustom() = Custom(
        id = attributes["id"] as String,
        username = attributes["username"] as String,
        phone = attributes["phone"] as String,
        email = attributes["email"] as String,
    )
}

data class Admin(
    val id: String,
    val username: String,
){
    fun toAuthUser() = AuthUser(
        mapOf(
            "id" to id,
            "username" to username,
        )
    )
}

data class Custom(
    val id: String,
    val username: String,
    val phone: String,
    val email: String,
){
    fun toAuthUser() = AuthUser(
        mapOf(
            "id" to id,
            "username" to username,
            "phone" to phone,
            "email" to email,
        )
    )
}
```

## 3.4 Transform token between rsocket services

```
val token = ReactiveSecurityContextHolder.getContext().map { 
  it.authentication.credentials as OAuth2AccessToken 
}.awaitFirst()
log.info("-------token: {}", token.tokenValue)
```

# 4 signin

## 4.1 auth

重构account的服务，将与token相关的功能移入`auth`中。`auth`开放的接口如下：

| endpoint                   | Desc                                                         | Input                                | Output                                           |
| -------------------------- | ------------------------------------------------------------ | ------------------------------------ | ------------------------------------------------ |
| auth.generate.token        | 发放token                                                    | clientId, clientSecret, UserDetails  | UserDetails，<br />AccessToken<br />RefreshToken |
| auth.delete.token          | 清理token相关的所有缓存                                      | clientId, clientSecret, token        |                                                  |
| auth.get.authentication    | 拿token换UserDetails                                         | clientId, clientSecret,token         | UserDetails                                      |
| auth.update.authentication | 1. 更新（缓存中的）UserDetails<br />2. 删除AccessToken<br />3. 抛异常：AccessToken过期 | clientId, clientSecret, UserDetails  |                                                  |
| auth.refresh.token         | 刷新token<br />1. 生成新的accessToken<br />2. 重新加载UserDetails | clientId, clientSecret, refreshToken | UserDetails，AccessToken                         |



## 4.2 Spring Data Redis Reactive

### 4.2.1 Deploy and Run redis in docker

```
# 1 下载redis镜像
docker image pull redis:rc-alpine3.13

# 2 创建redis需要的volume
yuri@yuris-MBP ~ % docker volume create redis
redis

# 3 启动redis container
docker container run --name redis \
--mount type=volume,source=redis,target=/data \
-p 6379:6379 \
-d redis:rc-alpine3.13 \
redis-server --appendonly yes

# 4 测试
yuri@yuris-MBP ~ % docker container exec -it redis sh  
/data # redis-cli
127.0.0.1:6379> keys *
1) "access_token_001"
127.0.0.1:6379> 
```

### 4.2.2 dependencies

```
implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
```

### 4.2.3 build.gradle.kts

```
@file:Suppress("SpellCheckingInspection")

plugins {
    idea
    id("org.springframework.boot") version "2.4.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"

    kotlin("jvm") version "1.4.21"
    kotlin("plugin.spring") version "1.4.21"
}

idea {
    module {
        isDownloadJavadoc = false
        isDownloadSources = true
    }
}

group = "org.study"
version = "1.0.0"

repositories {
    mavenCentral()
    maven(url = "http://localhost:5433/repository/rsocket/")
}

dependencies {
    val kotestVersion = "4.4.0.RC2"
    val springmockkVersion = "3.0.1"
    val commonVersion = "1.0.0"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.springframework.boot:spring-boot-starter-rsocket")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.study:common:$commonVersion")

    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
        exclude(module = "mockito-core")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.ninja-squad:springmockk:$springmockkVersion")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-spring-jvm:$kotestVersion")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }
    withType<Test> {
        useJUnitPlatform()
    }
    withType<Wrapper> {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "6.8.1"
    }
}
```



### 4.2.4 application.yml

```
spring:
  redis:
    host: localhost
    port: 6379
```

### 4.2.5 Controller

```
package org.study.auth.controller

import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class AuthController(val strTemplate: ReactiveStringRedisTemplate) {
    @MessageMapping("auth.generate.token")
    suspend fun generateToken(): String {
        val key = "access_token_001"
        val tokenValue = UUID.randomUUID().toString()
        strTemplate.opsForValue().set(key, tokenValue).awaitFirst()
        return strTemplate.opsForValue().get(key).awaitFirst()
    }
}
```

### 4.2.6 Testing

```
package org.study.auth

import io.kotest.core.spec.style.StringSpec
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.rsocket.RSocketRequester
import reactor.kotlin.test.test

@SpringBootTest
class AuthControllerSpec(val requester: RSocketRequester) : StringSpec({
    "generate token"{
        requester
            .route("auth.generate.token")
            .retrieveMono(String::class.java)
            .test()
            .expectNextMatches {
                log.info("retrieve value from RSocket mapping: {}", it)
                it is String
            }
            .expectComplete()
            .verify()
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
```

## 4.3 Store Client Object in redis

### 4.3.1 Client

```
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
```

### 4.3.2 redis template

```
package org.study.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.study.auth.model.Client


@Configuration
class RedisConfig {
    @Bean("clientRedisTemplate")
    fun clientRedisTemplate(factory: ReactiveRedisConnectionFactory) = ReactiveRedisTemplate(
        factory,
        RedisSerializationContext
            .newSerializationContext<String, Client>(StringRedisSerializer()) // keySerializer
            .value(Jackson2JsonRedisSerializer(Client::class.java)) //valueSerializer
            .build()
    )
}
```

### 4.3.3 Service

为了安全，不开放操作client的接口，所以，没有controller，只有service

> 注：请重点关注kotlin coroutines + reactive API的代码风格

```
package org.study.auth.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import org.study.auth.model.API
import org.study.auth.model.Client
import reactor.core.publisher.Flux

@Service
class ClientService(val clientRedisTemplate: ReactiveRedisTemplate<String, Client>) {
    suspend fun save(): Flow<Boolean> {
        val account = Client(
            name = "account",
            apiList = listOf(
                API.GENERATE_TOKEN,
                API.DELETE_TOKEN,
                API.GET_AUTHENTICATION,
                API.UPDATE_AUTHENTICATION,
                API.REFRESH_TOKEN,
            )
        )

        val order = Client("order")

        return Flux.just(account, order).flatMap {
            clientRedisTemplate.opsForValue().set("auth:client:${it.name}", it)
        }.asFlow()
    }
}
```

### 4.3.4 Testing

```
package org.study.auth

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.study.auth.service.ClientService

@SpringBootTest
class ClientServiceSpec(val clientService: ClientService) : StringSpec({
    "save clients"{
        clientService
            .save()
            .onEach {
                it.shouldBeTrue()
            }
            .onCompletion {
                if (it == null) log.info("Completed successfully")
            }.collect()
    }

}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
```

### 4.3.5 view redis client

```
# 1 进入redis container
docker container exec -it redis sh

# 2 进入redis client
redis-cli

# 3 查看key
127.0.0.1:6379> keys *
1) "auth:client:order"
2) "auth:client:account"
3) "access_token_001"
127.0.0.1:6379> get auth:client:order
"{\"name\":\"order\",\"id\":\"2dba9bc0-ca66-4e8b-b1a1-75c664aac639\",\"secret\":\"*#XR%NS{G}$%L)v4N9un)N_\",\"apiList\":[\"auth.get.authentication\"]}"
127.0.0.1:6379> get auth:client:account
"{\"name\":\"account\",\"id\":\"9825ad09-3eb2-4df4-8b5b-6b60aa4008da\",\"secret\":\"I.}u(jvB@L1BqTL`PLQH3t/\",\"apiList\":[\"auth.generate.token\",\"auth.delete.token\",\"auth.get.authentication\",\"auth.update.authentication\",\"auth.refresh.token\"]}"
127.0.0.1:6379> ttl auth:client:account
(integer) -1
127.0.0.1:6379> 
```

![image-20210220023035747](/image-20210220023035747.png)

# 5 Refresh Token



# 6 Exception

## 6.1 expired

### 6.1.1 AccessToken

### 6.1.2 RefreshToken



## 6.2 error

error AccessToken/RefreshToken











































