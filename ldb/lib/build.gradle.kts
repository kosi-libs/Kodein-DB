val buildAll = tasks.create("build") {
    group = "build"
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()

class Options {
    val map = HashMap<String, ArrayList<String>>()

    operator fun String.plusAssign(value: String) {
        map.getOrPut(this) { ArrayList() } .add(value)
    }
}

fun addCMakeTasks(lib: String, target: String, conf: Options.() -> Unit): Pair<Task, Task> {

    val srcDir = "${project(":ldb:lib").projectDir}/src/$lib"

    val configure = tasks.create<Exec>("configure${lib.capitalize()}${target.capitalize()}") {
        group = "build"

        workingDir("$buildDir/cmake/$lib-$target")
        commandLine("cmake")

        val options = Options().apply {
            "CMAKE_POSITION_INDEPENDENT_CODE:BOOL" += "1"
            "CMAKE_INSTALL_PREFIX:PATH" += "$buildDir/out/$target"
            "CMAKE_C_FLAGS:STRING" += "-D_GLIBCXX_USE_CXX11_ABI=0"
            "CMAKE_CXX_FLAGS:STRING" += "-D_GLIBCXX_USE_CXX11_ABI=0"
            "CMAKE_BUILD_TYPE" += "Release"

            conf()
        }

        args(options.map.map { "-D${it.key}=${it.value.joinToString(" ")}" } + srcDir)

        inputs.dir(srcDir)
        outputs.dir(workingDir)

        doFirst {
            mkdir(workingDir)
        }
    }

    val build = tasks.create<Exec>("build${lib.capitalize()}${target.capitalize()}") {
        group = "build"

        dependsOn(configure)

        workingDir(configure.workingDir)
        commandLine("make")
        args(
                "all",
                "install",
                "VERBOSE=1"
        )

        inputs.dir(srcDir)
        inputs.dir(configure.workingDir)
        outputs.dir("${buildDir}/out")
    }

    buildAll.dependsOn(build)

    return configure to build
}

fun addTarget(target: String, conf: Options.() -> Unit) {
    val (_, crc32c) = addCMakeTasks("crc32c", target) {
        "CRC32C_BUILD_BENCHMARKS:BOOL" += "0"
        "CRC32C_BUILD_TESTS:BOOL" += "0"
        "CRC32C_USE_GLOG:BOOL" += "0"

        "CMAKE_C_FLAGS:STRING" += "-fPIC"
        "CMAKE_CXX_FLAGS:STRING" += "-fPIC"

        conf()
    }

    val (_, snappy) = addCMakeTasks("snappy", target) {
        "SNAPPY_BUILD_TESTS:BOOL" += "0"

        conf()
    }

    val (leveldb, _) = addCMakeTasks("leveldb", target) {
        "LEVELDB_BUILD_BENCHMARKS:BOOL" += "0"
        "LEVELDB_BUILD_TESTS:BOOL" += "0"

        "CMAKE_C_FLAGS:STRING" += "-I$buildDir/out/$target/include"
        "CMAKE_CXX_FLAGS:STRING" += "-I$buildDir/out/$target/include"
        "CMAKE_EXE_LINKER_FLAGS:STRING" += "-L$buildDir/out/$target/lib -lstdc++"

        conf()
    }

    leveldb.dependsOn(crc32c)
    leveldb.dependsOn(snappy)
}

addTarget("host") {
    "CMAKE_C_COMPILER:STRING" += "clang"
    "CMAKE_CXX_COMPILER:STRING" += "clang++"
}

addTarget("konan") {
    "CMAKE_C_COMPILER:STRING" += "clang"
    "CMAKE_CXX_COMPILER:STRING" += "clang++"

    if (currentOs.isLinux()) {
        "CMAKE_SYSROOT:PATH" += "${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64/x86_64-unknown-linux-gnu/sysroot"
        "CMAKE_C_FLAGS:STRING" += "--gcc-toolchain=${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64"
        "CMAKE_CXX_FLAGS:STRING" += "--gcc-toolchain=${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64"
    }
}

fun addAndroidTarget(target: String) = addTarget("android-$target") {
    "CMAKE_TOOLCHAIN_FILE:PATH" += "/opt/Android/Sdk/ndk-bundle/build/cmake/android.toolchain.cmake"
    "ANDROID_NDK:PATH" += "/opt/Android/Sdk/ndk-bundle/"
    "ANDROID_PLATFORM:STRING" += "android-21"
    "ANDROID_ABI:STRING" += target
}

addAndroidTarget("armeabi-v7a")
addAndroidTarget("arm64-v8a")
addAndroidTarget("x86")
addAndroidTarget("x86_64")

tasks.create<Delete>("clean") {
    delete(buildDir)
}
