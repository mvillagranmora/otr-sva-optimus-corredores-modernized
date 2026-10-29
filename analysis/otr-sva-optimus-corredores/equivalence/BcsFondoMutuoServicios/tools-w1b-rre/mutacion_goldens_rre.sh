#!/usr/bin/env bash
# F2 W1 front 2, plan 7 "mutacion de goldens" (LEGACY_OVERLAY + ORACLE_OUT): a MUTATED COPY of the legacy CrearSolicitudRescateReinversion.java (in a
# scratch dir; legacy/ is only read) is compiled into the oracle and the FMM-RRE cases are re-run into ORACLE_OUT; each mutation must change at least one
# golden (otherwise the goldens do not pin that line). The goldens are never touched. Result: tools-w1b-rre/mutacion-goldens/RESULTADO.txt
#   MUT_SCRATCH=<dir outside modernized/ and analysis/> ./mutacion_goldens_rre.sh
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BASE="$(cd "$HERE/.." && pwd)"
WS="$(cd "$BASE/../../../.." && pwd)"
RR="$WS/legacy/otr-sva-optimus-corredores/BSCL/FondoMutuo/JAVA/BcsFondoMutuoServicios/src/cl/bcs/fondomutuo/CrearSolicitudRescateReinversion.java"
: "${MUT_SCRATCH:?set MUT_SCRATCH}"
case "$MUT_SCRATCH" in *modernized*|*analysis*) echo "scratch must be outside modernized/ and analysis/" >&2; exit 2;; esac
OUT="$HERE/mutacion-goldens"; rm -rf "$OUT"; mkdir -p "$OUT"
mutar() {  # $1 id, $2 description, $3 sed expression
  local ov="$MUT_SCRATCH/overlay-$1"; rm -rf "$ov"; mkdir -p "$ov"
  sed "$3" "$RR" > "$ov/CrearSolicitudRescateReinversion.java"
  if cmp -s "$RR" "$ov/CrearSolicitudRescateReinversion.java"; then echo "$1: NOT APPLIED ($2)"; return; fi
  LEGACY_OVERLAY="$ov" ORACLE_WORK="$MUT_SCRATCH/work-$1" ORACLE_OUT="$OUT/m$1" bash "$BASE/legacy-oracle/record_rre.sh" FMM-RRE > "$OUT/m$1.log" 2>&1
  local n=0 d=""
  for c in "$BASE"/cases/FMM-RRE-*; do
    id="$(basename "$c")"
    if ! cmp -s "$c/expected.json" "$OUT/m$1/$id.json"; then n=$((n+1)); d="$d $id"; fi
  done
  echo "$1: $2 -> $n goldens changed:$d"
}
{
mutar 1 "1N message: only EJE omits it (instead of WEB)" 's/!_c.CanalWEB.equals(entrada.p_IdTipoCanalIngreso) \&\& !(out/!_c.CanalEjecutivo.equals(entrada.p_IdTipoCanalIngreso) \&\& !(out/'
mutar 2 "custody: continue instead of break outside CAR" '1212s/break;/continue;/'
mutar 3 "FechaFinalReinversion = liquidation" 's/reinversion.FechaFinalReinversion = fecha.FechaCertificacion;/reinversion.FechaFinalReinversion = fecha.FechaLiquidacion;/'
mutar 4 "final release of the lock removed" '1396s/eliminarReinversionBloqueada/\/\/eliminarReinversionBloqueada/'
mutar 5 "lock id without the system date" "519s/+ fechaSistema.toString();/+ \"\";/"
mutar 6 "IdEmisorDestino always the contribution's" 's/sol.IdEmisorDestino = haportes.size() == 1 ? apor.IdEmisor : "";/sol.IdEmisorDestino = apor.IdEmisor;/'
} | tee "$OUT/RESULTADO.txt"
