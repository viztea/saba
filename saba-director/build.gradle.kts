plugins {
    application
}

apply(plugin = "kotlin")
apply(plugin = "kotlinx-serialization")

description = "a saba director, in the form of a discord bot."
version = "1.0.0"

application {
    mainClass.set("lol.saba.director.discord.Bot")
}

dependencies {
    /* kotlin */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-RC3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.3.1")

    /* discord */
    implementation("dev.kord:kord-core:0.8.0-M5")
    implementation("com.sedmelluq:lavaplayer:1.4.6.1")

    /* saba */
    implementation(project(":saba-common"))

    /* config */
    implementation("com.typesafe:config:1.4.1")

    /* networking */
    implementation("io.ktor:ktor-network:1.6.7")

    /* logging */
    implementation("ch.qos.logback:logback-classic:1.2.8")
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.20")
}

