#!/bin/sh
#
# Convenience Linux script to upgrade mpg-coach-bot version
#

VERSION="$1";
if [ -z "$VERSION" ]; then
  echo "Version should be first parameter: $0 x.y[-SNAPSHOT]";
  exit 1;
fi
echo "Version to upgrade: $VERSION";
cd $(dirname $0)

REPO="generic";
echo "$VERSION" | grep "SNAPSHOT" > /dev/null
if [ $? -eq 0 ]; then
  REPO="generic-dev";
fi

FILE_PREFIX="mpg-coach-bot";
FILE_DOWNLOAD="$FILE_PREFIX-$VERSION.zip";
URL="https://dl.bintray.com/axel3rd/$REPO/$FILE_DOWNLOAD"

echo "Download: $URL";
curl -L "$URL" -o "$FILE_DOWNLOAD";

rm "$FILE_PREFIX-"*".jar";
rm "mpg-coach-bot.sh";

unzip "$FILE_DOWNLOAD" "mpg-coach-bot.sh";
unzip "$FILE_DOWNLOAD" "$FILE_PREFIX-$VERSION.jar";

chmod 700 "mpg-coach-bot.sh";

rm "$FILE_DOWNLOAD";
