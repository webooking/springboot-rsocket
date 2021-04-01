package org.study.account.validation.constraints

import org.study.account.validation.validator.OddValidator
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@kotlin.annotation.Repeatable
@MustBeDocumented
@Constraint(validatedBy = [OddValidator::class])
annotation class Odd(
    val message: String = "{validate.leg}",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
) {
    @Target(
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
    )
    @kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
    annotation class List(vararg val value: Odd)
}