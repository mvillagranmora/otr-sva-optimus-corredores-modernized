#!/usr/bin/env python3
"""Writes the INPUT side of the pilot cases (cases/<id>_<slug>/{case.json,seed.sql}) and the inventory cases_matrix.json (the ~30 Normal + Manejador cases of
PLAN section 7.1 as a list: what each pins; NOT recorded yet). expected.json is NEVER written here: it is recorded by running the legacy (legacy-oracle/record.sh).
Fake data only. Run: py -3 make_cases.py   (idempotent, overwrites case.json / seed.sql / cases_matrix.json only)."""
import json
import os

from seedlib import HERE

DUMP = [{"nombre": "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate", "orden": '"IdSolicitudAporteRescate"'},
        {"nombre": "BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate", "orden": '"IdBloqueoSolicitudAporteRescate"'},
        {"nombre": "BSCL.FondoMutuo.BD::FMM.InterfazAgf", "orden": '"IdInterfazAgf"'}]


def sol(interno=1, **kw):
    d = {"IdInterno": interno, "IdCliente": 100, "IdPortafolio": 500, "TipoSolicitud": "RES", "IdSerieFondo": "FM001A", "CalculoPorMontoCuota": "C", "EsPorMontoLiquido": "N",
         "Monto": 0, "NumeroCuotas": 10, "IdMoneda": "CLP", "IdMedioDePago": "CIN", "IdPreferenciaPagoPortafolio": 0, "Visado": "N", "SolicitudBatch": "N",
         "OrigenOperacion": "M", "FechaOperacion": "0001-01-01", "IdRegimenTributario": "GEN", "Glosa": "", "IndicadorMLT": "N", "IdSerieFondoDestino": "",
         "IdEmisorDestino": "", "IdAutorizadoOrigen": 0, "Porcentaje": 0, "FolioContrato": 0, "IdMonedaPago": "CLP"}
    d.update(kw)
    return d


def normal(sols, **kw):
    d = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "N", "p_UsaReglasNegocio": "S", "p_SolicitudesAporteRescate": sols, "p_FormaPagoSpot": []}
    d.update(kw)
    return d


def manejador(sols, **kw):
    d = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "N", "p_UsaReglasNegocio": "S", "p_IdTipoCanalIngreso": "OPT", "p_FormaPagoSpot": [],
         "p_SolicitudesAporteRescateNormal": sols}
    d.update(kw)
    return d


PILOTS = [
    ("FMM-NOR-P01_rescate-minimo-por-cuotas", "CrearSolicitudAporteRescateNormal",
     "Normal.logica: rescate de 10 cuotas de FM001A (cliente 100, portafolio 500, medio CIN, origen OPT manual => bloqueo anti duplicados); custodia 1000 por doble; "
     "pasa el bloqueo (crea, confirma en autonomo, elimina), INSERT de la solicitud, AGF, AgregarLog; commit del servlet", normal([sol()]), ""),
    ("FMM-NOR-P02_rescate-mayor-a-custodia-rechazado", "CrearSolicitudAporteRescateNormal",
     "Normal.logica:1289-1298 (rescate de 5000 cuotas con custodia 1000 por doble): UI_ERROR_FM_CLIENTE_SIN_CUSTODIA, contador 1 => ROLLBACK del servlet; "
     "el bloqueo se creo (autonomo) y se libera igual; sin INSERT. Caso de rechazo del piloto: fija contador, mensaje y decision", normal([sol(NumeroCuotas=5000)]), ""),
    ("FMM-MAN-P01_solo-lista-normal-un-rescate", "ManejadorSolicitudAporteRescate",
     "Manejador:117-137 y despacho: solo p_SolicitudesAporteRescateNormal con 1 rescate; pasada p_SimularOperacion=S y pasada N (la clase Normal real por dentro); "
     "p_FormaPagoSpot=[] presente (D-MAN-2: sin el campo el Manejador falla)", manejador([sol()]), ""),
]

