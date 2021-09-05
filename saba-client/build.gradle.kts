plugins {
    application
}

apply(plugin = "kotlin")
apply(plugin = "kotlinx-serialization")

description = "A desktop application for saba"
version = "1.0.0"

application {
    mainClass.set("lol.saba.server.Launcher")
}

dependencies {
    /* kotlin */
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.2.2")

    /* ktor */
    implementation("io.ktor:ktor-network:1.6.3")

    /* logging */
    implementation("ch.qos.logback:logback-classic:1.2.5")

    /* saba */
    implementation(project(":saba-common"))
}

