import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.kodein.internal.gradle.KodeinMPPExtension

plugins {
    id("org.kodein.library.mpp-with-android")
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()

evaluationDependsOn(":ldb:jni")
evaluationDependsOn(":ldb:lib")

val kodeinLogVer: String by rootProject.extra

kodeinAndroid {
    android {
        defaultConfig {
            externalNativeBuild {
                cmake {
                    arguments.add("-DPATH_BASE:PATH=${project(":ldb").projectDir.absolutePath}")
                }
            }
        }
        externalNativeBuild {
            cmake {
                setPath("src/androidMain/cpp/CMakeLists.txt")
            }
        }
    }
}

kodein {
    kotlin {
        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
        }

        common.test.dependencies {
            implementation(project(":test-utils"))
            implementation("org.kodein.log:kodein-log-frontend-print:$kodeinLogVer")
        }

        add(kodeinTargets.android)

        add(kodeinTargets.jvm) {
            (tasks[mainCompilation.processResourcesTaskName] as ProcessResources).apply {
                dependsOn(
                        project(":ldb:jni").tasks["linkRelease"],
                        project(":ldb:jni").tasks["genInfoRelease"]
                )
                from(
                        project(":ldb:jni").tasks["linkRelease"].outputs,
                        project(":ldb:jni").tasks["genInfoRelease"].outputs
                )
            }
        }

        fun KodeinMPPExtension.TargetBuilder<KotlinNativeTarget>.configureCInterop(compilation: String) {
            mainCompilation.cinterops.create("libleveldb") {
                packageName("org.kodein.db.libleveldb")

                includeDirs(Action {
                    headerFilterOnly(project(":ldb:lib").file("build/out/$compilation/include"))
                })

                if (currentOs.isLinux) {
                    includeDirs(Action {
                        headerFilterOnly("/usr/include")
                    })
                }
            }

            // https://github.com/JetBrains/kotlin-native/issues/2314
            mainCompilation.kotlinOptions.freeCompilerArgs = listOf(
                    "-include-binary", "${project(":ldb:lib").buildDir}/out/$compilation/lib/libleveldb.a",
                    "-include-binary", "${project(":ldb:lib").buildDir}/out/$compilation/lib/libcrc32c.a",
                    "-include-binary", "${project(":ldb:lib").buildDir}/out/$compilation/lib/libsnappy.a"
            )

            tasks[mainCompilation.cinterops["libleveldb"].interopProcessingTaskName].dependsOn(project(":ldb:lib").tasks["build${compilation.capitalize()}Leveldb"])
            tasks[mainCompilation.compileAllTaskName].dependsOn(project(":ldb:lib").tasks["build${compilation.capitalize()}Leveldb"])
        }

        add(kodeinTargets.native.host) {
            configureCInterop("konan")
        }

        add(listOf(kodeinTargets.native.iosArm32, kodeinTargets.native.iosArm64)) {
            configureCInterop("iosOs")
        }

        add(kodeinTargets.native.iosX64) {
            configureCInterop("iosSimulator64")
        }

    }
}

if (kodeinAndroid.isIncluded) {
    afterEvaluate {
        configure(listOf("Debug", "Release").map { tasks["externalNativeBuild$it"] }) {
            dependsOn(
                    project(":ldb:lib").tasks["buildAndroidLeveldb"],
                    project(":ldb:jni").tasks["generateJniHeaders"]
            )
        }
    }
}
