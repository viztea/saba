rootProject.name = "Saba-Root"

include("saba-server")
include("saba-director")
include("saba-app")
include("saba-common")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
