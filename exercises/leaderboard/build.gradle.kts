import org.flywaydb.gradle.task.AbstractFlywayTask
import org.flywaydb.gradle.task.FlywayMigrateTask

plugins {
    kotlin("jvm")
    id("org.flywaydb.flyway") version "10.10.0"
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.flywaydb:flyway-database-hsqldb:10.10.0")
    }
}

dependencies {
    implementation(libs.faker)
    implementation(libs.values4k)
    implementation(libs.bundles.dbdev)
    
    testImplementation(libs.bundles.junit)
}

val taskGroupName = "*** leaderboard exercise ***"
val initialVersion = "001"

tasks.withType(AbstractFlywayTask::class) {
    workingDirectory = projectDir.absolutePath
    url = "jdbc:hsqldb:hsql://localhost/races"
    user = "sa"
    configFiles = arrayOf(projectDir.resolve("flyway.conf").absolutePath)
    
    /* WARNING: this has to be kept in sync with the locations in the testDataSource.
     * The config cannot be shared because of bugs in the Flyway Gradle task.
     */
    locations = arrayOf("filesystem:${projectDir.resolve("src/main/migrations").absolutePath}")
}

tasks.create("migrateToStartOfExercise", FlywayMigrateTask::class) {
    setGroup(taskGroupName)
    description = "Applies the database migration for the start of the refactoring exercise"
    target = initialVersion
}

tasks.create("migrateToLatestVersion", FlywayMigrateTask::class) {
    setGroup(taskGroupName)
    description = "Applies all the database migrations"
    target = "latest"
}

project.file("src/main/migrations").listFiles().orEmpty()
    .map { f ->
        val version = f.name.substringAfter("V").substringBefore("-")
        val description = f.name.substringAfter("-").substringBefore(".").replace('_', ' ')
        version to description
    }
    .filter { (version, _) -> version > initialVersion }
    .forEach { (version, description) ->
        tasks.create("migrateToV$version", FlywayMigrateTask::class) {
            setGroup(taskGroupName)
            this.target = version
            this.description = "Migrates database to $version ($description)"
        }
    }

tasks.create<JavaExec>("runDatabaseServer") {
    group = taskGroupName
    mainClass.set("org.hsqldb.Server")
    classpath(sourceSets["main"].compileClasspath)
    args = listOf(
        "--address", "localhost",
        "--database.0", "mem:races",
        "--dbname.0", "races"
    )
}

val jar: Task = tasks["jar"]

tasks.create<JavaExec>("runLeaderboardForCurrentRace") {
    dependsOn(jar)
    group = taskGroupName
    mainClass.set("example.leaderboard.LeaderboardKt")
    args = listOf("2")
    classpath(sourceSets["main"].runtimeClasspath)
    workingDir = projectDir
}

tasks.create<JavaExec>("runLeaderboardForEarlierRace") {
    dependsOn(jar)
    group = taskGroupName
    mainClass.set("example.leaderboard.LeaderboardKt")
    args = listOf("1")
    classpath(sourceSets["main"].runtimeClasspath)
    workingDir = projectDir
}

tasks.create<JavaExec>("runRaceSimulator") {
    dependsOn(jar)
    group = taskGroupName
    mainClass.set("example.leaderboard.RacesimulatorKt")
    classpath(sourceSets["main"].runtimeClasspath)
    workingDir = projectDir
}

