plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

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

        add(kodeinTargets.native.allApple + kodeinTargets.native.allDesktop) {
            main.dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kotlinxSerializationVer")
            }
        }
    }
}

kodeinUpload {
    name = "kodein-db-api"
    description = "Kodein-DB API library"
}