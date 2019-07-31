plugins {
    id("org.kodein.mpp")
}

val kodeinLogVer: String by getRootProject().extra
val kodeinMemoryVer: String by getRootProject().extra

kodein {
    kotlin {
        common.main.dependencies {
            api("org.kodein.log:kodein-log-api:$kodeinLogVer")
            api("org.kodein.memory:kodein-memory:$kodeinMemoryVer")
            api(project(":ldb:kodein-leveldb-api"))

            api("org.jetbrains.kotlin:kotlin-test-common")
            api("org.jetbrains.kotlin:kotlin-test-annotations-common")
        }

        add(kodeinTargets.jvm) {
            target.setCompileClasspath()

            main.dependencies {
                api("org.jetbrains.kotlin:kotlin-test")
                api("org.jetbrains.kotlin:kotlin-test-junit")
                api("junit:junit:4.12")
            }
        }

        add(kodeinTargets.native.host)

        add(kodeinTargets.native.allIos)
    }
}
