import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    kotlin("kapt") version "1.9.22"
}

allprojects {

    extra.apply {
        set("androidApplicationId", "com.guahoo.sacartrailo")
        set("androidVersionCode", 1_000_000)
        set("androidVersionName", "1.00.0")
        set("androidMinSdkVersion", 26)
        set("androidTargetSdkVersion", 34)
        set("androidCompileSdkVersion", 34)
    }

}

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

buildscript {
    repositories {
        google()
        mavenCentral()
        maven ("https://jitpack.io")

    }

    dependencies {
        // Make sure libs.hilt.android.gradle.plugin is defined in your version catalog (libs.versions.toml)
        classpath(libs.hilt.android.gradle.plugin)
    }
}
