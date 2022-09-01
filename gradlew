#!/usr/bin/env bash
# YuZJ rewritten gradlew
set -ue

GRADLE_WRAPPER_MIRROR="https://repo.gradle.org/artifactory/libs-releases-local"
GRADLE_WRAPPER_VERSION="6.1.1"
GRADLE_WRAPPER_JAR_DIR="$(readlink -f "$(dirname "${0}")")/gradle/wrapper"

[ -f "${GRADLE_WRAPPER_JAR_DIR}/gradle-wrapper.jar" ] || \
wget "${GRADLE_WRAPPER_MIRROR}/org/gradle/gradle-wrapper/${GRADLE_WRAPPER_VERSION}/gradle-wrapper-${GRADLE_WRAPPER_VERSION}.jar" -O "${GRADLE_WRAPPER_JAR_DIR}/gradle-wrapper.jar"
[ -f "${GRADLE_WRAPPER_JAR_DIR}/gradle-cli.jar" ] || \
wget "${GRADLE_WRAPPER_MIRROR}/org/gradle/gradle-cli/${GRADLE_WRAPPER_VERSION}/gradle-cli-${GRADLE_WRAPPER_VERSION}.jar" -O "${GRADLE_WRAPPER_JAR_DIR}/gradle-cli.jar"

java -Xmx64m -Xms64m \
-Dorg.gradle.appname=gradlew \
--class-path "${GRADLE_WRAPPER_JAR_DIR}"/gradle-wrapper.jar:"${GRADLE_WRAPPER_JAR_DIR}"/gradle-cli.jar \
org.gradle.wrapper.GradleWrapperMain "${@}"
