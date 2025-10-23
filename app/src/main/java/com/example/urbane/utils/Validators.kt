package com.example.urbane.utils

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isValidPhone(input: String): Boolean {
    val regex = Regex("""^\d{3}-\d{3}-\d{4}$""")
    return regex.matches(input)
}


fun formatIdCard(input: String): String {
    val digits = input.filter { it.isDigit() }.take(11)
    return when {
        digits.length <= 3 -> digits
        digits.length <= 10 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
        else -> "${digits.substring(0, 3)}-${digits.substring(3, 10)}-${digits.substring(10)}"
    }
}

fun formatPhone(input: String): String {
    val digits = input.filter { it.isDigit() }.take(10)
    return when {
        digits.length <= 3 -> digits
        digits.length <= 6 -> "${digits.substring(0, 3)}-${digits.substring(3)}"
        else -> "${digits.substring(0, 3)}-${digits.substring(3, 6)}-${digits.substring(6)}"
    }
}



fun isValidIdCard(input: String): Boolean {
    val regex = Regex("""^\d{3}-\d{7}-\d{1}$""")
    return regex.matches(input)
}

