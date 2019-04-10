plugins {
    id("org.kodein.library.mpp")
}

val kotlinxIoVer: String by getRootProject().extra
val kodeinLogVer: String by rootProject.extra
val kotlinxAtomicFuVer: String by getRootProject().extra

evaluationDependsOn(":ldb:lib:crc32c")
evaluationDependsOn(":ldb:lib:snappy")
evaluationDependsOn(":ldb:lib:leveldb")
evaluationDependsOn(":ldb:jni:kodein-leveldb-jni-jvm")

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
                implementation(project(":ldb:jni:kodein-leveldb-jni-api"))
                runtimeOnly(files(project.project(":ldb:jni:kodein-leveldb-jni-jvm").tasks["linkDebug"].outputs.files))
            }
        }

        add(kodeinTargets.native.linuxX64) {
            main.dependencies {
                implementation(project(":ldb:kodein-leveldb-native"))
                api("org.jetbrains.kotlinx:kotlinx-io-native:$kotlinxIoVer")
                api("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
            }

            testCompilation.linkerOpts("-L${project(":ldb:lib:crc32c").buildDir}/out/lib/ -L${project(":ldb:lib:snappy").buildDir}/out/lib/ -L${project(":ldb:lib:leveldb").buildDir}/out/lib/")
            afterEvaluate {
                tasks[testCompilation.linkTaskName("EXECUTABLE", "DEBUG")].dependsOn(project(":ldb:lib:leveldb").tasks["build"])
            }
        }
    }
}

tasks.withType<Test> {
    dependsOn(project(":ldb:jni:kodein-leveldb-jni-jvm").tasks["linkDebug"])
    systemProperty("java.library.path", project(":ldb:jni:kodein-leveldb-jni-jvm").tasks["linkDebug"].outputs.files.first())
}
