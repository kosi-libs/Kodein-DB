rootProject.name = "Kodein-DB"

enableFeaturePreview("GRADLE_METADATA")

include(
        ":test:test-utils-common",
        ":test:test-utils-jvm",
        ":test:test-utils-native",

        ":ldb:ldb-api:kodein-leveldb-api-common",
        ":ldb:ldb-api:kodein-leveldb-api-jvm",
        ":ldb:ldb-api:kodein-leveldb-api-native",
        ":ldb:ldb-lib:leveldb",
        ":ldb:ldb-lib:leveldb-native-interop",
        ":ldb:ldb-jni:kodein-leveldb-jni",
        ":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm",
        ":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-android",
        ":ldb:k-ldb:k-ldb-tests-common",
        ":ldb:k-ldb:kodein-leveldb-jvm",
        ":ldb:k-ldb:kodein-leveldb-android",
        ":ldb:k-ldb:kodein-leveldb-native",

//        ":kdb:kdb-api:kodein-db-api-common",
//        ":kdb:kdb-impl:kodein-db-common",

        ""
)
