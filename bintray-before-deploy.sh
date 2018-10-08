#!/bin/sh
#
# Retrieve project version (and SNAPSHOT or not) for update bintray descriptor dynamically
#

FILE_DESCRIPTOR="bintray-descriptor.json";

VERSION=`cat target/classes/META-INF/maven/org.blondin/mpg-coach-bot/pom.properties | grep "^version" | cut -d= -f2`;
echo "Project version: $VERSION";
VERSION_SHORT=`echo "$VERSION" | sed 's/SNAPSHOT/SNAP/'`;
echo "Version short  : $VERSION_SHORT";

echo "Updating version in '$FILE_DESCRIPTOR' file";
sed -i -- "s/VERSION_TO_UPDATE/$VERSION_SHORT/g" $FILE_DESCRIPTOR

if [[ "$VERSION_SHORT" =~ "SNAP" ]]; then
    echo "Updating repository (snapshot) in '$FILE_DESCRIPTOR' file";
    sed -i -- "s/generic/generic-dev/g" $FILE_DESCRIPTOR
fi