plugins {
    id("org.kodein.library.jvm")
}

kodeinLib {
    dependencies {
        api(project(":kdb:kodein-db-api") target "jvm")
        api("com.esotericsoftware:kryo:4.0.2")
    }
}
