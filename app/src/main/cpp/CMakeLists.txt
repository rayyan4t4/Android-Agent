cmake_minimum_required(VERSION 3.22.1)
project("llama-android")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -DNDEBUG")

set(LLAMA_DIR ${CMAKE_CURRENT_SOURCE_DIR}/llama.cpp)

file(GLOB LLAMA_SOURCES
    ${LLAMA_DIR}/src/*.cpp
    ${LLAMA_DIR}/src/*.c
    ${LLAMA_DIR}/ggml/src/*.cpp
    ${LLAMA_DIR}/ggml/src/*.c
    ${LLAMA_DIR}/ggml/src/ggml-cpu/*.cpp
    ${LLAMA_DIR}/ggml/src/ggml-cpu/*.c
)

add_library(llama-android SHARED
    llama_jni.cpp
    ${LLAMA_SOURCES}
)

target_include_directories(llama-android PRIVATE
    ${LLAMA_DIR}/include
    ${LLAMA_DIR}/ggml/include
    ${LLAMA_DIR}/ggml/src
    ${LLAMA_DIR}/ggml/src/ggml-cpu
    ${LLAMA_DIR}/src
    ${LLAMA_DIR}/common
)

target_compile_definitions(llama-android PRIVATE
    GGML_USE_CPU
)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(llama-android
    ${log-lib}
    ${android-lib}
)
