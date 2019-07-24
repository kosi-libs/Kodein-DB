import org.kodein.internal.gradle.isExcluded
import java.util.Properties

val buildAll = tasks.create("build") {
    group = "build"
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()

val withAndroid = !isExcluded("android")

class Options {
    val map = HashMap<String, ArrayList<String>>()

    operator fun String.plusAssign(value: String) {
        map.getOrPut(this) { ArrayList() } .add(value)
    }
}

fun addCMakeTasks(lib: String, target: String, conf: Options.() -> Unit): Pair<Task, Task> {

    val srcDir = "${project(":ldb:lib").projectDir}/src/$lib"

    val configure = tasks.create<Exec>("configure${target.capitalize()}${lib.capitalize()}") {
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

    val build = tasks.create<Exec>("build${target.capitalize()}${lib.capitalize()}") {
        group = "build"

        dependsOn(configure)

        workingDir(configure.workingDir)
        commandLine("cmake")
        args(
                "--build", ".",
                "--config", "Release",
                "--target", "install"
        )

        inputs.dir(srcDir)
        inputs.dir(configure.workingDir)
        outputs.dir("${buildDir}/out")
    }

    return configure to build
}

fun addTarget(target: String, conf: Options.() -> Unit) : Task {
    val (_, buildCrc32c) = addCMakeTasks("crc32c", target) {
        "CRC32C_BUILD_BENCHMARKS:BOOL" += "0"
        "CRC32C_BUILD_TESTS:BOOL" += "0"
        "CRC32C_USE_GLOG:BOOL" += "0"

        "CMAKE_C_FLAGS:STRING" += "-fPIC"
        "CMAKE_CXX_FLAGS:STRING" += "-fPIC"

        conf()
    }

    val (_, buildSnappy) = addCMakeTasks("snappy", target) {
        "SNAPPY_BUILD_TESTS:BOOL" += "0"

        conf()
    }

    val (configureLeveldb, buildLevelDB) = addCMakeTasks("leveldb", target) {
        "LEVELDB_BUILD_BENCHMARKS:BOOL" += "0"
        "LEVELDB_BUILD_TESTS:BOOL" += "0"

        "CMAKE_C_FLAGS:STRING" += "-I$buildDir/out/$target/include"
        "CMAKE_CXX_FLAGS:STRING" += "-I$buildDir/out/$target/include"
        "CMAKE_EXE_LINKER_FLAGS:STRING" += "-L$buildDir/out/$target/lib -lstdc++"

        conf()
    }

    configureLeveldb.dependsOn(buildCrc32c)
    configureLeveldb.dependsOn(buildSnappy)

    buildAll.dependsOn(buildLevelDB)

    return buildLevelDB
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

if (withAndroid) {
    val localPropsFile = rootProject.file("local.properties")
    if (!localPropsFile.exists())
        throw IllegalStateException("Please create android root local.properties")
    val localProps = localPropsFile.inputStream().use { Properties().apply { load(it) } }
    val androidSdkDir = localProps["sdk.dir"]
            ?: throw IllegalStateException("Please set sdk.dir android sdk path in root local.properties")

    fun addAndroidTarget(target: String) {
        val build = addTarget("android-$target") {
            "CMAKE_TOOLCHAIN_FILE:PATH" += "$androidSdkDir/ndk-bundle/build/cmake/android.toolchain.cmake"
            "ANDROID_NDK:PATH" += "$androidSdkDir/ndk-bundle/"
            "ANDROID_PLATFORM:STRING" += "android-21"
            "ANDROID_ABI:STRING" += target
        }

        tasks.maybeCreate("buildAndroidLeveldb").apply {
            group = "build"
            dependsOn(build)
        }
    }

    addAndroidTarget("armeabi-v7a")
    addAndroidTarget("arm64-v8a")
    addAndroidTarget("x86")
    addAndroidTarget("x86_64")
}

tasks.create<Delete>("clean") {
    delete(buildDir)
}
