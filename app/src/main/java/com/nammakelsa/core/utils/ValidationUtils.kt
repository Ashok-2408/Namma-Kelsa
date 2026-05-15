package com.nammakelsa.core.utils

object ValidationUtils {
    fun isValidPhone(phone: String): Boolean = phone.length >= 10
    fun isValidEmail(email: String): Boolean = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    fun isValidPassword(password: String): Boolean = password.length >= 6
}
