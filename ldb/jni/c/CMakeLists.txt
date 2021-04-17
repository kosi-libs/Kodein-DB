cmake_minimum_required(VERSION 3.13)
project(LevelDB-JNI)

find_package(JNI REQUIRED)

set(CMAKE_CXX_STANDARD 14)

add_library(
        kodein-leveldb-jni SHARED
        src/main/cpp/leveldb-jni.cc
)

if(NOT DEFINED DEPS_DIR)
    message(FATAL_ERROR "DEPS_DIR must be defined")
endif()

target_include_directories(
        kodein-leveldb-jni
        PUBLIC build/nativeHeaders
        PRIVATE ${JNI_INCLUDE_DIRS}
        PRIVATE ${DEPS_DIR}/include
)

target_compile_definitions(
        kodein-leveldb-jni
        PUBLIC _GLIBCXX_USE_CXX11_ABI=0
)

target_link_libraries(
        kodein-leveldb-jni
        ${DEPS_DIR}/lib/libleveldb.a
        ${DEPS_DIR}/lib/libsnappy.a
        ${DEPS_DIR}/lib/libcrc32c.a
)

install(
        TARGETS kodein-leveldb-jni
        ARCHIVE EXCLUDE_FROM_ALL
        RUNTIME DESTINATION lib
        LIBRARY DESTINATION lib
        COMPONENT library
)
