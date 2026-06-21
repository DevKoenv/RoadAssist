val buildNumber = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 0
val appVersion = rootProject.file("version").readText().trim()

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
}

group = "dev.koenv.roadassist"
version = if (buildNumber > 0) "$appVersion-$buildNumber" else appVersion

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveFileName = "roadassist-server.jar"
}

application {
    mainClass = "dev.koenv.roadassist.server.ApplicationKt"
}

dependencies {
    api(projects.core)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
    implementation(libs.jbcrypt)
    implementation(libs.logback)
    implementation(libs.sqlite.jdbc)
    implementation(libs.ktor.serverCallLogging)
    implementation(libs.ktor.serverConfigYaml)
    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverAuthJwt)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverSse)
    implementation(libs.ktor.serializationKotlinxJson)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.ktor.clientContentNegotiation)
    testImplementation(libs.kotlin.testJunit)
}
