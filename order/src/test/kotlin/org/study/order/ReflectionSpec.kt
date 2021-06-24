package org.study.order

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import java.lang.reflect.GenericArrayType
import java.lang.reflect.ParameterizedType
import javax.lang.model.type.ArrayType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaType

class ReflectionSpec : StringSpec({
    "test reflection type"{
        val properties = Demo::class.declaredMemberProperties
        properties.forEach {
            when (val type = it.returnType.javaType) {
//                is Class<*> -> log.info("${it.name}, ${it.returnType} --> Class<*>")
                is ParameterizedType -> log.info("${it.name}, ${it.returnType} --> ParameterizedType")
                else -> log.info("${it.name}, ${it.returnType} --> Others")
            }
        }
    }
}) {
    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}

class Demo(
    val arr: Array<Int>,
    val entry: Map.Entry<String, *>,
    val list: List<*>,
    val map: Map<*, *>,

    val nullableEntry: Map.Entry<String, *>?,
    val nullableList: List<*>?,
    val nullableMap: Map<*, *>?,

    val nullableStringList: List<String>?,
    val nullableStringStringMap: Map<String, String>?,

    val stringList: List<String>,
    val stringStringMap: Map<String, String>,
)