plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //dependencias para firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.proyecto.appmascotasinicial"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.proyecto.appmascotasinicial"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    //dependencias para firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("com.google.firebase:firebase-firestore:24.11.0")

    //fragments
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    //settings preferences
    implementation("androidx.preference:preference-ktx:1.2.1")

    //datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    //permite convertir urls en imágenes
    implementation("com.squareup.picasso:picasso:2.5.2")

    //corrutinas
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    //para que al presionar enviar comentarios o los productos de marketplace permita abrir el navegador
    implementation("androidx.browser:browser:1.8.0")

    //recyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    //para el reinicio de tareas
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    //para cargar imagen url
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    //para las notificaciones
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}