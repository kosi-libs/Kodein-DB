plugins {
    id("org.kodein.library.mpp")
}

evaluationDependsOn(":ldb:lib")
evaluationDependsOn(":ldb:kodein-leveldb")

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

        add(kodeinTargets.native.linuxX64)

    }
}

tasks.withType<Test> {
    dependsOn(project(":ldb:kodein-leveldb").tasks["linkDebug"])
    systemProperty("java.library.path", project(":ldb:kodein-leveldb").tasks["linkDebug"].outputs.files.first())
}
