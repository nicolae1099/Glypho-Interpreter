JFLAGS = -g
JC = javac
JVM = java
HW_PATH = src/Main

build:
	$(JC) $(JFLAGS) $(HW_PATH).java -d out

clean:
	rm -rf bin

run:
	$(JVM) -cp out/ Main $(input) $(base)