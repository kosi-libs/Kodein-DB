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
                implementation("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVer")
            }

            test.dependencies {
                implementation(project(":test-utils"))
                implementation(project(":kdb:serializer:kodein-db-serializer-kotlinx"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVer")
            }
        }

        add(kodeinTargets.jvm.jvm) {
            test.dependencies {
                implementation(project(":kdb:serializer:kodein-db-serializer-kryo-jvm"))
                implementation(project(":ldb:kodein-leveldb-jni"))
            }
        }

        add(kodeinTargets.jvm.android) {
            test.dependencies {
                implementation(project(":kdb:serializer:kodein-db-serializer-kryo-jvm"))
            }
        }

        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)
    }
}

kodeinUpload {
    name = "kodein-db"
    description = "Kodein-DB implementation library"
}