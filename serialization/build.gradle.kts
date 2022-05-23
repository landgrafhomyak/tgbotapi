@Suppress("PropertyName")
val ktor_version: String by project

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

repositories {
    mavenCentral()
}

kotlin {
    jvm {
    }

    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.3.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("com.google.devtools.ksp:symbol-processing-api:1.6.20-1.0.5")
            }
        }
    }
}