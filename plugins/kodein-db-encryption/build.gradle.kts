plugins {
    id("org.kodein.library.mpp")
}

val kodeinMemoryVer: String by getRootProject().extra

kodein {
    kotlin {
        common {
            main.dependencies {
                api(project(":kdb:kodein-db-api"))
                api("org.kodein.memory:kodein-memory-crypto:$kodeinMemoryVer")
            }
            test.dependencies {
                api(project(":kdb:kodein-db-inmemory"))
                api(project(":test-utils"))
            }
        }

        add(kodeinTargets.jvm.jvm)
        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)

    }
}
