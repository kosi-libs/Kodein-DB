buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://raw.githubusercontent.com/Kodein-Framework/kodein-internal-gradle-plugin/mvn-repo")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:6.5.1")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "Kodein-DB"

include(
        ":ldb:kodein-leveldb-api",

        ":test-utils",

        ":ldb:lib",

        ":ldb:jni",
        ":ldb:jni:c",
        ":ldb:jni:kodein-leveldb-jni-jvm",
        ":ldb:jni:kodein-leveldb-jni-jvm:kodein-leveldb-jni-jvm-macos",
        ":ldb:jni:kodein-leveldb-jni-jvm:kodein-leveldb-jni-jvm-linux",
        ":ldb:jni:kodein-leveldb-jni-jvm:kodein-leveldb-jni-jvm-windows",

        ":ldb:kodein-leveldb",
        ":ldb:kodein-leveldb-inmemory",

        ":kdb:kodein-db-api",
        ":kdb:kodein-db",
        ":kdb:kodein-db-inmemory",

        ":kdb:serializer:kodein-db-serializer-kotlinx",
        ":kdb:serializer:kodein-db-serializer-kryo-jvm",

        ":plugins:kodein-db-encryption",
        ""
)
