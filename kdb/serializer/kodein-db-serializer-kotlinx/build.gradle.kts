plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        val kotlinxSerializationVer: String by rootProject.extra

        common.main.dependencies {
            api(project(":kdb:kodein-db-api"))
            api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kotlinxSerializationVer")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor-common:$kotlinxSerializationVer")
        }

        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVer")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor:$kotlinxSerializationVer")
            }
        }

        add(kodeinTargets.native.allApple + kodeinTargets.native.allDesktop) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kotlinxSerializationVer")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-cbor-native:$kotlinxSerializationVer")
            }
        }

    }
}
