import com.android.build.gradle.tasks.factory.AndroidUnitTest

plugins {
    id("org.kodein.library.mpp-with-android")
    id("kotlinx-serialization")
    id("kotlinx-atomicfu")
}

val kotlinxAtomicFuVer: String by rootProject.extra

atomicfu {
    dependenciesVersion = null
}

afterEvaluate {
    tasks.withType<AndroidUnitTest>().all {
        enabled = false
    }
}

kodein {
    kotlin {

        val kotlinxSerializationVer: String by rootProject.extra

        common {
            main.dependencies {
                api(project(":kdb:kodein-db-api"))
                api(project(":ldb:kodein-leveldb"))
                implementation("org.jetbrains.kotlinx:atomicfu-common:$kotlinxAtomicFuVer")
            }

            test.dependencies {
                implementation(project(":test-utils"))
                implementation(project(":kdb:serializer:kodein-db-serializer-kotlinx"))
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kotlinxSerializationVer")
            }
        }

        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVer")
            }

            test.dependencies {
                implementation(project(":kdb:serializer:kodein-db-serializer-kryo-jvm"))
                implementation(project(":ldb:kodein-leveldb-jni"))
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVer")
            }
        }

        add(kodeinTargets.jvm.android) {
            main.dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVer")
            }

            test.dependencies {
                implementation(project(":kdb:serializer:kodein-db-serializer-kryo-jvm"))
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kotlinxSerializationVer")
            }
        }

        add(kodeinTargets.native.allApple + kodeinTargets.native.allDesktop) {
            main.dependencies {
                implementation("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }
            test.dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kotlinxSerializationVer")
            }
        }
    }
}
