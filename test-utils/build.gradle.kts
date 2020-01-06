plugins {
    id("org.kodein.mpp-with-android")
}

val kodeinLogVer: String by rootProject.extra
val kodeinMemoryVer: String by rootProject.extra

kodein {
    kotlin {
        common.main.dependencies {
            api("org.kodein.log:kodein-log:$kodeinLogVer")
            api("org.kodein.memory:kodein-memory:$kodeinMemoryVer")
            api(project(":ldb:kodein-leveldb-api"))

            api("org.jetbrains.kotlin:kotlin-test-common")
            api("org.jetbrains.kotlin:kotlin-test-annotations-common")
        }

        add(kodeinTargets.jvm.jvm) {
            target.setCompileClasspath()

            main.dependencies {
                api("org.jetbrains.kotlin:kotlin-test")
                api("org.jetbrains.kotlin:kotlin-test-junit")
                api("junit:junit:4.12")
            }
        }

        add(kodeinTargets.jvm.android) {
            main.dependencies {
                implementation("androidx.test.ext:junit:1.1.1")
                implementation("androidx.test.espresso:espresso-core:3.2.0")
            }
        }

        add(kodeinTargets.native.host)

        add(kodeinTargets.native.allIos)

        allTargets {
            mainCommonCompilation.kotlinOptions.freeCompilerArgs = listOf("-Xuse-experimental=kotlin.Experimental")
        }
    }
}
