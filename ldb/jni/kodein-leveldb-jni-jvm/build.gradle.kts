plugins {
    id("org.kodein.library.jvm")
}

dependencies {
    api(project(":ldb:jni:kodein-leveldb-jni-jvm:kodein-leveldb-jni-jvm-linux"))
    api(project(":ldb:jni:kodein-leveldb-jni-jvm:kodein-leveldb-jni-jvm-macos"))
    api(project(":ldb:jni:kodein-leveldb-jni-jvm:kodein-leveldb-jni-jvm-windows"))
}

kodeinUpload {
    name = "kodein-leveldb-jni-jvm"
    description = "LevelDB native libraries for desktop JVMs"
}
