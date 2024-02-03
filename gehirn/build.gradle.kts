plugins {
    alias(libs.plugins.kotlinMultiplatform)

    alias(libs.plugins.jetbrainsCompose)
    kotlin("plugin.serialization") version "1.9.21"
}

group = "org.cec.brick"
version = "1.0-SNAPSHOT"

kotlin {
    jvm("desktop")
    sourceSets {
        val desktopMain by getting
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.1")
            implementation("io.insert-koin:koin-core:3.5.3")
            implementation("io.insert-koin:koin-compose:1.1.2")
//            implementation("io.insert-koin:koin-core-coroutines:3.5.3")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(project(":engine"))
        }
    }
}