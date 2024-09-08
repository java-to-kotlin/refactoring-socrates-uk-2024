package example.boatlog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import java.time.LocalDate


class LoadMaintenanceLogTest {
    companion object {
        val examplesDir = File("src/test/resources/example/boatlog")
        
        val examples = mapOf(
            "empty-log.json" to MaintenanceLog(
                boat = "Example Boat",
                tasks = emptyList()
            ),
            "regular-task.json" to MaintenanceLog(
                boat = "Another Boat",
                tasks = listOf(
                    RegularMaintenance(
                        description = "A regular task",
                        frequency = 4,
                        lastDone = LocalDate.of(2023, 10, 12)
                    )
                )
            ),
            "cruising-task.json" to MaintenanceLog(
                boat = "Another Boat",
                tasks = listOf(
                    CruisingMaintenance(
                        description = "A cruising task",
                        frequency = 150,
                        remaining = 30
                    )
                )
            ),
            "larger-example.json" to MaintenanceLog(
                boat = "Boaty McBoatface",
                tasks = listOf(
                    RegularMaintenance(
                        description = "Check oil level",
                        frequency = 1,
                        lastDone = LocalDate.of(2024,4,8)
                    ),
                    RegularMaintenance(
                        description = "Grease stern gland",
                        frequency = 2,
                        lastDone = LocalDate.of(2023,11,24)
                    ),
                    RegularMaintenance(
                        description = "Black bottom",
                        frequency = 12,
                        lastDone = LocalDate.of(2024,2,12)
                    ),
                    CruisingMaintenance(
                        description = "Drain fuel system sedimenters",
                        frequency = 100,
                        remaining = 0
                    ),
                    RegularMaintenance(
                        description = "Varnish the barge pole",
                        frequency = 2,
                        lastDone = LocalDate.of(2024,1,23)
                    ),
                    CruisingMaintenance(
                        description = "Replace fuel system pre-filter",
                        frequency = 150,
                        remaining = 40
                    )
                )
            )
        )
    }
    
    @TestFactory
    fun examples(): List<DynamicTest> {
        val allExamples = examples.keys +
            examplesDir.list {_,name -> name.endsWith(".json")}.orEmpty().toSet()
        
        return allExamples.map { filename ->
            dynamicTest(filename) {
                assertEquals(
                    examples[filename] ?: fail("expected value is missing"),
                    loadMaintenanceLog(examplesDir.resolve(filename)))
            }
        }
    }
}
