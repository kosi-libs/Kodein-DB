plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        val kotlinxCoroutinesVer: String by rootProject.extra
        val kotlinxSerializationVer: String by rootProject.extra

        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
            compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kotlinxSerializationVer")
        }

        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVer")
            }
        }

        add(listOf(kodeinTargets.native.host) + kodeinTargets.native.allIos) {
            main.dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kotlinxSerializationVer")
            }
        }
    }
}
