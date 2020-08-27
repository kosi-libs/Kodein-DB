import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        val kotlinxSerializationVer: String by rootProject.extra

        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kotlinxSerializationVer")
        }

        add(kodeinTargets.jvm.jvm)
        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)

        sourceSets.all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }
}

kodeinUpload {
    name = "kodein-db-api"
    description = "Kodein-DB API library"
}
