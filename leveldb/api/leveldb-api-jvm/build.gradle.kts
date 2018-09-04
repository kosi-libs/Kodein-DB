plugins {
    id("kodein-jvm")
}

dependencies {
    expectedBy(project(":leveldb:api:leveldb-api-common"))
    compile("org.kodein.log:kodein-log-api-jvm:1.0.0")
    compile("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.0-alpha-4")
}

kodeinPublication {
    upload {
        name = "Kodein-DB-LevelDB-API-JVM"
        description = "Kodein LevelDB API for the JVM and Android"
        repo = "Kodein-DB"
    }
}
