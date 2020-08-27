buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
        maven(url = "https://dl.bintray.com/kodein-framework/kodein-dev")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:4.0.1")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "Kodein-DB"

include(
        ":ldb:kodein-leveldb-api",

        ":test-utils",

        ":ldb:lib",

        ":ldb:kodein-leveldb",
        ":ldb:jni",
        ":ldb:kodein-leveldb-jni",

        ":kdb:kodein-db-api",
        ":kdb:kodein-db",

        ":kdb:serializer:kodein-db-serializer-kotlinx",
        ":kdb:serializer:kodein-db-serializer-kryo-jvm",

        ""
)
