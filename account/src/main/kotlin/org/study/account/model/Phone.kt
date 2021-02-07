package org.study.account.model

import javax.validation.constraints.Pattern

data class Phone(
    @get:Pattern(regexp = "^\\+(1|86)$")
    val countryCode: String,
    @get:Pattern(regexp = "^\\d{10,11}$")
    val number: String
)