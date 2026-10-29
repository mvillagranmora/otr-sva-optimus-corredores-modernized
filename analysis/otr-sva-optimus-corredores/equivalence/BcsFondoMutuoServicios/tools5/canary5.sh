#!/usr/bin/env bash
# Step 5 canary: 8 mutations over the NEW code of Normal and Manejador, each applied to an ISOLATED COPY of the module (never to the real sources).
# Per mutation: (1) JUnit of the Normal + Manejador test classes on the copy (must go RED), (2) the FRESH inputs through the oracle with the mutant classes
# (compare.py on the fresh dumps against the legacy dumps), (3) green control run after restoring the copy file.
# usage: canary5.sh [n ...]    needs: JAVA_HOME, ORACLE_WORK (compiled oracle), FRESH_CASES (fresh/cases), FRESH_BASE and FRESH_BASE_G0 (dumps of the UNMUTATED new code: baseline, all lists served / gate G0)
set -u
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
REAL="$WS/modernized/otr-sva-optimus-corredores"
SCR="${CANARY_SCRATCH:-$HOME/AppData/Local/Temp/mvscratch/canary5}"
LOG="$WS/analysis/otr-sva-optimus-corredores/equivalence/canary/BcsFondoMutuoServicios-solicitud"
CP="$SCR/copy/otr-sva-optimus-corredores"
MOD="$CP/BcsFondoMutuoServicios"
SRC="cl/bcs/fondomutuo"
TESTS='NormalEquivalenciaTest,NormalCompuertasYDefectosTest,NormalTesoreriaSpotAbiertasTest,ManejadorOraculoTest,ManejadorServiceTest,ManejadorServiceCompuertaTest,ManejadorJavaCallHandlerTest'
mkdir -p "$LOG" "$SCR/dumps"
if [ ! -f "$MOD/pom.xml" ]; then   # isolated copy: the module without target/, the parent pom reduced to this one module
  rm -rf "$SCR/copy"; mkdir -p "$MOD"
  cp -r "$REAL/BcsFondoMutuoServicios/src" "$REAL/BcsFondoMutuoServicios/pom.xml" "$MOD/"
  grep -v "<module>" "$REAL/pom.xml" | sed 's#</modules>#<module>BcsFondoMutuoServicios</module></modules>#' > "$CP/pom.xml"
fi
pristine() { cp "$REAL/BcsFondoMutuoServicios/src/main/java/$SRC/$1" "$MOD/src/main/java/$SRC/$1"; }
junit() { mvn -o -f "$CP/pom.xml" -pl :bcs-fondo-mutuo-servicios test -Dtest="$TESTS" -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false \
  -Dfile.encoding=UTF-8 2>&1 | grep -E "Tests run:.*Fail|FAIL|ERROR.*Test|BUILD|COMPILATION" | tail -n 16; }
oracle() { # kind
  rm -rf "$SCR/dumps/new"; mkdir -p "$SCR/dumps/new"
  if [ "$1" = nor ]; then
    MODULE_CLASSES="$(cygpath -m "$MOD/target/classes")" ORACLE_WORK="$ORACLE_WORK" ORACLE_OUT="$SCR/dumps/new" JAVA_OPTS="-Doracle.cases=$FRESH_CASES" "$BASE/legacy-oracle/subject4a.sh" FRESH-NOR > "$SCR/oracle.log" 2>&1
  else
    MODULE_CLASSES="$(cygpath -m "$MOD/target/classes")" ORACLE_WORK="$ORACLE_WORK" JAVA_OPTS="-Doracle.cases=$FRESH_CASES" "$BASE/legacy-oracle/run4b.sh" "$SCR/dumps/new" FRESH-MAN > "$SCR/oracle.log" 2>&1
  fi
  py -3 "$HERE/cmp_dumps.py" "$FRESH_BASE" "$SCR/dumps/new" "$(cygpath -m "$BASE")" FRESH-NOR FRESH-MAN
  if [ "$1" = man ]; then   # second run: the F1 configuration of the gate (only the Normal list served), baseline = dumps of the unmutated new code with the same gate
    rm -rf "$SCR/dumps/g0"; mkdir -p "$SCR/dumps/g0"
    MODULE_CLASSES="$(cygpath -m "$MOD/target/classes")" ORACLE_WORK="$ORACLE_WORK" JAVA_OPTS="-Doracle.cases=$FRESH_CASES -Doracle.listas=NORMAL" "$BASE/legacy-oracle/run4b.sh" "$SCR/dumps/g0" FRESH-MAN > "$SCR/oracle-g0.log" 2>&1
    echo "(gate G0 run, only the Normal list served)"
    py -3 "$HERE/cmp_dumps.py" "$FRESH_BASE_G0" "$SCR/dumps/g0" "$(cygpath -m "$BASE")" FRESH-MAN
  fi
}
mutar() { # n kind file 'sed expr' description
  local n="$1" kind="$2" f="$3" expr="$4" desc="$5"
  if [ -n "${ONLY:-}" ] && [[ " $ONLY " != *" $n "* ]]; then return; fi
  local path="$MOD/src/main/java/$SRC/$f"
  pristine "$f"; cp "$path" "$SCR/$n.orig"
  sed -i "$expr" "$path"
  if cmp -s "$SCR/$n.orig" "$path"; then echo "MUTATION $n NOOP (sed did not match)" > "$LOG/$n.log"; return; fi
  { echo "MUTATION $n: $desc"; echo "FILE $f"; diff "$SCR/$n.orig" "$path"; echo "--- JUnit on the mutant copy (must be RED)"; junit
    mkdir -p "$LOG/surefire-$n"; cp "$MOD"/target/surefire-reports/*.xml "$LOG/surefire-$n/" 2>/dev/null
    echo "--- fresh inputs through the oracle with the mutant classes"; oracle "$kind"; } > "$LOG/$n.log" 2>&1
  pristine "$f"; touch "$path"
  { echo "--- green control after restoring the copy"; junit; } >> "$LOG/$n.log" 2>&1
}
M="manejador/ManejadorSolicitudAporteRescateService.java"; N="normal"
mutar 1 man "$M" '105{/p_SimularOperacion = "S"/s/"S"/"N"/}' "Manejador first pass of Normal runs REAL (p_SimularOperacion S -> N): the simulation pass writes"
mutar 2 man "$M" '161{/ERRORESCONTADOR += /s/+=/-=/}' "Manejador counter semantics: += of the real pass becomes -="
mutar 3 man "$M" '53s/l.size() > 0/l.size() >= 0/' "Manejador G0 input: an empty list counts as a list with rows (size > 0 -> >= 0)"
mutar 4 nor "$N/NormalControles.java" '101{/total.compareTo(saldo) == 1/s/== 1/>= 0/}' "cash-flow check boundary: total > balance becomes total >= balance"
mutar 5 nor "$N/NormalRequisitos.java" '109{/MontoMinimo.compareTo(sol.Monto) == 1/s/== 1/>= 0/}' "series minimum amount boundary: minimum > amount becomes minimum >= amount"
mutar 6 nor "$N/NormalEscritura.java" '126{/break;/d}' "D-NOR-5: rejectFirst loses its break (all requests are rejected, not only the first)"
mutar 7 nor "$N/CrearSolicitudAporteRescateNormalService.java" '101{/eliminarBloqueo(c);/d}' "anti-duplicate lock is not released when the processing throws"
mutar 8 nor "$N/NormalArmado.java" '119s/MathContext.DECIMAL128/MathContext.DECIMAL32/' "precision: MontoPagoLocal subtraction with DECIMAL32 (7 digits) instead of DECIMAL128"
echo canary done
