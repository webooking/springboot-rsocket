package org.study.account.validation.validator

import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import java.util.*
import javax.validation.Valid

@Component
@Validated
class ArgumentValidator {
    fun <T> validate(locale: Locale, @Valid request: T) = request
}