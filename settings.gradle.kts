rootProject.name = "Kodein-DB"

enableFeaturePreview("GRADLE_METADATA")

include(
        ":leveldb:api:leveldb-api-common",
        ":leveldb:api:leveldb-api-jvm",
        ":leveldb:api:leveldb-api-native",
        ":leveldb:lib:leveldb-native",
        ":leveldb:jni:leveldb-jni",
        ":leveldb:jni:leveldb-jni-native",
        ":leveldb:jni:leveldb-jni-android",
        ":leveldb:tests:common",
        ":leveldb:tests:jvm",
        ":leveldb:kodein-leveldb:kodein-leveldb-jvm",
        ":leveldb:kodein-leveldb:kodein-leveldb-android"
//        ":leveldb:kodein-leveldb:kodein-leveldb-native"
)
