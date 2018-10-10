#!/bin/sh
#
# Retrieve project version (and SNAPSHOT or not) for update bintray descriptor dynamically
#

FILE_DESCRIPTOR="bintray-descriptor.json";

VERSION=`cat target/maven-archiver/pom.properties | grep "^version" | cut -d= -f2`;
echo "Project version: $VERSION";
VERSION_SHORT=`echo "$VERSION" | sed 's/SNAPSHOT/SNAP/'`;
echo "Version short  : $VERSION_SHORT";

echo "Updating version in '$FILE_DESCRIPTOR' file";
sed -i -- "s/VERSION_TO_UPDATE/$VERSION_SHORT/g" $FILE_DESCRIPTOR

IS_SNAP=`echo $VERSION_SHORT | grep SNAP`;

if [ "$IS_SNAP" ]; then
    echo "Updating repository (snapshot) in '$FILE_DESCRIPTOR' file";
    sed -i -- "s/generic/generic-dev/g" $FILE_DESCRIPTOR
fi