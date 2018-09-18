import org.jetbrains.kotlin.gradle.plugin.experimental.KotlinNativeTestComponent
import org.jetbrains.kotlin.gradle.plugin.experimental.internal.KotlinNativeMainComponent

plugins {
    id("kodein-native")
}

components.named<KotlinNativeMainComponent>("main") {

    dependencies(Action {
        cinterop("libleveldb-interop", Action {
            defFile("src/main/c_interop/libleveldb.def")

            includeDirs(Action {
                headerFilterOnly(project(":ldb:ldb-lib").file("src/leveldb/include"))
            })

            target("linux_x64", Action {
                includeDirs(Action {
                    headerFilterOnly("/usr/include")
                })
            })

            target("macos_x64", Action {
                includeDirs(Action {
                    headerFilterOnly("/usr/include", "/opt/local/include")
                })
            })
        })
    })

}

evaluationDependsOn(":ldb:ldb-lib:leveldb")

components.named<KotlinNativeTestComponent>("test") {
    allTargets(Action {
        linkerOpts("-L" + project(":ldb:ldb-lib:leveldb").tasks["createDebug"].outputs.files.first().parent)
    })

    binaries.configureEach {
        compileTask.get().dependsOn(project(":ldb:ldb-lib:leveldb").tasks["createDebug"])
    }
}


kodeinPublication {
    upload {
        name = "Kodein-DB-LevelDB-Lib-Native"
        description = "Kodein LevelDB Library C Interop for Kotlin/Native"
        repo = "Kodein-DB"
    }
}
