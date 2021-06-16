package org.study.feign.model

data class FeignClientMapping(
    val host: String,
    val port: Int,
    @Suppress("ArrayInDataClass") val classes: Array<Class<*>>
)
