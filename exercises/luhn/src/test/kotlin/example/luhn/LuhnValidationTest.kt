package example.luhn

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

private val validTestCardNumbers = listOf(
    "378282246310005",  // American Express
    "378734493671000",  // American Express Corporate
    "30569309025904",   // Diners Club
    "3566002020360505", // JCB
    "5425233430109903", // Mastercard
    "4012888888881881", // VISA
    "4701322211111234", // VISA Debit
    "4701 3222 1111 1234", // With whitespace
)


class LuhnValidationTest {
    @TestFactory
    fun `positive cases`(): List<DynamicNode> {
        return validTestCardNumbers.map { s ->
            dynamicTest(s) {
                assertTrue(s.isValidCardNumber())
            }
        }
    }

    @TestFactory
    fun `negative cases`(): List<DynamicNode> {
        return validTestCardNumbers
            .map { it.withSingleDigitMistyped() }
            .map { (description, s) ->
                dynamicTest(description) {
                    assertFalse(s.isValidCardNumber())
                }
            }
    }

    @TestFactory
    fun `invalid characters`(): List<DynamicNode> {
        return validTestCardNumbers
            .map { it.withInvalidCharacterInserted() }
            .map { s ->
                dynamicTest(s) {
                    assertFalse(s.isValidCardNumber())
                }
            }
    }

    private fun String.withSingleDigitMistyped(): Pair<String, String> {
        val i = indices.filterNot { get(it).isWhitespace() }.random()

        val oldDigit = get(i)
        val newDigit = oldDigit.digitToInt().plus(1).rem(10)

        val description = replaceRange(i, i + 1, "[$oldDigit → $newDigit]")
        val badCardNumber = replaceRange(i, i + 1, newDigit.toString())

        return description to badCardNumber
    }
}

private fun String.withInvalidCharacterInserted(): String =
    indices.random().let { i ->
        substring(0,i) + "-" + substring(i+1, length)
    }
