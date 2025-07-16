plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}

buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}