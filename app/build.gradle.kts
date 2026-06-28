import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localPropertiesFile = rootProject.file("local.properties")
val properties = Properties()

if (localPropertiesFile.exists()) {
    properties.load(FileInputStream(localPropertiesFile))
}
android {
    namespace = "com.okane.paperaero"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.okane.paperaero"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //환경변수
        val serverUrl = properties.getProperty("GO_SERVER_URL") ?: "\"http://localhost:8080\""
        buildConfigField("String", "GO_SERVER_URL", serverUrl)
        val liveKitUrl = properties.getProperty("LIVE_KIT_SERVER_URL") ?: "\"ws://localhost:8080/livekit\""
        buildConfigField("String", "LIVE_KIT_SERVER_URL", liveKitUrl)
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("io.livekit:livekit-android:2.4.0")
    implementation("io.livekit:livekit-android-compose-components:2.4.0")
}