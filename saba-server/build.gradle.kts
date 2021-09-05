plugins {
    application
}

apply(plugin = "kotlin")
apply(plugin = "kotlinx-serialization")

description = "Runs the saba communication server."
version = "1.0.0"

application {
    mainClass.set("lol.saba.server.Launcher")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.2.2")

    implementation("io.ktor:ktor-server-core:1.6.3")
    implementation("io.ktor:ktor-server-cio:1.6.3")
    implementation("io.ktor:ktor-network:1.6.3")

    implementation("ch.qos.logback:logback-classic:1.2.5")
}
