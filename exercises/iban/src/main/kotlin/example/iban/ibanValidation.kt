package example.iban

import java.math.BigInteger

val whitespace = Regex("\\s+")

// https://en.wikipedia.org/wiki/International_Bank_Account_Number#Validating_the_IBAN
fun String.isValidIBAN(): Boolean {
    val noWhitespace = replace(whitespace, "")

    if (!noWhitespace.all { it in '0'..'9' || it.uppercaseChar() in 'A'..'Z' }) {
        return false
    }

    val reordered = noWhitespace.run { drop(4) + take(4) }

    val digitsAsString = StringBuilder()
    for (ch in reordered.uppercase()) {
        when (ch) {
            in '0'..'9' -> digitsAsString.append(ch.toString())
            in 'A'..'Z' -> digitsAsString.append((10 + (ch - 'A')).toString())
        }
    }

    val digitsAsInt = digitsAsString.toString().toBigInteger()

    val checkSum = digitsAsInt.remainder(BigInteger.valueOf(97))

    return checkSum == BigInteger.valueOf(1)
}
