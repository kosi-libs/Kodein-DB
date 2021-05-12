import com.android.build.gradle.tasks.factory.AndroidUnitTest

plugins {
    id("org.kodein.library.mpp-with-android")
    id("kotlinx-serialization")
    id("kotlinx-atomicfu")
}

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

        val kotlinxAtomicFuVer: String by rootProject.extra
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
                implementation(project(":kdb:kodein-db-inmemory"))
                implementation(project(":plugins:kodein-db-encryption"))
            }
        }

        add(kodeinTargets.jvm.jvm) {
            test.dependencies {
                implementation(project(":kdb:serializer:kodein-db-serializer-kryo-jvm"))
                implementation(project(":ldb:jni:kodein-leveldb-jni-jvm"))
            }
        }

        add(kodeinTargets.jvm.android) {
            test.dependencies {
                implementation(project(":kdb:serializer:kodein-db-serializer-kryo-jvm"))
            }
        }

        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}

kodeinUpload {
    name = "kodein-db"
    description = "Kodein-DB implementation library"
}