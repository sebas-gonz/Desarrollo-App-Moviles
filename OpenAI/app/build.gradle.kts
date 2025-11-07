import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}


// 🔑 Cargar archivo keys.properties
val keysPropertiesFile = rootProject.file("key.properties")
val keysProperties = Properties()
if (keysPropertiesFile.exists()) {
    keysProperties.load(keysPropertiesFile.inputStream())
}


android {
    namespace = "com.example.openai"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.openai"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String",
            "OPENAI_API_KEY",
            "\"${keysProperties.getProperty("OPENAI_API_KEY") ?: ""}\""
        )
        buildConfigField(
            "String", "OWM_API_KEY", "\"${keysProperties.getProperty("OWM_API_KEY") ?: ""}\""
        )
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //implementacion de las dependencias para las corrutinas (funciones asincronas)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    // Núcleo de Retrofit: cliente HTTP para consumir APIs REST con interfaces anotadas (@GET, @POST, etc.)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // Convertidor JSON <-> data classes usando Moshi dentro de Retrofit (MoshiConverterFactory)
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    // Interceptor de OkHttp para loguear requests/responses (útil en debug)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // Coroutines Soporte de coroutines en Android (Dispatcher.Main, launch en UI, etc.)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    // Extensiones KTX de Lifecycle: lifecycleScope, repeatOnLifecycle y helpers para coroutines
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    // Adaptadores de Moshi para Kotlin (null-safety, data classes, KotlinJsonAdapterFactory)
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")

    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
}