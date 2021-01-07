import com.android.build.gradle.tasks.factory.AndroidUnitTest
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.kodein.internal.gradle.KodeinMppExtension

plugins {
    id("org.kodein.library.mpp-with-android")
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()!!

evaluationDependsOn(":ldb:lib")

val kodeinLogVer: String by rootProject.extra

kodeinAndroid {
    android {
        defaultConfig {
            externalNativeBuild {
                cmake {
                    arguments.add("-DPATH_BASE:PATH=$rootDir/ldb")
                }
            }
        }
        externalNativeBuild {
            cmake {
                path = file("src/androidMain/cpp/CMakeLists.txt")
            }
        }
    }
}

afterEvaluate {
    tasks.withType<AndroidUnitTest>().all {
        enabled = false
    }
}

kodein {
    kotlin {
        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
        }

        common.test.dependencies {
            implementation(project(":test-utils"))
            implementation("org.kodein.log:kodein-log:$kodeinLogVer")
            implementation(project(":ldb:kodein-leveldb-inmemory"))
        }

        add(kodeinTargets.jvm.android)

        add(kodeinTargets.jvm.jvm) {
            test.dependencies {
                implementation(project(":ldb:jni:kodein-leveldb-jni-jvm"))
            }
        }

        fun KodeinMppExtension.TargetBuilder<KotlinNativeTarget>.configureCInterop(compilation: String, useFat: Boolean = false) {
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

            val dep = if (useFat) {
                // https://github.com/JetBrains/kotlin-native/issues/2314
                mainCompilation.kotlinOptions.freeCompilerArgs = listOf(
                    "-include-binary", "${project(":ldb:lib").buildDir}/out/$compilation/lib/libfatleveldb.a"
                )
                "archiveFatLeveldb"
            } else {
                // https://github.com/JetBrains/kotlin-native/issues/2314
                mainCompilation.kotlinOptions.freeCompilerArgs = listOf(
                        "-include-binary", "${project(":ldb:lib").buildDir}/out/$compilation/lib/libcrc32c.a",
                        "-include-binary", "${project(":ldb:lib").buildDir}/out/$compilation/lib/libsnappy.a",
                        "-include-binary", "${project(":ldb:lib").buildDir}/out/$compilation/lib/libleveldb.a"
                )
                "buildLeveldb"
            }

            tasks[mainCompilation.cinterops["libleveldb"].interopProcessingTaskName].dependsOn(":ldb:lib:$dep-$compilation")
            tasks[mainCompilation.compileAllTaskName].dependsOn(":ldb:lib:$dep-$compilation")
        }

        add(kodeinTargets.native.linuxX64) {
            if (currentOs.isLinux) configureCInterop("konan-linux")
        }

        add(kodeinTargets.native.macosX64) {
            if (currentOs.isMacOsX) configureCInterop("konan-macos")
        }

        add(kodeinTargets.native.mingwX64) {
            if (currentOs.isWindows) configureCInterop("konan-windows", useFat = true)
        }

        add(listOf(kodeinTargets.native.iosArm32, kodeinTargets.native.iosArm64)) {
            configureCInterop("ios-os")
        }

        add(kodeinTargets.native.iosX64) {
            configureCInterop("ios-simulator64")
        }

        add(listOf(kodeinTargets.native.watchosArm32, kodeinTargets.native.watchosArm64)) {
            configureCInterop("ios-watchos")
        }

        add(kodeinTargets.native.watchosX86) {
            configureCInterop("ios-simulator_watchos")
        }

        add(kodeinTargets.native.tvosArm64) {
            configureCInterop("ios-tvos")
        }

        add(kodeinTargets.native.tvosX64) {
            configureCInterop("ios-simulator_tvos")
        }

    }
}

if (kodeinAndroid.isIncluded) {
    afterEvaluate {
        configure(listOf("Debug", "Release").map { tasks["externalNativeBuild$it"] }) {
            dependsOn(
                    ":ldb:lib:buildAllAndroidLibs",
                    ":ldb:jni:c:generateJniHeaders"
            )
        }
    }
}

(tasks.findByName("linkDebugTestMingwX64") as org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink?)?.apply {
    this.binary.linkerOpts.addAll(listOf("--verbose", "-femulated-tls"))
}

kodeinUpload {
    name = "kodein-leveldb"
    description = "LevelDB JNI and K/N implementation library"
}