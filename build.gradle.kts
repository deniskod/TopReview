plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}

buildscript {
    dependencies {
        // Add the Google services classpath here
        classpath("com.google.gms:google-services:4.3.15") // Use the latest version
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")  // Kotlin version 1.9.22
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}