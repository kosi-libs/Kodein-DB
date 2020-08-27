plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        val kotlinxSerializationVer: String by rootProject.extra

        common.main.dependencies {
            api(project(":kdb:kodein-db-api"))
            api("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVer")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:$kotlinxSerializationVer")
        }

        add(kodeinTargets.jvm.jvm)
        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)
    }
}

kodeinUpload {
    name = "kodein-db-serializer-kotlinx"
    description = "Kodein-DB with KotlinX serializer library"
}