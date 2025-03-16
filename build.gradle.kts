plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}

buildscript {
    dependencies {
        // Add the Google services classpath here
        classpath("com.google.gms:google-services:4.3.15") // Use the latest version
    }
}