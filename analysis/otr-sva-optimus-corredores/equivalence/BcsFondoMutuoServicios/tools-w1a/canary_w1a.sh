#!/usr/bin/env bash
# F2 W1 front 1 canary (PLAN_FONDOMUTUO_F2 7: union to the transaction, G1, plus the helper's defects and C1b/C3/DEV-F5).
# Each mutation is applied to a SCRATCH COPY of the module (never to modernized/ or analysis/), the selected tests run, the result is logged, the copy is
# restored from the pristine copy and a green control run follows at the end.
#   CANARY_SCRATCH=<dir outside modernized/ and analysis/> ./canary_w1a.sh
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
SRC="$WS/modernized/otr-sva-optimus-corredores"
LOGS="$WS/analysis/otr-sva-optimus-corredores/equivalence/canary/BcsFondoMutuoServicios-W1A"
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
TESTS='TesoreriaRollbackConjuntoTest,TesoreriaEnProcesoContextoTest,ProcesarIngresoEgresoTesoreriaTest,RecursosDuplicadosTest,NormalEquivalenciaTest,NormalCompuertasYDefectosTest,FondoMutuoCableadoRealTest'

restaurar() { rm -rf "$M/src"; cp -r "$P/BcsFondoMutuoServicios/src" "$M/"; }
correr() {  # $1 id, $2 description
  local log="$LOGS/$1.log"
  echo "== mutation $1: $2" > "$log"
  diff -r "$P/BcsFondoMutuoServicios/src" "$M/src" >> "$log" 2>&1
  (cd "$M" && mvn -o -q -f pom.xml test -Dfile.encoding=UTF-8 -Dtest="$TESTS" -Dsurefire.failIfNoSpecifiedTests=false) >> "$log" 2>&1
  echo "exit $?" >> "$log"
  echo "$1: $(grep -a -c -E '<<< (FAIL|ERROR)' "$log") failing tests, compile errors: $(grep -a -c -E 'COMPILATION ERROR|Fatal error compiling' "$log") -> $log"
  restaurar
}

# 1 union to the transaction: the unit of work no longer opens an effective transaction (NOT_SUPPORTED): Tesoreria's @Transactional opens and commits its own
sed -i 's/this.transaccion = new TransactionTemplate(transactionManager);/this.transaccion = new TransactionTemplate(transactionManager); this.transaccion.setPropagationBehavior(4);/' "$J/adapter/in/javacall/JavaCallUnitOfWorkFilter.java"
correr 1 "unit of work without an effective transaction (PROPAGATION_NOT_SUPPORTED)"
# 2 C1b: Spring's default globalRollbackOnParticipationFailure
sed -i 's/manager.setGlobalRollbackOnParticipationFailure(false);/manager.setGlobalRollbackOnParticipationFailure(true);/' "$J/config/TransaccionConfig.java"
correr 2 "C1b removed (globalRollbackOnParticipationFailure = true)"
# 3 G1: Normal does not call Tesoreria for a single non-CIN contribution
sed -i '0,/if (c.entradaTesoreria.p_Solicitudes.size() > 0) {/s//if (c.entradaTesoreria.p_Solicitudes.size() > 1) {/' "$J/normal/NormalEscritura.java"
correr 3 "G1 condition: Tesoreria called only with more than one request"
# 4 G1 closed again by configuration
sed -i 's/^    habilitada: true$/    habilitada: false/' "$M/src/main/resources/application.yml"
correr 4 "G1 property back to false in application.yml"
# 5 DEV-F5: the swallowed HTTP failure of a Tesoreria collaborator is no longer rethrown
sed -i 's/            if (fallo != null) {/            if (fallo != null \&\& false) {/' "$J/tesoreria/ColaboradoresTesoreria.java"
correr 5 "DEV-F5 rethrow removed"
# 6 D-TES-2 fixed: APOREINV with concept APOREI
sed -i 's/idConcepto = APORTE.equalsIgnoreCase(sol.TipoSolicitud) ? CONCEPTO_APORTE$/idConcepto = APORTE.equalsIgnoreCase(sol.TipoSolicitud) ? ("APOREINV".equals(sol.IdTipoMovimientoFondoMutuo) ? "APOREI" : CONCEPTO_APORTE)/' "$J/tesoreria/TesoreriaArmado.java"
correr 6 "D-TES-2 fixed (APOREINV -> APOREI)"
# 7 D-TES-1 fixed: TitularPago compares with the payer of the portfolio preference
sed -i 's/ie.TitularPago = sol.IdPersona.equals(ie.IdPersonaTitularPago) ? NO : SI;/ie.TitularPago = sol.IdPersona.equals(ie.IdPersonaTitularPago) ? SI : NO;/' "$J/tesoreria/TesoreriaArmado.java"
correr 7 "D-TES-1 TitularPago inverted"
# 8 C3: the shadow application.properties is removed (Tesoreria's would be loaded)
rm "$M/src/main/resources/application.properties"
rm -f "$M/target/classes/application.properties"   # the copy is built without clean: a stale copy of the resource would survive (first run: false survivor)
correr 8 "C3 shadow application.properties removed"
# 9 FM-TES-12: p_SimularOperacion is passed as given (null no longer becomes N)
sed -i 's/entrada.p_SimularOperacion == null ? TesoreriaArmado.NO : entrada.p_SimularOperacion;/entrada.p_SimularOperacion;/' "$J/tesoreria/ProcesarIngresoEgresoTesoreriaService.java"
correr 9 "FM-TES-12 null simulation flag passed as null"
# control
correr 0 "control (no mutation)"
