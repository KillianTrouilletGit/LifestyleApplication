plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}


import java.util.Properties
import java.io.FileInputStream

val properties = Properties()
val localPropertiesFile = project.rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    properties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.personallevelingsystem"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.personallevelingsystem"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "USDA_API_KEY", "\"${properties.getProperty("USDA_API_KEY")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${properties.getProperty("GEMINI_API_KEY")}\"")
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation(libs.androidx.compose.runtime.livedata)
    implementation(libs.google.api.client.android)
    implementation("com.google.http-client:google-http-client-gson:1.40.1")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")
    implementation("androidx.fragment:fragment-ktx:1.8.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.jjoe64:graphview:4.2.2")


    implementation(libs.play.services.drive)
    implementation("com.google.api-client:google-api-client:2.0.0")
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.work.runtime.ktx)

    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.android.gms:play-services-auth:20.5.0")
    // implementation("com.google.cloud:google-cloud-datastore:2.1.0") // Removed for GitHub cleanup
    implementation(libs.google.ai.client.generativeai)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    configurations.all {
        exclude(group = "com.android.support")
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
    }
}