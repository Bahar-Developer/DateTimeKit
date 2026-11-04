plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
    signing
}

android {
    namespace = "com.smart.kahkashan.date_time_kit"
    compileSdk = 37

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
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

// -------------------------------------------------------------
// تنظیمات بخش انتشار روی Maven Central (کتابخانه DateTimeKit)
// -------------------------------------------------------------
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "io.github.bahar-developer"
            artifactId = "datetimekit"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }

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
        }
    }

    repositories {
        maven {
            name = "sonatypeCentral"
            url = uri("https://central.sonatype.com/api/v1/publisher/deployments/upload")
            credentials {
                username = providers.gradleProperty("sonatypeUsername").orNull
                password = providers.gradleProperty("sonatypePassword").orNull
            }
        }
    }
}
