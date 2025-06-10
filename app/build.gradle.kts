plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.uocdaily"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.uocdaily"
        minSdk = 24
        targetSdk = 35
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

    // Add this to handle potential resource conflicts
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Firebase BOM - This ensures all Firebase libraries use compatible versions
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase Libraries (versions managed by BOM) - REMOVE duplicate entries
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-analytics")

    // Google Play Services
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // AndroidX Libraries - Updated versions
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity:1.9.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")

    implementation ("com.github.bumptech.glide:glide:4.15.1")
    // for get firebase data
    implementation ("com.google.firebase:firebase-auth:22.3.0")
    implementation ("com.google.firebase:firebase-firestore:24.9.1")
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation ("de.hdodenhof:circleimageview:3.1.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.google.firebase:firebase-auth:22.3.0")
    implementation ("com.google.firebase:firebase-firestore:24.10.0")

    implementation ("com.google.firebase:firebase-auth:22.3.0")


    implementation ("com.google.firebase:firebase-database:20.3.0")


    //admin user mangment
    implementation ("com.google.firebase:firebase-database:20.3.0:")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")

    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


}