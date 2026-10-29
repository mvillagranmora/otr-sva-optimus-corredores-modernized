#!/usr/bin/env python3
"""Writes the INPUTS (case.json + seed.sql) of the Manejador cases FMM-MAN-02.. (step 4B). The goldens (expected.json) are NEVER written here:
they are recorded by legacy-oracle/record.sh FMM-MAN executing the unmodified legacy. FMM-MAN-P01 (pilot) is not touched.
  py -3 -I make_cases_man.py
"""
import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
TABLAS = [
    {"nombre": "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate", "orden": "\"IdSolicitudAporteRescate\""},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate", "orden": "\"IdBloqueoSolicitudAporteRescate\""},
    {"nombre": "BSCL.FondoMutuo.BD::FMM.InterfazAgf", "orden": "\"IdInterfazAgf\""},
]
LISTAS = ["p_SolicitudesAporteRescateNormal", "p_SolicitudesAporteRescateProgramado", "p_SolicitudAporteOvernight", "p_SolicitudAporteReinversion",
          "p_SolicitudCambioSerieRescate", "p_SolicitudCambioSerieAporte", "p_SolicitudRescateReinversionRescate", "p_SolicitudRescateReinversionAporte",
          "p_SolicitudRescateTransferencia", "p_SolicitudRescateTraspaso"]


def rescate(i, cuotas=10):
    return {"IdInterno": i, "IdCliente": 100, "IdPortafolio": 500, "TipoSolicitud": "RES", "IdSerieFondo": "FM001A", "CalculoPorMontoCuota": "C",
            "EsPorMontoLiquido": "N", "Monto": 0, "NumeroCuotas": cuotas, "IdMoneda": "CLP", "IdMedioDePago": "CIN", "IdPreferenciaPagoPortafolio": 0,
            "Visado": "N", "SolicitudBatch": "N", "OrigenOperacion": "M", "FechaOperacion": "0001-01-01", "IdRegimenTributario": "GEN", "Glosa": "",
            "IndicadorMLT": "N", "IdSerieFondoDestino": "", "IdEmisorDestino": "", "IdAutorizadoOrigen": 0, "Porcentaje": 0, "FolioContrato": 0,
            "IdMonedaPago": "CLP"}


def entrada(**kw):
    e = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "N", "p_UsaReglasNegocio": "S", "p_IdTipoCanalIngreso": "OPT",
         "p_FormaPagoSpot": []}
    e.update(kw)
    return e


def res(*ids):
    return [{"IdInterno": i, "IdPortafolio": 500, "TipoSolicitud": "APO"} for i in ids]


def doble(sim, real=None, **extra):
    """scripted destination: answer for the simulation pass ("S") and, when given, for the real pass ("N")"""
    r = [{"cuando": {"0.p_SimularOperacion": "S"}, "respuesta": sim}]
    if real is not None:
        r.append({"cuando": {"0.p_SimularOperacion": "N"}, "respuesta": real})
    return {"respuestas": r, **extra}


def ok(ids, mensajes=None, contador=0):
    return {"P_OUT_ERRORESCONTADOR": contador, "P_OUT_MENSAJES": mensajes or [], "P_OUT_RESULTADO": res(*ids)}


def msg(i, tipo, codigo, texto):
    return {"id": i, "tipo": tipo, "codigo": codigo, "descripcion": texto}


C = {}
C["FMM-MAN-02_normal-una-buena-y-una-rechazada"] = ("FM-MAN-02/03/05/06 D-MAN-1: IdInterno 1 buena (10 cuotas), 2 rechazada en la simulacion (5000 cuotas: VALOR_CUOTA_INCORRECTO y MONTO_VALIDO): el filtro por IdInterno deja solo la 1 en la pasada N; contador solo de la N; mensaje de la simulacion",
    entrada(p_SolicitudesAporteRescateNormal=[rescate(1), rescate(2, 5000)]), {})
