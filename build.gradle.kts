plugins {
    id("org.kodein.root")
}

val kotlinxAtomicFuVer by extra { "0.14.1" } // CAUTION: also change in buildscript!
val kotlinxSerializationVer by extra { "0.14.0" }
val kotlinxCoroutinesVer by extra { "1.3.3" }
val kodeinLogVer by extra { "0.1.0" }
val kodeinMemoryVer by extra { "0.1.0" }

buildscript {
    repositories {
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.14.1")
    }
}

allprojects {
    group = "org.kodein.db"
    version = "0.1.0"

    repositories {
        mavenLocal()
        google()
        maven(url = "https://kotlin.bintray.com/kotlinx")
        jcenter()
    }
}

kodeinPublications {
    repo = "Kodein-DB"
}

// see https://github.com/gradle/kotlin-dsl/issues/607#issuecomment-375687119
subprojects { parent!!.path.takeIf { it != rootProject.path }?.let { evaluationDependsOn(it)  } }
