plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(libs.kondor)
    implementation(libs.result4k)
    implementation(libs.values4k)
    implementation(libs.tuples4k)
    
    testImplementation(libs.bundles.junit)
}

tasks.create<Copy>("createExampleMaintenanceLog") {
    group = "*** boatlog exercise ***"
    
    from(projectDir) {
        include("maintenance-log.json")
    }
    destinationDir = File(System.getProperty("user.home"))
    
    doNotTrackState("because I say so")
}