C["FMM-MAN-03_sin-p_FormaPagoSpot-NPE"] = ("FM-MAN-04 D-MAN-2: p_FormaPagoSpot ausente => null => NullPointerException DESPUES de la simulacion (ROLLBACK_HTTP500)",
    {k: v for k, v in entrada(p_SolicitudesAporteRescateNormal=[rescate(1)]).items() if k != "p_FormaPagoSpot"}, {})
C["FMM-MAN-04_siete-destinos-orden-fijo"] = ("FM-MAN-01 orden fijo Programado, Overnight, Reinversion, CambioSerie, RescateReinversion, Transferencia, Traspaso (dobles); sin Normal; solo pasadas buenas",
    entrada(p_SolicitudesAporteRescateProgramado=[{"IdInterno": 11}], p_SolicitudAporteOvernight=[{"IdInterno": 12}], p_SolicitudAporteReinversion=[{"IdInterno": 13}],
            p_SolicitudCambioSerieRescate=[{"IdInterno": 14}], p_SolicitudCambioSerieAporte=[{"IdInterno": 15}],
            p_SolicitudRescateReinversionRescate=[{"IdInterno": 16}], p_SolicitudRescateReinversionAporte=[{"IdInterno": 17}],
            p_SolicitudRescateTransferencia=[{"IdInterno": 18}], p_SolicitudRescateTraspaso=[{"IdInterno": 19}]),
    {"CrearSolicitudAporteRescateProgramado": doble(ok([11]), ok([11])), "CrearSolicitudAporteOvernight": doble(ok([12]), ok([12])),
     "CrearSolicitudAporteReinversion": doble(ok([13]), ok([13])), "CrearSolicitudCambioSerie": doble(ok([14, 15]), ok([14, 15])),
     "CrearSolicitudRescateReinversion": doble(ok([16, 17]), ok([16, 17])), "CrearSolicitudRescateTransferencia": doble(ok([18]), ok([18])),
     "CrearSolicitudRescateTraspaso": doble(ok([19]), ok([19]))})
C["FMM-MAN-05_p_SimularOperacion-de-entrada-se-ignora"] = ("FM-MAN-05/08: p_SimularOperacion=S de entrada se ignora (la pasada N se ejecuta y escribe)",
    entrada(p_SimularOperacion="S", p_SolicitudesAporteRescateNormal=[rescate(1)]), {})
C["FMM-MAN-06_excepcion-del-destino"] = ("FM-MAN-09 D-MAN-4: Normal escribe, luego el doble de Overnight lanza: contador 1 y relanza (ROLLBACK_HTTP500, sin cuerpo)",
    entrada(p_SolicitudesAporteRescateNormal=[rescate(1)], p_SolicitudAporteOvernight=[{"IdInterno": 12}]),
    {"CrearSolicitudAporteOvernight": {"respuestas": [{"error": "boom de Overnight"}]}})
