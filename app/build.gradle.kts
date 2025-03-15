plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "kiit.project.kimsmedicineapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "kiit.project.kimsmedicineapp"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures{
        viewBinding=true
    }
}

dependencies {
    implementation("de.hdodenhof:circleimageview:3.1.0") //Dexter dependency
    implementation("com.karumi:dexter:6.2.3")

    implementation("androidx.room:room-runtime:2.6.1") // Replace with the latest version
    implementation("androidx.room:room-ktx:2.6.1") // For Kotlin extensions
    kapt("androidx.room:room-compiler:2.6.1") // Annotation processor
    implementation("androidx.activity:activity-ktx:1.9.3")// Support for co-routines with activities

    implementation("com.airbnb.android:lottie:6.1.0") // Lottie Animation dependency

    implementation("com.squareup.okhttp3:okhttp:4.12.0") //okhttp

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}