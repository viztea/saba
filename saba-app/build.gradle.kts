plugins {
    application

    id("org.openjfx.javafxplugin") version "0.0.9"
}

apply(plugin = "kotlin")
apply(plugin = "kotlinx-serialization")
apply(plugin = "com.github.johnrengelman.shadow")

description = "A desktop application for saba"
version = "1.0.0"

application {
    mainClass.set("lol.saba.app.SabaApp")
}

javafx {
    version = "15.0.1"
    modules = listOf("javafx.controls")
}

dependencies {
    /* kotlin */
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.2.2")

    /* audio playing */
    implementation("com.sedmelluq:lavaplayer:1.4.6.1")
    implementation("com.github.natanbc:lp-cross:0.1.3-1")
    implementation("com.github.natanbc:native-loader:0.7.2")
    implementation("com.squareup.okio:okio:3.0.0-alpha.10")

    /* server for oauth */
    implementation("com.sparkjava:spark-kotlin:1.0.0-alpha")

    /* ktor */
    
    val ktorVersion = "1.6.3"

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
    implementation("ch.qos.logback:logback-classic:1.2.5")

    /* saba */
    implementation(project(":saba-common"))
}