C["FMM-MAN-07_peticion-sin-listas-con-filas"] = ("FM-MAN-12: las 10 listas vacias y sin p_FormaPagoSpot: ningun if entra, contador 0, COMMIT",
    {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_UsaReglasNegocio": "S"}, {})
C["FMM-MAN-08_simulacion-rechaza-todo-contador-0"] = ("FM-MAN-06 D-MAN-1: la simulacion rechaza todo (5000 cuotas) => sin pasada N, contador 0 con mensaje de error y resultado vacio (COMMIT)",
    entrada(p_SolicitudesAporteRescateNormal=[rescate(1, 5000)]), {})
C["FMM-MAN-09_resultado-con-IdInterno-repetido"] = ("FM-MAN-03: la simulacion de Programado devuelve dos resultados con el mismo IdInterno: Exception key field is not unique (ROLLBACK_HTTP500)",
    entrada(p_SolicitudesAporteRescateProgramado=[{"IdInterno": 11}]), {"CrearSolicitudAporteRescateProgramado": doble(ok([11, 11]))})
C["FMM-MAN-10_canal-ausente-pisa-el-por-defecto"] = ("FM-MAN-08 D-MAN-6: p_IdTipoCanalIngreso ausente llega como null a Normal (pisa el OPT por defecto)",
    {k: v for k, v in entrada(p_SolicitudesAporteRescateNormal=[rescate(1)]).items() if k != "p_IdTipoCanalIngreso"}, {})
C["FMM-MAN-11_cambio-serie-solo-rescate-no-ejecuta"] = ("FM-MAN-07 D-MAN-7: CambioSerie con una sola lista (rescate): solo simula, sin mensaje, contador 0; RescateReinversion con par completo ejecuta",
    entrada(p_SolicitudCambioSerieRescate=[{"IdInterno": 21}], p_SolicitudRescateReinversionRescate=[{"IdInterno": 22}],
            p_SolicitudRescateReinversionAporte=[{"IdInterno": 23}]),
    {"CrearSolicitudCambioSerie": doble(ok([21])), "CrearSolicitudRescateReinversion": doble(ok([22, 23]), ok([22, 23]))})
C["FMM-MAN-12_par-con-una-mala-no-ejecuta-ninguno"] = ("FM-MAN-07 D-MAN-7: RescateReinversion, la simulacion solo acepta el aporte: no se ejecuta ninguno; mensajes de la simulacion se conservan",
    entrada(p_SolicitudRescateReinversionRescate=[{"IdInterno": 31}], p_SolicitudRescateReinversionAporte=[{"IdInterno": 32}]),
    {"CrearSolicitudRescateReinversion": doble(ok([32], [msg(1, "ERROR", "X_RECHAZO", "rescate 31 rechazado")]))})
C["FMM-MAN-13_normal-y-transferencia-orden-y-escritura"] = ("FM-MAN-01/02: Normal (escribe) y despues Transferencia (doble); resultados en ese orden; contador solo de las pasadas N",
    entrada(p_SolicitudesAporteRescateNormal=[rescate(1)], p_SolicitudRescateTransferencia=[{"IdInterno": 41}, {"IdInterno": 42}]),
    {"CrearSolicitudRescateTransferencia": doble(ok([41], [msg(1, "ERROR", "X_42", "42 rechazada")]), ok([41]))})
C["FMM-MAN-14_pasada-real-con-error-sus-mensajes-se-pierden"] = ("FM-MAN-05 D-MAN-5: Programado, la pasada N devuelve contador 1 con mensaje: el contador sube, el mensaje de la pasada N se descarta (ROLLBACK del anfitrion)",
    entrada(p_SolicitudesAporteRescateProgramado=[{"IdInterno": 51}]),
    {"CrearSolicitudAporteRescateProgramado": doble(ok([51], [msg(1, "ADVERTENCIA", "X_SIM", "aviso de simulacion")]), ok([51], [msg(2, "ERROR", "X_REAL", "error solo real")], 1))})
C["FMM-MAN-15_dos-pasadas-de-normal-consumen-dos-secuencias"] = ("FM-MAN-02: dos solicitudes; la simulacion consume numeros de secuencia para ambas (2+2) y la pasada N solo para la aceptada (1+1) (ver secuenciasSolicitadas; la IdInterno 2 la rechaza el doble de paridad ficticio del oraculo)",
    entrada(p_SolicitudesAporteRescateNormal=[rescate(1), rescate(2)]), {})

if __name__ == "__main__":
    for nombre, (ref, ent, colab) in C.items():
        d = os.path.join(HERE, "cases", nombre)
        os.makedirs(d, exist_ok=True)
        caso = {"servicio": "ManejadorSolicitudAporteRescate", "ref": ref, "entrada": ent}
        if colab:
            caso["colaboradores"] = colab
        caso["tablas"] = TABLAS
        with open(os.path.join(d, "case.json"), "w", encoding="utf-8", newline="\n") as f:
            json.dump(caso, f, indent=2, ensure_ascii=False)
            f.write("\n")
        with open(os.path.join(d, "seed.sql"), "w", encoding="utf-8", newline="\n") as f:
            f.write("-- case seed on top of seed/base-fmm.sql (fake data)\nSELECT 1;\n")
    print(len(C), "cases written")
