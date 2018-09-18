buildscript {

    repositories {
        jcenter()
        google()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://dl.bintray.com/salomonbrys/KMP-Gradle-Utils")
        maven(url = "https://dl.bintray.com/kodein-framework/Kodein-Internal-Gradle")
        maven(url = "https://dl.bintray.com/salomonbrys/wup-digital-maven")
        mavenLocal()
    }

    dependencies {
        classpath("org.kodein.internal.gradle:kodein-internal-gradle-plugin:1.2.0-K1.3")
    }

}

val kotlinxIoVer by extra { "0.1.0-eap13-gradle-4.10" }
val kodeinLogVer by extra { "1.0.0" }

allprojects {
    group = "org.kodein.db"
    version = "1.0"

    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx") {
            metadataSources {
                mavenPom()
            }
        }
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        jcenter()
        google()
        mavenLocal()
    }
}

val travisBuild by extra { System.getenv("TRAVIS") == "true" }
