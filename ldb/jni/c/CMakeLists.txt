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

#if(MSVC)
#    set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} /NODEFAULTLIB:leveldb")
#endif()

target_link_libraries(
        kodein-leveldb-jni
        ${DEPS_DIR}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}leveldb${CMAKE_STATIC_LIBRARY_SUFFIX}
        ${DEPS_DIR}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}snappy${CMAKE_STATIC_LIBRARY_SUFFIX}
        ${DEPS_DIR}/lib/${CMAKE_STATIC_LIBRARY_PREFIX}crc32c${CMAKE_STATIC_LIBRARY_SUFFIX}
)

install(
        TARGETS kodein-leveldb-jni
        ARCHIVE EXCLUDE_FROM_ALL
        RUNTIME DESTINATION lib
        LIBRARY DESTINATION lib
        COMPONENT library
)
