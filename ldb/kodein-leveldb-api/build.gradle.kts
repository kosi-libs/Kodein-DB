plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-atomicfu")
}

atomicfu {
    dependenciesVersion = null
}

val kodeinLogVer: String by getRootProject().extra
val kodeinMemoryVer: String by getRootProject().extra

kodein {
    kotlin {

        val kotlinxAtomicFuVer: String by rootProject.extra

        common.main.dependencies {
            api("org.kodein.log:kodein-log:$kodeinLogVer")
            api("org.kodein.memory:kodein-memory:$kodeinMemoryVer")
            implementation("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVer")
        }

        add(kodeinTargets.jvm.jvm) {
            target.setCompileClasspath()
        }

        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)
    }
}

kodeinUpload {
    name = "kodein-leveldb-api"
    description = "LevelDB API library"
}