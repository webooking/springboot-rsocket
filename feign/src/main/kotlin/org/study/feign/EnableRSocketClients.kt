@file:Suppress("unused")

package org.study.feign

import org.springframework.context.annotation.Import
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention //反射时可以读取
@MustBeDocumented
@Import(RSocketClientsRegistrar::class)
annotation class EnableRSocketClients(
    vararg val value: FeignClientMapping
)

@Retention
annotation class FeignClientMapping(
    val name: String,
    val host: String,
    val port: Int,
    vararg val classes: KClass<*>
)

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention
@MustBeDocumented
annotation class Anonymous

@Target(AnnotationTarget.CLASS)
@Retention
@MustBeDocumented
annotation class RSocketClient(
    val value: String = ""
)

data class FeignClientMappingDto(
    val host: String,
    val port: Int,
    @Suppress("ArrayInDataClass") val classes: Array<Class<*>>
)
