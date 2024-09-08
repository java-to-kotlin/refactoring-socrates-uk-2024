import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin

plugins {
    val kotlinVersion = "2.0.20"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion apply false
}

repositories {
    mavenCentral()
}

subprojects {
    repositories {
        mavenCentral()
    }

    plugins.withType<KotlinPlatformJvmPlugin> {
        kotlin {
            jvmToolchain(17)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}


tasks.create<Copy>("createStartingPoint") {
    destinationDir = layout.buildDirectory.dir("starting-point").get().asFile

    from(projectDir) {
        exclude("**/build/**")
        exclude("**/.git/**")
        exclude("**/.gradle/**")
        exclude("**/*.class")
        exclude("**/PRESENTER-NOTES.*")
        exclude("**/.idea/**")
    }
}

