plugins {
    id("kodein-common")
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

val kotlinxCoroutinesVer: String by getRootProject().extra

dependencies {
    compile(project(":kdb:kdb-api:kodein-db-api-common"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:$kotlinxCoroutinesVer")
    testCompile(project(":test:test-utils-common"))
    testCompile("org.jetbrains.kotlin:kotlin-test-common:${kodeinVersions.kotlin}")
    testCompile("org.jetbrains.kotlin:kotlin-test-annotations-common:${kodeinVersions.kotlin}")
}

kodeinPublication {
    upload {
        name = "Kodein-DB-Common"
        description = "Kodein DB Commons"
        repo = "Kodein-DB"
    }
}
