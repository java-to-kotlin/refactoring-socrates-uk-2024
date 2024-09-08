package example.leaderboard

import dev.forkhandles.values.ComparableValue
import dev.forkhandles.values.IntValue
import dev.forkhandles.values.IntValueFactory
import java.math.BigDecimal


data class Rider(
    val id: RiderId,
    val name: String
)

class RiderId private constructor(value: Int) : IntValue(value), ComparableValue<RiderId,Int> {
    companion object : IntValueFactory<RiderId>(::RiderId, {it > 0})
}


data class Race(
    val id: RaceId,
    val name: String
)

class RaceId private constructor(value: Int) : IntValue(value) {
    companion object : IntValueFactory<RaceId>(::RaceId, {it > 0})
}

interface CycleRaces {
    fun registerRider(name: String): RiderId
    fun loadRider(riderId: RiderId): Rider?
    fun listRiders(count: Int, after: RiderId? = null): List<Rider>
    
    fun createRace(name: String): Race
    fun loadRace(raceId: RaceId): Race?
    fun addCompetitor(raceId: RaceId, riderId: RiderId)
    fun loadCompetitors(raceId: RaceId): Set<RiderId>
    fun logDistance(raceId: RaceId, riderId: RiderId, distance: BigDecimal)
    fun loadLeaderboard(raceId: RaceId): Leaderboard?
}

data class Leaderboard(
    val raceName: String,
    val rankings: List<LeaderboardRow>
) : List<LeaderboardRow> by rankings

data class LeaderboardRow(
    val riderId: RiderId,
    val riderName: String,
    val distance: BigDecimal
)


