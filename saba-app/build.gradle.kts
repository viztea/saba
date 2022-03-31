import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    idea
    java

    id("org.jetbrains.compose") version "1.0.1-rc2"
    kotlin("jvm")
}

apply(plugin = "kotlinx-serialization")
apply(plugin = "com.github.johnrengelman.shadow")

description = "A desktop application for saba"
version = "1.0.0"

compose.desktop {
    application {
        mainClass = "lol.saba.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            packageName = "Saba"
            packageVersion = "1.0.0"

            description = "Desktop Actor for Saba"
            vendor = "Dimensional Fun"
        }
    }

}

repositories {
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    /* kotlin */
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.1")

    /* discord rpc */
    implementation("com.github.Vatuu:discord-rpc:1.6.2")
    implementation("com.github.Vatuu:discord-rpc-binaries:3.4.0")


    /* audio playing */
    implementation("com.sedmelluq:lavaplayer:1.4.6.1")
    implementation("com.github.natanbc:lp-cross:0.1.3-1")
    implementation("com.github.natanbc:native-loader:0.7.2")
    implementation("com.squareup.okio:okio:3.0.0")

    /* server for oauth */
    implementation("com.sparkjava:spark-kotlin:1.0.0-alpha")

    /* ui */
    implementation(compose.desktop.currentOs)

    /* ktor */
    val ktorVersion = "1.6.7"

    // server
    implementation("io.ktor:ktor-serialization:$ktorVersion")

    // client
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    implementation("io.ktor:ktor-network:$ktorVersion")

    /* config */
    implementation("com.typesafe:config:1.4.1")

    /* logging */
    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")

    /* saba */
    implementation(project(":saba-common"))
}

