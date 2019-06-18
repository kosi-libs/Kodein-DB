plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        common.main.dependencies {
            api(project(":kdb:kodein-db-api"))
            api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.11.0")
        }

        add(kodeinTargets.jvm) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.11.0")
            }
        }

        add(kodeinTargets.native.linuxX64)
        add(kodeinTargets.native.macosX64)

        sourceSet(kodeinSourceSets.allNative) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:0.11.0")
            }
        }

    }
}
