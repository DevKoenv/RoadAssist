import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val buildNumber = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 0

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.app.shared)

    implementation(libs.androidx.activity.compose)
    implementation(libs.play.services.location)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "dev.koenv.roadassist"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    base.archivesName = "roadassist"

    defaultConfig {
        applicationId = "dev.koenv.roadassist"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = maxOf(1, buildNumber)
        versionName = if (buildNumber > 0) "1.1.$buildNumber" else "1.1.0"

        val localProps = Properties()
        val localPropsFile = rootProject.file("local.properties")
        if (localPropsFile.exists()) localPropsFile.inputStream().use { localProps.load(it) }
        resValue("string", "server_url", localProps.getProperty("serverUrl", "https://roadassist.koenv.dev"))
    }
    buildFeatures {
        resValues = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        val keystoreFile = System.getenv("KEYSTORE_FILE")
        val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
        val keyAlias = System.getenv("KEY_ALIAS")
        val keyPassword = System.getenv("KEY_PASSWORD")

        if (keystoreFile != null && keystorePassword != null && keyAlias != null && keyPassword != null) {
            create("release") {
                storeFile = file(keystoreFile)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.findByName("release")
        }
    }
    // Fail explicitly at execution time if release signing env vars are absent
    tasks.matching { it.name == "assembleRelease" || it.name == "bundleRelease" }.configureEach {
        doFirst {
            listOf("KEYSTORE_FILE", "KEYSTORE_PASSWORD", "KEY_ALIAS", "KEY_PASSWORD").forEach { key ->
                requireNotNull(System.getenv(key)) { "$key env var is required for release builds" }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    lint {
        // Project uses ComponentActivity directly; no Fragments are in use.
        // The transitive fragment:1.1.0 artifact (pulled in by legacy deps) triggers
        // this lint check as a false positive.
        disable += "InvalidFragmentVersionForActivityResult"
    }
}