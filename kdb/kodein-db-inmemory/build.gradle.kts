plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

        val kotlinxSerializationVer: String by rootProject.extra

        common.main.dependencies {
            api(project(":kdb:kodein-db"))
            implementation(project(":ldb:kodein-leveldb-inmemory"))
        }

        add(kodeinTargets.jvm.jvm)
        add(kodeinTargets.native.allDarwin + kodeinTargets.native.allDesktop)
    }
}

kodeinUpload {
    name = "kodein-db-inmemory"
    description = "Kodein-DB with in-memory storage for testing"
}
