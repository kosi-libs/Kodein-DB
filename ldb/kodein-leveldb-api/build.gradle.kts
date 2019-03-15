plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-atomicfu")
}

val kotlinxIoVer: String by getRootProject().extra
val kotlinxAtomicFuVer: String by getRootProject().extra
val kodeinLogVer: String by getRootProject().extra

kodein {
    kotlin {
        common.main.dependencies {
            api("org.kodein.log:kodein-log-api:$kodeinLogVer")

            api("org.jetbrains.kotlinx:kotlinx-io:$kotlinxIoVer")
            api("org.jetbrains.kotlinx:atomicfu-common:$kotlinxAtomicFuVer")
        }

        add(kodeinTargets.jvm) {
            target.setCompileClasspath()

            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-io-jvm:$kotlinxIoVer")
                compileOnly("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVer")
            }
        }

        add(kodeinTargets.native.linuxX64) {
            main.dependencies {
                api("org.jetbrains.kotlinx:kotlinx-io-native:$kotlinxIoVer")
                api("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }
        }
    }
}
