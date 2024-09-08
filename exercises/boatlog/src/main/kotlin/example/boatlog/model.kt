package example.boatlog

import java.time.LocalDate
import kotlin.math.max


data class MaintenanceLog(
    val boat: String,
    val tasks: List<MaintenanceTask>,
)

fun MaintenanceLog.tasksDue(onDate: LocalDate): List<Pair<Int, MaintenanceTask>> =
    tasks.mapIndexed(::Pair)
        .filter { (_, task) -> task.isDue(onDate) }
        .sortedBy { it.first }

fun MaintenanceLog.havingDone(index: Int, onDate: LocalDate): MaintenanceLog =
    copy(tasks = tasks.take(index) + tasks[index].done(onDate) + tasks.drop(index + 1))

fun MaintenanceLog.afterCruising(hours: Int): MaintenanceLog =
    copy(tasks = tasks.map { it.afterCruising(hours) })


sealed interface MaintenanceTask {
    val description: String
    
    fun isDue(onDate: LocalDate): Boolean
    fun afterCruising(hours: Int): MaintenanceTask
    fun done(onDate: LocalDate): MaintenanceTask
}

data class RegularMaintenance(
    override val description: String,
    val frequency: Int,
    val lastDone: LocalDate
) : MaintenanceTask {
    
    override fun isDue(onDate: LocalDate): Boolean =
        lastDone.plusMonths(frequency.toLong()) <= onDate
    
    override fun afterCruising(hours: Int): RegularMaintenance =
        this
    
    override fun done(onDate: LocalDate): MaintenanceTask =
        copy(lastDone = onDate)
}

data class CruisingMaintenance(
    override val description: String,
    val frequency: Int,
    val remaining: Int
) : MaintenanceTask {
    
    override fun isDue(onDate: LocalDate): Boolean =
        remaining <= 0
    
    override fun afterCruising(hours: Int): CruisingMaintenance =
        copy(remaining = max(0, remaining - hours))
    
    override fun done(onDate: LocalDate): MaintenanceTask =
        copy(remaining = frequency)
}

