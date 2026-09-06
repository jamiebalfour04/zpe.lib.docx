#!/usr/bin/env bash
set -euo pipefail
GRAAL_HOME="${GRAAL_HOME:-${GRAALVM_HOME:-$HOME/.sdkman/candidates/java/current}}"
JARS="${ZPE_DEPENDENCY_DIR:-$HOME/Sync/Programs/JARs}"
BUILD_DIR="${BUILD_DIR:-build/native}"
# POI only requires the Log4j API. Deliberately omit log4j-core from the native
# image: its runtime plugin discovery needs substantial GraalVM reflection
# metadata and otherwise produces broken PatternLayout converter warnings.
DEPENDENCIES=(poi-5.4.1.jar poi-ooxml-5.4.1.jar poi-ooxml-full-5.4.1.jar xmlbeans-5.3.0.jar commons-compress-1.27.1.jar commons-io-2.19.0.jar commons-collections4-4.4.jar log4j-api-2.24.3.jar curvesapi-1.08.jar)
JAVAC="${JAVAC:-$GRAAL_HOME/bin/javac}"
NATIVE_IMAGE="${NATIVE_IMAGE:-$GRAAL_HOME/bin/native-image}"
if [ ! -x "$NATIVE_IMAGE" ] && command -v native-image >/dev/null 2>&1; then
  NATIVE_IMAGE="$(command -v native-image)"
  JAVAC="$(command -v javac)"
fi
if [ ! -x "$NATIVE_IMAGE" ]; then
  printf 'GraalVM native-image was not found. Set GRAAL_HOME or GRAALVM_HOME.\n' >&2
  exit 1
fi
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) CP_SEPARATOR=';' ;;
  *) CP_SEPARATOR=':' ;;
esac
CP="$BUILD_DIR/classes"
for dependency in "${DEPENDENCIES[@]}"; do
  if [ ! -f "$JARS/$dependency" ]; then
    printf 'Missing dependency: %s\n' "$JARS/$dependency" >&2
    exit 1
  fi
  CP="$CP$CP_SEPARATOR$JARS/$dependency"
done
mkdir -p "$BUILD_DIR/classes"
"$JAVAC" -cp "$CP" -d "$BUILD_DIR/classes" native-src/DocxNativePlugin.java
"$NATIVE_IMAGE" --shared --no-fallback --enable-native-access=ALL-UNNAMED \
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
 MINGW*|MSYS*|CYGWIN*) OUTPUT="$BUILD_DIR/zpe.lib.docx.dll";;
 *) OUTPUT="$BUILD_DIR/zpe.lib.docx.dll";;
esac
if [ ! -f "$OUTPUT" ]; then
  printf 'Native plugin was not produced at %s\n' "$OUTPUT" >&2
  exit 1
fi
printf 'Built %s\n' "$OUTPUT"

# ZenC --binary builds use a portable static ABI rather than the GraalVM
# isolate ABI used by ZPEX's shared library.
if [ "${BUILD_STATIC_PLUGIN:-true}" = "false" ]; then
  exit 0
fi
"${CXX:-c++}" -std=c++17 -O2 -c native-src/docx_static.cpp -o "$BUILD_DIR/docx_static.o"
ar rcs "$BUILD_DIR/libzpe.lib.docx.a" "$BUILD_DIR/docx_static.o"
mkdir -p examples/binary-plugins
cp "$BUILD_DIR/libzpe.lib.docx.a" examples/binary-plugins/libzpe.lib.docx.a
printf 'Built %s and copied it beside the native example\n' "$BUILD_DIR/libzpe.lib.docx.a"
