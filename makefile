JFLAGS = -g
JC = javac
RM = rm -f
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java
	
CLASSES = \
		Process.java\
		ProcessInterface.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) Process.class ProcessInterface.class
