package example.boatlog

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MaintenanceLog_MarkingDone_Test {
    @Test
    fun `mark first regular task as done`() {
        val log = MaintenanceLog(
            boat = "Test Boat",
            tasks = listOf(
                RegularMaintenance("A", 4, LocalDate.of(2000,4,12)),
                RegularMaintenance("B", 6, LocalDate.of(2000,4,12)),
                RegularMaintenance("C", 2, LocalDate.of(2000,4,12))
            )
        )
        
        val actual = log.havingDone(0, LocalDate.of(2000,5,1))
        
        val expected = MaintenanceLog(
            boat = "Test Boat",
            tasks = listOf(
                RegularMaintenance("A", 4, LocalDate.of(2000,5,1)),
                RegularMaintenance("B", 6, LocalDate.of(2000,4,12)),
                RegularMaintenance("C", 2, LocalDate.of(2000,4,12))
            )
        )
        
        assertEquals(expected, actual)
    }
    
    @Test
    fun `mark last regular task as done`() {
        val log = MaintenanceLog(
            boat = "Test Boat",
            tasks = listOf(
                RegularMaintenance("A", 4, LocalDate.of(2000,4,12)),
                RegularMaintenance("B", 6, LocalDate.of(2000,4,12)),
                RegularMaintenance("C", 2, LocalDate.of(2000,4,12))
            )
        )
        
        val actual = log.havingDone(2, LocalDate.of(2000,5,1))
        
        val expected = MaintenanceLog(
            boat = "Test Boat",
            tasks = listOf(
                RegularMaintenance("A", 4, LocalDate.of(2000,4,12)),
                RegularMaintenance("B", 6, LocalDate.of(2000,4,12)),
                RegularMaintenance("C", 2, LocalDate.of(2000,5,1))
            )
        )
        
        assertEquals(expected, actual)
    }
    
    @Test
    fun `mark middle regular task as done`() {
        val log = MaintenanceLog(
            boat = "Test Boat",
            tasks = listOf(
                RegularMaintenance("A", 4, LocalDate.of(2000,4,12)),
                RegularMaintenance("B", 6, LocalDate.of(2000,4,12)),
                RegularMaintenance("C", 2, LocalDate.of(2000,4,12))
            )
        )
        
        val actual = log.havingDone(1, LocalDate.of(2000,5,1))
        
        val expected = MaintenanceLog(
            boat = "Test Boat",
            tasks = listOf(
                RegularMaintenance("A", 4, LocalDate.of(2000,4,12)),
                RegularMaintenance("B", 6, LocalDate.of(2000,5,1)),
                RegularMaintenance("C", 2, LocalDate.of(2000,4,12))
            )
        )
        
        assertEquals(expected, actual)
    }
}