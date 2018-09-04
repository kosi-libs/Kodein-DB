buildscript {

    repositories {
        jcenter()
        google()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
        maven(url = "https://dl.bintray.com/salomonbrys/KMP-Gradle-Utils")
        maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
        maven(url = "https://dl.bintray.com/salomonbrys/wup-digital-maven")
        mavenLocal()
    }

    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-plugin:1.2.0")
    }

}

allprojects {
    group = "org.kodein.db"
    version = "1.0"

    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx") {
            metadataSources {
                mavenPom()
            }
        }
        jcenter()
        google()
        mavenLocal()
    }
}

val travisBuild by extra { System.getenv("TRAVIS") == "true" }
