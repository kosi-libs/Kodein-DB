plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        val kxSerRtVer = "0.13.0"

        common.main.dependencies {
            api(project(":kdb:kodein-db-api"))
            api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kxSerRtVer")
        }

        add(kodeinTargets.jvm) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kxSerRtVer")
            }
        }

        add(kodeinTargets.native.host) {
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
