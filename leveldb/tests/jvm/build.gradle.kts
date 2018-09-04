plugins {
    id("kodein-jvm")
}

dependencies {
    expectedBy(project(":leveldb:tests:common"))

    compile(project(":leveldb:api:leveldb-api-jvm"))
    compile("org.jetbrains.kotlin:kotlin-test:${kodeinVersions.kotlin}")
    compile("org.jetbrains.kotlin:kotlin-test-junit:${kodeinVersions.kotlin}")
    compile("junit:junit:4.12")
}
