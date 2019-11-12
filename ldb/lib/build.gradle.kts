import org.jetbrains.kotlin.gradle.targets.js.npm.SemVer
import org.kodein.internal.gradle.isExcluded
import java.util.*

val buildAll = tasks.create("build") {
    group = "build"
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()!!

val withAndroid = !isExcluded("android")

class CMakeOptions {
    val defines = HashMap<String, ArrayList<String>>()
    val raw = ArrayList<String>()

    operator fun String.plusAssign(value: String) {
        defines.getOrPut(this) { ArrayList() } .add(value)
    }

    operator fun String.unaryPlus() { this += "1" }

    operator fun String.minusAssign(value: String) {
        raw.addAll(arrayOf("-$this", value))
    }

    operator fun String.unaryMinus() {
        raw.add("-$this")
    }

}

fun addCMakeTasks(lib: String, target: String, dir: String = lib, conf: CMakeOptions.() -> Unit): Pair<Task, Task> {

    val srcDir = "${project(":ldb:lib").projectDir}/src/$dir"

    val configure = tasks.create<Exec>("configure${target.capitalize()}${lib.capitalize()}") {
        group = "build"

        workingDir("$buildDir/cmake/$lib-$target")
        commandLine("cmake")

        val options = CMakeOptions().apply {
            +"CMAKE_POSITION_INDEPENDENT_CODE:BOOL"
            "CMAKE_INSTALL_PREFIX:PATH" += "$buildDir/out/$target"
            "CMAKE_C_FLAGS:STRING" += "-D_GLIBCXX_USE_CXX11_ABI=0"
            "CMAKE_CXX_FLAGS:STRING" += "-D_GLIBCXX_USE_CXX11_ABI=0"
            "CMAKE_BUILD_TYPE" += "Release"

            conf()
        }

        args(options.raw + options.defines.map { "-D${it.key}=${it.value.joinToString(" ")}" } + srcDir)

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
        outputs.dir("$buildDir/out")
    }

    return configure to build
}

fun addTarget(target: String, conf: CMakeOptions.() -> Unit) : Task {
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

    val (configureLeveldb, buildLevelDB) = addCMakeTasks("leveldb", target, "leveldb-kodein") {
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

    if (currentOs.isLinux) {
        "CMAKE_SYSROOT:PATH" += "${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64/x86_64-unknown-linux-gnu/sysroot"
        "CMAKE_C_FLAGS:STRING" += "--gcc-toolchain=${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64"
        "CMAKE_CXX_FLAGS:STRING" += "--gcc-toolchain=${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64"
    } else if (currentOs.isMacOsX) {
        "CMAKE_C_FLAGS:STRING" += "-mmacosx-version-min=10.11"
        "CMAKE_CXX_FLAGS:STRING" += "-mmacosx-version-min=10.11"
    }
}

if (withAndroid) {
    val localPropsFile = rootProject.file("local.properties")
    check(localPropsFile.exists()) { "Please create android root local.properties" }
    val localProps = localPropsFile.inputStream().use { Properties().apply { load(it) } }
    val sdkDir = localProps["sdk.dir"]?.let { file(it) }
            ?: throw IllegalStateException("Please set sdk.dir android sdk path in root local.properties")
    val ndkDir = sdkDir.resolve("ndk-bundle").takeIf { it.exists() }
            ?: sdkDir.resolve("ndk").takeIf { it.exists() }
                    ?.listFiles { f -> f.isDirectory }
                    ?.reduce { l, r -> if (SemVer.from(l.name) >= SemVer.from(r.name)) l else r }
            ?: throw IllegalStateException("Please install NDK")

    fun addAndroidTarget(target: String) {
        val build = addTarget("android${target.capitalize()}") {
            "CMAKE_TOOLCHAIN_FILE:PATH" += "${ndkDir.absolutePath}/build/cmake/android.toolchain.cmake"
            "ANDROID_NDK:PATH" += "${ndkDir.absolutePath}/"
            "ANDROID_PLATFORM:STRING" += "android-16"
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

if (currentOs.isMacOsX) {
    fun addIosTarget(target: String) {
        val build = addTarget("ios${target.capitalize()}") {
            "G" -= "Xcode"
            "CMAKE_TOOLCHAIN_FILE:PATH" += "${projectDir.absolutePath}/src/ios-cmake/ios.toolchain.cmake"
            "PLATFORM:STRING" += target.toUpperCase()
            "CMAKE_C_FLAGS:STRING" += "-Wno-shorten-64-to-32"
            "CMAKE_CXX_FLAGS:STRING" += "-Wno-shorten-64-to-32"
        }

        tasks.maybeCreate("buildIosLeveldb").apply {
            group = "build"
            dependsOn(build)
        }
    }

    addIosTarget("os")
    addIosTarget("simulator64")
}

tasks.create<Delete>("clean") {
    delete(buildDir)
}
