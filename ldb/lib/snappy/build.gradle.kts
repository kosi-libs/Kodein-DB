
tasks.create<Delete>("clean") {
    delete(buildDir)
}

val srcDir = "${project(":ldb:lib").projectDir}/src/snappy"

val configure = tasks.create<Exec>("configure") {
    group = "build"

    val currentOs = org.gradle.internal.os.OperatingSystem.current()

    workingDir("$buildDir/cmake")
    commandLine("cmake")
    args(
            "-DSNAPPY_BUILD_TESTS:BOOL=0",

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
