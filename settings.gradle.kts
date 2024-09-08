import org.apache.tools.ant.DirectoryScanner

rootProject.name = "mastering-kotlin-refactoring"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

val http4kVersion = "5.20.0.0"
val forkhandlesVersion = "2.13.1.0"
val jacksonVersion = "2.17.1"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

            library("forkhandles", "dev.forkhandles:forkhandles-bom:$forkhandlesVersion")
            library("result4k","dev.forkhandles:result4k:$forkhandlesVersion")
            library("values4k","dev.forkhandles:values4k:$forkhandlesVersion")
            library("tuples4k","dev.forkhandles:tuples4k:$forkhandlesVersion")

            library("http4k-bom", "org.http4k:http4k-bom:$http4kVersion")
            library("http4k-core", "org.http4k:http4k-core:$http4kVersion")
            library("http4k-server-undertow", "org.http4k:http4k-server-undertow:$http4kVersion")
            library("http4k-client-apache", "org.http4k:http4k-client-apache:$http4kVersion")
            library("http4k-cloudnative", "org.http4k:http4k-cloudnative:$http4kVersion")
            library("http4k-template-handlebars", "org.http4k:http4k-template-handlebars:$http4kVersion")

            library("http4k-testing-approval", "org.http4k:http4k-testing-approval:$http4kVersion")
            library("http4k-testing-hamkrest", "org.http4k:http4k-testing-hamkrest:$http4kVersion")
            library("http4k-testing-strikt", "org.http4k:http4k-testing-strikt:$http4kVersion")

            bundle("http4k", listOf(
                "http4k-bom",
                "http4k-core",
                "http4k-server-undertow",
                "http4k-client-apache",
                "http4k-cloudnative",
                "http4k-template-handlebars"
            ))

            bundle("http4k-testing", listOf(
                "http4k-testing-approval",
                "http4k-testing-hamkrest",
                "http4k-testing-strikt"
            ))

            library("kotlin-serialisation-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            library("kondor", "com.ubertob.kondor:kondor-core:2.3.2")

            library("jackson-databind", "com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
            library("jackson-module-kotlin", "com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
            library("jackson-module-parameter-names", "com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
            library("jackson-datatype-jdk8", "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
            library("jackson-datatype-jsr310", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
            bundle("jackson", listOf(
                "jackson-databind",
                "jackson-module-kotlin",
                "jackson-module-parameter-names",
                "jackson-datatype-jdk8",
                "jackson-datatype-jsr310"
            ))

            library("hsqldb", "org.hsqldb:hsqldb:2.7.2")
            library("flyway", "org.flywaydb:flyway-database-hsqldb:10.10.0")

            bundle("dbdev", listOf(
                "hsqldb",
                "flyway"
            ))

            library("faker", "io.github.serpro69:kotlin-faker:1.16.0")

            library("junit-bom", "org.junit:junit-bom:5.10.1")
            library("junit-jupiter", "org.junit.jupiter:junit-jupiter:5.10.1")

            bundle("junit", listOf(
                "junit-jupiter"
            ))
        }
    }
}

/* Unfortunately, Gradle requires this to be configured globally, not
 * in the task that needs it.
 */
DirectoryScanner.removeDefaultExclude("**/.gitignore")

include("exercises:luhn")
include("exercises:iban")
include("exercises:signup")
include("exercises:leaderboard")
include("exercises:boatlog")
include("exercises:gilded-rose")

