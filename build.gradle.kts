plugins {
    id("org.kodein.root")
}

val kotlinxAtomicFuVer by extra { "0.14.4" } // CAUTION: also change in buildscript!
val kotlinxSerializationVer by extra { "1.0.0-RC" }
val kodeinLogVer by extra { "0.5.0" }
val kodeinMemoryVer by extra { "0.3.0" }

buildscript {
    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.14.4")
    }
}

allprojects {
    group = "org.kodein.db"
    version = "0.3.0-beta"

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
