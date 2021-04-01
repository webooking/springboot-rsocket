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
                .route("signIn").permitAll()
                .anyRequest().authenticated()
                .anyExchange().permitAll()
        }
            .simpleAuthentication { simple ->
                simple.authenticationManager { authentication ->
                    val accessToken = authentication.name
                    val user = Custom(
                        "user001",
                        accessToken,
                        "1374567890",
                        "yuri@qq.com",
                        /*when (accessToken.toInt() % 2 == 0) {
                            true -> "zh_CN"
                            else -> "en_US"
                        },*/
                        "en_US",
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