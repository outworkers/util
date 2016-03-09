#!/usr/bin/env bash

if [ "${TRAVIS_PULL_REQUEST}" == "false" ] && [ "${TRAVIS_BRANCH}" == "develop" ];
then
    CURRENT_VERSION = "$(sbt version)"
    echo "Bumping release version with a patch increment from $CURRENT_VERSION"
    sbt version-bump-patch

    NEW_VERSION = "$(sbt version)"
    echo "Creating Git tag for version $NEW_VERSION"
    git tag -a "v$NEW_VERSION" -m "Releasing $NEW_VERSION"

    echo "Pushing tag to GitHub."
    git push --tags

    echo "Publishing signed artifact"
    sbt bintray:publish

    echo "Going to master branch"
    git checkout master

    echo "Integrating latest release with master branch"
    git merge develop

else
    echo "This is either a pull request or the branch is not develop, deployment not necessary"
fi
