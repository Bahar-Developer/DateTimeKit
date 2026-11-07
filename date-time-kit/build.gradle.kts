import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
}

android {
    namespace = "com.smart.kahkashan.date_time_kit"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    implementation("com.ibm.icu:icu4j:76.1")
}

// تنظیمات تمیز و استاندارد با پلاگین vanniktech (سازگار با ویندوز و پورتال جدید)
mavenPublishing {
    coordinates(
        groupId = "io.github.bahar-developer",
        artifactId = "datetimekit",
        version = "1.0.4"
    )

    pom {
        name.set("DateTimeKit")
        description.set("A powerful Jetpack Compose date and time utility library.")
        url.set("https://github.com/Bahar-Developer/DateTimeKit")

        licenses {
            license {
                name.set("The MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("bahar-developer")
                name.set("Bahar")
                email.set("mahboobeRezaee68@gmail.com")
            }
        }

        scm {
            connection.set("scm:git:github.com/Bahar-Developer/DateTimeKit.git")
            developerConnection.set("scm:git:ssh://github.com/Bahar-Developer/DateTimeKit.git")
            url.set("https://github.com/Bahar-Developer/DateTimeKit/tree/main")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
