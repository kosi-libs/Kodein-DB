
tasks.create<Delete>("clean") {
    delete(buildDir)
}

val srcDir = "${project(":ldb:lib").projectDir}/src/crc32c"

val configure = tasks.create<Exec>("configure") {
    group = "build"

    val currentOs = org.gradle.internal.os.OperatingSystem.current()
    fun ifLinux(str: String) = if (currentOs.isLinux) str else ""

    workingDir("$buildDir/cmake")
    commandLine("cmake")

    val toolchainDir = "${System.getenv("HOME")}/.konan/dependencies/target-gcc-toolchain-3-linux-x86-64"

    args(
            "-DCRC32C_BUILD_BENCHMARKS:BOOL=0",
            "-DCRC32C_BUILD_TESTS:BOOL=0",
            "-DCRC32C_USE_GLOG:BOOL=0",

            "-DCMAKE_C_COMPILER:STRING=clang",
            "-DCMAKE_CXX_COMPILER:STRING=clang++",

            ifLinux("-DCMAKE_SYSROOT=$toolchainDir/x86_64-unknown-linux-gnu/sysroot"),

            "-DCMAKE_POSITION_INDEPENDENT_CODE:BOOL=1",

            "-DCMAKE_INSTALL_PREFIX:PATH=$buildDir/out",

            ifLinux("-DCMAKE_C_FLAGS:STRING=-D_GLIBCXX_USE_CXX11_ABI=0 -fPIC -pthread --gcc-toolchain=$toolchainDir"),
            ifLinux("-DCMAKE_CXX_FLAGS:STRING=-D_GLIBCXX_USE_CXX11_ABI=0  -fPIC -pthread --gcc-toolchain=$toolchainDir"),

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
