package com.paan.safa.core.util

object PhoneValidator {
    // Indian mobile numbers: 10 digits starting with 6-9
    private val regex = Regex("^[6-9][0-9]{9}$")
    fun isValid(phone: String) = regex.matches(phone)
}