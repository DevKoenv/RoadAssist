import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val generateAppVersion by tasks.registering {
    val outDir = layout.buildDirectory.dir("generated/appVersion/commonMain/kotlin")
    val buildNum = providers.environmentVariable("BUILD_NUMBER")
        .map { it.toIntOrNull() ?: 0 }
        .orElse(0)

    outputs.dir(outDir)
    inputs.property("buildNumber", buildNum)

    doLast {
        val bn = inputs.properties["buildNumber"] as Int
        val display = if (bn > 0) "1.0 (build $bn)" else "1.0 (local)"
        val pkg = outputs.files.singleFile.resolve("dev/koenv/roadassist/app")
        pkg.mkdirs()
        pkg.resolve("AppVersion.kt").writeText(
            """
            package dev.koenv.roadassist.app

            object AppVersion {
                const val BUILD = $bn
                const val DISPLAY = "$display"
            }
            """.trimIndent()
        )
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()

    android {
        namespace = "dev.koenv.roadassist.app.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.clientOkhttp)
            implementation(libs.androidx.security.crypto)
            implementation(libs.play.services.location)
        }
        commonMain {
            kotlin.srcDir(generateAppVersion)
        }
        commonMain.dependencies {
            api(projects.core)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.coroutinesCore)
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.serializationKotlinxJsonKmp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.clientCio)
        }
    }
}

compose.resources {
    publicResClass = true
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
