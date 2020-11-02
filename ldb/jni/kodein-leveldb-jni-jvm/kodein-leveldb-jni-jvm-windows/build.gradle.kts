plugins {
    id("org.kodein.library.jvm")
}

dependencies {
    implementation(project(":ldb:kodein-leveldb"))
}

val currentOs = org.gradle.internal.os.OperatingSystem.current()

val copyResources by tasks.creating(Sync::class) {
    onlyIf { currentOs.isWindows }
    if (currentOs.isWindows) {
        dependsOn(
            ":ldb:jni:c:buildKodein-leveldb-jni-windows",
            ":ldb:jni:c:genInfoProperties-windows"
        )
    }
    from(rootDir.resolve("ldb/jni/c/build/cmake/out/kodein-leveldb-jni-windows/lib/libkodein-leveldb-jni.dll"))
    from(rootDir.resolve("ldb/jni/c/build/generated/kodein-leveldb-jni-windows/kodein-leveldb-jni.properties"))
    into(buildDir.resolve("jniResources/org/kodein/db/leveldb/jvm/jni/windows"))
}

tasks.getByName<ProcessResources>(kotlin.target.compilations["main"].processResourcesTaskName) {
    onlyIf { currentOs.isWindows }
    dependsOn(copyResources)
    from(buildDir.resolve("jniResources"))
}

kodeinUpload {
    name = "kodein-leveldb-jni-jvm-windows"
    description = "LevelDB native library for Windows JVM"
    packageOf = ":ldb:jni:kodein-leveldb-jni-jvm"
}

tasks.withType<PublishToMavenRepository>().all {
    onlyIf { currentOs.isWindows }
    if (currentOs.isWindows) tasks["hostOnlyPublish"].dependsOn(this)
}
