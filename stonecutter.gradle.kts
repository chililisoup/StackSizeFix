plugins {
    id("dev.kikugie.stonecutter")

    val modstitchVersion = "0.5.15-unstable"
    id("dev.isxander.modstitch.base") version modstitchVersion apply false
    id("dev.isxander.modstitch.shadow") version modstitchVersion apply false
}
stonecutter active "1.21.1-neoforge"

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.fabricmc.net/")
    }
}