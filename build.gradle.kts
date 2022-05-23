@Suppress("PropertyName")
val ktor_version: String by project

plugins {
    val kotlinVersion: String = "1.6.20"
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
}

group = "com.github.landgrafhomyak"
version = "0"

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
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
                api("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:$ktor_version")
            }
        }
    }
}
