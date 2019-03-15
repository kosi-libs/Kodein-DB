plugins {
    id("org.kodein.library.mpp")
}

evaluationDependsOn(":ldb:ldb-lib:leveldb")

kodein {
    kotlin {

        add(kodeinTargets.native.linuxX64) {
            mainCompilation.cinterops.apply {
                create("libleveldb-interop") {
                    defFile("src/allNativeMain/c_interop/libleveldb.def")
                    packageName("org.kodein.db.libleveldb")

                    includeDirs(Action {
                        headerFilterOnly(project(":ldb:ldb-lib").file("src/leveldb/include"))
                    })

                    includeDirs(Action {
                        headerFilterOnly("/usr/include")
                    })

                }
            }

            tasks[mainCompilation.compileAllTaskName].dependsOn(project(":ldb:ldb-lib:leveldb").tasks["createDebug"])
        }

    }
}
