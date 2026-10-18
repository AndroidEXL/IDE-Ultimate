import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

val demoKeystore = rootProject.file("keystore/ide-ultimate.p12")

fun secret(name: String): String? =
    (project.findProperty(name) as String?) ?: System.getenv(name)

val productionStoreFile = secret("IEU_STORE_FILE")

android {
    namespace = "com.androidexl.ideultimate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.androidexl.ideultimate"
        minSdk = 24
        targetSdk = 35
        versionCode = 31
        versionName = "1.30.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (productionStoreFile != null) {
            create("production") {
                storeFile = file(productionStoreFile)
                storePassword = secret("IEU_STORE_PASSWORD")
                keyAlias = secret("IEU_KEY_ALIAS")
                keyPassword = secret("IEU_KEY_PASSWORD")
            }
        }
        if (demoKeystore.exists()) {
            create("demo") {
                storeFile = demoKeystore
                storePassword = "ideultimate"
                keyAlias = "ideultimate"
                keyPassword = "ideultimate"
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.findByName(
                if (productionStoreFile != null) "production" else "demo",
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName(
                if (productionStoreFile != null) "production" else "demo",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)

    // Firebase (Auth + Realtime Database + Storage configured via google-services.json)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.common)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(kotlin("test"))
}
