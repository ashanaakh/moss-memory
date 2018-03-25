JC       := javac
JCFLAGS  := -d

JVM      := java
JVMFLAGS := -cp

OUT_DIR  := out

default: run

.PHONY: run
run: prepare
	@$(JVM) $(JVMFLAGS) $(OUT_DIR) MemoryManagement

.PHONY: prepare
prepare:
	@$(JC) src/*.java $(JCFLAGS) $(OUT_DIR)
