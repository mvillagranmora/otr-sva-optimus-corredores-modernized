#!/usr/bin/env bash
# Step 6: the 3 canary survivors (4, 5, 8) of Step 5 re-applied to an ISOLATED COPY of the module (same sed expressions as tools5/canary5.sh);
# the module JUnit (same 7 classes as the canary) must now go RED. The real sources are never touched.
set -u
REAL=/c/Users/m.villagran.mora/Downloads/develop/nuam-modernizado/modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios
SCR=/c/Users/m.villagran.mora/AppData/Local/Temp/mvscratch/survivors6b
LOG=/c/Users/m.villagran.mora/Downloads/develop/nuam-modernizado/analysis/otr-sva-optimus-corredores/equivalence/canary/BcsFondoMutuoServicios-solicitud/survivors-after
CP=$SCR/copy/otr-sva-optimus-corredores
MOD=$CP/BcsFondoMutuoServicios
SRC=cl/bcs/fondomutuo
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
TESTS='NormalEquivalenciaTest,NormalCompuertasYDefectosTest,NormalTesoreriaSpotAbiertasTest,ManejadorOraculoTest,ManejadorServiceTest,ManejadorServiceCompuertaTest,ManejadorJavaCallHandlerTest'
mkdir -p "$LOG"
junit() { mvn -o -f "$CP/pom.xml" -pl :bcs-fondo-mutuo-servicios test -Dtest="$TESTS" -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dfile.encoding=UTF-8 2>&1 | grep -a -E "Tests run:|FAIL|ERROR.*(Test|equivalente)|BUILD|COMPILATION" | tail -n 24; }
mutar() { # n file 'sed expr' description
  local n="$1" f="$2" expr="$3" desc="$4"
  local path="$MOD/src/main/java/$SRC/$f"
  cp "$REAL/src/main/java/$SRC/$f" "$path"; cp "$path" "$SCR/$n.orig"
  sed -i "$expr" "$path"
  if cmp -s "$SCR/$n.orig" "$path"; then echo "MUTATION $n NOOP (sed did not match)" > "$LOG/$n.log"; return; fi
  { echo "MUTATION $n: $desc"; echo "FILE $f"; diff "$SCR/$n.orig" "$path"; echo "--- module JUnit on the mutant copy (RED = killed)"; junit; } > "$LOG/$n.log" 2>&1
  cp "$REAL/src/main/java/$SRC/$f" "$path"; touch "$path"
  { echo "--- green control after restoring the copy"; junit; } >> "$LOG/$n.log" 2>&1
}
N=normal
mutar 4 "$N/NormalControles.java" '101{/total.compareTo(saldo) == 1/s/== 1/>= 0/}' "cash-flow check boundary: total > balance becomes total >= balance"
mutar 5 "$N/NormalRequisitos.java" '109{/MontoMinimo.compareTo(sol.Monto) == 1/s/== 1/>= 0/}' "series minimum amount boundary: minimum > amount becomes minimum >= amount"
mutar 8 "$N/NormalArmado.java" '119s/MathContext.DECIMAL128/MathContext.DECIMAL32/' "precision: MontoPagoLocal subtraction with DECIMAL32 (7 digits) instead of DECIMAL128"
echo survivors done > "$LOG/DONE2.txt"
