package example.boatlog

import java.io.PrintWriter
import java.io.Writer
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId


fun main() {
    val clock = Clock.systemUTC()
    val console = Console(::readlnOrNull, System.out.bufferedWriter())
    val loadLog = { loadMaintenanceLog() }
    val saveLog = { log: MaintenanceLog -> saveMaintenanceLog(log) }
    
    mainLoop(clock, console, loadLog, saveLog)
}

class Console(private val readlnOrNull: () -> String?, out: Writer) : PrintWriter(out) {
    fun readLine(): String? = readlnOrNull()
}

fun mainLoop(
    clock: Clock,
    console: Console,
    loadLog: () -> MaintenanceLog,
    saveLog: (MaintenanceLog) -> Unit
) {
    var log = loadLog()
    
    while (true) {
        val todo = log.tasksDue(clock.currentDate())
        console.printTasksToDo(todo, log.boat)
        
        when (console.readTopLevelMenu(
            "Mark task as done",
            "Record cruising time"
        )) {
            -1 -> break
            0 -> {
                val todoIndex = console.readSubMenu(todo.map { (_, item) -> item.description })
                if (todoIndex >= 0) {
                    val (indexInLog, _) = todo[todoIndex]
                    log = log.havingDone(indexInLog, clock.currentDate())
                    saveLog(log)
                }
            }
            1 -> {
                val hours = console.readCruisingTime()
                if (hours > 0) {
                    log = log.afterCruising(hours)
                    saveLog(log)
                }
            }
        }
    }
}

private fun PrintWriter.printTasksToDo(
    todo: List<Pair<Int, MaintenanceTask>>,
    boatName: String
) {
    if (todo.isEmpty()) {
        println("No tasks to do. $boatName is ready for cruising.")
    } else {
        println("Tasks to do:")
        todo.forEachIndexed { index, (_, task) ->
            println("%2d. %s".format(index + 1, task.description))
        }
    }
    flush()
}


private fun Clock.currentDate(): LocalDate =
    instant().atZone(ZoneId.systemDefault()).toLocalDate()


tailrec fun Console.readMenuIndex(choices: List<String>, zeroChoice: String): Int {
    val itemCount = choices.size
    
    println()
    println("Choose one of...")
    choices.forEachIndexed { i, choice ->
        println("${i + 1}: $choice")
    }
    println("0: $zeroChoice")
    print("> ")
    flush()
    
    val index = readLine()
        ?.toIntOrNull()
        ?.takeIf { it in 0..itemCount }
        ?.let { it - 1 }
    
    if (index != null) {
        return index
    } else {
        println("Invalid input: enter a number between 0 and $itemCount")
        return this@readMenuIndex.readMenuIndex(choices, zeroChoice)
    }
}

tailrec fun Console.readCruisingTime(): Int {
    print("Enter hours cruised > ")
    flush()
    
    val index = readLine()
        ?.toIntOrNull()
        ?.takeIf { it >= 0 }
    
    if (index != null) {
        return index
    } else {
        println("Invalid input")
        return this.readCruisingTime()
    }
}

fun Console.readTopLevelMenu(vararg choices: String): Int =
    readMenuIndex(choices.toList(), "Exit")

fun Console.readSubMenu(choices: List<String>): Int =
    readMenuIndex(choices.toList(), "Back")

