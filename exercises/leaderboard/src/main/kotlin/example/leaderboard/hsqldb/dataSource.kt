package example.leaderboard.hsqldb

import org.hsqldb.jdbc.JDBCDataSource

fun createHsqldbDataSource(): JDBCDataSource {
    return JDBCDataSource().apply {
        database = "jdbc:hsqldb:hsql://localhost/races"
        user = "sa"
    }
}
