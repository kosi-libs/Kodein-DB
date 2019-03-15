plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

        common.main.dependencies {
            api(project(":ldb:kodein-leveldb-api"))
        }

        add(kodeinTargets.jvm)

        add(kodeinTargets.native.linuxX64)

    }
}
