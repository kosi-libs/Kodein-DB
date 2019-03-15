plugins {
    id("org.kodein.library.mpp")
}

val kotlinxIoVer: String by getRootProject().extra
val kodeinLogVer: String by rootProject.extra
val kotlinxAtomicFuVer: String by getRootProject().extra

evaluationDependsOn(":ldb:ldb-lib:leveldb")
evaluationDependsOn(":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm")

kodein {
    kotlin {
        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
        }

        common.test.dependencies {
            implementation(project(":test-utils"))
            implementation("org.kodein.log:kodein-log-frontend-print:$kodeinLogVer")
        }

        add(kodeinTargets.jvm) {
            main.dependencies {
                implementation(project(":ldb:ldb-jni:kodein-leveldb-jni"))
                runtimeOnly(files(project.project(":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm").tasks["linkDebug"].outputs.files))
            }
        }

        add(kodeinTargets.native.linuxX64) {
            main.dependencies {
                implementation(project(":ldb:ldb-lib:leveldb-native-interop"))
                api("org.jetbrains.kotlinx:kotlinx-io-native:$kotlinxIoVer")
                api("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }

            testCompilation.linkerOpts("-L" + project(":ldb:ldb-lib:leveldb").tasks["createDebug"].outputs.files.first().parent)
            afterEvaluate {
                tasks[testCompilation.linkTaskName("EXECUTABLE", "DEBUG")].dependsOn(project(":ldb:ldb-lib:leveldb").tasks["createDebug"])
            }
        }
    }
}

tasks.withType<Test> {
    dependsOn(project(":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm").tasks["linkDebug"])
    systemProperty("java.library.path", project(":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm").tasks["linkDebug"].outputs.files.first())
}
