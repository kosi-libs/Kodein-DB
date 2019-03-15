plugins {
    id("org.kodein.library.jvm")
}

kodeinLib {
    dependencies {
        compile(project(":ldb:kodein-leveldb-api") target "jvm")
    }
}
