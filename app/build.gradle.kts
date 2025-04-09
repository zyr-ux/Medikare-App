plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.project.medikare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.project.medikare"
        minSdk = 29
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
    implementation("androidx.room:room-runtime:2.6.1") // Room DB dependency
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1") // Annotation processor
    implementation("androidx.activity:activity-ktx:1.10.1")// Support for co-routines with activities
    implementation("com.airbnb.android:lottie:6.1.0") // Lottie Animation dependency
    implementation("com.squareup.okhttp3:okhttp:4.12.0") //okhttp
    implementation("com.google.code.gson:gson:2.12.1")
    // Firebase dependencies
    implementation (platform("com.google.firebase:firebase-bom:32.2.3"))
    implementation ("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.facebook.shimmer:shimmer:0.5.0") //Facebook Shimmer
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}