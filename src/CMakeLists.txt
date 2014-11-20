#created by Gleb Ostrowski 20141120
cmake_minimum_required(VERSION 3.0)
project( jcmatweann )

find_package(Java REQUIRED)
find_package(JNI REQUIRED)
include(UseJava)

# compile de.goost.jcmatweann.NeuralNet.java and de.goost.jcmatweann.CMATWEANN.java
#add_jar(jcmatweann de.goost.jcmatweann.NeuralNet.java de.goost.jcmatweann.CMATWEANN.java) 
#get_target_property(_jarFile jcmatweann JAR_FILE)
#get_target_property(_classDir jcmatweann CLASSDIR)


# generate libfoo.jnilib
set(CMAKE_BUILD_TYPE "Release")
set(projectpath ${PROJECT_SOURCE_DIR}/native)
set(header ${projectpath}/cmatweann/Eigen ${projectpath}/cmatweann/cmat.h ${projectpath}/cmatweann/nn.h ${projectpath}/de_goost_jcmatweann_CMATWEANN.h ${projectpath}/de_goost_jcmatweann_NeuralNet.h)
include_directories(${JAVA_INCLUDE_PATH} ${JAVA_INCLUDE_PATH2} )# ${_classDir})
add_library(jcmatweannNative MODULE  ${projectpath}/NeuralNet.cpp ${projectpath}/cmatweann.cpp ${header})
#set_target_properties(foo PROPERTIES SUFFIX ".jnilib")
target_link_libraries(jcmatweannNative RECUR ${JNI_LIBRARIES})