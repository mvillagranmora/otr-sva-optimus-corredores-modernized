"""Step 5 FRESH inputs for ManejadorSolicitudAporteRescate: FRESH-MAN-nn under fresh/cases/. Valid records only (malformed JSON is left out). The helpers
(entrada, rescate, doble, ok, msg) are the ones of make_cases_man.py; none of the inputs below was used by the development cases FMM-MAN-02..15 / P01.
"""
import json
import os

import make_cases_man as M
from make_cases_man import TABLAS, doble, entrada, msg, ok, rescate
from make_fresh_nor import precios

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "fresh", "cases")
SIN_SPOT = lambda e: {k: v for k, v in e.items() if k != "p_FormaPagoSpot"}   # noqa: E731


def escribir(cid, ref, ent, colab=None, seed=None):
    d = os.path.join(OUT, cid)
    os.makedirs(d, exist_ok=True)
    caso = {"servicio": "ManejadorSolicitudAporteRescate", "ref": ref, "entrada": ent}
    if colab:
        caso["colaboradores"] = colab
    caso["tablas"] = TABLAS
    with open(os.path.join(d, "case.json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(caso, f, indent=2, ensure_ascii=False)
        f.write("\n")
    with open(os.path.join(d, "seed.sql"), "w", encoding="utf-8", newline="\n") as f:
        f.write("-- case seed on top of seed/base-fmm.sql (fake data)\n" + ("\n".join(seed) if seed else "SELECT 1;") + "\n")


def generar():
    n = "FRESH-MAN-%02d_"
    escribir(n % 1 + "normal-tres-dos-buenas", "Normal con 3 solicitudes: la 2da excede la custodia en la simulacion", entrada(p_SolicitudesAporteRescateNormal=[rescate(1), rescate(2, 5000), rescate(3, 20)]))
    escribir(n % 2 + "normal-usuario-sin-perfil", "Normal con un usuario sin perfil de fondos mutuos (9): todo rechazado en la simulacion",
             entrada(p_IdUsuario=9, p_SolicitudesAporteRescateNormal=[rescate(1)]),
             seed=['INSERT INTO "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario", "IdEmpresa", "IdEstado", "TipoEntidad", "IdEntidad") VALUES (9, 1, \'ACT\', \'EMP\', 70);'])
    escribir(n % 3 + "id-interno-cero", "Normal con IdInterno 0: ID_INTERNO_NO_CERO en la simulacion", entrada(p_SolicitudesAporteRescateNormal=[rescate(0)]))
    escribir(n % 4 + "normal-programado-overnight", "Normal (real) + Programado + Overnight (dobles): mezcla de listas (el modulo F1 responde 503 por G0)",
             entrada(p_SolicitudesAporteRescateNormal=[rescate(1)], p_SolicitudesAporteRescateProgramado=[{"IdInterno": 11}], p_SolicitudAporteOvernight=[{"IdInterno": 12}]),
             {"CrearSolicitudAporteRescateProgramado": doble(ok([11]), ok([11])), "CrearSolicitudAporteOvernight": doble(ok([12], [msg(1, "ERROR", "X_OVN", "overnight 12")]), ok([12]))})
    escribir(n % 5 + "normal-vacia-con-programado", "Lista Normal vacia y Programado con filas (503 por G0 en el modulo nuevo)",
             entrada(p_SolicitudesAporteRescateNormal=[], p_SolicitudesAporteRescateProgramado=[{"IdInterno": 31}]),
             {"CrearSolicitudAporteRescateProgramado": doble(ok([31]), ok([31]))})
    escribir(n % 6 + "cambio-serie-par-bueno", "CambioSerie con el par rescate/aporte (se ejecuta)",
             entrada(p_SolicitudCambioSerieRescate=[{"IdInterno": 41}], p_SolicitudCambioSerieAporte=[{"IdInterno": 42}]),
             {"CrearSolicitudCambioSerie": doble(ok([41, 42]), ok([41, 42]))})
    escribir(n % 7 + "reinversion-una-rechazada", "Reinversion de aporte con 2 solicitudes, la 2da rechazada en la simulacion",
             entrada(p_SolicitudAporteReinversion=[{"IdInterno": 51}, {"IdInterno": 52}]),
             {"CrearSolicitudAporteReinversion": doble(ok([51], [msg(1, "ERROR", "X_REI", "reinversion 52")]), ok([51]))})
    escribir(n % 8 + "sin-spot-solo-programado", "p_FormaPagoSpot ausente pero la lista Normal vacia: solo Programado (503 por G0)",
             SIN_SPOT(entrada(p_SolicitudesAporteRescateProgramado=[{"IdInterno": 61}])),
             {"CrearSolicitudAporteRescateProgramado": doble(ok([61]), ok([61]))})
    escribir(n % 9 + "sin-spot-normal-npe", "p_FormaPagoSpot ausente con 2 solicitudes Normal (NPE de D-MAN-2)", SIN_SPOT(entrada(p_SolicitudesAporteRescateNormal=[rescate(1), rescate(2)])))
    escribir(n % 10 + "spot-con-filas-normal", "p_FormaPagoSpot con una fila y Normal de rescate (se envia a Normal tal cual)",
             entrada(p_SolicitudesAporteRescateNormal=[rescate(1)], p_FormaPagoSpot=[{"IdInternoPagoOperacion": 1, "IdComprobantePagoOperacion": 1, "IdTipoMovimiento": "PAGO",
                                                                                      "IdFormaDePago": "TRA", "IdMoneda": "CLP", "IdMonedaLiquidacion": "CLP", "IdOperacion": 1,
                                                                                      "RecibePagaPortafolio": "R", "MontoFormaDePago": 90000, "AfectarTesoreria": "S"}]))
    escribir(n % 11 + "transferencia-excepcion-segunda", "Transferencia: el doble falla en la pasada real tras escribir Normal (rollback total)",
             entrada(p_SolicitudesAporteRescateNormal=[rescate(1)], p_SolicitudRescateTransferencia=[{"IdInterno": 71}]),
             {"CrearSolicitudRescateTransferencia": {"respuestas": [{"cuando": {"0.p_SimularOperacion": "S"}, "respuesta": ok([71])}, {"error": "boom transferencia"}]}})
    escribir(n % 12 + "traspaso-y-rescate-reinversion", "Traspaso + RescateReinversion con par completo (503 por G0 en el modulo nuevo)",
             entrada(p_SolicitudRescateTraspaso=[{"IdInterno": 81}], p_SolicitudRescateReinversionRescate=[{"IdInterno": 82}], p_SolicitudRescateReinversionAporte=[{"IdInterno": 83}]),
             {"CrearSolicitudRescateTraspaso": doble(ok([81]), ok([81])), "CrearSolicitudRescateReinversion": doble(ok([82, 83]), ok([82, 83]))})
    escribir(n % 13 + "veinticinco-rescates", "Normal con 25 rescates de 1..25 cuotas (25 precios guionados, ids de bloqueo distintos)",
             entrada(p_SolicitudesAporteRescateNormal=[rescate(i, i) for i in range(1, 26)]), precios(25))
    escribir(n % 14 + "canal-owp-parametro-n", "canal OWP con el parametro de notificacion en N (Normal)", entrada(p_IdTipoCanalIngreso="OWP", p_SolicitudesAporteRescateNormal=[rescate(1)]))
    # added after the first canary run: the counter of the real pass was not observed by the first fresh set (mutation 2)
    escribir(n % 15 + "dos-listas-contador-de-la-pasada-real", "Programado y Overnight: las pasadas reales devuelven contadores 2 y 3 con sus mensajes (el contador total es la suma; los mensajes de la pasada real se pierden)",
             entrada(p_SolicitudesAporteRescateProgramado=[{"IdInterno": 91}], p_SolicitudAporteOvernight=[{"IdInterno": 92}]),
             {"CrearSolicitudAporteRescateProgramado": doble(ok([91]), ok([91], [msg(1, "ERROR", "X_P1", "p1"), msg(2, "ERROR", "X_P2", "p2")], 2)),
              "CrearSolicitudAporteOvernight": doble(ok([92]), ok([92], [msg(3, "ERROR", "X_O1", "o1"), msg(4, "ERROR", "X_O2", "o2"), msg(5, "ERROR", "X_O3", "o3")], 3))})
    escribir(n % 16 + "transferencia-y-traspaso-solo-aportes-buenos", "Transferencia con 2 filas (la 2da rechazada) y Traspaso con 1 fila buena: filtro por IdInterno y orden de las listas de rescate",
             entrada(p_SolicitudRescateTransferencia=[{"IdInterno": 101}, {"IdInterno": 102}], p_SolicitudRescateTraspaso=[{"IdInterno": 103}]),
             {"CrearSolicitudRescateTransferencia": doble(ok([101], [msg(1, "ERROR", "X_T2", "t2")]), ok([101])), "CrearSolicitudRescateTraspaso": doble(ok([103]), ok([103]))})


if __name__ == "__main__":
    generar()
    print("fresh Manejador cases written")
