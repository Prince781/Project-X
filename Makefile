# build Java projects
SRC=src
BIN=bin
LIB=lib
JAR=`basename "$(PWD)"`.jar
MANIFEST=manifest.mf
JCC=javac
LIBJARS=$(shell find $(LIB) -type f -name '*.jar' | sed ':a;N;$$!ba;s/\n/:/g')
LIBDIRS=$(shell find $(LIB) -type d)
JFLAGS=-g
JARCC=jar
JARFLAGS=cfm
JFILES=$(shell find $(SRC) -type f -name '*.java')

all: $(JFILES)
	$(JCC) $(JFLAGS) $(JFILES) -d $(BIN)
	$(JARCC) $(JARFLAGS) $(JAR) $(MANIFEST) -C $(BIN) .

clean:
	@rm -f $(JAR)
	@rm -rf $(BIN)/com
