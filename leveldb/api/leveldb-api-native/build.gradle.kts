import org.jetbrains.kotlin.gradle.plugin.KonanArtifactContainer

plugins {
    id("kodein-native")
}

//konanArtifacts {
//    library(mapOf("targets" to kodeinNative.allTargets), name) {
//        enableMultiplatform(true)
//    }
//}

extensions.configure<KonanArtifactContainer>("konanArtifacts") {
    library(mapOf("targets" to kodeinNative.allTargets), name, Action {
        enableMultiplatform(true)
    })
}

dependencies {
    expectedBy(project(":leveldb:api:leveldb-api-common"))

    "artifact$name"("org.kodein.log:kodein-log-api-native")

    "artifact$name"("org.jetbrains.kotlinx:kotlinx-io-native:0.1.0-alpha-4")
}

kodeinPublication {
    upload {
        name = "Kodein-DB-LevelDB-API-Native"
        description = "Kodein LevelDB API for the Native platforms"
        repo = "Kodein-DB"
    }
}
