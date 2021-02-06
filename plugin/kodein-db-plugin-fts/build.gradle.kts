plugins {
    id("org.kodein.library.mpp")
}

kodein {
    kotlin {

        common.main {
            kotlin.srcDir("$buildDir/generated/stemmers/commonKotlin")
            dependencies {
                api(project(":ldb:kodein-leveldb-api"))
            }
        }

        add(kodeinTargets.jvm.jvm) {
            main.dependencies {
            }
        }

//        add(kodeinTargets.native.allApple + kodeinTargets.native.allDesktop) {
//            main.dependencies {
//            }
//        }
    }
}
