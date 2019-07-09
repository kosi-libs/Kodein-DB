import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.kodein.internal.gradle.KodeinMPPExtension

plugins {
    id("org.kodein.library.mpp")
    `cpp-library`
}

val kotlinxAtomicFuVer: String by getRootProject().extra
val kodeinLogVer: String by rootProject.extra

evaluationDependsOn(":ldb:lib")
evaluationDependsOn(":ldb:kodein-leveldb-api")

kodein {
    kotlin {
        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
        }

        common.test.dependencies {
            implementation(project(":test-utils"))
            implementation("org.kodein.log:kodein-log-frontend-print:$kodeinLogVer")
        }

        add(kodeinTargets.jvm)

        fun KodeinMPPExtension.TargetBuilder<KotlinNativeCompilation, KotlinNativeTarget>.configureNative() {
            mainCompilation.cinterops.create("libleveldb") {
                packageName("org.kodein.db.libleveldb")

                includeDirs(Action {
                    headerFilterOnly(project(":ldb:lib").file("build/out/konan/include"))
                })

                includeDirs(Action {
                    headerFilterOnly("/usr/include")
                })
            }

            // https://github.com/JetBrains/kotlin-native/issues/2314
            mainCompilation.kotlinOptions.freeCompilerArgs = listOf(
                    "-include-binary", "${project(":ldb:lib").buildDir}/out/konan/lib/libleveldb.a",
                    "-include-binary", "${project(":ldb:lib").buildDir}/out/konan/lib/libcrc32c.a",
                    "-include-binary", "${project(":ldb:lib").buildDir}/out/konan/lib/libsnappy.a"
            )

            tasks[mainCompilation.cinterops["libleveldb"].interopProcessingTaskName].dependsOn(project(":ldb:lib").tasks["buildLeveldbKonan"])
            tasks[mainCompilation.compileAllTaskName].dependsOn(project(":ldb:lib").tasks["buildLeveldbKonan"])
        }

        add(kodeinTargets.native.linuxX64) {
            configureNative()
        }

        add(kodeinTargets.native.macosX64) {
            configureNative()
        }

        sourceSet(kodeinSourceSets.allNative) {
            main.dependencies {
                api("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }
        }

    }
}

val generation = task<Exec>("generateJniHeaders") {
    group = "build"

    dependsOn(
            project(":ldb:kodein-leveldb-api").tasks["jvmMainClasses"],
            tasks["jvmMainClasses"]
    )

    val output = "${buildDir}/nativeHeaders/kodein"

    afterEvaluate {
        val ldbApiKotlin = project(":ldb:kodein-leveldb-api").extensions["kotlin"] as org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
        val classPath =
                ldbApiKotlin.targets["jvm"].compilations["main"].output.classesDirs + ldbApiKotlin.targets["jvm"].compilations["main"].compileDependencyFiles +
                kotlin.targets["jvm"].compilations["main"].output.classesDirs + kotlin.targets["jvm"].compilations["main"].compileDependencyFiles

        val javah: String? by project
        setCommandLine(javah ?: "javah", "-d", output, "-cp", classPath.joinToString(":"), "org.kodein.db.leveldb.jni.Native")
    }

    outputs.dir(output)
}

val javaHome = System.getProperty("java.home").let { if (it.endsWith("/jre")) file("$it/..").absolutePath else it }
val currentOs = org.gradle.internal.os.OperatingSystem.current()

library {
    source {
        from("${project.projectDir}/src/allJvmMain/cpp")
    }

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
        compileTask.get().dependsOn(generation)
        compileTask.get().dependsOn(project(":ldb:lib").tasks["buildLeveldbHost"])

        if (this is CppSharedLibrary) {
            linkTask.get().linkerArgs.addAll(
                    "-L${project(":ldb:lib").buildDir}/out/host/lib",
                    "-lleveldb", "-lsnappy", "-lcrc32c"
            )
            compileTask.get().compilerArgs.add("-std=c++11")
        }
    }
}

apply(from = rootProject.file("gradle/toolchains.gradle"))

tasks.withType<CppCompile> {
    macros.put("_GLIBCXX_USE_CXX11_ABI", "0")
}

afterEvaluate {
    tasks.withType<Test> {
        dependsOn(tasks["linkDebug"])
        systemProperty("java.library.path", tasks["linkDebug"].outputs.files.first())
    }
}
