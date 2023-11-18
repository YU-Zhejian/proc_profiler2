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
	java \
	-Dlogback.configurationFile=logback.xml \
	-jar process-info-collector/build/libs/process-info-collector-1.0-SNAPSHOT-all.jar \
	--help
	java \
	-Dlogback.configurationFile=logback.xml \
	-jar process-info-collector/build/libs/process-info-collector-1.0-SNAPSHOT-all.jar \
	--write-default-config --config test.out.d/default_config.properties
	java \
	-Dlogback.configurationFile=logback.xml \
	-jar process-info-collector/build/libs/process-info-collector-1.0-SNAPSHOT-all.jar \
	--test-main
	java \
	-Dlogback.configurationFile=logback.xml \
	-jar process-info-collector/build/libs/process-info-collector-1.0-SNAPSHOT-all.jar \
	--frontend-impl LOG \
	-o test.out.d/test_sleep \
	-- sleep 5

.PHONY: scc
scc:
	bash scc.sh
