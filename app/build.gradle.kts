plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}

// The release workflow passes the tag-derived version name and the monotonic
// GitHub Actions run number. Local builds keep the checked-in defaults.
val releaseVersionName = providers.gradleProperty("versionName").orNull ?: "1.0"
val releaseVersionCode = providers.gradleProperty("versionCode").orNull?.let { value ->
    value.toIntOrNull() ?: error("versionCode must be an integer, got '$value'")
} ?: 1

// Release signing is configured only through environment variables so no
// keystore or password ever needs to live in the repository.
val signingEnvironment = listOf(
    "RELEASE_KEYSTORE_PATH",
    "RELEASE_KEYSTORE_PASSWORD",
    "RELEASE_KEY_ALIAS",
    "RELEASE_KEY_PASSWORD",
)
val signingValues = signingEnvironment.associateWith { name ->
    providers.environmentVariable(name).orNull?.takeIf { it.isNotBlank() }
}
val configuredSigningValues = signingValues.values.count { it != null }
require(
    configuredSigningValues == 0 || configuredSigningValues == signingEnvironment.size,
) {
    "Release signing is partially configured. Set all of: ${signingEnvironment.joinToString()}"
}
val releaseSigningConfigured = configuredSigningValues == signingEnvironment.size

android {
    namespace = "com.pratap.vibhor"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.pratap.vibhor"
        minSdk = 28
        targetSdk = 37
        versionCode = releaseVersionCode
        versionName = releaseVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseSigningConfigured) {
            create("release") {
                storeFile = file(signingValues.getValue("RELEASE_KEYSTORE_PATH")!!)
                storePassword = signingValues.getValue("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = signingValues.getValue("RELEASE_KEY_ALIAS")
                keyPassword = signingValues.getValue("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
