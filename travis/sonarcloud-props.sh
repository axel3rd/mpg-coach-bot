#!/bin/sh

echo "Define SonarCloud branch vs pull-request properties"

export SONARCLOUD_PROPERTIES="-Dsonar.branch.name=$TRAVIS_BRANCH" 
if [ $TRAVIS_PULL_REQUEST != "false" ]; then 
    export SONARCLOUD_PROPERTIES="-Dsonar.pullrequest.key=$TRAVIS_PULL_REQUEST -Dsonar.pullrequest.branch=$TRAVIS_BRANCH"
fi

echo "SONARCLOUD_PROPERTIES: $SONARCLOUD_PROPERTIES"