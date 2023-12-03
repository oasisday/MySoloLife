plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
    id("kotlin-kapt")

}

android {
    namespace = "com.example.mysololife"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mysololife"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }


    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }



    /*
    //추가해준거
    externalNativeBuild{
        cmake{
            path = file("CMakeLists.txt")
        }
    }
     */
}

dependencies {

    //notification
    //noinspection GradleCompatible
    implementation ("com.android.support:support-compat:28.0.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database-ktx:20.3.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // [파이어베이스 관련 dependency]
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-analytics-ktx")

    //auth
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    //stroage
    implementation("com.google.firebase:firebase-storage-ktx")

    //cloud messaging
    implementation ("com.google.firebase:firebase-messaging-ktx")


    // [UI 관련 dependency]

    //glide 불러오기
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation ("androidx.activity:activity-ktx:1.2.0-alpha04")
    implementation ("androidx.fragment:fragment-ktx:1.3.0-alpha04")

    //circleImg
    implementation ("de.hdodenhof:circleimageview:3.1.0")

    //lottie animation
    implementation ("com.airbnb.android:lottie:5.2.0")

    //시간표 library
    implementation ("com.github.islandparadise14:Mintable:1.5.1")
    implementation ("com.github.skydoves:colorpickerview:2.3.0")

    //boardUI
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    //card stack view
    implementation ("com.yuyakaido.android:card-stack-view:2.3.4")


    // [기능 관련 dependency]

    // 레트로핏 (인터넷 불러오기)
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")

    //okhttp
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")

    // 룸데이터 베이스 저장소 제작
    val room_version = "2.5.0"
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    //camera
    implementation("io.fotoapparat:fotoapparat:2.7.0")
    implementation("com.jraska:falcon:2.2.0")

    //kakao 로그인
    implementation ("com.kakao.sdk:v2-user:2.16.0")

    // translator
    implementation("com.google.mlkit:translate:17.0.2")
    implementation(platform("com.google.firebase:firebase-bom:32.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-functions:20.4.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-chinese:16.0.0")
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-devanagari:16.0.0")
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-japanese:16.0.0")
    implementation ("com.google.android.gms:play-services-mlkit-text-recognition-korean:16.0.0")

    //alarm manager
    implementation("com.github.ColdTea-Projects:SmplrAlarm:2.1.1")
}