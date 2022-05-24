import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

val ktorVersion: String by project
val kotlinxSerializationVersion: String by project
val kotlinxDatetimeVersion: String by project

plugins {
    val kotlinVersion: String = "1.6.20"
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    `maven-publish`
}

group = "com.github.landgrafhomyak"
version = "0.0a0"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js(BOTH) {
        nodejs()
    }

    // ios() // doesn't compiles
    // linuxArm32Hfp() // Not supported by kotlinx.datetime
    // linuxArm64() // Not supported by kotlinx.datetime
    // linuxMips32() // Not supported by kotlinx.datetime
    // linuxX64() // doesn't compiles
    // macosArm64() // doesn't compiles
    // macosX64() // doesn't compiles
    // mingwX86() // Not supported by kotlinx.serialization.json
    // mingwX64() doesn't compiles
    // tvos() // doesn't compiles
    // watchos() // doesn't compiles

    /* wasm { // Not supported by ktor.serialization.json
        nodejs()
    } */

    targets
        .filter { t -> t.platformType == KotlinPlatformType.native }
        .map { c -> c as KotlinNativeTarget }
        .forEach { t ->
            t.binaries.sharedLib(t.name, NativeBuildType.values().asList())
        }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
                api("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
