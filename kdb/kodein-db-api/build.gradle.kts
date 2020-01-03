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
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$kotlinxCoroutinesVer")
            compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kotlinxSerializationVer")
        }

        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVer")
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVer")
            }
        }

        add(listOf(kodeinTargets.native.host) + kodeinTargets.native.allIos) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$kotlinxCoroutinesVer")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kotlinxSerializationVer")
            }
        }
    }
}
