import org.jetbrains.kotlin.gradle.plugin.experimental.KotlinNativeTestComponent

plugins {
    id("kodein-native")
}

dependencies {
    expectedBy(project(":ldb:k-ldb:k-ldb-tests-common"))

    implementation(project(":ldb:ldb-api:kodein-leveldb-api-native"))
    implementation(project(":ldb:ldb-lib:leveldb-native-interop"))
    testImplementation(project(":test:test-utils-native"))
    testImplementation("org.kodein.log:kodein-log-frontend-print-native:1.0.0")
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

