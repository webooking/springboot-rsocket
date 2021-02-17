package org.study.account.validation.validator

import org.study.account.model.Custom
import org.study.account.validation.constraints.AgeBracket
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class AgeBracketValidator : ConstraintValidator<AgeBracket, Custom> {
    override fun isValid(value: Custom, context: ConstraintValidatorContext): Boolean = when(value){
        is Custom.CreateRequest -> checkCreate(value)
        else -> false
    }

    private fun checkCreate(custom: Custom.CreateRequest): Boolean = when(custom.age){
        in 0 until 2 -> isContains(custom.ageBracket, "Baby", "幼儿")
        in 2 until 13 -> isContains(custom.ageBracket, "Child", "儿童")
        else -> isContains(custom.ageBracket, "Adolescent", "青少年")
    }
    private fun isContains(value:String, vararg names:String) = names.contains(value)
}