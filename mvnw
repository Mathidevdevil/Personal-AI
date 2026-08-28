#!/bin/sh
# Maven Wrapper Script for Unix

PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`"/$link"
  fi
done
DIR=`dirname "$PRG"`

if [ -f "$DIR/.tools/apache-maven-3.9.6/bin/mvn" ]; then
    chmod +x "$DIR/.tools/apache-maven-3.9.6/bin/mvn"
    exec "$DIR/.tools/apache-maven-3.9.6/bin/mvn" "$@"
else
    exec mvn "$@"
fi
