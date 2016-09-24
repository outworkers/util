#!/usr/bin/env bash
if [ "${TRAVIS_SCALA_VERSION}" == "2.11.8" ] && [ "${TRAVIS_JDK_VERSION}" == "oraclejdk8" ];
then
    echo "Running tests with coverage and report submission"
    sbt ++$TRAVIS_SCALA_VERSION coverage ++$TRAVIS_SCALA_VERSION test ++$TRAVIS_SCALA_VERSION coverageReport ++$TRAVIS_SCALA_VERSION coverageAggregate ++$TRAVIS_SCALA_VERSION coveralls
    exit $?
else
    echo "Running tests without attempting to submit coverage reports"
    sbt ++$TRAVIS_SCALA_VERSION test
    exit $?
fi