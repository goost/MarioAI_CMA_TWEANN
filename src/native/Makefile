LIBRARY := libjcmatweann.so
OBJFILES := cmatweann.o NeuralNet.o
INCLUDES := -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux
CXXFLAGS := ${INCLUDES} -fPIC


${LIBRARY}: cmatweann.cpp NeuralNet.cpp
	g++ -I/usr/local/java/jdk1.8.0_25/include -I/usr/local/java/jdk1.8.0_25/include/linux -fPIC -shared -o libjcmatweann.so cmatweann.cpp