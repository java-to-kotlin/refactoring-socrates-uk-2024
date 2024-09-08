package example.leaderboard.hsqldb

import example.leaderboard.CycleRaces
import example.leaderboard.Leaderboard
import example.leaderboard.LeaderboardRow
import example.leaderboard.RaceId
import example.leaderboard.Rider
import example.leaderboard.RiderId
import example.leaderboard.Transactor.Mode.ReadOnly
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigDecimal.ZERO

private val zeroDistance = ZERO.setScale(4)

class HsqldbCycleRacesTest {
    private val testDb = setUpTestDatabase(targetVersion = "latest")
    private val transactor = HsqldbTransactor<CycleRaces>(testDb::getConnection, ::HsqldbCycleRaces)
    
    @Test
    fun `register and look up rider`() {
        val riderId = transactor.perform { it.registerRider("Alice") }
        
        transactor.perform(ReadOnly) {
            val loaded = it.loadRider(riderId) ?: fail("rider $riderId not found")
            assertEquals(Rider(id = riderId, name = "Alice"), loaded)
        }
    }
    
    @Test
    fun `rider IDs start at 1`() {
        val aliceId = transactor.perform { it.registerRider("Alice") }
        val bobId = transactor.perform { it.registerRider("Bob") }
        val carolId = transactor.perform { it.registerRider("Carol") }
        
        assertEquals(RiderId.of(1), aliceId)
        assertEquals(RiderId.of(2), bobId)
        assertEquals(RiderId.of(3), carolId)
    }
    
    @Test
    fun `page through list of all riders`() {
        repeat(10) { i ->
            transactor.perform { it.registerRider("Rider-${i + 1}") }
        }
        
        val rs1 = transactor.perform(ReadOnly) {
            it.listRiders(count = 3)
        }
        assertEquals(
            listOf(
                Rider(id = RiderId.of(1), name = "Rider-1"),
                Rider(id = RiderId.of(2), name = "Rider-2"),
                Rider(id = RiderId.of(3), name = "Rider-3")
            ),
            rs1
        )
        
        val rs2 = transactor.perform(ReadOnly) {
            it.listRiders(count = 4, after = rs1.last().id)
        }
        assertEquals(
            listOf(
                Rider(id = RiderId.of(4), name = "Rider-4"),
                Rider(id = RiderId.of(5), name = "Rider-5"),
                Rider(id = RiderId.of(6), name = "Rider-6"),
                Rider(id = RiderId.of(7), name = "Rider-7")
            ),
            rs2
        )
        
        val rs3 = transactor.perform(ReadOnly) {
            it.listRiders(count = 5, after = rs2.last().id)
        }
        assertEquals(
            listOf(
                Rider(id = RiderId.of(8), name = "Rider-8"),
                Rider(id = RiderId.of(9), name = "Rider-9"),
                Rider(id = RiderId.of(10), name = "Rider-10")
            ),
            rs3
        )
    }
    
    @Test
    fun `looking up non-existent rider`() {
        transactor.perform(ReadOnly) {
            assertNull(it.loadRider(RiderId.of(99)))
        }
    }
    
    @Test
    fun `create and look up race`() {
        val raceId = transactor.perform { it.createRace("Test Race").id }
        
        transactor.perform(ReadOnly) {
            val loaded = it.loadRace(raceId) ?: fail("race $raceId not found")
            assertEquals("Test Race", loaded.name)
        }
    }
    
    @Test
    fun `looking up non-existent race`() {
        transactor.perform(ReadOnly) {
            assertNull(it.loadRace(RaceId.of(13)))
        }
    }
    
    @Test
    fun `add riders to race`() {
        val riderA = transactor.perform { it.registerRider("Alice") }
        val riderB = transactor.perform { it.registerRider("Bob") }
        transactor.perform { it.registerRider("Carol") }
        
        val raceId = transactor.perform {
            val race = it.createRace("The Race")
            it.addCompetitor(race.id, riderA)
            it.addCompetitor(race.id, riderB)
            race.id
        }
        
        transactor.perform(ReadOnly) { races ->
            val leaderboard = races.loadLeaderboard(raceId) ?: fail("no race with ID $raceId")
            assertEquals(setOf(riderA, riderB), leaderboard.rankings.map { it.riderId }.toSet())
        }
    }
    
    @Test
    fun `initially all riders at distance zero and leaderboard lists them in ID order`() {
        val riderIds = transactor.perform { club ->
            listOf("Alice", "Bob", "Carol", "Dave").map(club::registerRider)
        }
        val race = transactor.perform { club ->
            club.createRace("Example Race")
                .also { race ->
                    riderIds.forEach { riderId -> club.addCompetitor(race.id, riderId) }
                }
        }
        
        assertEquals(
            Leaderboard(
                raceName = "Example Race",
                rankings = listOf(
                    LeaderboardRow(riderIds[0], "Alice", zeroDistance),
                    LeaderboardRow(riderIds[1], "Bob", zeroDistance),
                    LeaderboardRow(riderIds[2], "Carol", zeroDistance),
                    LeaderboardRow(riderIds[3], "Dave", zeroDistance)
                )
            ),
            transactor.perform(ReadOnly) { it.loadLeaderboard(race.id) }
        )
    }
    
    @Test
    fun `after update, leaderboard lists riders ordered by distance then ID`() {
        val riderIds = transactor.perform { club ->
            listOf("Alice", "Bob", "Carol", "Dave").map(club::registerRider)
        }
        val race = transactor.perform { club ->
            club.createRace("Example Race")
                .also { race ->
                    riderIds.forEach { riderId -> club.addCompetitor(race.id, riderId) }
                }
        }
        
        transactor.perform { club ->
            club.logDistance(race.id, riderIds[0], BigDecimal("0.9876"))
            club.logDistance(race.id, riderIds[1], BigDecimal("1.6789"))
            club.logDistance(race.id, riderIds[2], BigDecimal("1.6789"))
            club.logDistance(race.id, riderIds[3], BigDecimal("1.2345"))
        }
        
        assertEquals(
            Leaderboard(
                raceName = "Example Race",
                rankings = listOf(
                    LeaderboardRow(riderIds[1], "Bob",   BigDecimal("1.6789")),
                    LeaderboardRow(riderIds[2], "Carol", BigDecimal("1.6789")),
                    LeaderboardRow(riderIds[3], "Dave", BigDecimal("1.2345")),
                    LeaderboardRow(riderIds[0], "Alice",  BigDecimal("0.9876"))
                )
            ),
            transactor.perform(ReadOnly) { it.loadLeaderboard(race.id) }
        )
    }
}

