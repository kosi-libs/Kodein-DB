plugins {
    id("kodein-native")
}

dependencies {
    expectedBy(project(":kdb:kdb-api:kodein-db-api-common"))
    implementation(project(":ldb:ldb-api:kodein-leveldb-api-native"))
}

kodeinPublication {
    upload {
        name = "Kodein-DB-API-Native"
        description = "Kodein DB API for Native platforms"
        repo = "Kodein-DB"
    }
}
