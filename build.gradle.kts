plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.modstitch.base")
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
        ?.let(consumer)
}

class ModData {
    val version = property("mod.version") as String
    val group = property("mod.group") as String
    val id = property("mod.id") as String
    val name = property("mod.name") as String
    val authors = property("mod.authors") as String
    val description = property("mod.description") as String
    val homepage = property("mod.homepage") as String
    val sources = property("mod.sources") as String
    val issues = property("mod.issues") as String
    val license = property("mod.license") as String
}

val mod = ModData()
val minecraft = property("deps.minecraft") as String

// Stonecutter constants for mod loaders.
// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
var loader: String = name.split("-")[1]
stonecutter {
    constants {
        match(loader, "fabric", "neoforge")
    }
}

modstitch {
    minecraftVersion = minecraft

    // Alternatively use stonecutter.eval if you have a lot of versions to target.
    // https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions
    javaTarget = when (minecraft) {
        "1.21.1" -> 21
        else -> throw IllegalArgumentException("Please store the java version for ${property("deps.minecraft")} in build.gradle.kts!")
    }

    // If parchment doesn't exist for a version yet, you can safely
    // omit the "deps.parchment" property from your versioned gradle.properties
    parchment {
        prop("deps.parchment") { mappingsVersion = it }
    }

    // This metadata is used to fill out the information inside
    // the metadata files found in the templates folder.
    metadata {
        modId = mod.id
        modName = mod.name
        modVersion = "${mod.version}-$loader"
        modGroup = mod.group
        modAuthor = mod.authors
        modLicense = mod.license
        modDescription = mod.description

        fun <K, V> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            put("mod_homepage", mod.homepage)
            put("mod_sources", mod.sources)
            put("mod_issues", mod.issues)
            put("mod_author_list", mod.authors.split(", ").joinToString("\",\""))
            prop("deps.minecraft_range") { put("minecraft_range", it) }
        }
    }

    // Fabric Loom (Fabric)
    loom {
        prop("deps.fabric_loader") { fabricLoaderVersion = it }

        // Configure loom like normal in this block.
        configureLoom {
            runConfigs.all {
                ideConfigGenerated(false)
            }

            runs {
                register("testClient") {
                    client()
                    name = "Test Client"
                    vmArgs("-Dmixin.debug.export=true")
                    runDir = "../../run"
                    ideConfigGenerated(true)
                }
            }
        }
    }

    // ModDevGradle (NeoForge, Forge, Forgelike)
    moddevgradle {
        enable {
            prop("deps.neoforge") { neoForgeVersion = it }
        }

        configureNeoforge {
            runs {
                register("testClient") {
                    client()
                    gameDirectory = layout.projectDirectory.dir("../../run")
                }
            }
        }
    }

    mixin {
        addMixinsToModManifest = true
        configs.register(mod.id)
    }
}

tasks.named("generateModMetadata") {
    dependsOn("stonecutterGenerate")
}
modstitch.moddevgradle {
    tasks.named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }
}

val finalJarTasks = listOf(
    modstitch.finalJarTask
)
tasks.register<Copy>("buildAndCollect") {
    group = "build"

    finalJarTasks.forEach { jar ->
        dependsOn(jar)
        from(jar.flatMap { it.archiveFile })
    }

    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn("build")
}

tasks.register<Delete>("buildCollectAndClean") {
    group = "build"

    delete(layout.buildDirectory.dir("libs"))
    delete(layout.buildDirectory.dir("devlibs"))

    dependsOn("buildAndCollect")
}

tasks.register<Delete>("deleteBuildCache") {
    group = "build"

    delete(layout.buildDirectory)
}