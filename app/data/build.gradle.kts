plugins {
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt") version "1.9.22"
    id ("com.android.library")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = 34

    defaultConfig {
        minSdk = extra["androidMinSdkVersion"] as Int
        namespace = "com.guahoo.data"
        buildConfigField("long", "VERSION_CODE", "${extra["androidVersionCode"] as Int}")
        buildConfigField("String","VERSION_NAME","\"${extra["androidVersionName"]}\"")
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"b542736e613d2382837ad821803eb507\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        getByName("release") {

            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        create("pre") {
            // isMinifyEnabled = true
            //proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        create("dev") {
            // isMinifyEnabled = true
            // proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
            // isMinifyEnabled = true
            // proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    lint {
        abortOnError = false
        disable.add("InvalidPackage")
        disable.add("OldTargetApi")
        ignoreWarnings = true
        quiet = true
    }

    buildFeatures {
        buildConfig = true
    }

}



dependencies {
    implementation(project(":app:domain"))
    implementation(libs.retrofit)
    implementation(libs.squareup.logging.interceptor)
    implementation(libs.converter.gson)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")

    // Hilt dependencies
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    kapt(libs.hilt.compiler)

    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // To use Kotlin annotation processing tool (kapt)
    kapt("androidx.room:room-compiler:$room_version")
    // To use Kotlin Symbol Processing (KSP)
//    ksp("androidx.room:room-compiler:$room_version")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")

    // optional - RxJava2 support for Room
    implementation("androidx.room:room-rxjava2:$room_version")

    // optional - RxJava3 support for Room
    implementation("androidx.room:room-rxjava3:$room_version")

    // optional - Guava support for Room, including Optional and ListenableFuture
    implementation("androidx.room:room-guava:$room_version")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")

    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")
}