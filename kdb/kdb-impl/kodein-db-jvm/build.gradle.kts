plugins {
    id("kodein-jvm")
}

dependencies {
    expectedBy(project(":kdb:kdb-impl:kodein-db-common"))
    compile(project(":kdb:kdb-api:kodein-db-api-jvm"))
    testCompile(project(":test:test-utils-jvm"))
}

kodeinPublication {
    upload {
        name = "Kodein-DB-API-Common"
        description = "Kodein DB API Commons"
        repo = "Kodein-DB"
    }
}
