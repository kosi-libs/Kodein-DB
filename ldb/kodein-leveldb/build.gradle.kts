import com.android.build.gradle.tasks.factory.AndroidUnitTest
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.kodein.internal.gradle.KodeinMPPExtension

plugins {
    id("org.kodein.library.mpp-with-android")
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()!!

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
        }

        add(kodeinTargets.jvm.android)

        add(kodeinTargets.jvm.jvm) {
            test.dependencies {
                implementation(project(":ldb:kodein-leveldb-jni"))
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

//            // https://github.com/JetBrains/kotlin-native/issues/2314
            mainCompilation.kotlinOptions.freeCompilerArgs = listOf(
                    "-include-binary", "${project(":ldb:lib").buildDir}/out/$compilation/lib/libfatleveldb.a"
            )

            tasks[mainCompilation.cinterops["libleveldb"].interopProcessingTaskName].dependsOn(project(":ldb:lib").tasks["build${compilation.capitalize()}FatLeveldb"])
            tasks[mainCompilation.compileAllTaskName].dependsOn(project(":ldb:lib").tasks["build${compilation.capitalize()}FatLeveldb"])
        }

        add(kodeinTargets.native.allDesktop) {
            configureCInterop("konan")
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
                    project(":ldb:lib").tasks["buildAllAndroidLibs"],
                    project(":ldb:jni").tasks["generateJniHeaders"]
            )
        }
    }
}

(tasks.findByName("linkDebugTestMingwX64") as org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink?)?.apply {
    this.binary.linkerOpts.addAll(listOf("--verbose", "-femulated-tls"))
}
