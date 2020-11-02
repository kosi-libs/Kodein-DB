package org.kodein.gradle.cmake

import org.gradle.api.tasks.*
import java.io.File


open class CMakeBuildTask : AbstractExecTask<CMakeBuildTask>(CMakeBuildTask::class.java) {

    private lateinit var conf: CMakeConfigureTask

    @get:Input
    var target: String = "install"

    @get:InputDirectory
    val cmakeProjectDir: File get() = conf.cmakeProjectDir

    @get:OutputDirectory
    val installDir: File
        get() = File(conf.installPath)

    init {
        group = "build"
        executable = "cmake"
    }

    internal fun initialize(conf: CMakeConfigureTask) {
        this.conf = conf
        dependsOn(conf)
        workingDir = conf.workingDir
    }

    override fun exec() {
        val a = mutableListOf("--build", ".")
        a.addAll(args ?: emptyList())
        a.addAll(listOf("--target", target))
        setArgs(a)
        super.exec()
    }

}
