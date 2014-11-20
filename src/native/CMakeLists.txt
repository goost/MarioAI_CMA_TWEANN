#created by Gleb Ostrowski 20141120
cmake_minimum_required(VERSION 2.8)
project( jCMATWEANN )
set(CMAKE_BUILD_TYPE "Release")
#set(EIGEN_DIRECTORY . CACHE STRING "Eigen directory. The directory specified should have a subdirectory 'Eigen'.")
include_directories(${PROJECT_SOURCE_DIR})
set(header ${PROJECT_SOURCE_DIR}/cmatweann/cmat.h )
add_library( jCMATWEANN SHARED jCMATWEANN.cpp ${header})