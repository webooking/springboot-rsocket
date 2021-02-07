package org.study.account.validation.constraints

import org.study.account.validation.validator.AgeBracketValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS,
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@kotlin.annotation.Repeatable
@MustBeDocumented
@Constraint(validatedBy = [AgeBracketValidator::class])
annotation class AgeBracket(
    val message: String = "年龄段的名称错误",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
) {
    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
    )
    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    annotation class List(vararg val value: AgeBracket)
}