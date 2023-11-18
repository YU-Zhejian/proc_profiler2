include exports.mk

export OLDPATH:=$(PATH)
export PATH:=$(JAVA_HOME)/bin:$(OLDPATH)


.PHONY: build
build: fmt
	./gradlew build

.PHONY: fmt
fmt:
	./gradlew goJF

.PHONY: test_main
test_main: build
	java -jar process-info-collector/build/libs/process-info-collector-1.0-SNAPSHOT-all.jar --test-main


.PHONY: test_sleep
test_sleep: build
	java \
	-Dlogback.configurationFile=logback.xml \
	-jar process-info-collector/build/libs/process-info-collector-1.0-SNAPSHOT-all.jar \
	-o test.out.d/test_sleep -- sleep 5

.PHONY: scc
scc:
	bash scc.sh
