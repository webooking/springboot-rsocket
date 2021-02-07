package org.study.account.validation.validator

import org.study.account.validation.constraints.Odd
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class OddValidator : ConstraintValidator<Odd, Int> {
    override fun isValid(value: Int, context: ConstraintValidatorContext): Boolean = value % 2 == 0
}