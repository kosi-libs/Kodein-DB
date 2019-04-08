plugins {
    id("org.kodein.mpp")
}

val kotlinxIoVer: String by getRootProject().extra
val kodeinLogVer: String by getRootProject().extra


kodein {
    kotlin {
        common.main.dependencies {
            api("org.kodein.log:kodein-log-api:$kodeinLogVer")
            api(project(":ldb:kodein-leveldb-api"))

            api("org.jetbrains.kotlinx:kotlinx-io:$kotlinxIoVer")
            api("org.jetbrains.kotlin:kotlin-test-common")
            api("org.jetbrains.kotlin:kotlin-test-annotations-common")
        }

        add(kodeinTargets.jvm) {
            target.setCompileClasspath()

            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-io-jvm:$kotlinxIoVer")
                api("org.jetbrains.kotlin:kotlin-test")
                api("org.jetbrains.kotlin:kotlin-test-junit")
                api("junit:junit:4.12")
            }
        }

        add(kodeinTargets.native.linuxX64) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-io-native:$kotlinxIoVer")
            }
        }
    }
}
