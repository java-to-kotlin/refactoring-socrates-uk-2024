package example.luhn

fun String.isValidCardNumber(): Boolean {
    val digits = filterNot { it.isWhitespace() }
        .map { ch -> ch.digitToIntOrNull() ?: return false }

    var checkSum = 0
    digits.forEachIndexed { index, digit ->
        val digitComponent = when ((digits.size - index) % 2) {
            1 -> digit
            else -> digit * 2
        }

        checkSum += when {
            digitComponent >= 10 -> digitComponent / 10 + digitComponent % 10
            else -> digitComponent
        }
    }

    return checkSum % 10 == 0
}
