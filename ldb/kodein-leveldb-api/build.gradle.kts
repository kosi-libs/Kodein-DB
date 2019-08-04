plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-atomicfu")
}

val kodeinLogVer: String by getRootProject().extra
val kodeinMemoryVer: String by getRootProject().extra

kodein {
    kotlin {
        common.main.dependencies {
            api("org.kodein.log:kodein-log-api:$kodeinLogVer")
            api("org.kodein.memory:kodein-memory:$kodeinMemoryVer")
        }

        add(kodeinTargets.jvm) {
            target.setCompileClasspath()
        }

        add(kodeinTargets.native.host)

        add(kodeinTargets.native.allIos)
    }
}
