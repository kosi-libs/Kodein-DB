plugins {
    id("kodein-common")
}

dependencies {
    compile(project(":leveldb:api:leveldb-api-common"))
    compile("org.jetbrains.kotlin:kotlin-test-common:${kodeinVersions.kotlin}")
    compile("org.jetbrains.kotlin:kotlin-test-annotations-common:${kodeinVersions.kotlin}")
}
