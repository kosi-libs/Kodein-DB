plugins {
    id("org.kodein.library.mpp-with-android")
    id("kotlinx-serialization")
    id("kotlinx-atomicfu")
}

val kotlinxAtomicFuVer: String by rootProject.extra

atomicfu {
    dependenciesVersion = null
}

kodein {
    kotlin {

        common {
            main.dependencies {
                api(project(":kdb:kodein-db-api"))
                api(project(":ldb:kodein-leveldb"))
                implementation("org.jetbrains.kotlinx:atomicfu-common:$kotlinxAtomicFuVer")
            }

            test.dependencies {
                implementation(project(":test-utils"))

                implementation(project(":kdb:serializer:kodein-db-serializer-kotlinx"))
            }
        }

        add(kodeinTargets.jvm) {
            main.dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVer")
            }

            test.dependencies {
                implementation(project(":kdb:serializer:kodein-db-serializer-kryo-jvm"))
                implementation("org.xerial:sqlite-jdbc:3.28.0")
            }
        }

        add(kodeinTargets.android) {
            main.dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVer")
            }

            test.dependencies {
                implementation(project(":kdb:serializer:kodein-db-serializer-kryo-jvm"))
            }
        }

        add(kodeinTargets.native.host) {
            main.dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }
        }

        add(kodeinTargets.native.allIos) {
            main.dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }
        }
    }
}
