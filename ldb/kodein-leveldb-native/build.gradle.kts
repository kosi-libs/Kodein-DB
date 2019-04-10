plugins {
    id("org.kodein.library.mpp")
}

evaluationDependsOn(":ldb:lib:leveldb")

kodein {
    kotlin {

        add(kodeinTargets.native.linuxX64) {
            mainCompilation.cinterops.apply {
                create("libleveldb-interop") {
                    defFile("src/allNativeMain/c_interop/libleveldb.def")
                    packageName("org.kodein.db.libleveldb")

                    includeDirs(Action {
                        headerFilterOnly(project(":ldb:lib:leveldb").file("build/out/include"))
                    })

                    includeDirs(Action {
                        headerFilterOnly("/usr/include")
                    })

                }
            }

            tasks[mainCompilation.cinterops["libleveldb-interop"].interopProcessingTaskName].dependsOn(project(":ldb:lib:leveldb").tasks["build"])
            tasks[mainCompilation.compileAllTaskName].dependsOn(project(":ldb:lib:leveldb").tasks["build"])
        }

    }
}
