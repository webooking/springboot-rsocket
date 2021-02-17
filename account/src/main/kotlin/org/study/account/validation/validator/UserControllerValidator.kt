package org.study.account.validation.validator

import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.study.account.model.Custom
import javax.validation.Valid

@Component
@Validated
class UserControllerValidator {
    fun create(@Valid request: Custom.CreateRequest) = request
}