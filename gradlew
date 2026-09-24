#!/bin/sh
# Gradle start up script, delegating to the wrapper jar. Android Studio will
# offer to download the matching gradle-wrapper.jar automatically the first
# time you open this project (it isn't included in this export — see README).

DIR="$(cd "$(dirname "$0")" && pwd)"
APP_HOME="$DIR"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$CLASSPATH" ]; then
  echo "gradle-wrapper.jar not found. Open this project in Android Studio and" >&2
  echo "let it regenerate the wrapper, or run: gradle wrapper --gradle-version 8.7" >&2
  exit 1
fi

exec java -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
