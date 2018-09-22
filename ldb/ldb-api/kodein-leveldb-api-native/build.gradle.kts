plugins {
    id("kodein-native")
}

val kotlinxIoVer: String by rootProject.extra
val kotlinxAtomicFuVer: String by rootProject.extra
val kodeinLogVer: String by rootProject.extra

dependencies {
    expectedBy(project(":ldb:ldb-api:kodein-leveldb-api-common"))
    implementation("org.kodein.log:kodein-log-api-native:$kodeinLogVer")
    implementation("org.jetbrains.kotlinx:kotlinx-io-native:$kotlinxIoVer")
    implementation("org.jetbrains.kotlinx:atomicfu-native:$kotlinxAtomicFuVer")
}

kodeinPublication {
    upload {
        name = "Kodein-DB-LevelDB-API-Native"
        description = "Kodein LevelDB API for the Native platforms"
        repo = "Kodein-DB"
    }
}
