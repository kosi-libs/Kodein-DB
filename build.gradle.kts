plugins {
    id("org.kodein.root")
}

val kotlinxIoVer by extra { "0.1.7" }
val kotlinxAtomicFuVer by extra { "0.12.2" }
val kotlinxCoroutinesVer by extra { "1.1.1" }
val kodeinLogVer by extra { "0.1.0" }

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.11.12")
    }
}

allprojects {
    group = "org.kodein.db"
    version = "0.1.0-LGM"

    repositories {
        jcenter()
        maven(url = "https://kotlin.bintray.com/kotlinx")
        mavenLocal()
    }
}

kodeinPublications {
    repo = "Kodein-DB"
}
