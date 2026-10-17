import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

/**
 * Signing setup.
 *
 * CI runners start from a clean image, so the auto-generated debug keystore is
 * different on every run. That makes each APK carry a different signature and
 * Android refuses to install a new build over the previous one. To keep every
 * artifact upgradeable we ship one demo key in `keystore/` and use it for both
 * build types.
 *
 * For store distribution, provide a private key through Gradle properties or
 * environment variables (IDEU_STORE_FILE, IEU_STORE_PASSWORD, IEU_KEY_ALIAS,
 * IEU_KEY_PASSWORD) — they take priority when present.
 */
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
        versionCode = 8
        versionName = "1.7.0"

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
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Demo APK: signed with the committed demo key so upgrades install cleanly.
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

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(kotlin("test"))
}
