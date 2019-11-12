plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        val coroutinesVer = "1.3.2"
        val serializationVer = "0.13.0"

        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVer")
            compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationVer")
        }

        add(kodeinTargets.jvm) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVer")
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationVer")
            }
        }

        add(listOf(kodeinTargets.native.host) + kodeinTargets.native.allIos) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVer")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serializationVer")
            }
        }

        allTargets {
            mainCommonCompilation.kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
        }
    }
}
