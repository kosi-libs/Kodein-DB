import org.jetbrains.kotlin.gradle.targets.js.npm.SemVer
import java.util.*

val buildAll = tasks.create("build") {
    group = "build"
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()!!

val excludedTargets = (project.findProperty("excludeTargets") as String?)
        ?.split(",")
        ?.map { it.trim() }
        ?: emptyList()
val withAndroid = "android" !in excludedTargets

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
        executable = "cmake"

        val options = CMakeOptions().apply {
            +"CMAKE_POSITION_INDEPENDENT_CODE:BOOL"
            "CMAKE_INSTALL_PREFIX:PATH" += "$buildDir/out/$target"
            "CMAKE_C_FLAGS:STRING" += "-D_GLIBCXX_USE_CXX11_ABI=0"
            "CMAKE_CXX_FLAGS:STRING" += "-D_GLIBCXX_USE_CXX11_ABI=0"
            "CMAKE_BUILD_TYPE" += "Release"
//            +"CMAKE_VERBOSE_MAKEFILE"

            if (currentOs.isWindows) {
                "G" -= "MinGW Makefiles"
            }

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
        executable = "cmake"
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

fun addTarget(target: String, fpic: Boolean = true, conf: CMakeOptions.() -> Unit) : Task {
    val (_, buildCrc32c) = addCMakeTasks("crc32c", target) {
        "CRC32C_BUILD_BENCHMARKS:BOOL" += "0"
        "CRC32C_BUILD_TESTS:BOOL" += "0"
        "CRC32C_USE_GLOG:BOOL" += "0"

        if (fpic) {
            "CMAKE_C_FLAGS:STRING" += "-fPIC"
            "CMAKE_CXX_FLAGS:STRING" += "-fPIC"
        }

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
        +"HAVE_CRC32C"
        +"HAVE_SNAPPY"

        conf()
    }

    configureLeveldb.dependsOn(buildCrc32c)
    configureLeveldb.dependsOn(buildSnappy)

    return buildLevelDB
}

addTarget("host", fpic = !currentOs.isWindows) {
    "CMAKE_C_COMPILER:STRING" += "clang"
    "CMAKE_CXX_COMPILER:STRING" += "clang++"

    if (currentOs.isWindows) {
        "CMAKE_C_FLAGS:STRING" += "-target x86_64-pc-windows-gnu -femulated-tls"
        "CMAKE_CXX_FLAGS:STRING" += "-target x86_64-pc-windows-gnu -femulated-tls"
    }
}

addTarget("konan", fpic = !currentOs.isWindows) {
    when {
        currentOs.isLinux -> {
            "CMAKE_C_COMPILER:STRING" += "clang"
            "CMAKE_CXX_COMPILER:STRING" += "clang++"
            val path = "${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64"
            "CMAKE_SYSROOT:PATH" += "$path/x86_64-unknown-linux-gnu/sysroot"
            val cFlags = "--gcc-toolchain=$path"
            "CMAKE_C_FLAGS:STRING" += cFlags
            "CMAKE_CXX_FLAGS:STRING" += cFlags
        }
        currentOs.isMacOsX -> {
            "CMAKE_C_COMPILER:STRING" += "clang"
            "CMAKE_CXX_COMPILER:STRING" += "clang++"
            "CMAKE_C_FLAGS:STRING" += "-mmacosx-version-min=10.11"
            "CMAKE_CXX_FLAGS:STRING" += "-mmacosx-version-min=10.11"
        }
        currentOs.isWindows -> {
            val userHome = System.getProperty("user.home").replace('\\', '/')
            val path = "$userHome/.konan/dependencies/msys2-mingw-w64-x86_64-clang-llvm-lld-compiler_rt-8.0.1"
            "CMAKE_C_COMPILER:STRING" += "$path/bin/clang.exe"
            "CMAKE_CXX_COMPILER:STRING" += "$path/bin/clang++.exe"

            "CMAKE_SYSROOT:PATH" += path

            val cFlags = "-femulated-tls"
            "CMAKE_C_FLAGS:STRING" += cFlags
            "CMAKE_CXX_FLAGS:STRING" += "$cFlags -std=c++11"
        }
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
        val build = addTarget("android-$target") {
            "CMAKE_TOOLCHAIN_FILE:PATH" += "${ndkDir.absolutePath}/build/cmake/android.toolchain.cmake"
            "ANDROID_NDK:PATH" += "${ndkDir.absolutePath}/"
            "ANDROID_PLATFORM:STRING" += "android-16"
            "ANDROID_ABI:STRING" += target
        }

        tasks.maybeCreate("buildAllAndroidLibs").apply {
            group = "build"
            dependsOn(build)
        }
    }

    addAndroidTarget("armeabi-v7a")
    addAndroidTarget("arm64-v8a")
    addAndroidTarget("x86")
    addAndroidTarget("x86_64")
}

fun addIosTarget(target: String) {
    val build = if (currentOs.isMacOsX) addTarget("ios-$target") {
        "G" -= "Xcode"
        "CMAKE_TOOLCHAIN_FILE:PATH" += "${projectDir.absolutePath}/src/ios-cmake/ios.toolchain.cmake"
        "PLATFORM:STRING" += target.toUpperCase()
        "CMAKE_C_FLAGS:STRING" += "-Wno-shorten-64-to-32"
        "CMAKE_CXX_FLAGS:STRING" += "-Wno-shorten-64-to-32"
    }
    else task("buildIos-${target}Leveldb") {
        enabled = false
    }

    tasks.maybeCreate("buildAllIosLibs").apply {
        group = "build"
        dependsOn(build)
    }
}

addIosTarget("os")
addIosTarget("simulator64")
addIosTarget("watchos")
addIosTarget("simulator_watchos")
addIosTarget("tvos")
addIosTarget("simulator_tvos")

tasks.create<Delete>("clean") {
    delete(buildDir)
}
