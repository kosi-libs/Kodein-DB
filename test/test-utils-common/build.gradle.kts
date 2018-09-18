plugins {
    id("kotlin-platform-common")
    id("kodein-versions")
}

val kotlinxIoVer: String by getRootProject().extra
val kodeinLogVer: String by getRootProject().extra

dependencies {
    compile(project(":ldb:ldb-api:kodein-leveldb-api-common"))
    compile("org.jetbrains.kotlin:kotlin-stdlib-common:${kodeinVersions.kotlin}")
    compile("org.jetbrains.kotlin:kotlin-test-common:${kodeinVersions.kotlin}")
    compile("org.jetbrains.kotlin:kotlin-test-annotations-common:${kodeinVersions.kotlin}")
    compile("org.jetbrains.kotlinx:kotlinx-io:$kotlinxIoVer")
    compile("org.kodein.log:kodein-log-api-common:$kodeinLogVer")
}
