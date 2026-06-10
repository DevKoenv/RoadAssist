plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

dependencies {
    detektPlugins(libs.detekt.formatting)
}

val coverageThreshold = 60 // raise as coverage improves

kover {
    merge {
        projects(":core", ":server")
    }
    reports {
        total {
            xml {
                onCheck = false
                xmlFile.set(layout.buildDirectory.file("reports/kover/report.xml"))
            }
            verify {
                onCheck = true
                rule {
                    minBound(coverageThreshold)
                }
            }
        }
    }
}

detekt {
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
    source.setFrom(
        fileTree(rootDir) {
            include(
                "**/src/commonMain/**/*.kt",
                "**/src/jvmMain/**/*.kt",
                "**/src/androidMain/**/*.kt",
                "**/src/main/kotlin/**/*.kt",
            )
            exclude("**/build/**")
        }
    )
}