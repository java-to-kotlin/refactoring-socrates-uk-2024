package example.leaderboard.hsqldb

import example.leaderboard.CycleRaces
import example.leaderboard.Leaderboard
import example.leaderboard.LeaderboardRow
import example.leaderboard.Race
import example.leaderboard.RaceId
import example.leaderboard.Rider
import example.leaderboard.RiderId
import java.math.BigDecimal
import java.sql.Connection
import java.sql.ResultSet

class HsqldbCycleRaces(private val c: Connection) : CycleRaces {
    override fun registerRider(name: String): RiderId =
        c.prepareStatement(
            //language=HSQLDB
            "insert into rider (name) values (?)",
            arrayOf("id")
        ).use { s ->
            s.setString(1, name)
            s.executeUpdate()
            s.generatedKeys.use { rs ->
                rs.singleOrNull { get(RiderId, 1) }
                    ?: error("did not receive id of new Rider")
            }
        }
    
    override fun loadRider(riderId: RiderId): Rider? =
        c.prepareStatement(
            //language=HSQLDB
            "select id, name from rider where id = ?"
        ).use { s ->
            s.set(1, riderId)
            s.executeQuery().use { rs ->
                rs.singleOrNull { getRider() }
            }
        }
    
    override fun listRiders(count: Int, after: RiderId?): List<Rider> {
        return c.prepareStatement(
            //language=HSQLDB
            """
            select id, name
            from rider
            where id > ?
            limit ?
            """
        ).use { s ->
            s.setInt(1, after?.value ?: 0)
            s.setInt(2, count)
            
            s.executeQuery().use { rs ->
                rs.asSequence { getRider() }.toList()
            }
        }
    }
    
    override fun createRace(name: String): Race {
        val id = c.prepareStatement(
            //language=HSQLDB
            "insert into race (name) values (?)",
            arrayOf("id")
        ).use { s ->
            s.setString(1, name)
            s.executeUpdate()
            s.generatedKeys.use { rs ->
                rs.singleOrNull { get(RaceId, 1) }
                    ?: error("did not receive id of new Race")
            }
        }
        
        return Race(id, name)
    }
    
    override fun loadRace(raceId: RaceId): Race? =
        c.prepareStatement(
            //language=HSQLDB
            "select name from race where id = ?"
        ).use { s ->
            s.set(1, raceId)
            s.executeQuery().use { rs ->
                rs.singleOrNull { Race(raceId, getString("name")) }
            }
        }
    
    override fun loadCompetitors(raceId: RaceId): Set<RiderId> =
        c.prepareStatement(
            //language=HSQLDB
            """
            select rider_id from result
            where race_id = ?
            order by rider_id
            """
        ).use { s ->
            s.set(1, raceId)
            s.executeQuery().use { rs ->
                rs.asSequence { rs.get(RiderId, "rider_id") }.toSet()
            }
        }
    
    
    override fun addCompetitor(raceId: RaceId, riderId: RiderId) {
        c.prepareStatement(
            //language=HSQLDB
            """
            insert into result (race_id, rider_id, distance)
            values (?, ?, 0)
            """
        ).use { s ->
            s.set(1, raceId)
            s.set(2, riderId)
            s.executeUpdate()
        }
    }
    
    override fun logDistance(raceId: RaceId, riderId: RiderId, distance: BigDecimal) {
        c.prepareStatement(
            //language=HSQLDB
            """
            update result
            set distance = ?
            where race_id = ?
              and rider_id = ?
            """
        ).use { s ->
            s.setBigDecimal(1, distance)
            s.set(2, raceId)
            s.set(3, riderId)
            s.executeUpdate()
        }
    }
    
    override fun loadLeaderboard(raceId: RaceId): Leaderboard? {
        val race = loadRace(raceId) ?: return null
        val rankings = c.prepareStatement(
            //language=HSQLDB
            """
            select
                rider.id as rider_id,
                rider.name as rider_name,
                result.distance as distance
            from result
            join rider on rider.id = result.rider_id
            where race_id = ?
            order by distance desc, rider.id
            """
        ).use { s ->
            s.set(1, race.id)
            s.executeQuery().use { rs ->
                rs.asSequence {
                    LeaderboardRow(
                        riderId = rs.get(RiderId, "rider_id"),
                        riderName = rs.getString("rider_name"),
                        distance = rs.getBigDecimal("distance")
                    )
                }.toList()
            }
        }
        return Leaderboard(race.name, rankings)
    }
    
    private fun ResultSet.getRider(): Rider =
        Rider(get(RiderId, "id"), getString("name"))
}
