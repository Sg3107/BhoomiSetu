package com.satya.bhoomisetu.util

import android.util.Patterns

object Validators {

    fun isValidName(name: String): Boolean {
        return name.trim().length >= 2
    }

    private val PHONE_REGEX = Regex("^[6-9]\\d{9}$")

    fun isValidPhone(phone: String): Boolean {
        val cleaned = phone.trim().replace(" ", "").replace("-", "")
        return PHONE_REGEX.matches(cleaned)
    }

    fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun doPasswordsMatch(p1: String, p2: String): Boolean {
        return p1 == p2
    }

    fun isValidLicenseNumber(license: String): Boolean {
        return license.trim().length >= 4
    }

    fun isValidExperienceYears(yearsString: String): Boolean {
        val years = yearsString.toIntOrNull()
        return years != null && years in 0..50
    }
}
