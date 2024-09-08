package example.boatlog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory


class MaintenanceLogRoundTripTest {
    companion object {
        val examples = LoadMaintenanceLogTest.examples
    }
    
    @TestFactory
    fun examples(): List<DynamicTest> {
        return examples.map { (filename, example) ->
            DynamicTest.dynamicTest(filename.toTestName()) {
                val serialised = MaintenanceLogFormat.toJson(example)
                val deserialised = MaintenanceLogFormat.fromJson(serialised).orThrow()
                
                assertEquals(example, deserialised)
            }
        }
    }
    
    private fun String.toTestName() = substringBefore(".").replace("-", " ")
}
