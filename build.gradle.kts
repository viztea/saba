plugins {
    kotlin("jvm") version "1.5.30"
}

allprojects {
    group = "lol.dimensional"

    repositories {
        maven("https://dimensional.jfrog.io/artifactory/maven")
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://m2.dv8tion.net/releases")
        maven("https://jitpack.io")
        jcenter()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        sourceCompatibility = "16"
        targetCompatibility = "16"
        kotlinOptions {
            jvmTarget = "16"
            incremental = true
            freeCompilerArgs = listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.ExperimentalStdlibApi",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-Xopt-in=kotlin.time.ExperimentalTime",
                "-Xinline-classes",
            )
        }
    }
}

subprojects {
    buildscript {
        repositories {
            mavenCentral()
            maven("https://plugins.gradle.org/m2/")
        }

        dependencies {
            classpath("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7.0.0")
            classpath("org.jetbrains.kotlin:kotlin-serialization:1.5.30")
        }
    }
}
