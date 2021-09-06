plugins {
    application
}

apply(plugin = "kotlin")
apply(plugin = "kotlinx-serialization")

description = "common kotlin stuff that might be shared among a client and server lol."
version = "1.0.0"

application {
    mainClass.set("lol.saba.server.Launcher")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.2.2")
    implementation("com.sedmelluq:lavaplayer:1.4.6.1")
    implementation("io.ktor:ktor-network:1.6.3")
}
