plugins {
    id("org.kodein.library.mpp")
    id("kotlinx-serialization")
}

evaluationDependsOn(":ldb:lib")
evaluationDependsOn(":ldb:kodein-leveldb")

kodein {
    kotlin {

        common {
            main.dependencies {
                api(project(":kdb:kodein-db-api"))
                api(project(":ldb:kodein-leveldb"))
            }

            test.dependencies {
                implementation(project(":test-utils"))

                implementation(project(":kdb:serializer:kodein-db-serializer-kotlinx"))
            }
        }

        add(kodeinTargets.jvm) {
            test.dependencies {
                implementation(project(":kdb:serializer:kodein-db-serializer-kryo-jvm"))
            }
        }

        add(kodeinTargets.native.linuxX64)
        add(kodeinTargets.native.macosX64)
    }
}

tasks.withType<Test> {
    dependsOn(project(":ldb:kodein-leveldb").tasks["linkDebug"])
    systemProperty("java.library.path", project(":ldb:kodein-leveldb").tasks["linkDebug"].outputs.files.first())
}
