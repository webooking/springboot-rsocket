package org.study.feign.annotation

import org.springframework.context.annotation.Import
import org.study.feign.core.RSocketClientsRegistrar
import kotlin.reflect.KClass

@Retention
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@Import(RSocketClientsRegistrar::class)
annotation class EnableRSocketClients(
    vararg val value: String = [],
    val basePackages: Array<String> = [],
    val basePackageClasses: Array<KClass<*>> = []
)
