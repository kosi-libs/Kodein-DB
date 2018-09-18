plugins {
    id("kotlin-platform-jvm")
    id("kodein-versions")
}

val kotlinxIoVer: String by rootProject.extra
val kodeinLogVer: String by rootProject.extra

dependencies {
    expectedBy(project(":test:test-utils-common"))
    compile(project(":ldb:ldb-api:kodein-leveldb-api-jvm"))
    compile("org.jetbrains.kotlin:kotlin-stdlib:${kodeinVersions.kotlin}")
    compile("org.jetbrains.kotlin:kotlin-test:${kodeinVersions.kotlin}")
    compile("org.jetbrains.kotlin:kotlin-test-junit:${kodeinVersions.kotlin}")
    compile("org.kodein.log:kodein-log-api-jvm:$kodeinLogVer")
    compile("org.jetbrains.kotlinx:kotlinx-io-jvm:$kotlinxIoVer")
}
