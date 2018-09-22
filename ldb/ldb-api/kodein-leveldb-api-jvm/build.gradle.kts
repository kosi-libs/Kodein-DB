plugins {
    id("kodein-jvm")
    id("kotlinx-atomicfu")
}

val kotlinxIoVer: String by rootProject.extra
val kotlinxAtomicFuVer: String by rootProject.extra
val kodeinLogVer: String by rootProject.extra

dependencies {
    expectedBy(project(":ldb:ldb-api:kodein-leveldb-api-common"))
    compile("org.kodein.log:kodein-log-api-jvm:$kodeinLogVer")
    compile("org.jetbrains.kotlinx:kotlinx-io-jvm:$kotlinxIoVer")
    compileOnly("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVer")
    testRuntime("org.jetbrains.kotlinx:atomicfu:$kotlinxAtomicFuVer")
}

kodeinPublication {
    upload {
        name = "Kodein-DB-LevelDB-API-JVM"
        description = "Kodein LevelDB API for the JVM and Android"
        repo = "Kodein-DB"
    }
}
