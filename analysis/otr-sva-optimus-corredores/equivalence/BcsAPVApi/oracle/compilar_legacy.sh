#!/usr/bin/env bash
# Compiles the REAL legacy classes of BcsAPVServicios (cl.bcs.APV.*, the Entrada/Salida nested classes are inside them) into $WORK/legacy-classes, with the whole legacy
# source path (BcsUtils, BcsPlataformaServicios ... resolved lazily) and the jars of legacy/libs-private + the local maven repository. legacy/ is only read.
#
# Two NON-APV legacy classes cannot compile on this JDK/jar set and are replaced by a patched COPY placed first on the source path (the originals stay untouched):
#   - cl.bcs.plataforma.val.util.ValorizarWsBCS    javax.xml.soap -> jakarta.xml.soap (JDK 11+ no longer ships SAAJ)       (valuation web service client, not part of APV)
#   - cl.bcs.normativa.cartola.utils.GraficadorPastel   lines 46-48 (setBaseSectionOutline*) removed: API removed in jfreechart 1.5   (pie chart painting, not part of APV)
# No Entrada/Salida class, no APV class and no BcsUtils class is modified. The script checks the two edits are exactly those.
set -euo pipefail
source "$(dirname "${BASH_SOURCE[0]}")/entorno.sh"
OUT="$WORK/legacy-build"
$PY -I "$HERE/listar_operaciones.py" "$(mx "$HERE/ops.json")"
rm -rf "$OUT"; mkdir -p "$OUT/shim/cl/bcs/plataforma/val/util" "$OUT/shim/cl/bcs/normativa/cartola/utils" "$OUT/classes"

VW="$LEG/BSCL/Plataforma/JAVA/BcsPlataformaServicios/src/cl/bcs/plataforma/val/util/ValorizarWsBCS.java"
sed 's/javax\.xml\.soap/jakarta.xml.soap/g' "$VW" > "$OUT/shim/cl/bcs/plataforma/val/util/ValorizarWsBCS.java"
GP="$LEG/BSCL/Normativa/Java/BcsNormativaServicios/src/cl/bcs/normativa/cartola/utils/GraficadorPastel.java"
sed '46,48d' "$GP" > "$OUT/shim/cl/bcs/normativa/cartola/utils/GraficadorPastel.java"
# the edits are the expected ones: only those lines differ
d1=$(diff <(tr -d '\r' < "$VW") <(tr -d '\r' < "$OUT/shim/cl/bcs/plataforma/val/util/ValorizarWsBCS.java") | grep -c '^>' || true)
d1b=$(grep -c 'javax\.xml\.soap' "$VW" || true)
[ "$d1" = "$d1b" ] || { echo "ValorizarWsBCS copy differs in $d1 lines (expected $d1b)" >&2; exit 5; }
d2=$(diff <(tr -d '\r' < "$GP") <(tr -d '\r' < "$OUT/shim/cl/bcs/normativa/cartola/utils/GraficadorPastel.java") | grep -c '^<' || true)
[ "$d2" = "3" ] || { echo "GraficadorPastel copy removed $d2 lines (expected 3)" >&2; exit 5; }

SP="$(mx "$OUT/shim")"
for d in $(find "$LEG/BSCL" -maxdepth 4 -name src -type d | grep -v -i "test"); do SP="$SP$SEP$(mx "$d")"; done
CP="$(classpath_legacy)"
find "$LEG/BSCL/APV/JAVA/BcsAPVServicios/src" -name '*.java' | while read -r f; do echo "\"$(mx "$f")\""; done > "$OUT/sources.txt"
echo "== compiling $(wc -l < "$OUT/sources.txt") APV sources (+ what they reference) into $OUT/classes"
"$JAVA_HOME/bin/javac" --release 11 -encoding UTF-8 -proc:none -nowarn -Xlint:none -Xmaxerrs 200 -d "$(mx "$OUT/classes")" -sourcepath "$SP" -cp "$CP" @"$(mx "$OUT/sources.txt")" 2> "$OUT/javac.log"
tail -3 "$OUT/javac.log" || true
echo "== compiling the legacy oracle"
mkdir -p "$WORK/oracle-legacy"
"$JAVA_HOME/bin/javac" -encoding UTF-8 -nowarn -d "$(mx "$WORK/oracle-legacy")" -cp "$(mx "$OUT/classes")$SEP$CP" "$(mx "$HERE/java/LegacyApvOracle.java")"
echo "ok: $OUT/classes ($(find "$OUT/classes/cl/bcs/APV" -maxdepth 1 -name '*.class' | wc -l) APV classes)"
