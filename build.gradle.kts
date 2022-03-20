plugins {
    kotlin("jvm") version "1.6.10"
}

group = "dev.mslalith"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

    implementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
    testImplementation("app.cash.turbine:turbine:0.7.0")
    testImplementation("com.google.truth:truth:1.1.3")
}