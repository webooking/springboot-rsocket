package org.study.account.service.validator

import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.study.account.model.User
import javax.validation.Valid

@Component
@Validated
class UserControllerValidator {
    fun create(@Valid request: User.CreateRequest) = request
}