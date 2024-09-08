package example.leaderboard

import example.leaderboard.hsqldb.HsqldbCycleRaces
import example.leaderboard.hsqldb.HsqldbTransactor
import example.leaderboard.hsqldb.createHsqldbDataSource
import io.github.serpro69.kfaker.Faker
import io.github.serpro69.kfaker.FakerConfig
import java.lang.Thread.sleep
import java.time.Clock
import java.util.Random

data class SimulatedRace(
    val raceId: RaceId,
    val riders: List<SimulatedRider>
)

data class SimulatedRider(
    val riderId: RiderId,
    val meanSpeed: Double,
    val speedStdDev: Double,
    val distance: Double = 0.0
)

fun SimulatedRace.tick(rng: Random) = copy(riders = riders.map { it.tick(rng) })
fun SimulatedRider.tick(rng: Random) = copy(
    distance = distance + rng.nextGaussian(meanSpeed, speedStdDev)
)

val completedRaceId = RaceId.of(1)
val currentRaceId = RaceId.of(2)

fun main() {
    val clock = Clock.systemUTC()
    val dataSource = createHsqldbDataSource()
    val transactor = HsqldbTransactor<CycleRaces>(dataSource, ::HsqldbCycleRaces)
    val rng = Random()
    
    var raceSim = transactor.perform { races ->
        initialiseSimulation(races)
    }
    
    while (true) {
        sleep(1000)
        println("Tick ${clock.instant()}")
        raceSim = raceSim.tick(rng)
        
        transactor.perform { races ->
            races.updateRaceStatus(raceSim)
        }
    }
}

private fun initialiseSimulation(races: CycleRaces): SimulatedRace {
    val faker = Faker(FakerConfig.builder().build())
    
    val leaderboard = races.loadLeaderboard(currentRaceId)
    val riderStatuses = if (leaderboard == null) {
        val completedRace = createNewRace(races, faker, completedRaceId)
        completedRace.forEach { riderId ->
            races.logDistance(
                completedRaceId, riderId,
                (faker.random.nextDouble() * 25.0 + 75.0).toBigDecimal()
            )
        }
        
        val nextRace = createNewRace(races, faker, currentRaceId)
        println("Race started")
        nextRace
            .map { it to 0.0 }
    } else {
        println("Continuing race")
        leaderboard.rankings
            .sortedBy { it.riderId }
            .map { it.riderId to it.distance.toDouble() }
    }
    
    // Deterministic so that restarting the app continues the simulated race with the
    // same rider behaviour
    val rng = Random(currentRaceId.value.toLong())
    
    return SimulatedRace(
        raceId = currentRaceId,
        riders = riderStatuses.map { (riderId, distance) ->
            SimulatedRider(
                riderId = riderId,
                meanSpeed = rng.nextDouble(0.003, 0.005),
                speedStdDev = rng.nextDouble(0.001),
                distance = distance
            )
        }
    )
}

private fun createNewRace(
    races: CycleRaces,
    faker: Faker,
    raceId: RaceId
): List<RiderId> {
    val newRace = races.createRace(faker.address.city() + " Endurance Race")
    
    // To make it easy to run the exercise, we only simulate the race with id 2
    if (newRace.id != raceId) {
        error("did not assign expected race ID: expected id $raceId, created id ${newRace.id}")
    }
    
    return (1..10).map {
        races.registerRider(faker.funnyName.name())
            .also { races.addCompetitor(raceId, it) }
    }
}

private fun CycleRaces.updateRaceStatus(raceSim: SimulatedRace) {
    raceSim.riders.forEach { rider ->
        logDistance(raceSim.raceId, rider.riderId, rider.distance.toBigDecimal())
    }
}

