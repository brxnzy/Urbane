import java.util.Properties
import java.io.FileReader

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.2.20"
}

val localProperties = rootProject.file("local.properties").takeIf { it.exists() }?.let {
    Properties().apply { load(FileReader(it)) }
} ?: Properties()

val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: "DEFAULT_URL"
val supabaseKey = localProperties.getProperty("SUPABASE_KEY") ?: "DEFAULT_KEY"

android {
    namespace = "com.example.urbane"
    compileSdk = 36

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.urbane"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 🔹 BuildConfig con variables de entorno
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation(platform("io.github.jan-tennert.supabase:bom:3.2.4"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:functions-kt")
    implementation("io.ktor:ktor-client-android:3.3.1")
    implementation("androidx.datastore:datastore-core-android:1.1.0")
    implementation("androidx.datastore:datastore-preferences:1.1.0")
    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation(libs.androidx.benchmark.traceprocessor.android)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.2.20")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.0")
    implementation("io.ktor:ktor-client-core:2.3.3")
    implementation("io.ktor:ktor-client-cio:2.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.3")








    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}