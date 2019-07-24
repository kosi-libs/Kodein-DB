plugins {
    id("org.kodein.library.mpp-with-android")
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()

evaluationDependsOn(":ldb:jni")
evaluationDependsOn(":ldb:lib")

val kotlinxAtomicFuVer: String by getRootProject().extra
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

        add(kodeinTargets.android) {
            test.dependencies {
                implementation("androidx.test.ext:junit:1.1.1")
                implementation("androidx.test.espresso:espresso-core:3.2.0")
            }
        }

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

        add(listOf(kodeinTargets.native.linuxX64, kodeinTargets.native.macosX64)) {
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

            tasks[mainCompilation.cinterops["libleveldb"].interopProcessingTaskName].dependsOn(project(":ldb:lib").tasks["buildKonanLeveldb"])
            tasks[mainCompilation.compileAllTaskName].dependsOn(project(":ldb:lib").tasks["buildKonanLeveldb"])
        }

        sourceSet(kodeinSourceSets.allNative) {
            main.dependencies {
                api("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }
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