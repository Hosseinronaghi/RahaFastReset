plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "ir.raha.fastreset"
    compileSdk = 35
    defaultConfig {
        applicationId = "ir.raha.fastreset"
        minSdk = 21
        targetSdk = 32
        versionCode = 2
        versionName = "1.0.1"
        resValue("string", "variant_label", "Unified • automatic mode")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
}
