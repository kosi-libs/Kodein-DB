package org.kodein.gradle.cmake

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.create


class CMakePlugin : Plugin<Project> {

    private object Default {
        const val androidApi = 21
    }

    @DslMarker
    annotation class CMakeDsl

    @CMakeDsl
    class Extension(private val project: Project) {

        @CMakeDsl
        class Compilation(val conf: CMakeConfigureTask, val build: CMakeBuildTask) {
            fun conf(configure: CMakeConfigureTask.() -> Unit) = conf.configure()
            fun build(configure: CMakeBuildTask.() -> Unit) = build.configure()
        }

        fun compilation(libName: String, configure: Compilation.() -> Unit = {}): Task {

            val conf = project.tasks.create<CMakeConfigureTask>("configure${libName.capitalize()}") {
                initialize(libName)
            }

            val build = project.tasks.create<CMakeBuildTask>("build${libName.capitalize()}") {
                initialize(conf)
            }

            Compilation(conf, build).configure()

            return build
        }

    }

    override fun apply(target: Project) {
        target.extensions.add("cmake", Extension(target))

        val cleanCMake = target.tasks.create<Delete>("cleanCMake") {
            group = "build"
            delete(target.buildDir.resolve("cmake"))
        }

        target.afterEvaluate {
            val clean = target.tasks.maybeCreate("clean").apply { group = "build" }
            clean.dependsOn(cleanCMake)
        }
    }

}
