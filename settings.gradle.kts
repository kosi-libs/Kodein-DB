buildscript {
    repositories {
        mavenLocal()
        maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
    }
    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-settings:2.3.2")
    }
}

apply { plugin("org.kodein.settings") }

rootProject.name = "Kodein-DB"

include(
        ":ldb:kodein-leveldb-api",

//        ":test-utils",
//
//        ":ldb:ldb-lib:leveldb",
//        ":ldb:ldb-lib:leveldb-native-interop",
//
//        ":ldb:ldb-jni:kodein-leveldb-jni",
//        ":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-jvm",
//        ":ldb:ldb-jni:ldb-jni-lib:kodein-leveldb-jni-lib-android",
//
//        ":ldb:kodein-leveldb",
//
//        ":kdb:kodeindb-api",
//        ":kdb:kodeindb",

        ""
)

//val excludeAndroid: String? by settings
//
//if (excludeAndroid != "true") {
//}
