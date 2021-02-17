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