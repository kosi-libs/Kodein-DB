plugins {
    id("kodein-common")
}

repositories {
    maven(url = "https://kotlin.bintray.com/kotlinx")
}


dependencies {
    implementation("org.kodein.log:kodein-log-api-common:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-io:0.1.0-alpha-4")
}

kodeinPublication {
    upload {
        name = "Kodein-DB-LevelDB-API-Common"
        description = "Kodein LevelDB API Commons"
        repo = "Kodein-DB"
    }
}
