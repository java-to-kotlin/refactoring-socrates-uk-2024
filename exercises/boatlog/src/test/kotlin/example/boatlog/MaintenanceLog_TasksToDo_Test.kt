package example.boatlog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MaintenanceLog_TasksToDo_Test {
    
    @Test
    fun `due tasks returned with index`() {
        val log = MaintenanceLog(
            boat = "Test Boat",
            tasks = listOf(
                RegularMaintenance("A", 4, LocalDate.of(2000, 1, 23)),
                CruisingMaintenance("B", 6, 0),
                RegularMaintenance("C", 2, LocalDate.of(1999, 12, 20)),
                CruisingMaintenance("D", 4, 2)
            )
        )
        
        val due = log.tasksDue(onDate = LocalDate.of(2000,3,2))
        
        val expectedDue = listOf(
            1 to CruisingMaintenance("B", 6, 0),
            2 to RegularMaintenance("C", 2, LocalDate.of(1999, 12, 20))
        )
        
        assertEquals(expectedDue, due)
    }
    
    
}