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

        add(listOf(kodeinTargets.native.linuxX64, kodeinTargets.native.macosX64))

        add(kodeinTargets.native.allIos)

        allTargets {
            mainCommonCompilation.kotlinOptions.freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
        }
    }
}
