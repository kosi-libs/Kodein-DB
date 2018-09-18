plugins {
    id("kotlin-platform-native")
    id("kodein-versions")
}

val kotlinxIoVer: String by rootProject.extra
val kodeinLogVer: String by rootProject.extra

dependencies {
    expectedBy(project(":test:test-utils-common"))
    implementation(project(":ldb:ldb-api:kodein-leveldb-api-native"))
    implementation("org.kodein.log:kodein-log-api-native:$kodeinLogVer")
    implementation("org.jetbrains.kotlinx:kotlinx-io-native:$kotlinxIoVer")
}
