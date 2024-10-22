plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt") version "1.9.22" // Ensure this matches your Kotlin version
    id("dagger.hilt.android.plugin")
}


android {
    namespace = "com.guahoo.app.presentation"
    compileSdk = extra["androidCompileSdkVersion"] as Int

    defaultConfig {
        minSdk = extra["androidMinSdkVersion"] as Int
        targetSdk = extra["androidTargetSdkVersion"] as Int
        versionCode = extra["androidVersionCode"] as Int
        versionName = extra["androidVersionName"] as String
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        multiDexEnabled = true
    }




    signingConfigs {}

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")

        }

        create("pre") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        create("dev") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")

        }

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "default"


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"  // Use a compatible Compose Compiler version
    }

    packaging {
        resources {
            excludes += "/META-INF/sdk_release.kotlin_module"
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs {
                srcDirs("libs")
            }
        }
    }
}

dependencies {
    implementation(project(":app:domain"))
    implementation(project(":app:data"))


    implementation(libs.androidx.activity.ktx)
    implementation(libs.coil.kt.coil.compose)
    kapt(libs.kotlinx.metadata.jvm)
    implementation (libs.androidx.activity.compose)
    implementation (libs.androidx.navigation.compose)

    implementation (libs.androidx.foundation)
    implementation (libs.ui)
    implementation (libs.material3)
    implementation (libs.androidx.navigation.compose)

    // Hilt dependencies
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.hilt.compiler)
    implementation (libs.google.accompanist.systemuicontroller)

    //Map
    implementation(libs.locationtech.jts.core)
    implementation (libs.osmdroid.android)
    implementation (libs.osmdroid.wms)      // WMS support if needed
    implementation (libs.osmdroid.mapsforge)// Optional, if using Mapsforge
  //  implementation("org.osmdroid:osmbonuspack:6.9.0") // Marker clustering support

}
