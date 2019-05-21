buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:2.4.1")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "Kodein-DB"

include(
        ":ldb:kodein-leveldb-api",

        ":test-utils",

        ":ldb:lib",

        ":ldb:kodein-leveldb",

        ":kdb:kodein-db-api",
        ":kdb:kodein-db",

        ":kdb:serializer:kodein-db-serializer-kotlinx",
        ":kdb:serializer:kodein-db-serializer-kryo-jvm",

        ""
)

//val excludeAndroid: String? by settings

//if (excludeAndroid != "true") {
//}
