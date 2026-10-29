#!/usr/bin/env bash
# F2 W1 front 2 canary (PLAN_FONDOMUTUO_F2 7, W1 list + the defects of RescateReinversion). Each mutation is applied to a SCRATCH COPY of the module (never to
# modernized/ or analysis/), the RescateReinversion / Manejador tests run, the result is logged, the copy is restored from the pristine copy; a green control
# run follows at the end. Logs: analysis/otr-sva-optimus-corredores/equivalence/canary/BcsFondoMutuoServicios-W1B/
#   CANARY_SCRATCH=<dir outside modernized/ and analysis/> ./canary_rre.sh
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
SRC="$WS/modernized/otr-sva-optimus-corredores"
LOGS="$WS/analysis/otr-sva-optimus-corredores/equivalence/canary/BcsFondoMutuoServicios-W1B"
: "${CANARY_SCRATCH:?set CANARY_SCRATCH to a directory outside modernized/ and analysis/}"
case "$CANARY_SCRATCH" in *modernized*|*analysis*) echo "scratch must be outside modernized/ and analysis/" >&2; exit 2;; esac
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot"
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
X="$J/rescatereinversion"
TESTS='RreEquivalenciaTest,RreCompuertasTest,RreFrescasTest,ManejadorRescateReinversionTest,ManejadorServiceCompuertaTest'
restaurar() { rm -rf "$M/src" "$M/target/classes" "$M/target/test-classes"; cp -r "$P/BcsFondoMutuoServicios/src" "$M/"; }
correr() {  # $1 id, $2 description
  local log="$LOGS/$1.log"
  echo "== mutation $1: $2" > "$log"
  diff -r "$P/BcsFondoMutuoServicios/src" "$M/src" >> "$log" 2>&1
  if ! diff -rq "$P/BcsFondoMutuoServicios/src" "$M/src" > /dev/null; then :; else echo "MUTATION NOT APPLIED" >> "$log"; fi
  (cd "$M" && mvn -o -q -f pom.xml test -Dfile.encoding=UTF-8 -Dtest="$TESTS" -Dsurefire.failIfNoSpecifiedTests=false) >> "$log" 2>&1
  echo "exit $?" >> "$log"
  echo "$1: $(grep -a -c -E '<<< (FAIL|ERROR)' "$log") failing tests, compile errors: $(grep -a -c -E 'COMPILATION ERROR|Fatal error compiling' "$log"), not applied: $(grep -c 'MUTATION NOT APPLIED' "$log") -> $log"
  restaurar
}
{
sed -i 's/            if (c.bloqueoCreado) eliminarBloqueo(c);   \/\/ plan 6.2/            \/\/ plan 6.2 (mutated: no release)/' "$X/CrearSolicitudRescateReinversionService.java"
correr 1 "lock NOT released before the 503 (G-CUS / G5)"
sed -i 's/if (aportesBuenos.size() > 0 \&\& rescatesBuenos.size() > 0) {/if (aportesBuenos.size() > 0 || rescatesBuenos.size() > 0) {/' "$J/manejador/ManejadorSolicitudAporteRescateService.java"
correr 2 "Manejador: && of the reinversion contribution/rescue pair -> ||"
sed -i 's/? java.sql.Date.valueOf(fecha.FechaCertificacion) : null;/? java.sql.Date.valueOf(fecha.FechaLiquidacion) : null;/' "$X/RreEscritura.java"
correr 3 "FechaFinalReinversion of the destination row = liquidation instead of certification (the date AporteReinversion's UPDATE later rewrites)"
sed -i 's/        } catch (ServicioNoServidoException e) {/        } catch (RuntimeException e) {/' "$X/CrearSolicitudRescateReinversionService.java"
correr 4 "D-F2-1 'fixed': any runtime exception after the lock releases it"
sed -i 's/if (idsBd.size() > 0 \&\& ids.equals(idsBd)) {/if (idsBd.size() > 0 \&\& ids.size() > idsBd.size()) {/' "$X/CrearSolicitudRescateReinversionService.java"
correr 5 "duplicate lock detection by size instead of list equality"
sed -i 's|            c.out.P_OUT_RESULTADO.add(c.hSolicitudSalida.get(primera.IdInterno));   // D-F2-9|            // D-F2-9 fixed (mutation)|' "$X/RreBatch.java"
correr 6 "D-F2-9 fixed: the first result is not added twice"
sed -i 's/key.IdAgrupador = RreReglas.esCanalIngresoWebServices(c.entrada.p_IdTipoCanalIngreso) ? 0L : sol.IdAgrupador;/key.IdAgrupador = RreReglas.esCanalIngresoWebServices(c.entrada.p_IdTipoCanalIngreso) ? 0L : solicitud.IdAgrupador;/' "$X/RreRequisitos.java"
correr 7 "D-F2-15 fixed: grouped amount read with the INPUT IdAgrupador"
sed -i 's/                    break;   \/\/ D-F2-8: the following requests are not checked/                    continue;/' "$X/RreValidacion.java"
correr 8 "D-F2-8 fixed: custody failure continues instead of break"
sed -i 's/        if (!compuertas.listasServidas().contains(RescateReinversionConfiguracion.LISTA)) {/        if (false) {/' "$X/RescateReinversionJavaCallHandler.java"
correr 9 "direct FQN served without its list (gate removed)"
correr 0 "CONTROL (no mutation)"
} 2>&1 | tee "$LOGS/SUMMARY.txt"
