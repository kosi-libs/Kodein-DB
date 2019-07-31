import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

kodein {
    kotlin {

        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
        }

        add(kodeinTargets.jvm)

        add(kodeinTargets.native.host)

        add(kodeinTargets.native.allIos)

        allTargets {
            mainCommonCompilation.kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
        }
    }
}
