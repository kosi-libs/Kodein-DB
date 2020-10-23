plugins {
    id("org.kodein.library.jvm")
}

dependencies {
    api(project(":kdb:kodein-db-api"))
    api("com.esotericsoftware:kryo:4.0.2")
}

kodeinUpload {
    name = "kodein-db-serializer-kryo-jvm"
    description = "Kodein-DB with Kryo JVM serializer library"
}