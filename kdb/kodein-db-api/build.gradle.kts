plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

val kotlinxCoroutinesVer: String by rootProject.extra

kodein {
    kotlin {

        val kotlinxSerializationVer: String by rootProject.extra

        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVer")
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVer")
        }

        add(kodeinTargets.jvm.jvm)
        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)
    }
}

kodeinUpload {
    name = "kodein-db-api"
    description = "Kodein-DB API library"
}