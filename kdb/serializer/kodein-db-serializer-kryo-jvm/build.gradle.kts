plugins {
    id("org.kodein.library.jvm")
}

kodeinLib {
    dependencies {
        api(project(":kdb:kodein-db-api") target "jvm")
        api("com.esotericsoftware:kryo:5.0.0-RC4")
    }
}
