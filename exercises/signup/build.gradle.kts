plugins {
    kotlin("jvm")
}

dependencies {
    implementation(libs.result4k)
    implementation(libs.values4k)
    implementation(libs.tuples4k)
    implementation(libs.bundles.http4k)
    
    testImplementation(libs.bundles.junit)
}
