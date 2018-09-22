plugins {
    id("kodein-common")
}

val kotlinxIoVer: String by getRootProject().extra
val kotlinxAtomicFuVer: String by getRootProject().extra
val kodeinLogVer: String by getRootProject().extra

dependencies {
    compile("org.kodein.log:kodein-log-api-common:$kodeinLogVer")
    compile("org.jetbrains.kotlinx:kotlinx-io:$kotlinxIoVer")
    compile("org.jetbrains.kotlinx:atomicfu-common:$kotlinxAtomicFuVer")
}

kodeinPublication {
    upload {
        name = "Kodein-DB-LevelDB-API-Common"
        description = "Kodein LevelDB API Commons"
        repo = "Kodein-DB"
    }
}
