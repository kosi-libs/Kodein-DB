import org.jetbrains.kotlin.daemon.common.toHexString
import java.util.*
import java.security.MessageDigest

plugins {
    `cpp-library`
    kotlin("multiplatform")
}

evaluationDependsOn(":ldb:lib")

val copy = task<Sync>("syncJniKotlinSources") {
    from("${project(":ldb:kodein-leveldb").projectDir}/src/allJvmMain")
    into("$buildDir/jniKotlinSources/main")
}

kotlin {
    jvm {
        compilations["main"].compileKotlinTask.dependsOn(copy)
    }
    sourceSets["jvmMain"].apply {
        kotlin.srcDir("$buildDir/jniKotlinSources/main/kotlin")

        dependencies {
            api(project(":ldb:kodein-leveldb-api"))
            implementation(kotlin("stdlib"))
        }
    }
}

val configure = task("configureJniGeneration") {
    dependsOn("jvmMainClasses")

    doLast {
        val generation = tasks["generateJniHeaders"] as Exec

        val jvmCompilation = kotlin.targets["jvm"].compilations["main"]
        val classPath = jvmCompilation.output.classesDirs + jvmCompilation.compileDependencyFiles

        val javah: String? by project

        val output = "$buildDir/nativeHeaders/kodein"

        generation.setCommandLine(javah ?: "javah", "-d", output, "-cp", classPath.joinToString(":"), "org.kodein.db.leveldb.jni.Native")
        generation.outputs.dir(output)
    }
}

val generation = task<Exec>("generateJniHeaders") {
    group = "build"

    dependsOn(configure)
}

val javaHome = System.getProperty("java.home").let { if (it.endsWith("/jre")) file("$it/..").absolutePath else it }
val currentOs = org.gradle.internal.os.OperatingSystem.current()

library {
    this.baseName.set("kodein-leveldb-jni-${currentOs.name.toLowerCase()}-$version")

    privateHeaders {
        from("$javaHome/include")
        from("${project(":ldb:lib").buildDir}/out/host/include")
    }

    publicHeaders {
        from("$buildDir/nativeHeaders")
    }

    if (currentOs.isLinux()) {
        privateHeaders {
            from("$javaHome/include/linux")
        }
    }
    else if (currentOs.isMacOsX()) {
        privateHeaders {
            from("$javaHome/include/darwin")
        }
    }

    binaries.configureEach {
        compileTask.get().apply {
            dependsOn(generation)
            macros.put("_GLIBCXX_USE_CXX11_ABI", "0")
        }
        compileTask.get().dependsOn(generation)
        compileTask.get().dependsOn(project(":ldb:lib").tasks["buildLeveldbHost"])

        if (this is CppSharedLibrary) {
            linkTask.get().linkerArgs.addAll(
                    "-L${project(":ldb:lib").buildDir}/out/host/lib",
                    "-lleveldb", "-lsnappy", "-lcrc32c",
                    "-static-libgcc", "-static-libstdc++"
            )
            compileTask.get().compilerArgs.add("-std=c++11")
        }
    }
}

listOf("debug", "release").forEach { type ->
    val cType = type.capitalize()
    task("genInfo$cType") {
        dependsOn("link$cType")

        val outputFile = file("$buildDir/generated/infos/$type/info.properties")
        outputs.file(outputFile)

        doLast {
            val digest = MessageDigest.getInstance("SHA-1")
            val buf = ByteArray(8192)
            tasks["link$cType"].outputs.files.filter { it.name.endsWith(".so") }.first().inputStream().use {
                while (true) {
                    val n = it.read(buf)
                    if (n == -1) break
                    if (n > 0) digest.update(buf, 0, n)
                }
            }

            val props = Properties()
            props["version"] = version
            props["os"] = currentOs.name.toLowerCase()
            props["sha1"] = digest.digest().toHexString()
            outputFile.outputStream().use {
                props.store(it, null)
            }
        }
    }

}

apply(from = rootProject.file("gradle/toolchains.gradle"))
