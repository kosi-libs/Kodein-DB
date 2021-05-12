import org.jetbrains.kotlin.daemon.common.toHexString

plugins {
    id("org.kodein.local-properties")
    id("org.kodein.gradle.cmake")
    id("org.kodein.gradle.android-ndk")
}

evaluationDependsOn(":ldb:kodein-leveldb")

val excludedTargets = kodeinLocalProperties.getAsList("excludeTargets")
val withAndroid = "android" !in excludedTargets

val skipJNIGeneration: String? by project

task("configureJniGeneration") {
    dependsOn(":ldb:kodein-leveldb:jvmMainClasses")

    onlyIf { skipJNIGeneration != "true" }

    doLast {
        val generation = tasks["generateJniHeaders"] as Exec

        val kldbKotlin = project(":ldb:kodein-leveldb").extensions["kotlin"] as org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
        val jvmCompilation = kldbKotlin.targets["jvm"].compilations["main"]
        val classPath = jvmCompilation.output.classesDirs + jvmCompilation.compileDependencyFiles

        var javah = kodeinLocalProperties["javah"] ?: "javah"
        javah = javah.replace(Regex("\\\$\\{([^}]+)}")) { System.getenv(it.groupValues[1])
            ?: error("No such environment variable: ${it.groupValues[1]}.\n  Environment:\n${System.getenv().map { (k, v) -> "    $k = \"$v\"" }.joinToString("\n")}") }

        val output = "$buildDir/nativeHeaders/kodein"

        generation.setCommandLine(javah, "-d", output, "-cp", classPath.joinToString(File.pathSeparator), "org.kodein.db.leveldb.jni.Native")
        generation.outputs.dir(output)
    }
}

if (skipJNIGeneration == "true")
    task("generateJniHeaders") { onlyIf { false } }
else {
    task<Exec>("generateJniHeaders") {
        group = "build"

        onlyIf { skipJNIGeneration != "true" }

        dependsOn("configureJniGeneration")
    }
}

val osName: String by rootProject.extra
val libSuffix: String by rootProject.extra
val libPrefix: String by rootProject.extra

val buildHost = cmake.compilation("kodein-leveldb-jni-$osName") {
    conf {
        dependsOn("generateJniHeaders", ":ldb:lib:buildLeveldb-$osName")
        cmakeOptions {
            "DEPS_DIR:PATH" += rootDir.resolve("ldb/lib/build/out/$osName").absolutePath
            "CMAKE_BUILD_TYPE" += "Release"

            if (org.gradle.internal.os.OperatingSystem.current().isWindows) {
//                "G" -= "MinGW Makefiles"
                "CMAKE_C_FLAGS:STRING" += "-D__WINDOWS__"
                "CMAKE_CXX_FLAGS:STRING" += "-D__WINDOWS__"

            }
        }
        build {
            args("--config", "Release")
        }
    }
}

task("genInfoProperties-$osName") {
    group = "build"

    dependsOn(buildHost)

    val outputFile = file("$buildDir/generated/kodein-leveldb-jni-$osName/kodein-leveldb-jni.properties")
    outputs.file(outputFile)
    val library = file("$rootDir/ldb/jni/c/build/cmake/out/kodein-leveldb-jni-$osName/lib/${libPrefix}kodein-leveldb-jni.$libSuffix")
    inputs.file(library)

    doLast {
        val digest = java.security.MessageDigest.getInstance("SHA-1")
        val buf = ByteArray(8192)
        library
            .inputStream()
            .use {
                while (true) {
                    val n = it.read(buf)
                    if (n == -1) break
                    if (n > 0) digest.update(buf, 0, n)
                }
            }

        val props = java.util.Properties()
        props["version"] = version
        props["sha1"] = digest.digest().toHexString()
        outputFile.outputStream().use {
            props.store(it, null)
        }
    }
}


if (withAndroid) {
    val androidNdkVer: String by rootProject.extra
    val androidNdkDir = androidNdk.ndkPath(androidNdkVer)

    fun addAndroidTarget(target: String) {
        val build = cmake.compilation("kodein-leveldb-jni-android-$target") {
            conf {
                dependsOn("generateJniHeaders", ":ldb:lib:buildLeveldb-android-$target")
                cmakeOptions {
                    "DEPS_DIR:PATH" += rootDir.resolve("ldb/lib/build/out/android-$target").absolutePath

                    "CMAKE_TOOLCHAIN_FILE:PATH" += "$androidNdkDir/build/cmake/android.toolchain.cmake"
                    "ANDROID_NDK:PATH" += "$androidNdkDir/"
                    "ANDROID_PLATFORM:STRING" += "android-21"
                    "ANDROID_ABI:STRING" += target

                    "JAVA_AWT_LIBRARY" += "NotNeeded"
                    "JAVA_JVM_LIBRARY" += "NotNeeded"
                    "JAVA_INCLUDE_PATH2" += "NotNeeded"
                    "JAVA_AWT_INCLUDE_PATH" += "NotNeeded"
                }
            }
        }

        tasks.maybeCreate("buildKodein-leveldb-jni-android").apply {
            group = "build"
            dependsOn(build)
        }
    }

    addAndroidTarget("armeabi-v7a")
    addAndroidTarget("arm64-v8a")
    addAndroidTarget("x86")
    addAndroidTarget("x86_64")
}
