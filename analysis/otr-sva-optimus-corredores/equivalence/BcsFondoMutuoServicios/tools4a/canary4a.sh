#!/usr/bin/env bash
# Step 4A canary: ONE line of the NEW code is broken at a time (file saved aside, restored with touch afterwards), the Normal tests must go RED, and a final
# run after the restore must be GREEN. Logs: equivalence/canary/BcsFondoMutuoServicios-4A/<n>.log (diff, red run, green run summary).
# usage: canary4a.sh   (JAVA_HOME set; run from anywhere; the lead's full suite is not touched: only -Dtest=Normal*)
set -u
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WS="$(cd "$HERE/../../../../.." && pwd)"
SRC="$WS/modernized/otr-sva-optimus-corredores/BcsFondoMutuoServicios/src/main/java/cl/bcs/fondomutuo"
LOG="$WS/analysis/otr-sva-optimus-corredores/equivalence/canary/BcsFondoMutuoServicios-4A"
SAVE="${CANARY_SAVE:-$HOME/AppData/Local/Temp/mvscratch/canary4a}"
mkdir -p "$LOG" "$SAVE"
run() { mvn -o -q -f "$WS/modernized/otr-sva-optimus-corredores/pom.xml" -pl :bcs-fondo-mutuo-servicios test -Dtest='NormalEquivalenciaTest,NormalCompuertasYDefectosTest' \
  -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dfile.encoding=UTF-8 2>&1 | grep -E "Tests run:|FAIL|BUILD" | tail -4; }
mutar() { # n file 'sed expression' description
  local n="$1" f="$SRC/$2" expr="$3" desc="$4"
  if [ -n "${ONLY:-}" ] && [[ " $ONLY " != *" $n "* ]]; then return; fi   # ONLY="5 7" re-runs just those mutations
  cp "$f" "$SAVE/$n.orig"
  sed -i "$expr" "$f"
  { echo "MUTATION $n: $desc"; echo "FILE $2"; diff "$SAVE/$n.orig" "$f"; echo "--- red run (must fail)"; run; } > "$LOG/$n.log" 2>&1
  cp "$SAVE/$n.orig" "$f"; touch "$f"
  { echo "--- green run after restore"; run; } >> "$LOG/$n.log" 2>&1
}
mutar 1 normal/NormalControles.java 's/sol.NumeroCuotas.compareTo(custodia) == 1/sol.NumeroCuotas.compareTo(custodia) == 0/' "custody check of a rescue: > custody becomes == custody"
mutar 2 normal/NormalControles.java 's/entrada.p_IdOrigenProceso != "PAC"/!"PAC".equals(entrada.p_IdOrigenProceso)/' "D-NOR-4: reference compare != \"PAC\" fixed to equals (PAC exempt by JSON)"
mutar 3 normal/CrearSolicitudAporteRescateNormalService.java 's/c.solicitudesBloqueadas.size() == idsBd.size() \&\& ids.equals(idsBd)/c.solicitudesBloqueadas.size() >= idsBd.size()/' "anti-duplicate lock: the lists must be equal => only the size is compared"
mutar 4 normal/NormalValidacion.java 's/return !NormalConstantes.ORIGEN_PAC.equals(entrada.p_IdOrigenProceso) \&\& /return !NormalConstantes.ORIGEN_PAC.equals(entrada.p_IdOrigenProceso) || /' "origin rule: not PAC && not REC && not BONIF becomes not PAC || ..."
mutar 5 normal/NormalFechas.java 's/serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;/serie.EstadoRescateBackoffice : serie.EstadoRescateComercial;/' "profile: commercial and back-office states of a rescue swapped (first occurrence)"
mutar 6 normal/NormalFlujos.java 's/compuertas.g3SaldoFlujoCaja();//' "gate G3 not evaluated before SaldoFlujoCaja"
mutar 7 normal/NormalEscritura.java 's/if (c.entradaTesoreria.p_Solicitudes.size() > 0) compuertas.g1MedioDistintoDeCin();//' "gate G1 not evaluated before the Tesoreria write"
echo done
