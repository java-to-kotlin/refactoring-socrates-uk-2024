package example.leaderboard

import example.leaderboard.Transactor.Mode.ReadOnly
import example.leaderboard.hsqldb.HsqldbCycleRaces
import example.leaderboard.hsqldb.HsqldbTransactor
import example.leaderboard.hsqldb.createHsqldbDataSource
import java.lang.Thread.sleep

fun main(args: Array<String>) {
    val raceId = RaceId.of(args.getOrNull(0)?.toIntOrNull() ?: 1)
    
    val transactor = HsqldbTransactor(createHsqldbDataSource(), ::HsqldbCycleRaces)
    
    while (true) {
        val leaderboard = transactor.perform(ReadOnly) { club ->
            club.loadLeaderboard(raceId)
        }
        
        clearScreen()
        println()
        
        if (leaderboard == null) {
            println("Waiting for race $raceId to be created...")
        } else {
            println(leaderboard.raceName)
            println()
            if (leaderboard.isEmpty()) {
                println("Waiting for riders...")
            } else {
                println("%2s %-24s %s".format("ID", "Rider Name", "Distance (km)"))
                println("-- ------------------------ -------------")
                leaderboard.forEach { row ->
                    println("%02d %-24s %s".format(row.riderId.value, row.riderName, row.distance))
                }
            }
        }
        
        sleep(1000)
    }
}

private fun clearScreen() {
    System.out.print("\u001B[H\u001B[2J")
    System.out.flush()
}
