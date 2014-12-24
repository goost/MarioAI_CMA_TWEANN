all: release test
release: test.cpp
	g++ test.cpp -O3 -o release
test: test.cpp
	g++ test.cpp -Wall -Wextra -o test