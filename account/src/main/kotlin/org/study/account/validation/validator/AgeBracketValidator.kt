package org.study.account.validation.validator

import org.study.account.model.User
import org.study.account.validation.constraints.AgeBracket
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class AgeBracketValidator : ConstraintValidator<AgeBracket, User> {
    override fun isValid(value: User, context: ConstraintValidatorContext): Boolean = when(value){
        is User.CreateRequest -> checkCreate(value)
        else -> false
    }

    private fun checkCreate(user: User.CreateRequest): Boolean = when(user.age){
        in 0 until 2 -> isContains(user.ageBracket, "Baby", "幼儿")
        in 2 until 13 -> isContains(user.ageBracket, "Child", "儿童")
        else -> isContains(user.ageBracket, "Adolescent", "青少年")
    }
    private fun isContains(value:String, vararg names:String) = names.contains(value)
}