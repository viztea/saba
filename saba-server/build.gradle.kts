plugins {
    application
}

apply(plugin = "kotlin")
apply(plugin = "kotlinx-serialization")
apply(plugin = "com.github.johnrengelman.shadow")

description = "Runs the saba communication server."
version = "1.0.0"

application {
    mainClass.set("lol.saba.server.Saba")
}

dependencies {
    /* kotlin */
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.10")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.1")

    /* ktor */
    val ktorVersion = "1.6.7"
    
    // server
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-network:$ktorVersion")

    // client
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")
    
    /* logging */
    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")

    /* config */
    implementation("com.typesafe:config:1.4.1")

    /* saba */
    implementation(project(":saba-common"))
}
