#!/usr/bin/env bash
if [ "${TRAVIS_SCALA_VERSION}" == "2.12.4" ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
then
    echo "Running tests with coverage and report submission"
    sbt ++$TRAVIS_SCALA_VERSION coverage test coverageReport coverageAggregate
else
    echo "Running tests without attempting to submit coverage reports"
    sbt "plz $TRAVIS_SCALA_VERSION test"
fi
