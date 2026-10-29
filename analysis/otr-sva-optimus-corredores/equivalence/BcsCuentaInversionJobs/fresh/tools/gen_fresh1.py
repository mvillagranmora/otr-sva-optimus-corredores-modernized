"""FRESH inputs (verification step 4.2), part 1: AS AR CI PS PG IP RC GP. Inputs only; goldens are recorded by the legacy oracle."""
from gen_common import *
from gen_informes import informe_salida
from gen_pautas import salida_servicio

E, U = "3", "47"
T = [PROGRAMA, MARCA, TIPO_EJEC]


def gen():
    S = "ActualizarSaldoCargoAbono"
    P = {"IdEmpresa": E, "IdUsuario": U, "FechaSistema": "2024-02-29", "CuentasInversion": "1;2"}
    escribir("AS-F01", "ActualizarSaldoCargoAbonoJob", "fresh: leap day, 2 accounts, service reports 2 errors", parametros=P,
             colaboradores=svc(S, err(2), sql=marca("f01")), tablas=T)
    escribir("AS-F02", "ActualizarSaldoCargoAbonoJob", "fresh: 1000 accounts, empresa 3, no errors", parametros=con(P, CuentasInversion=";".join(str(5000 + 3 * i) for i in range(1000)), FechaSistema="2026-01-01"),
             colaboradores=svc(S, ok(), sql=marca("f02")), tablas=T)
    escribir("AS-F03", "ActualizarSaldoCargoAbonoJob", "fresh: empresa 0, user 0, account 0, state row busy, 1 error", parametros=con(P, IdEmpresa="0", IdUsuario="0", CuentasInversion="0"),
             colaboradores=svc(S, err(1, [msg("ADVERTENCIA", "CTA-W9", "saldo en cero", 1)]), sql=marca("f03")), tablas=T,
             seed="UPDATE \"BSCL.Plataforma.BD::BAT.TipoProgramaEjecucion\" SET \"IdEstado\" = 'EJEC';")
    A = "ActualizarSaldosReporteAntiguedad"
    p = {"IdEmpresa": "12", "IdUsuario": U, "Rut": "76543210-K", "CodigoPortafolio": "PF99", "IdMoneda": "USD", "FechaMovimiento": "2024-12-31"}
    escribir("AR-F01", "ActualizarSaldosReporteAntiguedadJob", "fresh: other empresa, USD, year-end date", parametros=p, colaboradores=svc(A, ok(), sql=marca("f01")))
    escribir("AR-F02", "ActualizarSaldosReporteAntiguedadJob", "fresh: UF currency, 2 errors", parametros=con(p, IdMoneda="UF", FechaMovimiento="2026-01-01"),
             colaboradores=svc(A, err(2), sql=marca("f02")))
    C = "CargaInicialSaldosReporteAntiguedad"
    q = {"IdEmpresa": "5", "IdUsuario": U, "FechaSistema": "2024-02-29"}
    escribir("CI-F01", "CargaInicialSaldosReporteAntiguedadJob", "fresh: leap day", parametros=q, colaboradores=svc(C, ok(), sql=marca("f01")))
    escribir("CI-F02", "CargaInicialSaldosReporteAntiguedadJob", "fresh: first day of year, 1 error", parametros=con(q, FechaSistema="2026-01-01", IdEmpresa="9"),
             colaboradores=svc(C, err(1), sql=marca("f02")))
    PP = {"IdEmpresa": 9999999999, "IdUsuario": 0}
    MS = [msg("INFORMACION", "CTA-I10", "Cuenta sin movimientos ñ", 1), msg("ADVERTENCIA", "CTA-A11", "Saldo negativo", 2), msg("ERROR", "CTA-E12", "Sin tipo de cambio", 3)]
    escribir("PS-F01", "PautaActualizarSaldoCargoAbono", "fresh: Long.MAX-like company, 3 messages", parametros=PP,
             colaboradores=svc("ActualizarSaldoCargoAbono", salida_servicio(1, MS)), config={"PLATAFORMA/PLA.FechaSistema": "2024-02-29"}, tablas=[PROGRAMA, MARCA])
    escribir("PS-F02", "PautaActualizarSaldoCargoAbono", "fresh: single message, other date", parametros={"IdEmpresa": 4, "IdUsuario": 1},
             colaboradores=svc("ActualizarSaldoCargoAbono", salida_servicio(0, MS[:1])), config={"PLATAFORMA/PLA.FechaSistema": "2026-01-01"}, tablas=[PROGRAMA, MARCA])
    escribir("PG-F01", "PautaGuardarSaldosReporteAntiguedad", "fresh: 3 messages, 3 errors", parametros={"IdEmpresa": 2, "IdUsuario": 15},
             colaboradores=svc("GuardarSaldosReporteAntiguedad", salida_servicio(3, MS)), config={"PLATAFORMA/PLA.FechaSistema": "2024-12-31"}, tablas=[PROGRAMA, MARCA])
    escribir("PG-F02", "PautaGuardarSaldosReporteAntiguedad", "fresh: service writes then no messages", parametros=PP,
             colaboradores=svc("GuardarSaldosReporteAntiguedad", salida_servicio(), sql=marca("pg")), tablas=[PROGRAMA, MARCA])
    for pre, job, S in (("IP", "BcsJobGenerarInformePatrimonioDiario", "GenerarInformePatrimonioDiario"), ("RC", "BcsJobGenerarInformeResumenCobro", "GenerarInformeResumenCobro")):
        Q = {"IdEmpresa": E, "IdUsuario": U, "IdCliente": "0", "IdPortafolio": "0", "IdEstado": "INA", "IdProductoAgrupador": "1",
             "FechaDesde": "2024-02-29", "FechaHasta": "2024-03-01", "Periodo": "202402"}
        escribir(pre + "-F01", job, "fresh: leap day to next day, client 0, portfolio 0, inactive", parametros=Q,
                 colaboradores=svc(S, informe_salida(p_out_NombreArchivo="Informe ñandú 2024.xlsx", p_out_Comentario="Comentario largo " * 20), sql=marca("f01")))
        escribir(pre + "-F02", job, "fresh: same day window, huge ids, other base path, secondary file", parametros=con(Q, IdCliente="9223372036854775807", FechaDesde="2025-12-31", FechaHasta="2025-12-31", Periodo="0"),
                 config={"PLATAFORMA/PLA.RutaBaseArchivos": "${RUTA_BASE}/otra/"}, colaboradores=svc(S, informe_salida(p_out_RutaArchivo="x/", p_out_ArchivoSecundario=["z.csv"]), sql=marca("f02")))
    G = "GuardarPatrimonioCaja"
    escribir("GP-F01", "GuardarPatrimonioCajaJob", "fresh: leap day, empresa 9", parametros={"IdEmpresa": "9", "IdUsuario": U, "Fecha": "2024-02-29"}, colaboradores=svc(G, ok(), sql=marca("f01")))
    escribir("GP-F02", "GuardarPatrimonioCajaJob", "fresh: date from config, 2 errors", parametros={"IdEmpresa": "9", "IdUsuario": U},
             config={"PLATAFORMA/PLA.FechaSistema": "2026-01-01"}, colaboradores=svc(G, err(2), sql=marca("f02")))


if __name__ == "__main__":
    gen()
