plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

val ghOwner = providers.gradleProperty("ghOwner").get()
val ghRepo = providers.gradleProperty("ghRepo").get()
val ghUser = providers.gradleProperty("gpr.user")
    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
    .getOrNull().orEmpty()
val ghKey = providers.gradleProperty("gpr.key")
    .orElse(providers.environmentVariable("GITHUB_TOKEN"))
    .getOrNull().orEmpty()

android {
    namespace = "com.kartview.rolodex"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.foundation)
}

publishing {
    publications {
        afterEvaluate {
            register<MavenPublication>("release") {
                groupId = "com.kartview"
                artifactId = "rolodex"
                version = providers.gradleProperty("pkVersion").getOrElse("0.1.0")
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/$ghOwner/$ghRepo")
            credentials {
                username = ghUser
                password = ghKey
            }
        }
    }
}