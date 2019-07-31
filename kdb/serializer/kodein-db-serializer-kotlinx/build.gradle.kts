plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        val kxSerRtVer = "0.11.1"

        common.main.dependencies {
            api(project(":kdb:kodein-db-api"))
            api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kxSerRtVer")
        }

        add(kodeinTargets.jvm) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kxSerRtVer")
            }
        }

        add(listOf(kodeinTargets.native.linuxX64, kodeinTargets.native.macosX64)) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kxSerRtVer")
            }
        }

        add(kodeinTargets.native.allIos) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kxSerRtVer")
            }
        }

        sourceSet(kodeinSourceSets.allNative) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kxSerRtVer")
            }
        }

    }
}
