package example.leaderboard.hsqldb

import org.flywaydb.core.Flyway
import org.hsqldb.jdbc.JDBCDataSource
import java.io.File
import java.util.Properties
import java.util.UUID


fun setUpTestDatabase(targetVersion: String) =
    testDataSource().apply {
        migrateSchema(targetVersion)
    }

fun testDataSource() = JDBCDataSource().apply {
    database = "jdbc:hsqldb:mem:testing-${UUID.randomUUID()}"
    user = "sa"
}

fun JDBCDataSource.migrateSchema(targetVersion: String) {
    val flyway = Flyway.configure()
        .configuration(
            File("flyway.conf").reader().use { r ->
                Properties().apply { load(r) }
            })
        /* WARNING: this has to be kept in sync with the locations in the Gradle file.
         * The config cannot be shared because of bugs in the Flyway Gradle task.
         */
        .locations("filesystem:src/main/migrations")
        .dataSource(this)
        .target(targetVersion)
        .load()
    flyway.migrate()
}

fun JDBCDataSource.clearTables() {
    connection.use { c ->
        c.createStatement().use { s ->
            s.execute("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK")
        }
    }
}
