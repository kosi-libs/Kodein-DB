plugins {
    id("kodein-native")
}

dependencies {
    expectedBy(project(":kdb:kdb-impl:kodein-db-common"))
    implementation(project(":kdb:kdb-api:kodein-db-api-native"))
    implementation(project(":test:test-utils-native"))
}

kodeinPublication {
    upload {
        name = "Kodein-DB-API-Common"
        description = "Kodein DB API Commons"
        repo = "Kodein-DB"
    }
}
