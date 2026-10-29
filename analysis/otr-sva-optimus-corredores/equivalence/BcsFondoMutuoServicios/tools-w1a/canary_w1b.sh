#!/usr/bin/env bash
# F2 W1B canary (layer 2): mutations on a SCRATCH COPY of the module (never modernized/ or analysis/), NormalEquivalenciaTest (134 cases: the 32
# layer-2 goldens FMM-NOR-T01..T32 recorded from the legacy with Tesoreria's CrearIngresoEgreso REAL) must go red; control run at the end.
#   CANARY_SCRATCH=<dir outside modernized/ and analysis/> ./canary_w1b.sh
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WS="$(cd "$HERE/../../../../.." && pwd)"
SRC="$WS/modernized/otr-sva-optimus-corredores"
LOGS="$WS/analysis/otr-sva-optimus-corredores/equivalence/canary/BcsFondoMutuoServicios-W1A/layer2"
: "${CANARY_SCRATCH:?set CANARY_SCRATCH to a directory outside modernized/ and analysis/}"
case "$CANARY_SCRATCH" in *modernized*|*analysis*) echo "scratch must be outside modernized/ and analysis/" >&2; exit 2;; esac
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"   # the default JAVA_HOME of this host is JDK 11
export JAVA_TOOL_OPTIONS="-Duser.timezone=UTC -Dfile.encoding=UTF-8"
mkdir -p "$LOGS"
R="$CANARY_SCRATCH/root"; P="$CANARY_SCRATCH/pristine"
rm -rf "$R" "$P"; mkdir -p "$R" "$P"
cp "$SRC/pom.xml" "$R/pom.xml"
for d in "$R" "$P"; do
  mkdir -p "$d/BcsFondoMutuoServicios"
  cp -r "$SRC/BcsFondoMutuoServicios/pom.xml" "$SRC/BcsFondoMutuoServicios/src" "$d/BcsFondoMutuoServicios/"
done
M="$R/BcsFondoMutuoServicios"
J="$M/src/main/java/cl/bcs/fondomutuo"
TJ="$M/src/test/java/cl/bcs/fondomutuo"

restaurar() { rm -rf "$M/src"; cp -r "$P/BcsFondoMutuoServicios/src" "$M/"; }
correr() {  # $1 id, $2 description
  local log="$LOGS/$1.log"
  echo "== mutation $1: $2" > "$log"
  diff -r "$P/BcsFondoMutuoServicios/src" "$M/src" >> "$log" 2>&1
  (cd "$M" && mvn -o -q -f pom.xml test -Dfile.encoding=UTF-8 -Dtest=NormalEquivalenciaTest -Dsurefire.failIfNoSpecifiedTests=false) >> "$log" 2>&1
  echo "exit $?" >> "$log"
  echo "$1: red cases $(grep -a -oE 'FMM-NOR-[A-Z0-9]+_[^ :]*' "$log" | sort -u | wc -l) (layer 2: $(grep -a -oE 'FMM-NOR-T[0-9]+_[^ :]*' "$log" | sort -u | wc -l)), compile errors: $(grep -a -c -E 'COMPILATION ERROR' "$log") -> $log"
  restaurar
}

# L1 the join to the transaction dropped: Tesoreria's work is committed when CrearIngresoEgreso returns (its own transaction), so a later rollback
#    of the request no longer removes its rows (harness-level: the layer-2 new side builds the library without Spring; the production-level variant
#    is mutation 1 of canary_w1a.sh, the unit of work with PROPAGATION_NOT_SUPPORTED)
sed -i 's#new TesoreriaJdbc(conexion), crearIngresoEgreso::logica);#new TesoreriaJdbc(conexion), e -> { var s = crearIngresoEgreso.logica(e); con.commit(); return s; });#' "$TJ/normal/TesoreriaReal.java"
correr L1 "Tesoreria not joined: its work committed at its return"
# L2 a Tesoreria-side field the helper sends: product of the movement INFM -> RV (exists in Tesoreria's seed: accepted, stored differently)
sed -i 's/static final String TIPO_PRODUCTO = "INFM";/static final String TIPO_PRODUCTO = "RV";/' "$J/tesoreria/TesoreriaArmado.java"
correr L2 "IdTipoProducto INFM -> RV"
# L3 Normal asks Tesoreria for an AUTOMATIC movement (p_EsIngresoAutomatico N -> S)
sed -i 's/c.entradaTesoreria.p_EsIngresoAutomatico = NormalConstantes.NO;/c.entradaTesoreria.p_EsIngresoAutomatico = "S";/' "$J/normal/NormalEscritura.java"
correr L3 "p_EsIngresoAutomatico N -> S"
# L4 the payment form's socio account taken from the request instead of the portfolio preference
sed -i 's/fp.IdCuentaBancariaSocio = info != null ? info.IdCuentaBancaria : 0L;/fp.IdCuentaBancariaSocio = sol.IdCuentaBancaria;/' "$J/tesoreria/TesoreriaArmado.java"
correr L4 "IdCuentaBancariaSocio from the request"
correr L0 "control (no mutation)"
