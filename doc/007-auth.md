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



# 3 signin



# 4 Refresh Token



# 5 Exception

## 5.1 expired AccessToken/RefreshToken

## 5.2 error AccessToken/RefreshToken













































