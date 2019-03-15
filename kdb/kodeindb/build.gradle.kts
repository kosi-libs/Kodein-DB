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
            api(project(":kdb:kodeindb-api"))
            api(project(":ldb:kodein-leveldb"))
        }

        common.test.dependencies {
            implementation(project(":test-utils"))
        }

        add(kodeinTargets.jvm)

//        add(kodeinTargets.native.linuxX64)

    }
}

tasks.withType<Test> {
    dependsOn(project(":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm").tasks["linkDebug"])
    systemProperty("java.library.path", project(":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm").tasks["linkDebug"].outputs.files.first())
}
