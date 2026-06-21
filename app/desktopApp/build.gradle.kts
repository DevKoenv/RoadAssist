import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val buildNumber = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 0
val appVersion = rootProject.file("version").readText().trim()

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.skiko") {
            useVersion("0.144.6")
            because("Force Skiko to CMP 1.11.1 version; Coil 3.x requests 0.8.18 which conflicts")
        }
    }
}

dependencies {
    implementation(projects.app.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.compose.components.resources)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)
}

compose.desktop {
    application {
        mainClass = "dev.koenv.roadassist.app.MainKt"
        jvmArgs += listOf("-Dskiko.renderApi=SOFTWARE")

        nativeDistributions {
            includeAllModules = true
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "RoadAssist"
            packageVersion = appVersion
            description = "Roadside assistance dispatch platform"

            macOS {
                bundleID = "dev.koenv.roadassist"
            }
            windows {
                iconFile = file("../../docs/icons/roadassist.ico")
                upgradeUuid = "9F2A4E8B-3C1D-4F56-A7E2-8B3C9D1E4F57"
                menuGroup = "RoadAssist"
                perUserInstall = false
                dirChooser = true
            }
            linux {
                packageName = "roadassist"
            }
        }
    }
}