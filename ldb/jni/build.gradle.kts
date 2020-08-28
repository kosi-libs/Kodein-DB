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

        var javah = project.findProperty("javah") as String? ?: "javah"
        javah = javah.replace(Regex("\\\$\\{([^}]+)}")) { System.getenv(it.groupValues[1])
                ?: error("No such environment variable: ${it.groupValues[1]}.\n  Environment:\n${System.getenv().map { (k, v) -> "    $k = \"$v\"" }.joinToString("\n")}") }

        val output = "$buildDir/nativeHeaders/kodein"

        generation.setCommandLine(javah, "-d", output, "-cp", classPath.joinToString(File.pathSeparator), "org.kodein.db.leveldb.jni.Native")
        generation.outputs.dir(output)
    }
}

val generation = task<Exec>("generateJniHeaders") {
    group = "build"

    dependsOn(configure)
}

val javaHome: String = System.getProperty("java.home").let { if (it.endsWith("/jre")) file("$it/..").absolutePath else it }

val os = System.getProperty("os.name").toLowerCase().let {
    when  {
        "windows" in it -> "windows"
        "mac os x" in it || "darwin" in it || "osx" in it -> "macos"
        "linux" in it -> "linux"
        else -> error("Unknown operating system $it")
    }
}

library {
    this.baseName.set("kodein-leveldb-jni-$os-${version.toString().replace('.', '_')}")

    privateHeaders {
        from("$javaHome/include")
        from("${project(":ldb:lib").buildDir}/out/host/include")
    }

    publicHeaders {
        from("$buildDir/nativeHeaders")
    }

    if (os == "linux") {
        privateHeaders {
            from("$javaHome/include/linux")
        }
    } else if (os == "macos") {
        privateHeaders {
            from("$javaHome/include/darwin")
        }
    } else if (os == "windows") {
        privateHeaders {
            from("$javaHome/include/win32")
        }
    }

    binaries.configureEach {
        compileTask.get().apply {
            dependsOn(generation)
            macros["_GLIBCXX_USE_CXX11_ABI"] = "0"
        }
        compileTask.get().dependsOn(generation)
        compileTask.get().dependsOn(project(":ldb:lib").tasks["buildHostLeveldb"])

        if (this is CppSharedLibrary) {
            linkTask.get().linkerArgs.addAll(
                    "-L${project(":ldb:lib").buildDir}/out/host/lib",
                    "-lleveldb", "-lsnappy", "-lcrc32c"
            )
            if (os == "linux") {
                linkTask.get().linkerArgs.addAll(
                        "-static-libgcc", "-static-libstdc++"
                )
            }
            compileTask.get().compilerArgs.add("-std=c++11")
        }
    }
}

if (os != "windows") {
    apply(from = rootProject.file("gradle/toolchains.gradle"))
}
