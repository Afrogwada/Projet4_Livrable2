plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
}

android {
  namespace = "com.aura"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aura"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    viewBinding = true
  }
}

dependencies {

  implementation("androidx.core:core-ktx:1.9.0")
  implementation("androidx.appcompat:appcompat:1.6.1")
  implementation("com.google.android.material:material:1.8.0")
  implementation("androidx.annotation:annotation:1.6.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

  // Retrofit (client HTTP)
  implementation("com.squareup.retrofit2:retrofit:2.9.0")

  // Moshi (parser JSON)
  implementation("com.squareup.moshi:moshi:1.15.0")
  implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
  implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

  // Coroutines (asynchrone)
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

  // Lifecycle (déjà ajouté mais je le répète au cas où)
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

  // Lifecycle & ViewModel KTX pour 'by viewModels()' et StateFlow
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
  implementation("androidx.activity:activity-ktx:1.10.1")

  // Kotlin Coroutines pour Flow
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

  // Tests
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}