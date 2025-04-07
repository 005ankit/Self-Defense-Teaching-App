plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.selfdefense"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.selfdefense"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation ("com.auth0:java-jwt:4.2.1")

//    implementation group:("com.google.android.material"),name:("material"),version:("1.1.0-alpha05")
    implementation ("com.google.android.material:material:1.9.0")


    implementation ("androidx.cardview:cardview:1.0.0")

    // WorkManager
    implementation ("androidx.work:work-runtime:2.8.1")

    // Optional: Notifications
    implementation ("androidx.core:core-ktx:1.12.0")

    // ExoPlayer core
    implementation ("androidx.media3:media3-exoplayer:1.5.0")

    // ExoPlayer UI (required for PlayerView)
    implementation ("androidx.media3:media3-ui:1.5.0")

    // Optional: Common media handling utilities
    implementation ("androidx.media3:media3-common:1.5.0")

    // Gson for JSON serialization/deserialization
    implementation ("com.google.code.gson:gson:2.10.1")

    implementation ("com.cloudinary:cloudinary-android:3.0.2")

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.activity)
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")


    // Google Play Services Location Library
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Firebase BOM for automatic version management
//    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
//    implementation("com.google.firebase:firebase-analytics")
//    implementation("com.google.firebase:firebase-auth")
//    implementation("com.google.firebase:firebase-database")
//    implementation("com.google.firebase:firebase-storage")

    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("org.json:json:20211205")

    // For parsing JSON responses
//    implementation ("com.google.code.gson:gson:2.8.9")
    // AndroidX Libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Unit Testing
    testImplementation("junit:junit:4.13.2")

    // Android Instrumentation Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
