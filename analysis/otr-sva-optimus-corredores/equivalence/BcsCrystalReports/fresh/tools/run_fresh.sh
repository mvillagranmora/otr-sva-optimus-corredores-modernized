#!/bin/bash
# Reproduces the fresh run (documentation of what was executed; nothing writes under modernized/ or legacy/).
# S = scratch dir; a COPY of the module without target/ is taken first (only the scratch env.sh REPO= line is pointed at the real repo
# so that the legacy sources are compiled from legacy/ read-only).
set -e
REPO=/c/Users/m.villagran.mora/Downloads/develop/nuam-modernizado
MODSRC=$REPO/modernized/otr-sva-optimus-corredores/BcsCrystalReports
EQ=$REPO/analysis/otr-sva-optimus-corredores/equivalence/BcsCrystalReports
S=/c/Users/m.villagran.mora/AppData/Local/Temp/mvscratch/crfresh
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-11.0.32.101-hotspot" PYTHONIOENCODING=utf-8
mkdir -p $S/mod $S/cases2
(cd $MODSRC && tar cf - --exclude=./target .) | (cd $S/mod && tar xf -)
sed -i "s|^REPO=.*|REPO=\"$REPO\"|" $S/mod/src/test/legacy-recorder/env.sh
# 1. inputs (needs $MODSRC/target/probe_all.json = probe.sh output of the development run)
py -3 -I $EQ/fresh/tools/gen_fresh.py "$MODSRC" "$EQ/fresh/input"
cp -r $EQ/fresh/input/* $S/cases2/
# 2. legacy engine (2 passes >= 65 s apart; writes $S/cases2/<case>/legacy/*)
(cd $S/mod && EQUIV_DIR=$(cygpath -w $S/cases2) bash src/test/legacy-recorder/record.sh)
# 3. sidecar through POST /report; facets of both sides into $S/out2/{legacy,new}
(cd $S/mod && mvn -o -f pom.xml test -Dtest=EquivalenceRestTest -Dequivalence.cases=$(cygpath -m $S/cases2) -Dequivalence.out=$(cygpath -m $S/out2))
# 4. copy facets to fresh/legacy and fresh/new, build fresh-cases.json (+ freshness check), run the real comparator
cp -r $S/out2/legacy/* $EQ/fresh/legacy/; cp -r $S/out2/new/* $EQ/fresh/new/
py -3 -I $EQ/fresh/tools/make_fresh_cases.py "$EQ"
py -3 -I /c/Users/m.villagran.mora/.claude/plugins/cache/claude-plugins-official/code-modernization/1.0.0/scripts/compare.py \
   $EQ/fresh-cases.json --out $EQ/FRESH_EQUIVALENCE.json
