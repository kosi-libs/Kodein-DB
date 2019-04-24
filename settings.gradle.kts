buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:2.4.0")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "Kodein-DB"

include(
        ":ldb:kodein-leveldb-api",

        ":test-utils",

        ":ldb:lib",

        ":ldb:kodein-leveldb",

        ":kdb:kodeindb-api",
        ":kdb:kodeindb",

        ""
)

//val excludeAndroid: String? by settings

//if (excludeAndroid != "true") {
//}
