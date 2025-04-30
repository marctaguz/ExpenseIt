import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.org.jetbrains.kotlin.kapt)
    alias(libs.plugins.google.dagger.hilt)
    id("com.google.gms.google-services")
    alias(libs.plugins.compose.compiler)
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.expenseit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.expenseit"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        val properties = Properties()
        properties.load(project.rootProject.file("local.properties").inputStream())
        buildConfigField("String", "AZURE_API_KEY", properties.getProperty("AZURE_API_KEY"))

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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.ui.text.google.fonts)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
//    implementation(libs.androidx.room.compiler)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.ui)
    implementation(libs.androidx.material)
    implementation(libs.ui.tooling.preview)

    implementation(libs.animated.navigation.bar)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.play.services.mlkit.document.scanner)
    implementation(libs.coil.compose)
    implementation(libs.text.recognition)

    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.storage)

    implementation(libs.accompanist.swiperefresh)
    implementation(libs.vico.compose.m3)
    implementation(libs.androidchart)
    implementation(libs.reorderable)

    implementation(libs.core)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(kotlin("test"))
    testImplementation(libs.turbine)
    testImplementation(libs.core.testing)
    testImplementation(libs.androidx.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.runner)
    testImplementation(libs.androidx.room.testing)

}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}