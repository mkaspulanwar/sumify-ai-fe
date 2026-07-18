plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

fun String.asBuildConfigString(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")}\""

val debugApiBaseUrl = providers.gradleProperty("SUMIFY_DEBUG_API_BASE_URL")
    .orElse(providers.environmentVariable("SUMIFY_DEBUG_API_BASE_URL"))
    .getOrElse("http://10.0.2.2:8000/")

val releaseApiBaseUrl = providers.gradleProperty("SUMIFY_RELEASE_API_BASE_URL")
    .orElse(providers.environmentVariable("SUMIFY_RELEASE_API_BASE_URL"))
    .getOrElse("")

val verifyReleaseApiBaseUrl by tasks.registering {
    doLast {
        check(releaseApiBaseUrl.isNotBlank()) {
            "Set SUMIFY_RELEASE_API_BASE_URL before building a release."
        }
        check(releaseApiBaseUrl.startsWith("https://")) {
            "SUMIFY_RELEASE_API_BASE_URL must use HTTPS."
        }
    }
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn(verifyReleaseApiBaseUrl)
}

android {
    namespace = "id.antasari.sumifyai"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "id.antasari.sumifyai"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", debugApiBaseUrl.asBuildConfigString())
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", releaseApiBaseUrl.asBuildConfigString())
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
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Retrofit & OkHttp
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.google.gson)

    // Navigation & ViewModel for Compose
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
