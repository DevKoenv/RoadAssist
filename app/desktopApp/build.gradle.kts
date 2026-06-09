import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "RoadAssist"
            packageVersion = "1.0.0"
            description = "Roadside assistance dispatch platform"

            macOS {
                bundleID = "dev.koenv.roadassist"
            }
            windows {
                upgradeUuid = "9F2A4E8B-3C1D-4F56-A7E2-8B3C9D1E4F57"
                menuGroup = "RoadAssist"
                perUserInstall = true
            }
            linux {
                packageName = "roadassist"
            }
        }
    }
}