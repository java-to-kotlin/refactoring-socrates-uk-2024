package example.boatlog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.io.StringWriter
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset.UTC
import java.util.concurrent.TimeUnit.SECONDS


class MainLoopTest {
    @Test
    @Timeout(1, unit = SECONDS)
    fun `run the app`() {
        val clock = Clock.fixed(LocalDate.of(2024,4,10).atTime(10,30).toInstant(UTC), UTC)
        
        var log = MaintenanceLog(
            boat = "Pequod",
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
                    remaining = 10
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
        
        val stdout = StringWriter()
        
        val inputs = listOf(
            "1", "2",
            "1", "1",
            "2", "12",
            "1", "1",
            "0"
        ).iterator()
        
        val readInput: () -> String? = {
            inputs.nextOrNull()
                ?.also {
                    stdout.write(it)
                    stdout.write("\n")
                }
        }
        
        val console = Console(readInput, stdout)
        
        mainLoop(
            clock = clock,
            loadLog = { log },
            saveLog = { log = it },
            console = console
        )
        
        val trace = stdout.toString().trim()
        val expectedTrace = javaClass.getResourceAsStream("expected-trace.txt")
            ?.reader()
            ?.use { it.readText().trim() }
        
        assertEquals(expectedTrace, trace)
    }
}

private fun <T> Iterator<T>.nextOrNull() = if (hasNext()) next() else null
