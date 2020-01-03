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
        }

        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVer")
            }
        }

        add(kodeinTargets.native.host) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kotlinxSerializationVer")
            }
        }

        add(kodeinTargets.native.allIos) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kotlinxSerializationVer")
            }
        }

        sourceSet(kodeinSourceSets.allNative) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kotlinxSerializationVer")
            }
        }

    }
}
