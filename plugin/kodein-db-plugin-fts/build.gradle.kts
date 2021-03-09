plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        val kotlinxSerializationVer: String by rootProject.extra

        common {
            main.dependencies {
                api(project(":kdb:kodein-db-api"))
            }
            test.dependencies {
                implementation(project(":test-utils"))
                implementation(project(":kdb:serializer:kodein-db-serializer-kotlinx"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVer")
                implementation(project(":kdb:kodein-db-inmemory"))
            }
        }

        add(kodeinTargets.jvm.jvm) {}

//        add(kodeinTargets.native.allApple + kodeinTargets.native.allDesktop) {
//            main.dependencies {
//            }
//        }

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
    }
}
