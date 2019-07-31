cmake_minimum_required(VERSION 3.9)
project(leveldb)

add_subdirectory(${CMAKE_CURRENT_SOURCE_DIR}/../leveldb leveldb)

set_target_properties(leveldbutil PROPERTIES EXCLUDE_FROM_ALL true)
