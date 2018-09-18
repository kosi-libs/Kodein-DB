plugins {
    id("kodein-common")
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx")
}

val kotlinxIoVer: String by getRootProject().extra
val kodeinLogVer: String by getRootProject().extra

dependencies {
    compile(project(":ldb:api:leveldb-api-common"))
    compile("org.kodein.log:kodein-log-api-common:$kodeinLogVer")
    compile("org.jetbrains.kotlinx:kotlinx-io:$kotlinxIoVer")
}

kodeinPublication {
    upload {
        name = "Kodein-DB-API-Common"
        description = "Kodein DB API Commons"
        repo = "Kodein-DB"
    }
}
