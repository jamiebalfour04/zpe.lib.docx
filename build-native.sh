#!/usr/bin/env bash
set -euo pipefail
GRAAL_HOME="${GRAAL_HOME:-$HOME/.sdkman/candidates/java/current}"
JARS="${ZPE_DEPENDENCY_DIR:-$HOME/Sync/Programs/JARs}"
BUILD_DIR="${BUILD_DIR:-build/native}"
# POI only requires the Log4j API. Deliberately omit log4j-core from the native
# image: its runtime plugin discovery needs substantial GraalVM reflection
# metadata and otherwise produces broken PatternLayout converter warnings.
DEPENDENCIES=(poi-5.4.1.jar poi-ooxml-5.4.1.jar poi-ooxml-full-5.4.1.jar xmlbeans-5.3.0.jar commons-compress-1.27.1.jar commons-io-2.19.0.jar commons-collections4-4.4.jar log4j-api-2.24.3.jar curvesapi-1.08.jar)
if [ ! -x "$GRAAL_HOME/bin/native-image" ]; then
  printf 'GraalVM native-image was not found at %s\n' "$GRAAL_HOME/bin/native-image" >&2
  exit 1
fi
CP="$BUILD_DIR/classes"
for dependency in "${DEPENDENCIES[@]}"; do
  if [ ! -f "$JARS/$dependency" ]; then
    printf 'Missing dependency: %s\n' "$JARS/$dependency" >&2
    exit 1
  fi
  CP="$CP:$JARS/$dependency"
done
mkdir -p "$BUILD_DIR/classes"
"$GRAAL_HOME/bin/javac" -cp "$CP" -d "$BUILD_DIR/classes" native-src/DocxNativePlugin.java
"$GRAAL_HOME/bin/native-image" --shared --no-fallback --enable-native-access=ALL-UNNAMED \
  -cp "$CP" \
  -H:+UnlockExperimentalVMOptions \
  -H:+AddAllCharsets \
  -H:Path="$BUILD_DIR" \
  -H:Name=zpe.lib.docx \
  -H:ConfigurationFileDirectories=native-config/generated \
  -H:ReflectionConfigurationFiles=native-config/reflect-config.json \
  -H:IncludeResources='.*\.(xsb|xml|rels|properties)|META-INF/services/.*' \
  -H:-UnlockExperimentalVMOptions \
  DocxNativePlugin
case "$(uname -s)" in
 Darwin) [ -f "$BUILD_DIR/libzpe.lib.docx.dylib" ] && mv "$BUILD_DIR/libzpe.lib.docx.dylib" "$BUILD_DIR/zpe.lib.docx.dylib";OUTPUT="$BUILD_DIR/zpe.lib.docx.dylib";;
 Linux) [ -f "$BUILD_DIR/libzpe.lib.docx.so" ] && mv "$BUILD_DIR/libzpe.lib.docx.so" "$BUILD_DIR/zpe.lib.docx.so";OUTPUT="$BUILD_DIR/zpe.lib.docx.so";;
 *) OUTPUT="$BUILD_DIR/zpe.lib.docx.dll";;
esac
printf 'Built %s\n' "$OUTPUT"
