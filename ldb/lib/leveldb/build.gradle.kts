
tasks.create<Delete>("clean") {
    delete(buildDir)
}

val srcDir = "${project(":ldb:lib").projectDir}/src/leveldb"

evaluationDependsOn(":ldb:lib:snappy")
evaluationDependsOn(":ldb:lib:crc32c")

val configure = tasks.create<Exec>("configure") {
    group = "build"

    val currentOs = org.gradle.internal.os.OperatingSystem.current()

    dependsOn(project(":ldb:lib:snappy").tasks["build"])
    dependsOn(project(":ldb:lib:crc32c").tasks["build"])

    workingDir("$buildDir/cmake")
    commandLine("cmake")
    args(
            "-DLEVELDB_BUILD_BENCHMARKS:BOOL=0",
            "-DLEVELDB_BUILD_TESTS:BOOL=0",

            "-DCMAKE_C_COMPILER:STRING=clang",
            "-DCMAKE_CXX_COMPILER:STRING=clang++",

            (
                    if (currentOs.isLinux())
                        "-DCMAKE_SYSROOT=${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64/x86_64-unknown-linux-gnu/sysroot"
                    else
                        ""
            ),

            "-DCMAKE_POSITION_INDEPENDENT_CODE:BOOL=1",

            "-DCMAKE_INSTALL_PREFIX:PATH=$buildDir/out",

            (
                    if (currentOs.isLinux())
                        "-DCMAKE_C_FLAGS:STRING=-D_GLIBCXX_USE_CXX11_ABI=0 -fPIC -pthread --gcc-toolchain=${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64"
                    else
                        ""
                    ),

            (
                    if (currentOs.isLinux())
                        "-DCMAKE_CXX_FLAGS:STRING=-D_GLIBCXX_USE_CXX11_ABI=0  -fPIC -pthread --gcc-toolchain=${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64"
                    else
                        ""
                    ),

            "-DCMAKE_EXE_LINKER_FLAGS:STRING=-L${project(":ldb:lib:snappy").buildDir}/out/lib -L${project(":ldb:lib:crc32c").buildDir}/out/lib -lstdc++",
            srcDir
    )

    inputs.dir(srcDir)
    outputs.dir(workingDir)

    doFirst {
        mkdir(workingDir)
    }
}

tasks.create<Exec>("build") {
    group = "build"

    dependsOn(configure)

    workingDir("$buildDir/cmake")
    commandLine("make")
    args(
            "all",
            "install",
            "VERBOSE=1"
    )

    inputs.dir(srcDir)
    inputs.dir(configure.workingDir)
    outputs.dir("${buildDir}/out")
}
