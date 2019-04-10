plugins {
    id("org.kodein.library.mpp")
}

val kotlinxIoVer: String by getRootProject().extra
val kodeinLogVer: String by rootProject.extra
val kotlinxAtomicFuVer: String by getRootProject().extra

evaluationDependsOn(":ldb:lib:leveldb")
evaluationDependsOn(":ldb:jni:kodein-leveldb-jni-jvm")

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
    dependsOn(project(":ldb:jni:kodein-leveldb-jni-jvm").tasks["linkDebug"])
    systemProperty("java.library.path", project(":ldb:jni:kodein-leveldb-jni-jvm").tasks["linkDebug"].outputs.files.first())
}
