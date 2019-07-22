plugins {
    id("org.kodein.root")
}

val kotlinxAtomicFuVer by extra { "0.12.9" }
val kotlinxCoroutinesVer by extra { "1.2.2" }
val kodeinLogVer by extra { "0.1.0" }
val kodeinMemoryVer by extra { "0.1.0" }

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.11.12")
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
