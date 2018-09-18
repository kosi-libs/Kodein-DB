plugins {
    id("kodein-jvm")
}

evaluationDependsOn(":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm")

dependencies {
    expectedBy(project(":ldb:k-ldb:k-ldb-tests-common"))

    compile(project(":ldb:ldb-jni:kodein-leveldb-jni"))
    compile(files(rootProject.project(":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm").tasks["linkDebug"].outputs.files))

    testImplementation(project(":test:test-utils-jvm"))
    testImplementation("org.kodein.log:kodein-log-frontend-print-jvm:1.0.0")
}

kodeinPublication {
    upload {
        name = "Kodein-DB-LevelDB-JVM"
        description = "Kodein LevelDB for the JVM"
        repo = "Kodein-DB"
    }
}

tasks.withType<Test> {
    systemProperty("java.library.path", project(":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm").tasks["linkDebug"].outputs.files.first())
}
