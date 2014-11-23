LIBRARY := libjcmatweann.so
OBJS := cmatweann.o
INCLUDES := -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
CXXFLAGS := ${INCLUDES} -fPIC -Wall


${LIBRARY}: ${OBJS}
	${CXX} -shared -o $@ $^ -Wall