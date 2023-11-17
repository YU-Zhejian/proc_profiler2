include exports.mk

export OLDPATH:=$(PATH)
export PATH:=$(JAVA_HOME)/bin:$(OLDPATH)


.PHONY: build
build: fmt
	./gradlew build

.PHONY: fmt
fmt:
	./gradlew goJF