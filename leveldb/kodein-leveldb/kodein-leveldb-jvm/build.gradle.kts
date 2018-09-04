plugins {
    id("kodein-jvm")
}

evaluationDependsOn(":leveldb:jni:leveldb-jni-native")

dependencies {
    compile(project(":leveldb:jni:leveldb-jni"))
    compile(files(rootProject.project(":leveldb:jni:leveldb-jni-native").tasks["linkDebug"].outputs.files))

    testCompile(project(":leveldb:tests:jvm"))
    testCompile("org.kodein.log:kodein-log-frontend-print-jvm:1.0.0")
}

kodeinPublication {
    upload {
        name = "Kodein-DB-LevelDB-JVM"
        description = "Kodein LevelDB for the JVM"
        repo = "Kodein-DB"
    }
}

tasks.withType<Test> {
    systemProperty("java.library.path", project(":leveldb:jni:leveldb-jni-native").tasks["linkDebug"].outputs.files.first())
}
