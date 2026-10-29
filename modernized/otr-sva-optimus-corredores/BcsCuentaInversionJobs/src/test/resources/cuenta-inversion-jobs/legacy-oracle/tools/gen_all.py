"""Regenerates every case.json / seed.sql (the INPUT of the characterization; the goldens are recorded by ../record.sh).

    py -3 gen_all.py [AS|AR|CI|IP|RC|GP|GC|CP|A1|DS|SC|RA|AB|AC|PAUTAS|GA|GF|LI]     (default: all)
"""
import sys

import gen_saldos, gen_informes, gen_archivos, gen_saldoscuentas, gen_reportes, gen_pautas, gen_ga, gen_gf, gen_libro

GRUPOS = {
    "AS": gen_saldos.gen_as, "AR": gen_saldos.gen_ar, "CI": gen_saldos.gen_ci,
    "IP": lambda: gen_informes.gen_informe("IP", "BcsJobGenerarInformePatrimonioDiario", "GenerarInformePatrimonioDiario", False),
    "RC": lambda: gen_informes.gen_informe("RC", "BcsJobGenerarInformeResumenCobro", "GenerarInformeResumenCobro", True),
    "GP": gen_informes.gen_gp,
    "GC": gen_archivos.gen_gc, "CP": gen_archivos.gen_cp, "A1": gen_archivos.gen_a1, "DS": gen_archivos.gen_ds,
    "SC": gen_saldoscuentas.gen_sc,
    "RA": lambda: gen_reportes.gen_reporte("RA", "GenerarReporteAcreedoresJob", "GenerarReporteAcreedores", False),
    "AB": lambda: gen_reportes.gen_reporte("AB", "GenerarReporteAntiguedadDeudaBolsaCTAJob", "GenerarReporteAntiguedadDeudaBolsaCTA", True),
    "AC": lambda: gen_reportes.gen_reporte("AC", "GenerarReporteAntiguedadDeudaCTAJob", "GenerarReporteAntiguedadDeudaCTA", True),
    "PAUTAS": gen_pautas.gen_all,
    "GA": gen_ga.gen_ga, "GF": gen_gf.gen_gf, "LI": gen_libro.gen_li,
}

if __name__ == "__main__":
    sel = sys.argv[1:] or list(GRUPOS)
    for g in sel:
        GRUPOS[g]()
        print("generated", g)