MATRIX = [  # (id, que fija)
    ("FMM-NOR-01", "rescate por cuotas, medio CIN, origen OPT (bloqueo): camino feliz (PILOTO P01)"),
    ("FMM-NOR-02", "rescate por monto (M) con valor cuota y redondeo HALF_DOWN"),
    ("FMM-NOR-03", "rescate total (T): custodia completa, RescateTotal en transito"),
    ("FMM-NOR-04", "aporte por monto con medio CIN origen manual: SaldoFlujoCaja (doble) suficiente"),
    ("FMM-NOR-05", "aporte CIN manual con saldo insuficiente y CELLINCECT-763 opera con saldo negativo S/N"),
    ("FMM-NOR-06", "aporte medio no CIN: ProcesarIngresoEgresoTesoreria -> CrearIngresoEgreso (doble) y D-NOR-5"),
    ("FMM-NOR-07", "aporte USD con pago CLP: GeneraSpot, ProcesarIngresoOperacionSpot (dobles Spot)"),
    ("FMM-NOR-08", "perfil comercial vs backoffice: estado Abierto/Cerrado de la serie por perfil"),
    ("FMM-NOR-09", "usuario sin perfil: PERFIL_INCORRECTO (salida temprana, bloqueo no creado)"),
    ("FMM-NOR-10", "origen PAC/REC/BONIF: sin bloqueo anti duplicados; PAC exento de patrimonio minimo solo en proceso (D-NOR-4 por JSON)"),
    ("FMM-NOR-11", "origen CAR y CSC: sin bloqueo"),
    ("FMM-NOR-12", "SolicitudBatch=S con RespetaValutasCargaMasiva N/S: fechas sin valutas"),
    ("FMM-NOR-13", "traspaso APV (FolioContrato en TRA.Traspaso) y EsTraspaso"),
    ("FMM-NOR-14", "patrimonio minimo de la serie insuficiente (UI_PATRIMONIO_MINIMO_INSUFICIENTE)"),
    ("FMM-NOR-15", "rescate mayor a la custodia: UI_ERROR_FM_CLIENTE_SIN_CUSTODIA (contador 1 => ROLLBACK, bloqueo ya liberado)"),
    ("FMM-NOR-16", "comision trimestral NN con CALCULAR_COMISION_CERTIFICACION=S (CrearSolicitudRescateComisionNN, D-NOR-6/14/17)"),
    ("FMM-NOR-17", "canal OWP con PLA.NotificarEstadoOrdenOWP=S (NotificarEstadoOrdenesOWP doble)"),
    ("FMM-NOR-18", "multi-solicitud: mezcla de buenas y rechazadas (contador por mensaje, D-NOR-7)"),
    ("FMM-NOR-19", "simulacion p_SimularOperacion=S: sin INSERT, sin bloqueo, sin colaboradores de escritura"),
    ("FMM-NOR-20", "bloqueo duplicado: segunda llamada identica (UI_ERROR_BLOQUEO_SOLICITUD) y lote parcialmente duplicado"),
    ("FMM-NOR-21", "lista vacia: NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE"),
    ("FMM-NOR-22", "IdInterno 0 / repetido: ID_INTERNO_NO_CERO, CORRELATIVO_INCORRECTO (el bloqueo se elimina)"),
    ("FMM-NOR-23", "requisitos basicos: empresa/usuario/modulo invalidos (RevisarRequisitosBasicos)"),
    ("FMM-NOR-24", "serie cerrada / bloqueada / restringida (ServicioCumpleRequisitos)"),
    ("FMM-NOR-25", "monto minimo/maximo de la serie y ValidaMontoInversion (D-REQ-6 maximo 0)"),
    ("FMM-NOR-26", "regimen sin relacion con la serie / PermiteRescate=N / proposito del portafolio"),
    ("FMM-NOR-27", "patrimonio emisor (PatrimonioAltos=S, ValidaPatrimonioEmisor, tabla de paso por sesion)"),
    ("FMM-NOR-28", "rescate de portafolio APV sin traspaso: InscribirProcesoBatch (doble)"),
    ("FMM-NOR-29", "excepcion del legacy: serie inexistente (NPE) => ROLLBACK_HTTP500 y bloqueo que queda (D-NOR-13)"),
    ("FMM-NOR-30", "fallo de un doble de escritura (AgregarLog/CrearIngresoEgreso con error): rollback total salvo el bloqueo autonomo"),
    ("FMM-MAN-01", "una sola lista Normal con 1 rescate (PILOTO P01)"),
    ("FMM-MAN-02", "dos solicitudes Normal: una buena y una rechazada en la simulacion (filtro por IdInterno, contador solo de la pasada N, D-MAN-1)"),
    ("FMM-MAN-03", "p_FormaPagoSpot ausente: NullPointerException (D-MAN-2)"),
    ("FMM-MAN-04", "otras listas con filas: orden fijo de despacho a los 7 destinos (dobles) y pares CambioSerie/RescateReinversion todo-o-nada"),
    ("FMM-MAN-05", "p_SimularOperacion de entrada se ignora (FM-MAN-05)"),
    ("FMM-MAN-06", "excepcion de la clase destino: contador 1 y relanza (D-MAN-4)"),
]


def main():
    for cid, servicio, ref, entrada, seed in PILOTS:
        d = os.path.join(HERE, "cases", cid)
        os.makedirs(d, exist_ok=True)
        with open(os.path.join(d, "case.json"), "w", encoding="utf-8", newline="\n") as f:
            json.dump({"servicio": servicio, "ref": ref, "entrada": entrada, "tablas": DUMP}, f, indent=2, ensure_ascii=False)
            f.write("\n")
        with open(os.path.join(d, "seed.sql"), "w", encoding="utf-8", newline="\n") as f:
            f.write("-- case seed on top of seed/base-fmm.sql (fake data)\n" + (seed or "SELECT 1") + ";\n")
    with open(os.path.join(HERE, "cases_matrix.json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump({"nota": "Inventario de la matriz de casos de PLAN section 7.1 (paso 1B). Solo los pilotos estan grabados; el resto se graba en los pasos 3-4.",
                   "pilotos": [p[0] for p in PILOTS], "casos": [{"id": i, "fija": q, "estado": "piloto" if "PILOTO" in q else "pendiente"} for i, q in MATRIX]},
                  f, indent=1, ensure_ascii=False)
        f.write("\n")
    print("pilots", len(PILOTS), "matrix", len(MATRIX))


if __name__ == "__main__":
    main()
