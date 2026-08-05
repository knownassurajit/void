plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.knownassurajit.app.launcher.voidlauncher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.voidlauncher.app"
        minSdk = 26
        targetSdk = 36

        // Deterministic versioning formula
        val major = 0
        val minor = 0
        val patch = 0
        val build = 10
        
        versionCode = major * 1_000_000 + minor * 10_000 + patch * 100 + build
        versionName = "$major.$minor.$patch.$build"

        resourceConfigurations += listOf("en", "ar", "de", "es-rES", "es-rUS", "fr", "he", "hr", "hu", "in", "it", "ja", "nl", "pl", "pt-rBR", "ru-rRU", "sv", "tr", "uk", "zh")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        disable += listOf("FrequentlyChangingValue", "NullSafeMutableLiveData", "RememberInComposition", "AutoboxingStateCreation")
        abortOnError = true
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.process)
    implementation(libs.work.runtime.ktx)
    implementation(libs.material)

    // Compose BOM controls every other androidx.compose.* artifact's version.
    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))

    implementation(libs.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.compose.ui.text.google.fonts)

    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.profileinstaller)

    // ML Kit GenAI — included in the single-variant OSS build.
    implementation(libs.mlkit.genai.summarization)
    implementation(libs.mlkit.genai.prompt)
    implementation(libs.kotlinx.coroutines.play.services)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
