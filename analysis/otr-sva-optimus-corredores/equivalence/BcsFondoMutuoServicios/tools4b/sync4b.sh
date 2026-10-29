#!/usr/bin/env bash
# Isolated copy of the module for the step 4B canary / coverage: the other agent's (4A) in-progress packages are dropped, so the copy always compiles.
# Kept: everything else, plus ONLY the two published contract files of Normal (NormalContrato, NormalPort) that the Manejador uses.
# usage: sync4b.sh <dest dir>   (scratch; the module itself is never mutated)
set -e
WS="/c/Users/m.villagran.mora/Downloads/develop/nuam-modernizado"
MOD="$WS/modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios"
DST="$1"
mkdir -p "$DST"
rm -rf "$DST/src"
mkdir -p "$DST/src"
cp -p "$MOD/pom.xml" "$DST/pom.xml"
cp -rp "$MOD/src/main" "$DST/src/main"
cp -rp "$MOD/src/test" "$DST/src/test"
find "$DST/src/main/java/cl/bcs/fondomutuo/normal" -type f ! -name NormalContrato.java ! -name NormalPort.java -delete
find "$DST/src/test" -path '*fondomutuo/normal*' -type f -delete
find "$DST/src/test" -path '*fondomutuo-nor*' -type f -delete
find "$DST/src/test" -path '*cierre4a*' -type f -delete
sed -i "s#<relativePath>[^<]*</relativePath>#<relativePath>$WS/modernized/otr-sva-optimus-corredores/pom.xml</relativePath>#" "$DST/pom.xml"
echo synced "$DST"
