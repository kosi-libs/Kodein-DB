plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        val coroutinesVer = "1.3.2"

        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
            api("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$coroutinesVer")
        }

        add(kodeinTargets.jvm) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVer")
            }
        }

        add(listOf(kodeinTargets.native.host) + kodeinTargets.native.allIos) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVer")
            }
        }

        allTargets {
            mainCommonCompilation.kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
        }
    }
}
