# Race Leaderboard refactoring exercise

_Database refactoring with expand/contract deployments_


# Scenario

This application manages endurance races for cycling clubs.

The application has two processes that share a database.

* One process tracks the distance riders have covered during a race and stores the statistics in the database.
* The other process loads the leaderboard of a race from the database and displays it.


# Simplifications

To make things practical for this workshop...

* We simulate the riders in the race with the `racesimulator.kt` program instead of receiving telemetry from trackers on real bikes.
* We print the leaderboard to the terminal
* We use HSQLDB for the database because it needs nothing more than the JVM.
* The model of clubs, riders and races has been simplified to remove details that are irrelevant to the exercise.
* We will run the processes and schema migrations in IntelliJ to simulate production deployments.

```plantuml
component "racesimulator.kt" as RaceSimulator
component "leaderboard.kt" as LeaderboardDisplay

database Database

RaceSimulator -d-> Database: distance\nof each rider
LeaderboardDisplay <-d- Database: leaderboard
```


# Getting started

Gradle tasks for use in this exercise are grouped under the `*** leaderboard exercise ***` category.

1. Start the database server by running the `gradle runDatabaseServer` task.  You can do this via the Gradle panel in IntelliJ or in a terminal window.

   * The database server stores data in memory. If you want to reset everything back to an empty state, restart the database server.

2. Create the initial version of the schema by running the `gradle migrateToStartOfExercise` task.

   * You will need to run this again if you restart the database server.

3. Start the application processes by running the
`gradle runRaceSimulator`, `gradle runLeaderboardForCurrentRace` and `gradle runLeaderboardForEarlierRace` tasks (these run the  racesimulator.kt and leaderboard.kt Kotlin files, respectively). Run a couple of instances of the leaderboard for the current race.

4. Run all the tests in `HsqldbCycleRacesTest`.

    * The tests exercise the persistence layer only. In this exercise, you only need to change the Java and SQL of the persistence layer.
    * The tests run their own in-memory HSQLDB database, so they do not interfere with the running application.


# Refactoring Task

In the code, the distance a rider has travelled is given the name `distance`. It is not obvious from the name what unit this is measured in, and that has resulted in embarrassing bugs. Distance is actually measured in kilometres. Rename the identifiers in the Kotlin code to `distanceKm` and the database column to `distance_km` to avoid confusion in the future.

But...

* Cycling clubs are using our application 24x7. You cannot shut the application down to deploy the refactored version.
* The deployment of database migrations and services is not atomic. You cannot upgrade the database schema, and Kotlin processes that depend on it, in one atomic change.

As a result, there is no way to avoid old and new versions of components and/or schema running at the same time.
You must use *expand/contract deployments* to perform the refactoring and safely upgrade the system to be running the refactored code.

Work out and apply a sequence of Kotlin and database changes and deployments to upgrade the applications without breaking them at runtime.

To simulate the constraints of a distributed system, only upgrade a single Kotlin process or the database schema at a time.

* To upgrade a Kotlin process, restart it in IntelliJ.  This will build the latest code before rerunning the application.
* To upgrade the database schema, use Flyway from Gradle to run the migration. Run the `gradle migrateToLatestVersion` task to apply all migrations up to the highest current version.  When you have written at least one migration, refreshing the Gradle pane in IntelliJ will create tasks that run the migrations up to a specific version, named `migrateToV002`, `migrateToV003`, etc.

If the Kotlin processes throw an exception, your upgrade plan has broken the app!  Reset everything and have another go.

Commit after writing every change, so you can easily recreate an earlier state and try something different if your sequence of changes breaks the app.

What is the shortest sequence of steps you need to apply the rename across the codebase and database?


# Hints and Tips

Create migrations by writing SQL files in the src/main/migrations directory.  Files must follow the naming convention `V{version}-<description>.sql`, where {version} is a three-digit number and {description} is a textual description of the migration, with words separated by underscores.  For example: `V002-my_first_migration.sql`, `V003-another_migration.sql`.

The user guide for the HSQLDB database server is in the docs/ directory, in case you need to documentation about its SQL dialect and feature set.

When writing migrations or changing the Kotlin code, run `HsqldbCycleRacesTest` to test that your SQL is correct and that your Kotlin and database remain compatible. This will help you avoid failed deployments, or migrations that fail halfway through and leave your database in an inconsistent state.

If you _do_ deploy a failed migration and flyway complains about the inconsistent state of its migration log, try running the `gradle flywayRepair` Gradle task to recover. It _might_ get you back to a working state.  If not, you will have to restart your database and run through the migration steps again – make sure to commit your changes to Git after each step, so you can recover without too much work.

How can you test the compatibility of your code and different versions of the database schema?
