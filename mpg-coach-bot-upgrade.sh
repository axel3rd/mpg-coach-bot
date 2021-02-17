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

echo "$VERSION" | grep "SNAPSHOT" > /dev/null
if [ $? -eq 0 ]; then
    echo "ERROR: SNAPSHOT version update is currently not supported (GitHub packages does not allow non-authenticated access), exiting";
    exit 1;
fi

FILE_PREFIX="mpg-coach-bot";
FILE_DOWNLOAD="$FILE_PREFIX-$VERSION.zip";
URL="https://github.com/axel3rd/mpg-coach-bot/releases/download/mpg-coach-bot-$VERSION/$FILE_DOWNLOAD"

echo "Download: $URL";
curl -L "$URL" -o "$FILE_DOWNLOAD";
if [ $? -ne 0 ]; then
    echo "ERROR: Package cannot be downloaded, exiting";
    exit 1;
fi

rm -f "$FILE_PREFIX-"*".jar";
rm -f "mpg-coach-bot.sh";

unzip "$FILE_DOWNLOAD" "mpg-coach-bot.sh";
unzip "$FILE_DOWNLOAD" "$FILE_PREFIX-$VERSION.jar";

chmod 700 "mpg-coach-bot.sh";

rm "$FILE_DOWNLOAD";
