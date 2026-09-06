#!/usr/bin/env bash
set -euo pipefail

DESTINATION="${1:-build/native-dependencies}"
BASE_URL="https://repo.maven.apache.org/maven2"
mkdir -p "$DESTINATION"

DEPENDENCIES=(
  "org/apache/poi/poi/5.4.1/poi-5.4.1.jar"
  "org/apache/poi/poi-ooxml/5.4.1/poi-ooxml-5.4.1.jar"
  "org/apache/poi/poi-ooxml-full/5.4.1/poi-ooxml-full-5.4.1.jar"
  "org/apache/xmlbeans/xmlbeans/5.3.0/xmlbeans-5.3.0.jar"
  "org/apache/commons/commons-compress/1.27.1/commons-compress-1.27.1.jar"
  "commons-io/commons-io/2.19.0/commons-io-2.19.0.jar"
  "org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar"
  "org/apache/logging/log4j/log4j-api/2.24.3/log4j-api-2.24.3.jar"
  "com/github/virtuald/curvesapi/1.08/curvesapi-1.08.jar"
)

for dependency in "${DEPENDENCIES[@]}"; do
  output="$DESTINATION/${dependency##*/}"
  if [ ! -f "$output" ]; then
    curl --fail --location --silent --show-error "$BASE_URL/$dependency" --output "$output"
  fi
done

printf 'Downloaded native dependencies to %s\n' "$DESTINATION"
