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
	mkdir -p test.out.d/
	java \
		-jar process-info-collector/build/libs/process-info-collector-1.0-SNAPSHOT-all.jar \
		--help
	java \
		-jar process-info-collector/build/libs/process-info-collector-1.0-SNAPSHOT-all.jar \
		--write-default-config \
		--config test.out.d/default_config.properties
	java \
		-jar process-info-collector/build/libs/process-info-collector-1.0-SNAPSHOT-all.jar \
		--test-main
	java \
		-jar process-info-collector/build/libs/process-info-collector-1.0-SNAPSHOT-all.jar \
		--frontend-impl LOG \
		-o test.out.d/test_sleep \
		-- sleep 5

.PHONY: test
test:
	./gradlew test

.PHONY: test_calibrate
test_calibrate: build
	java \
		-jar process-info-calibrator/build/libs/process-info-calibrator-1.0-SNAPSHOT-all.jar \
		process-info-calibrator/oldTest/*/*

.PHONY: test_spark
test_spark: build
	 ./sparkw process-info-analyzer/build/libs/process-info-analyzer-1.0-SNAPSHOT-all.jar

.PHONY: scc
scc:
	bash scc.sh
