"""FRESH inputs (verification step 4.2), part 2: GC CP A1 DS SC RA AB AC PR PB LI. Inputs only."""
from gen_common import *
from gen_archivos import salida_ruta
from gen_reportes import rep
from gen_pautas import salida_inscribir

E, U = "3", "47"


def gen():
    S = "GenerarArchivoCargoAbono"
    P = {"IdEmpresa": E, "IdUsuario": U, "IdModulo": "CTA", "IdEjecutivo": "9223372036854775807", "IdMoneda": "USD", "IdTipoMovimiento": "ABO",
         "IdConcepto": "ABONO", "IdEstado": "VIG", "FechaDesde": "2024-02-29", "FechaHasta": "2024-02-29"}
    r = salida_ruta("cta/CargoAbono_2024.csv", P_OUT_CANTIDADREGISTROS=2147483647)
    escribir("GC-F01", "GenerarArchivoCargoAbonoJob", "fresh: int-max record count, same-day window", parametros=P, colaboradores=svc(S, r, sql=marca("f01")))
    escribir("GC-F02", "GenerarArchivoCargoAbonoJob", "fresh: executive negative, 2 errors", parametros=con(P, IdEjecutivo="-1"), colaboradores=svc(S, salida_ruta("a.csv", errores=2, P_OUT_CANTIDADREGISTROS=3), sql=marca("f02")))
    S = "GenerarArchivoClientesPorPagarYCobrar"
    Q = {"IdEmpresa": E, "IdUsuario": U, "IdModulo": "CTA", "FechaSistema": "2024-02-29", "EsBusqueda": "S", "IdMoneda": "USD", "TipoSaldo": "POS"}
    escribir("CP-F01", "GenerarArchivoClientesPorPagarYCobrarJob", "fresh: USD, POS, accented path", parametros=Q, colaboradores=svc(S, salida_ruta("cta/Clientes Ñ.csv"), sql=marca("f01")))
    escribir("CP-F02", "GenerarArchivoClientesPorPagarYCobrarJob", "fresh: 3 messages with unicode text", parametros=Q,
             colaboradores=svc(S, salida_ruta(errores=3, mensajes=[msg("ERROR", "E1", "uno ñ", 1), msg("AVISO", "E2", "dos €", 2), msg("ERROR", "E3", "", 3)])))
    S = "GenerarArchivoCuentasPorCobrarAnexo16"
    A = {"IdEmpresa": E, "IdUsuario": U, "FechaHasta": "2024-02-29"}
    escribir("A1-F01", "GenerarArchivoCuentasPorCobrarAnexo16Job", "fresh: leap day", parametros=A, colaboradores=svc(S, salida_ruta("cta/Anexo16_2024.csv"), sql=marca("f01")))
    escribir("A1-F02", "GenerarArchivoCuentasPorCobrarAnexo16Job", "fresh: 2 errors, year start", parametros=con(A, FechaHasta="2026-01-01"), colaboradores=svc(S, salida_ruta(errores=2), sql=marca("f02")))
    S = "ConsultaComparacionSaldo"
    D = {"IdEmpresa": E, "IdUsuario": U, "IdModulo": "APV", "FechaDesde": "2024-02-29", "FechaHasta": "2024-02-29"}
    escribir("DS-F01", "GenerarArchivoDiferenciaSaldoJob", "fresh: APV, same-day window", parametros=D, colaboradores=svc(S, salida_ruta("cta/Dif_2024.xlsx"), sql=marca("f01")))
    escribir("DS-F02", "GenerarArchivoDiferenciaSaldoJob", "fresh: 7 errors", parametros=con(D, IdModulo="CTA"), colaboradores=svc(S, salida_ruta(errores=7), sql=marca("f02")))
    S = "GenerarArchivoSaldoCuentas"
    C = {"IdEmpresa": E, "IdUsuario": U, "IdModulo": "CTA", "IdEjecutivo": "9223372036854775807", "IdMoneda": "USD", "SoloClientesSobregirados": "S",
         "SoloClientesConSaldoDisponible": "N", "SoloClientesConSaldoDisponibleClientesSobregirados": "S", "IncluyeProductoApv": "N", "IncluyeOtrosSaldos": "S",
         "FechaDesde": "2024-02-29", "FechaHasta": "2024-03-01", "Encabezados": "[\"Solo uno\"]"}
    escribir("SC-F01", "GenerarArchivoSaldoCuentasJob", "fresh: huge executive, replaces .csv in directory part too", parametros=C, colaboradores=svc(S, salida_ruta("cta/mi.csv/saldo.csv"), sql=marca("f01")))
    escribir("SC-F02", "GenerarArchivoSaldoCuentasJob", "fresh: negative executive, headers with empty and unicode strings", parametros=con(C, IdEjecutivo="-3", Encabezados="[\"\",\"Ñ\",\"a b\"]"),
             colaboradores=svc(S, salida_ruta("cta/x.csv", errores=1), sql=marca("f02")))
    for pre, job, S, ex in (("RA", "GenerarReporteAcreedoresJob", "GenerarReporteAcreedores", False), ("AB", "GenerarReporteAntiguedadDeudaBolsaCTAJob", "GenerarReporteAntiguedadDeudaBolsaCTA", True),
                            ("AC", "GenerarReporteAntiguedadDeudaCTAJob", "GenerarReporteAntiguedadDeudaCTA", True)):
        R = {"IdEmpresa": E, "IdUsuario": U, "P_FechaSolicitud": "2024-02-29", "P_Moneda": "USD", "P_Consolidar": "N", "NombreInternoArchivo": "Rep_Z9"}
        if ex:
            R.update({"P_SeparaIntermediacion": "N", "P_AgrupaPortafolios": "S"})
        escribir(pre + "-F01", job, "fresh: USD, leap day, other path and broker", parametros=R, colaboradores=svc(S, rep("cta/Rep_2024.xlsx", "COR77"), sql=marca("f01")))
        escribir(pre + "-F02", job, "fresh: 2 errors, a long message", parametros=con(R, P_FechaSolicitud="2026-01-01"),
                 colaboradores=svc(S, rep(errores=2, mensajes=[msg("ERROR", "X9", "m" * 400, 1)]), sql=marca("f02")))
    for pre, job in (("PR", "PautaGenerarReporteAcreedores"), ("PB", "PautaGenerarReporteAntiguedadDeudaBolsaCTA")):
        escribir(pre + "-F01", job, "fresh: big company, 2 errors and messages", parametros={"IdEmpresa": 9999999999, "IdUsuario": 5},
                 colaboradores=svc("InscribirProcesoBatch", salida_inscribir(2, [msg("ERROR", "B1", "uno", 1), msg("ERROR", "B2", "dos ñ", 2)])), tablas=[PROGRAMA, MARCA])
    I = {"FechaMovimiento": "2024-02-29", "IdEjecucion": "1"}
    escribir("LI-F01", "LibroInventarioJob", "fresh: leap day, execution 1, many accounts", parametros=I, colaboradores={"procedimiento": {"cuentasAfectadas": 123456}})
    escribir("LI-F02", "LibroInventarioJob", "fresh: execution 0, year start", parametros={"FechaMovimiento": "2026-01-01", "IdEjecucion": "0"})


if __name__ == "__main__":
    gen()
