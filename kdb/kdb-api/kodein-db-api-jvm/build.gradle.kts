plugins {
    id("kodein-jvm")
}

dependencies {
    expectedBy(project(":kdb:kdb-api:kodein-db-api-common"))
    compile(project(":ldb:ldb-api:kodein-leveldb-api-jvm"))
}

kodeinPublication {
    upload {
        name = "Kodein-DB-API-Common"
        description = "Kodein DB API Commons"
        repo = "Kodein-DB"
    }
}
