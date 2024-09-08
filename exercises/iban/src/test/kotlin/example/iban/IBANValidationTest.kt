package example.iban

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.random.Random

// https://www.iban.com/structure
private val validIBANs = listOf(
    "GB33BUKB20201555555555",  // UK
    "GB33 BUKB 2020 1555 5555 55",  // UK, whitespace
    "GB 33 BUKB 2020 1555 5555 55",  // UK, whitespace in first four characters
    "gb 33 bukb 2020 1555 5555 55",  // UK, lowercase, whitespace
    "UA903052992990004149123456789",  // UAE
    "SC74MCBL01031234567890123456USD",   // Seychelles
    "RU0204452560040702810412345678901", // Russia
    "DE75512108001245126199", // Germany
    "CH5604835012345678009", // Switzerland
    "FR7630006000011234567890189", // France
)


class IBANValidationTest {
    @TestFactory
    fun `positive cases`(): List<DynamicNode> {
        return validIBANs.map { s ->
            dynamicTest(s) {
                assertTrue(s.isValidIBAN())
            }
        }
    }

    @TestFactory
    fun `negative cases`(): List<DynamicNode> {
        return validIBANs
            .map { it.withSingleDigitMistyped() }
            .map { (description, s) ->
                dynamicTest(description) {
                    assertFalse(s.isValidIBAN())
                }
            }
    }

    private fun String.withSingleDigitMistyped(): Pair<String, String> {
        val i = Random.nextInt(length - 1)
        val oldDigit = get(i)
        val newDigit = oldDigit + 1

        val description = replaceRange(i, i + 1, "[$oldDigit → $newDigit]")
        val badInput = replaceRange(i, i + 1, newDigit.toString())

        return description to badInput
    }
}
