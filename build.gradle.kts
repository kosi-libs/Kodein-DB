plugins {
    id("org.kodein.root")
}

val kotlinxAtomicFuVer by extra { "0.14.2" } // CAUTION: also change in buildscript!
val kotlinxSerializationVer by extra { "0.20.0" }
//val kotlinxCoroutinesVer by extra { "1.3.3" }
//val kodeinLogVer by extra { "0.2.0-dev-805458618" }
//val kodeinMemoryVer by extra { "0.2.0-dev-806010368" }
val kodeinLogVer by extra { "0.2.0-dev-17" }
val kodeinMemoryVer by extra { "0.2.0-dev-26" }

buildscript {
    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.14.2")
    }
}

allprojects {
    group = "org.kodein.db"
    version = "0.2.0"

    repositories {
        mavenLocal()
        google()
        maven(url = "https://kotlin.bintray.com/kotlinx")
        maven(url = "https://dl.bintray.com/kodein-framework/kodein-dev")
        jcenter()
    }
}

kodeinPublications {
    repo = "Kodein-DB"
}

// see https://github.com/gradle/kotlin-dsl/issues/607#issuecomment-375687119
subprojects { parent!!.path.takeIf { it != rootProject.path }?.let { evaluationDependsOn(it)  } }
