package org.kodein.gradle.cmake

import org.gradle.api.tasks.*
import java.io.File


@Suppress("LeakingThis")
open class CMakeConfigureTask : AbstractExecTask<CMakeConfigureTask>(CMakeConfigureTask::class.java) {

    private val options = CMakeOptions()

    @get:Input
    internal lateinit var libName: String

    @get:InputFile
    internal val cmakeListsTxt: File
        get() = cmakeProjectDir.resolve("CMakeLists.txt")

    @get:InputDirectory
    var cmakeProjectDir: File = project.projectDir
        set(value) {
            field = value
            resetCommandLine()
        }

    @get:OutputDirectory
    val outputDirectory: File get() = workingDir

    @get:Input
    var command: List<String> = listOf("cmake")

    @get:Input
    lateinit var installPath: String

    init {
        group = "build"
        resetCommandLine()
    }

    internal fun initialize(libName: String) {
        this.libName = libName
        workingDir = project.buildDir.resolve("cmake/build/$libName")
        installPath = project.buildDir.resolve("cmake/out/$libName").absolutePath
    }

    private fun resetCommandLine() {
        setCommandLine(command + options.raw + options.defines.map { "-D${it.key}=${it.value.joinToString(" ")}" } + cmakeProjectDir.absolutePath)
    }

    fun cmakeOptions(builder: CMakeOptions.() -> Unit) {
        options.builder()
        resetCommandLine()
    }

    override fun exec() {
        cmakeOptions {
            "CMAKE_INSTALL_PREFIX:PATH" += installPath
        }
        project.mkdir(workingDir)
        super.exec()
    }
}
