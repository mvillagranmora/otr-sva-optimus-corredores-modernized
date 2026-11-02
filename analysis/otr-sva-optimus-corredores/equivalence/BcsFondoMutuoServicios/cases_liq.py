"""F3 W3 T6-B (LiquidacionSolicitudAporteRescate, cards FM-LIQ-01..20 of RULES_FONDOMUTUO_F3_B.md 8.3): the case writer of cases-liq/FMM-LIQ-nnn_slug.

The case INPUTS are written here (see cases_liq_lib.py); the goldens (expected.json) are RECORDED by legacy-oracle/record_liq.sh from the UNMODIFIED legacy class, never derived from reading the code.
Every case is a COMPLETE run: the output of `call` (the legacy recount of the errors included), the table of the solicitudes before / after the call / after the host decision, EVERY call to the three
collaborators outside the module (Tesoreria CrearIngresoEgreso, CuentaInversion CrearCargoAbono, Plataforma AgregarLog) in order with its arguments, the sequences asked and the decision of the host.

ref tags: FM-LIQ-nn (the cards), `front:L`, `LIQ-modo:completo` | `LIQ-modo:excepcion` (the legacy threw somewhere in the run), and the QUIRK tags (each quirk has its own recorded cases and its own rule test):
  LIQ-quirk:log-sin-asignar        the list of logs is built and NEVER put in entradaAgregarLog (AgregarLog gets an empty log)
  LIQ-quirk:carta-lista-completa   carta_solicitud.put(numReferencia, solicitudesLiquidar) stores the COMPLETE list (not the one of the solicitud)
  LIQ-quirk:cin-referencia-0       the CIN branch reads carta_solicitud.get(0L) and secuencia_carta.get(K) with K counting the solicitudes of group 0
  LIQ-quirk:apv-empresa-1          the APV flag is read with IdEmpresa = 1L fixed
  LIQ-quirk:sin-filtros-sin-mov    no movements and no filters: findFirst().get() raises NoSuchElementException
  LIQ-quirk:omnibus-silencioso     the aportes without omnibus disappear with no message
  LIQ-quirk:conteo-por-referencia  call() recounts the errors comparing tipo by REFERENCE (D-REQ-11): messages of a collaborator (parsed JSON) never count
  LIQ-quirk:primer-filtro          only the first filter is used to select
  LIQ-quirk:fecha-vacia-0001       an empty date of the filter becomes 0001-01-01 and filters by it
  LIQ-quirk:estado-ING             the result carries the state read (ING), not LIQ
  LIQ-quirk:resultados-duplicados  the same solicitud is reported more than once
  LIQ-quirk:producto-n-por-m       one CrearCargoAbono per CIN solicitud, each with ALL the lines
  LIQ-quirk:movcar-siempre         IdTipoMovimiento is compared with "APO": never true, every cargo / abono is MOVCAR / CAPEFM
  LIQ-quirk:simulacion-escribe     p_SimularOperacion only goes to Tesoreria: the UPDATEs and CrearCargoAbono run anyway
  LIQ-quirk:mensajes-perdidos      the error of Tesoreria / CuentaInversion replaces the messages gathered before
  LIQ-quirk:orden-por-hash         the groups are visited in the order of a HashMap keyed by Grupo (sum of hashes)
  LIQ-quirk:comparacion-de-tipos   the retention only applies to RESNORM / RESTRASP of APV-B with a medium other than CIN
"""
import json  # noqa: F401
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from cases_liq_lib import *  # noqa: E402,F401,F403
import cases_liq_lib as LL  # noqa: E402


def mix(*ds):
    out = {}
    for d in ds:
        out.update(d)
    return out


def E(*ids, **kw):
    """the ordinary request of the UI: the ids of the movements and ONE all-pass filter"""
    return entrada(movs=list(ids), filtros=[filtro()], **kw)


def rng(n):
    return list(range(n))


def pers(i, **c):
    """an aporte of AGF person 7000+i (so that it is a group of its own)"""
    return aporte(i, IdPersonaAGF=7000 + i, **c)


def generar():
    P = ["front:L"]
    # ================================================================== FM-LIQ-09 / 14: one group, the happy path
    caso("un-aporte-genera-un-egreso-de-la-AGF", ["FM-LIQ-01", "FM-LIQ-07", "FM-LIQ-09", "FM-LIQ-14"], E(1), seed=[aporte(1)], colaboradores=ie_ok(0), tags=["LIQ-quirk:estado-ING"])
    caso("un-rescate-genera-un-ingreso-de-la-AGF", ["FM-LIQ-09", "FM-LIQ-14"], E(1), seed=[rescate(1)], colaboradores=ie_ok(0))
    caso("dos-aportes-del-mismo-grupo-suman-su-monto", ["FM-LIQ-07", "FM-LIQ-09", "FM-LIQ-14"], E(1, 2), seed=[aporte(1, Monto=100000), aporte(2, Monto=250000)], colaboradores=ie_ok(0))
    caso("el-monto-del-grupo-conserva-los-decimales", ["FM-LIQ-09"], E(1, 2), seed=[aporte(1, Monto=Num("100000.5")), aporte(2, Monto=Num("250000.25"))], colaboradores=ie_ok(0))
    caso("monto-grande-con-ocho-decimales", ["FM-LIQ-09"], E(1, 2), seed=[aporte(1, Monto=Num("123456789012345.12345678")), aporte(2, Monto=Num("0.00000001"))], colaboradores=ie_ok(0))
    caso("monto-negativo-y-cero-en-un-grupo", ["FM-LIQ-09"], E(1, 2, 3), seed=[aporte(1, Monto=-500), aporte(2, Monto=0), aporte(3, Monto=Num("-0.5"))], colaboradores=ie_ok(0))
    caso("medio-CIN-del-grupo-usa-su-modalidad", ["FM-LIQ-09"], E(1), seed=[aporte(1, IdMedioDePagoCargoAGF="CIN")], colaboradores=ie_ok(0))
    caso("un-aporte-en-dolares", ["FM-LIQ-09"], E(1), seed=[aporte(1, IdMoneda="USD", IdMonedaFondo="USD", Monto=5000)], colaboradores=ie_ok(0))
    caso("el-grupo-usa-la-moneda-del-fondo-no-la-del-cliente", ["FM-LIQ-07", "FM-LIQ-09"], E(1), seed=[aporte(1, IdMoneda="CLP", IdMonedaFondo="USD", Monto=5000)], colaboradores=ie_ok(0))
    # ================================================================== FM-LIQ-07 grouping: one case per field of the key
    caso("aportes-de-distinta-persona-AGF-son-dos-grupos", ["FM-LIQ-07", "FM-LIQ-08", "FM-LIQ-14"], E(1, 2), seed=[aporte(1, IdPersonaAGF=7001), aporte(2, IdPersonaAGF=7002)], colaboradores=ie_ok(0, 1),
         tags=["LIQ-quirk:orden-por-hash"])
    caso("un-aporte-y-un-rescate-iguales-son-dos-grupos", ["FM-LIQ-07", "FM-LIQ-09"], E(1, 2), seed=[aporte(1), rescate(2)], colaboradores=ie_ok(0, 1), tags=["LIQ-quirk:orden-por-hash"])
    caso("distinta-moneda-del-fondo-son-dos-grupos", ["FM-LIQ-07"], E(1, 2), seed=[aporte(1), aporte(2, IdMonedaFondo="USD")], colaboradores=ie_ok(0, 1))
    caso("distinto-portafolio-AGF-son-dos-grupos", ["FM-LIQ-07"], E(1, 2), seed=[aporte(1, IdPortafolioAGF=8001), aporte(2, IdPortafolioAGF=8002)], colaboradores=ie_ok(0, 1))
    caso("distinto-medio-de-cargo-AGF-son-dos-grupos", ["FM-LIQ-07"], E(1, 2), seed=[aporte(1, IdMedioDePagoCargoAGF="TRA"), aporte(2, IdMedioDePagoCargoAGF="CHE")], colaboradores=ie_ok(0, 1))
    caso("distinta-fecha-de-liquidacion-son-dos-grupos", ["FM-LIQ-07"], E(1, 2), seed=[aporte(1, FechaLiquidacion="2026-03-03"), aporte(2, FechaLiquidacion="2026-03-04")], colaboradores=ie_ok(0, 1))
    caso("distinta-fecha-de-certificacion-son-dos-grupos", ["FM-LIQ-07"], E(1, 2), seed=[aporte(1, FechaCertificacion="2026-03-02"), aporte(2, FechaCertificacion="2026-03-01")],
         colaboradores=ie_ok(0, 1))
    caso("el-medio-del-grupo-de-un-aporte-es-el-de-cargo", ["FM-LIQ-07", "FM-LIQ-09"], E(1), seed=[aporte(1, IdMedioDePagoCargoAGF="CHE", IdMedioDePagoAbonoAGF="TRA")], colaboradores=ie_ok(0))
    caso("el-medio-del-grupo-de-un-rescate-es-el-de-abono", ["FM-LIQ-07", "FM-LIQ-09"], E(1), seed=[rescate(1, IdMedioDePagoCargoAGF="CHE", IdMedioDePagoAbonoAGF="TRA")], colaboradores=ie_ok(0))
    caso("seis-grupos-se-recorren-en-el-orden-del-hash", ["FM-LIQ-07", "FM-LIQ-08", "FM-LIQ-14"], E(*range(1, 7)), seed=[pers(i) for i in range(1, 7)], colaboradores=ie_ok(*rng(6)),
         tags=["LIQ-quirk:orden-por-hash"])
    caso("diez-grupos-de-aportes-y-rescates-en-el-orden-del-hash", ["FM-LIQ-07", "FM-LIQ-08", "FM-LIQ-14"], E(*range(1, 11)),
         seed=[(rescate(i, IdPersonaAGF=7100 + i) if i % 2 else aporte(i, IdPersonaAGF=7200 + i, FechaLiquidacion="2026-03-0%d" % (3 + i % 3))) for i in range(1, 11)], colaboradores=ie_ok(*rng(10)),
         tags=["LIQ-quirk:orden-por-hash"])
    # ---- the nulls of the key raise NullPointerException (Grupo.hashCode)
    caso("persona-AGF-nula-lanza-NPE", ["FM-LIQ-07", "FM-LIQ-20"], E(1), seed=[aporte(1, IdPersonaAGF=None)], excepcion=True)
    caso("portafolio-AGF-nulo-lanza-NPE", ["FM-LIQ-07", "FM-LIQ-20"], E(1), seed=[aporte(1, IdPortafolioAGF=None)], excepcion=True)
    caso("medio-de-cargo-nulo-de-un-aporte-lanza-NPE", ["FM-LIQ-07", "FM-LIQ-20"], E(1), seed=[aporte(1, IdMedioDePagoCargoAGF=None)], excepcion=True)
    caso("medio-de-abono-nulo-de-un-rescate-lanza-NPE", ["FM-LIQ-07", "FM-LIQ-20"], E(1), seed=[rescate(1, IdMedioDePagoAbonoAGF=None)], excepcion=True)
    caso("medio-de-abono-nulo-de-un-aporte-no-importa", ["FM-LIQ-07"], E(1), seed=[aporte(1, IdMedioDePagoAbonoAGF=None)], colaboradores=ie_ok(0))
    caso("moneda-del-fondo-nula-lanza-NPE", ["FM-LIQ-07", "FM-LIQ-20"], E(1), seed=[aporte(1, IdMonedaFondo=None)], excepcion=True)
    caso("monto-nulo-lanza-NPE-al-sumar", ["FM-LIQ-09", "FM-LIQ-20"], E(1), seed=[aporte(1, Monto=None)], excepcion=True)
    # ================================================================== FM-LIQ-09 line of the AGF: parameters
    caso("liquidacion-por-cuenta-del-cliente-aporte", ["FM-LIQ-02", "FM-LIQ-09"], E(1), seed=[aporte(1), cta_inv("S")], colaboradores=ie_ok(0))
    caso("liquidacion-por-cuenta-del-cliente-rescate", ["FM-LIQ-02", "FM-LIQ-09"], E(1), seed=[rescate(1), cta_inv("S")], colaboradores=ie_ok(0))
    caso("medio-sin-modalidad-usa-MODCAJ", ["FM-LIQ-09"], E(1), seed=[aporte(1, IdMedioDePagoCargoAGF="EFE")], colaboradores=ie_ok(0))
    caso("tabla-de-modalidades-vacia-todo-es-MODCAJ", ["FM-LIQ-02", "FM-LIQ-09"], E(1, 2), seed=[aporte(1), rescate(2, IdMedioDePagoAbonoAGF="CHE"), modalidades()], colaboradores=ie_ok(0, 1))
    caso("modalidades-de-dos-medios-se-asignan-por-medio", ["FM-LIQ-09"], E(1, 2), seed=[aporte(1, IdMedioDePagoCargoAGF="CHE"), aporte(2, IdMedioDePagoCargoAGF="TRA"), modalidades(("CHE", "MODCAJ"), ("TRA", "MODDEP"))],
         colaboradores=ie_ok(0, 1))
    caso("medio-repetido-en-la-tabla-de-modalidades-lanza-IllegalState", ["FM-LIQ-02", "FM-LIQ-20"], E(1), seed=[aporte(1), modalidades(("TRA", "MODDEP"), ("TRA", "MODCAJ"))], excepcion=True)
    # ================================================================== FM-LIQ-01 contract: the request
    caso("la-solicitud-simulada-pasa-a-tesoreria", ["FM-LIQ-13", "FM-LIQ-19"], E(1, sim="S"), seed=[aporte(1)], colaboradores=ie_ok(0), tags=["LIQ-quirk:simulacion-escribe"])
    caso("simulada-y-tesoreria-no-genera-nada-no-actualiza", ["FM-LIQ-13", "FM-LIQ-14", "FM-LIQ-19"], E(1, sim="S"), seed=[aporte(1)], colaboradores=ie_ok())
    caso("la-cabecera-pasa-tal-cual-a-tesoreria", ["FM-LIQ-01", "FM-LIQ-13"], entrada(movs=[1], filtros=[filtro()], usuario=15, modulo="XYZ", sim="N", reglas="N"), seed=[aporte(1)], colaboradores=ie_ok(0))
    caso("cabecera-sin-modulo-ni-usuario-pasa-nula", ["FM-LIQ-01", "FM-LIQ-13"], entrada(movs=[1], filtros=[filtro()], usuario=ABSENT, modulo=ABSENT, sim=ABSENT, reglas=ABSENT), seed=[aporte(1)],
         colaboradores=ie_ok(0))
    caso("sin-empresa-lanza-excepcion", ["FM-LIQ-01", "FM-LIQ-02", "FM-LIQ-20"], entrada(movs=[1], filtros=[filtro()], empresa=ABSENT), seed=[aporte(1)], excepcion=True, tags=["LIQ-desvio:kit-1"])
    caso("movimientos-ausente-usa-la-lista-vacia-y-los-filtros", ["FM-LIQ-01", "FM-LIQ-05"], entrada(movs=ABSENT, filtros=[filtro(TipoSolicitud="APO", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")]),
         seed=[aporte(1)], colaboradores=ie_ok(0))
    caso("movimientos-nulo-lanza-NPE", ["FM-LIQ-01", "FM-LIQ-20"], entrada(movs=None, filtros=[filtro()]), seed=[aporte(1)], excepcion=True)
    caso("filtros-nulo-lanza-NPE-antes-de-todo", ["FM-LIQ-01", "FM-LIQ-03", "FM-LIQ-20"], entrada(movs=[1], filtros=None), seed=[aporte(1)], excepcion=True)
    # ================================================================== FM-LIQ-02 parameters
    caso("falta-LiquidacionCuentaInversionCliente", ["FM-LIQ-02", "FM-LIQ-20"], E(1), seed=[aporte(1), cta_inv(None)], excepcion=True)
    caso("falta-VALIDAR_LIQUIDACION_OMNIBUS", ["FM-LIQ-02", "FM-LIQ-20"], E(1), seed=[aporte(1), omnibus(None)], excepcion=True)
    caso("falta-la-fecha-del-sistema", ["FM-LIQ-02", "FM-LIQ-20"], E(1), seed=[aporte(1), config("PLA.FechaSistema", None)], excepcion=True)
    caso("falta-APV.Habilitado-de-la-empresa-1", ["FM-LIQ-02", "FM-LIQ-20"], E(1), seed=[aporte(1), apv(None)], excepcion=True, tags=["LIQ-quirk:apv-empresa-1"])
    caso("APV-se-lee-de-la-empresa-1-aunque-la-solicitud-sea-de-la-2", ["FM-LIQ-02", "FM-LIQ-10"], E(1, empresa=2),
         seed=[empresa_dos(), apv("S", 1), apv("N", 2), aporte(1, IdEmpresa=2)], colaboradores=ie_ok(0, 1), tags=["LIQ-quirk:apv-empresa-1", "LIQ-quirk:carta-lista-completa"])
    caso("APV-de-la-empresa-2-no-cuenta-si-la-1-dice-N", ["FM-LIQ-02", "FM-LIQ-10"], E(1, empresa=2),
         seed=[empresa_dos(), apv("N", 1), apv("S", 2), aporte(1, IdEmpresa=2)], colaboradores=ie_ok(0), tags=["LIQ-quirk:apv-empresa-1"])
    caso("la-fecha-del-sistema-va-a-la-cuenta-de-inversion", ["FM-LIQ-02", "FM-LIQ-15"], E(1), seed=[aporte(1, IdMedioDePago="CIN"), cta_inv("S"), config("PLA.FechaSistema", "2026-04-15")],
         colaboradores=mix(ie_ok(0), ca_ok(1)))
    # ================================================================== FM-LIQ-03 / 04 / 05 selection
    caso("sin-movimientos-ni-filtros-lanza-NoSuchElement", ["FM-LIQ-03", "FM-LIQ-05", "FM-LIQ-20"], entrada(movs=[], filtros=[]), seed=[aporte(1)], excepcion=True, tags=["LIQ-quirk:sin-filtros-sin-mov"])
    caso("sin-filtros-pero-con-movimientos-avisa-y-liquida", ["FM-LIQ-03", "FM-LIQ-04", "FM-LIQ-01"], entrada(movs=[1], filtros=[]), seed=[aporte(1)], colaboradores=ie_ok(0), tags=["LIQ-quirk:conteo-por-referencia"])
    caso("filtros-ausente-con-movimientos-avisa-y-liquida", ["FM-LIQ-03", "FM-LIQ-04"], entrada(movs=[1], filtros=ABSENT), seed=[aporte(1)], colaboradores=ie_ok(0))
    caso("sin-filtros-y-tesoreria-con-error-pierde-el-aviso", ["FM-LIQ-03", "FM-LIQ-13"], entrada(movs=[1], filtros=[]), seed=[aporte(1)],
         colaboradores=ie_ok(0, contador=1, mensajes=[msg("ERROR", "ERROR_TESORERIA", "falla de tesoreria")]), tags=["LIQ-quirk:mensajes-perdidos", "LIQ-quirk:conteo-por-referencia"])
    caso("los-movimientos-eligen-por-id", ["FM-LIQ-04"], E(2, 3), seed=[aporte(1), aporte(2), aporte(3), aporte(4)], colaboradores=ie_ok(0))
    caso("movimiento-ya-liquidado-no-se-selecciona", ["FM-LIQ-04"], E(1, 2), seed=[aporte(1, EstadoLiquidacion="LIQ"), aporte(2)], colaboradores=ie_ok(0))
    caso("movimiento-de-otro-tipo-no-se-selecciona", ["FM-LIQ-04"], E(1, 2), seed=[aporte(1, mov="APOAJU"), aporte(2)], colaboradores=ie_ok(0))
    caso("los-once-tipos-de-movimiento-se-seleccionan-y-el-doce-no", ["FM-LIQ-04"], E(*range(1, 13)),
         seed=[(aporte(i, mov=m, IdPersonaAGF=7000 + i) if m.startswith("APO") else rescate(i, mov=m, IdPersonaAGF=7000 + i)) for i, m in
               enumerate(["APONORM", "RESNORM", "APOREINV", "RESREINV", "APORESPRO", "APOOVER", "RESTRAPOR", "RESTRASP", "APOTRASP", "APODIVCFI", "RESCNN", "RESAJU"], start=1)],
         colaboradores=ie_ok(*rng(11)), tags=["LIQ-quirk:orden-por-hash"])
    caso("movimiento-de-otra-empresa-no-se-selecciona", ["FM-LIQ-04"], E(1, 2), seed=[empresa_dos(), aporte(1, IdEmpresa=2), aporte(2)], colaboradores=ie_ok(0))
    caso("ids-de-movimiento-repetidos", ["FM-LIQ-04"], E(1, 1), seed=[aporte(1)], colaboradores=ie_ok(0))
    caso("movimiento-sin-id", ["FM-LIQ-04", "FM-LIQ-20"], entrada(movs=[None], filtros=[filtro()]), seed=[aporte(1)], excepcion=False)
    caso("movimientos-que-no-existen-llaman-a-tesoreria-con-listas-vacias", ["FM-LIQ-04", "FM-LIQ-13"], E(77), seed=[aporte(1)], colaboradores=ie_ok())
    caso("movimientos-presentes-ignoran-los-filtros-para-seleccionar", ["FM-LIQ-04", "FM-LIQ-05"], entrada(movs=[1], filtros=[filtro(IdSerieFondo="NOEXISTE")]), seed=[aporte(1), aporte(2)], colaboradores=ie_ok(0))
    # ---- selection by the filters (no movements): ONLY the first filter
    caso("el-filtro-de-aportes-por-fechas", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(TipoSolicitud="APO", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")]),
         seed=[aporte(1), aporte(2, FechaLiquidacion="2026-03-09"), rescate(3)], colaboradores=ie_ok(0))
    caso("fechas-vacias-del-filtro-se-vuelven-0001-01-01-y-no-seleccionan", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(TipoSolicitud="APO")]), seed=[aporte(1)], colaboradores=ie_ok(),
         tags=["LIQ-quirk:fecha-vacia-0001"])
    caso("una-solicitud-con-fechas-0001-01-01-si-sale-con-el-filtro-vacio", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(TipoSolicitud="APO")]),
         seed=[aporte(1, FechaLiquidacion="0001-01-01", FechaCertificacion="0001-01-01"), aporte(2)], colaboradores=ie_ok(0), tags=["LIQ-quirk:fecha-vacia-0001"])
    caso("el-filtro-por-emisor", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(IdEmisor="AGF2", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")]),
         seed=[aporte(1, IdEmisor="AGF1"), aporte(2, IdEmisor="AGF2")], colaboradores=ie_ok(0))
    caso("el-filtro-por-serie-del-fondo", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(IdSerieFondo="FM002A", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")]),
         seed=[aporte(1, IdSerieFondo="FM001A"), aporte(2, IdSerieFondo="FM002A")], colaboradores=ie_ok(0))
    caso("el-filtro-por-tipo-rescate-solo-trae-los-certificados", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(TipoSolicitud="RES", IdEstado="ACP", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")]),
         seed=[rescate(1, Estado="CRT"), rescate(2, Estado="ACP")], colaboradores=ie_ok(0))
    caso("el-filtro-por-estado-de-un-aporte", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(TipoSolicitud="APO", IdEstado="ACP", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")]),
         seed=[aporte(1, Estado="CRT"), aporte(2, Estado="ACP")], colaboradores=ie_ok(0))
    caso("el-filtro-sin-tipo-trae-aportes-de-cualquier-estado-y-rescates-certificados", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")]),
         seed=[aporte(1, Estado="ACP"), aporte(2, Estado="CRT"), rescate(3, Estado="CRT"), rescate(4, Estado="ACP")], colaboradores=ie_ok(0, 1), tags=["LIQ-quirk:orden-por-hash"])
    caso("solo-se-usa-el-primer-filtro", ["FM-LIQ-05", "FM-LIQ-18"], entrada(movs=[], filtros=[filtro(IdSerieFondo="FM001A", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03", IdInterno=11),
                                                                                                     filtro(IdSerieFondo="FM002A", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03", IdInterno=12)]),
         seed=[aporte(1, IdSerieFondo="FM001A"), aporte(2, IdSerieFondo="FM002A")], colaboradores=ie_ok(0), tags=["LIQ-quirk:primer-filtro", "LIQ-quirk:log-sin-asignar"])
    caso("el-filtro-no-trae-lo-ya-liquidado", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")]),
         seed=[aporte(1, EstadoLiquidacion="LIQ"), aporte(2)], colaboradores=ie_ok(0))
    caso("el-filtro-con-fecha-de-certificacion-nula-lanza-NPE", ["FM-LIQ-05", "FM-LIQ-20"], entrada(movs=[], filtros=[filtro(FechaCertificacion=ABSENT, FechaLiquidacion="2026-03-03")]), seed=[aporte(1)], excepcion=True)
    caso("el-filtro-con-fecha-de-liquidacion-nula-lanza-NPE", ["FM-LIQ-05", "FM-LIQ-20"], entrada(movs=[], filtros=[filtro(FechaCertificacion="2026-03-02", FechaLiquidacion=ABSENT)]), seed=[aporte(1)], excepcion=True)
    caso("el-filtro-con-emisor-nulo", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(IdEmisor=ABSENT, FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")]), seed=[aporte(1)], colaboradores=ie_ok(0))
    caso("un-filtro-nulo-en-la-lista-lanza-NPE-al-seleccionar", ["FM-LIQ-05", "FM-LIQ-20"], entrada(movs=[], filtros=[None]), seed=[aporte(1)], excepcion=True)
    caso("un-filtro-nulo-en-la-lista-lanza-NPE-al-registrar-el-log", ["FM-LIQ-18", "FM-LIQ-20"], entrada(movs=[1], filtros=[None]), seed=[aporte(1)], colaboradores=ie_ok(0), excepcion=True)
    # ================================================================== FM-LIQ-06 omnibus
    caso("omnibus-S-quita-en-silencio-los-aportes-sin-omnibus", ["FM-LIQ-06", "FM-LIQ-08"], E(1, 2), seed=[omnibus("S"), aporte(1, IdOmnibus=0), aporte(2, IdOmnibus=5)], colaboradores=ie_ok(0),
         tags=["LIQ-quirk:omnibus-silencioso"])
    caso("omnibus-S-todos-sin-omnibus-no-queda-nada", ["FM-LIQ-06", "FM-LIQ-08"], E(1, 2), seed=[omnibus("S"), aporte(1, IdOmnibus=0), aporte(2, IdOmnibus=0)], colaboradores=ie_ok(),
         tags=["LIQ-quirk:omnibus-silencioso"])
    caso("omnibus-S-un-rescate-sin-omnibus-se-queda", ["FM-LIQ-06"], E(1), seed=[omnibus("S"), rescate(1, IdOmnibus=0)], colaboradores=ie_ok(0))
    caso("omnibus-S-un-aporte-con-omnibus-nulo-se-queda", ["FM-LIQ-06"], E(1), seed=[omnibus("S"), aporte(1, IdOmnibus=None)], colaboradores=ie_ok(0))
    caso("omnibus-N-no-quita-nada", ["FM-LIQ-06"], E(1), seed=[omnibus("N"), aporte(1, IdOmnibus=0)], colaboradores=ie_ok(0))
    caso("omnibus-con-valor-que-no-es-S-no-quita-nada", ["FM-LIQ-06"], E(1), seed=[omnibus("s"), aporte(1, IdOmnibus=0)], colaboradores=ie_ok(0))
    caso("omnibus-S-con-APV-cuenta-solo-lo-que-queda", ["FM-LIQ-06", "FM-LIQ-08", "FM-LIQ-10"], E(1, 2), seed=[omnibus("S"), apv("S"), aporte(1, IdOmnibus=0), aporte(2, IdOmnibus=5)],
         colaboradores=ie_ok(0, 1), tags=["LIQ-quirk:omnibus-silencioso", "LIQ-quirk:carta-lista-completa"])
    # ================================================================== FM-LIQ-10 / 12: APV lines of the client
    caso("APV-un-aporte-agrega-la-linea-del-cliente", ["FM-LIQ-08", "FM-LIQ-10", "FM-LIQ-12", "FM-LIQ-14"], E(1), seed=[apv("S"), aporte(1, MontoComision=10000)], colaboradores=ie_ok(0, 1),
         tags=["LIQ-quirk:carta-lista-completa", "LIQ-quirk:resultados-duplicados", "LIQ-quirk:estado-ING"])
    caso("APV-un-rescate-agrega-una-linea-que-no-va-a-tesoreria", ["FM-LIQ-10", "FM-LIQ-12"], E(1), seed=[apv("S"), rescate(1, MontoComision=10000)], colaboradores=ie_ok(0))
    caso("APV-un-aporte-pagado-con-CIN-no-va-a-tesoreria", ["FM-LIQ-10", "FM-LIQ-12"], E(1), seed=[apv("S"), aporte(1, IdMedioDePago="CIN")], colaboradores=ie_ok(0))
    caso("APV-un-rescate-pagado-con-CIN-no-va-a-tesoreria", ["FM-LIQ-10", "FM-LIQ-12"], E(1), seed=[apv("S"), rescate(1, IdMedioDePago="CIN", MontoComision=10000)], colaboradores=ie_ok(0))
    caso("APV-con-un-aporte-y-un-rescate-de-personas-distintas", ["FM-LIQ-08", "FM-LIQ-10", "FM-LIQ-12"], E(1, 2), seed=[apv("S"), aporte(1, IdPersonaAGF=7001), rescate(2, IdPersonaAGF=7002, MontoComision=7500)],
         colaboradores=ie_ok(0, 1, 2), tags=["LIQ-quirk:carta-lista-completa", "LIQ-quirk:orden-por-hash"])
    caso("APV-dos-aportes-en-un-grupo-repiten-los-resultados", ["FM-LIQ-10", "FM-LIQ-12", "FM-LIQ-14"], E(1, 2), seed=[apv("S"), aporte(1), aporte(2, Monto=300000)], colaboradores=ie_ok(0, 1, 2),
         tags=["LIQ-quirk:carta-lista-completa", "LIQ-quirk:resultados-duplicados"])
    caso("APV-aporte-con-comision-y-monto-del-cliente", ["FM-LIQ-10"], E(1), seed=[apv("S"), aporte(1, Monto=1000000, MontoComision=25000)], colaboradores=ie_ok(0, 1))
    caso("APV-monto-nulo-lanza-NPE", ["FM-LIQ-10", "FM-LIQ-20"], E(1), seed=[apv("S"), aporte(1, Monto=None)], colaboradores=ie_ok(0, 1), excepcion=True)
    caso("APV-persona-y-portafolio-de-la-solicitud-con-cuenta-del-cliente", ["FM-LIQ-10"], E(1), seed=[apv("S"), cta_inv("S"), aporte(1, IdPersona=1234, IdPortafolio=567, IdPersonaAGF=7009, IdPortafolioAGF=8009)],
         colaboradores=ie_ok(0, 1))
    caso("APV-medio-nulo-de-la-solicitud-lanza-NPE", ["FM-LIQ-10", "FM-LIQ-20"], E(1), seed=[apv("S"), aporte(1, IdMedioDePago=None)], colaboradores=ie_ok(0, 1), excepcion=True)
    caso("APV-tipo-de-movimiento-RESNORM-con-solicitud-de-aporte", ["FM-LIQ-10", "FM-LIQ-11"], E(1), seed=[apv("S"), aporte(1, mov="RESNORM", MontoComision=10000)], colaboradores=ie_ok(0, 1))
    # ================================================================== FM-LIQ-11 withholding APV-B (only visible through CrearCargoAbono or through a MOVING line)
    def rig(*, cfg, rescate_kw=None, mov="RESNORM"):
        """cuenta del cliente + APV + a rescate of APV-B paid by transfer (id 1) and a CIN rescate of ANOTHER person (id 2) that makes the class call CrearCargoAbono with ALL the lines"""
        kw = dict(IdPersonaAGF=7001, CodigoPortafolio="APV-B", MontoPago=1000000, MontoComision=10000, IdMatrizAgrupadorAPV=1)
        kw.update(rescate_kw or {})
        return [apv("S"), cta_inv("S"), retencion(*cfg), rescate(1, mov=mov, **kw), rescate(2, IdPersonaAGF=7002, IdMedioDePago="CIN")]
    quince = [dict(pct=15, retiro="RES")]
    caso("retencion-APV-B-en-un-RESNORM", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=quince), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)), tags=["LIQ-quirk:producto-n-por-m"])
    caso("retencion-APV-B-en-un-RESTRASP", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=quince, mov="RESTRASP"), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("sin-retencion-en-otro-tipo-de-movimiento", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=quince, mov="RESREINV"), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)), tags=["LIQ-quirk:comparacion-de-tipos"])
    caso("sin-retencion-si-el-portafolio-no-es-APV-B", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=quince, rescate_kw=dict(CodigoPortafolio="APV-A")), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("sin-retencion-si-el-medio-es-CIN", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=quince, rescate_kw=dict(IdMedioDePago="CIN")), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("la-primera-fila-con-porcentaje-cero-se-salta", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=[dict(pct=0), dict(pct=10)]), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("las-filas-que-no-son-de-rescate-se-saltan", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=[dict(pct=30, retiro="APO"), dict(pct=20, retiro="TRA")]), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("una-fila-de-otro-regimen-se-salta-y-la-sin-regimen-aplica", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=[dict(pct=30, regimen=99), dict(pct=12, regimen=None)]), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("una-fila-del-mismo-regimen-aplica", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=[dict(pct=18, regimen=1)]), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("gana-la-primera-fila-que-aplica", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=[dict(pct=15), dict(pct=20)]), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("regimen-de-la-fila-con-matriz-nula-lanza-NPE", ["FM-LIQ-11", "FM-LIQ-20"], E(1, 2), seed=rig(cfg=[dict(pct=15, regimen=1)], rescate_kw=dict(IdMatrizAgrupadorAPV=None)),
         colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)), excepcion=True)
    caso("sin-filas-de-retencion-no-hay-retencion", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=[]), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("retencion-con-porcentaje-fraccionario", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=[dict(pct=Num("12.345"))], rescate_kw=dict(MontoPago=Num("1234567.89"))), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("retencion-sobre-un-pago-menor-que-la-comision", ["FM-LIQ-11"], E(1, 2), seed=rig(cfg=quince, rescate_kw=dict(MontoPago=1000, MontoComision=50000)), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)))
    caso("retencion-con-portafolio-nulo-lanza-NPE", ["FM-LIQ-11", "FM-LIQ-20"], E(1, 2), seed=rig(cfg=quince, rescate_kw=dict(CodigoPortafolio=None)), colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)), excepcion=True)
    caso("retencion-en-una-linea-de-ingreso-llega-a-tesoreria-y-a-la-forma-de-pago", ["FM-LIQ-11", "FM-LIQ-12"],
         E(1), seed=[apv("S"), retencion(dict(pct=15)), aporte(1, mov="RESNORM", CodigoPortafolio="APV-B", MontoPago=1000000, MontoComision=10000, IdMatrizAgrupadorAPV=1)], colaboradores=ie_ok(0, 1))
    # ================================================================== FM-LIQ-13 call to Tesoreria
    caso("tesoreria-con-error-devuelve-su-contador-y-sus-mensajes", ["FM-LIQ-13", "FM-LIQ-01"], E(1), seed=[aporte(1)],
         colaboradores=ie_ok(0, contador=2, mensajes=[msg("ERROR", "ERR_TES_1", "uno"), msg("ERROR", "ERR_TES_2", "dos")]), tags=["LIQ-quirk:conteo-por-referencia"])
    caso("tesoreria-con-contador-y-sin-mensajes", ["FM-LIQ-13", "FM-LIQ-01"], E(1), seed=[aporte(1)], colaboradores=ie_ok(0, contador=1), tags=["LIQ-quirk:conteo-por-referencia"])
    caso("tesoreria-con-contador-nulo-sigue", ["FM-LIQ-13"], E(1), seed=[aporte(1)], colaboradores=ie_ok(0, contador=None))
    caso("tesoreria-con-contador-cero-y-mensajes-de-error-sigue", ["FM-LIQ-13"], E(1), seed=[aporte(1)], colaboradores=ie_ok(0, contador=0, mensajes=[msg("ERROR", "ERR_TES_X", "no cuenta")]))
    caso("tesoreria-con-advertencia-sigue-y-no-la-copia", ["FM-LIQ-13"], E(1), seed=[aporte(1)], colaboradores=ie_ok(0, mensajes=[msg("ADVERTENCIA", "ADV_TES", "aviso")]))
    caso("tesoreria-lanza-excepcion", ["FM-LIQ-13", "FM-LIQ-20"], E(1), seed=[aporte(1)], colaboradores=falla("CrearIngresoEgreso", "FALLA_SIMULADA_TESORERIA"), excepcion=True, tags=["LIQ-quirk:secuencias-consumidas"])
    caso("tesoreria-con-lista-de-generados-nula-lanza-NPE", ["FM-LIQ-13", "FM-LIQ-20"], E(1), seed=[aporte(1)], colaboradores=ie_ok(generados=None), excepcion=True)
    caso("tesoreria-con-referencia-desconocida-lanza-NPE", ["FM-LIQ-14", "FM-LIQ-20"], E(1), seed=[aporte(1)], colaboradores=ie_ok(99), excepcion=True)
    caso("tesoreria-con-referencia-nula-lanza-NPE", ["FM-LIQ-14", "FM-LIQ-20"], E(1), seed=[aporte(1)], colaboradores=ie_ok(generados=[{"NumeroReferencia": None, "IdIngresoEgreso": 9000}]), excepcion=True)
    caso("tesoreria-con-la-misma-referencia-dos-veces-actualiza-dos-veces", ["FM-LIQ-14"], E(1), seed=[aporte(1)], colaboradores=ie_ok(0, 0), tags=["LIQ-quirk:resultados-duplicados"])
    caso("tesoreria-solo-genera-un-grupo-el-otro-queda-ingresado", ["FM-LIQ-14"], E(1, 2), seed=[aporte(1, IdPersonaAGF=7001), aporte(2, IdPersonaAGF=7002)], colaboradores=ie_ok(1), tags=["LIQ-quirk:orden-por-hash"])
    caso("tesoreria-genera-un-movimiento-sin-grupos-lanza-NPE", ["FM-LIQ-13"], E(77), seed=[aporte(1)], colaboradores=ie_ok(0), excepcion=True)
    # ================================================================== FM-LIQ-15 / 16 / 17: cuenta de inversion
    caso("cuenta-del-cliente-y-un-aporte-CIN-genera-un-cargo-abono", ["FM-LIQ-15", "FM-LIQ-17"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")], colaboradores=mix(ie_ok(0), ca_ok(1)),
         tags=["LIQ-quirk:cin-referencia-0", "LIQ-quirk:movcar-siempre", "LIQ-quirk:resultados-duplicados"])
    caso("cuenta-del-cliente-y-un-rescate-CIN", ["FM-LIQ-15", "FM-LIQ-17"], E(1), seed=[cta_inv("S"), rescate(1, IdMedioDePago="CIN")], colaboradores=mix(ie_ok(0), ca_ok(1)), tags=["LIQ-quirk:movcar-siempre"])
    caso("sin-cuenta-del-cliente-un-CIN-no-llama-a-cuenta-de-inversion", ["FM-LIQ-15"], E(1), seed=[cta_inv("N"), aporte(1, IdMedioDePago="CIN")], colaboradores=ie_ok(0))
    caso("cuenta-del-cliente-sin-solicitudes-CIN-no-llama", ["FM-LIQ-15"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="TRA")], colaboradores=ie_ok(0))
    caso("dos-CIN-en-el-mismo-grupo-lanzan-IndexOutOfBounds", ["FM-LIQ-17", "FM-LIQ-20"], E(1, 2), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN"), aporte(2, IdMedioDePago="CIN")],
         colaboradores=mix(ie_ok(0), ca_ok(1)), excepcion=True, tags=["LIQ-quirk:cin-referencia-0"])
    caso("dos-CIN-de-grupos-distintos-llaman-dos-veces-con-todas-las-lineas", ["FM-LIQ-15", "FM-LIQ-17"], E(1, 2),
         seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN", IdPersonaAGF=7001), aporte(2, IdMedioDePago="CIN", IdPersonaAGF=7002)], colaboradores=mix(ie_ok(0, 1), ca_ok(1)),
         tags=["LIQ-quirk:producto-n-por-m", "LIQ-quirk:cin-referencia-0", "LIQ-quirk:resultados-duplicados", "LIQ-quirk:orden-por-hash"])
    caso("cada-cargo-abono-generado-repite-la-actualizacion", ["FM-LIQ-17"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")], colaboradores=mix(ie_ok(0), ca_ok(3)),
         tags=["LIQ-quirk:cin-referencia-0", "LIQ-quirk:resultados-duplicados"])
    caso("cuenta-de-inversion-sin-movimientos-generados-no-actualiza", ["FM-LIQ-17"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")], colaboradores=mix(ie_ok(0), ca_ok(0)))
    caso("CIN-con-APV-manda-todas-las-lineas-incluida-la-del-cliente", ["FM-LIQ-10", "FM-LIQ-15"], E(1), seed=[apv("S"), cta_inv("S"), aporte(1, IdMedioDePago="CIN", MontoComision=10000)],
         colaboradores=mix(ie_ok(0), ca_ok(1)), tags=["LIQ-quirk:producto-n-por-m", "LIQ-quirk:carta-lista-completa", "LIQ-quirk:cin-referencia-0"])
    caso("CIN-con-APV-y-un-rescate-de-otro-medio", ["FM-LIQ-10", "FM-LIQ-15"], E(1, 2), seed=[apv("S"), cta_inv("S"), aporte(1, IdMedioDePago="CIN", IdPersonaAGF=7001), rescate(2, IdPersonaAGF=7002, MontoComision=5000)],
         colaboradores=mix(ie_ok(0, 1, 2), ca_ok(1)), tags=["LIQ-quirk:producto-n-por-m", "LIQ-quirk:carta-lista-completa", "LIQ-quirk:orden-por-hash"])
    caso("la-cuenta-de-inversion-siempre-va-con-modulo-FMM-y-sin-simular", ["FM-LIQ-15", "FM-LIQ-19"], entrada(movs=[1], filtros=[filtro()], modulo="XYZ", sim="S", reglas="N", usuario=15),
         seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")], colaboradores=mix(ie_ok(0), ca_ok(1)), tags=["LIQ-quirk:simulacion-escribe"])
    caso("cuenta-de-inversion-con-error-devuelve-su-contador-y-sus-mensajes", ["FM-LIQ-16"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")],
         colaboradores=mix(ie_ok(0), ca_ok(0, contador=1, mensajes=[msg("ERROR", "ERR_CIN", "falla cuenta")])), tags=["LIQ-quirk:conteo-por-referencia"])
    caso("cuenta-de-inversion-con-error-pierde-el-aviso-de-filtros-y-conserva-los-resultados", ["FM-LIQ-03", "FM-LIQ-16"], entrada(movs=[1], filtros=[]), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")],
         colaboradores=mix(ie_ok(0), ca_ok(0, contador=1, mensajes=[msg("ERROR", "ERR_CIN", "falla cuenta")])), tags=["LIQ-quirk:conteo-por-referencia", "LIQ-quirk:mensajes-perdidos"])
    caso("cuenta-de-inversion-con-contador-cero-y-mensaje-ERROR-sale-igual", ["FM-LIQ-16"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")],
         colaboradores=mix(ie_ok(0), ca_ok(0, contador=0, mensajes=[msg("ERROR", "ERR_CIN", "contador cero")])), tags=["LIQ-quirk:conteo-por-referencia"])
    caso("cuenta-de-inversion-mensaje-error-en-minusculas-cuenta", ["FM-LIQ-16"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")],
         colaboradores=mix(ie_ok(0), ca_ok(0, contador=0, mensajes=[msg("error", "ERR_CIN_MIN", "minusculas")])), tags=["LIQ-quirk:conteo-por-referencia"])
    caso("cuenta-de-inversion-con-advertencia-sigue", ["FM-LIQ-16", "FM-LIQ-17"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")],
         colaboradores=mix(ie_ok(0), ca_ok(1, mensajes=[msg("ADVERTENCIA", "ADV_CIN", "aviso")])), tags=["LIQ-quirk:cin-referencia-0"])
    caso("cuenta-de-inversion-con-contador-nulo-lanza-NPE", ["FM-LIQ-16", "FM-LIQ-20"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")],
         colaboradores=mix(ie_ok(0), ca_ok(1, contador=None)), excepcion=True)
    caso("cuenta-de-inversion-lanza-excepcion", ["FM-LIQ-15", "FM-LIQ-20"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")], colaboradores=mix(ie_ok(0), falla("CrearCargoAbono", "FALLA_SIMULADA_CIN")),
         excepcion=True)
    caso("cuenta-de-inversion-con-lista-de-generados-nula-lanza-NPE", ["FM-LIQ-17", "FM-LIQ-20"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago="CIN")], colaboradores=mix(ie_ok(0), ca_ok(generados=None)),
         excepcion=True)
    caso("medio-nulo-con-cuenta-del-cliente-lanza-NPE-despues-de-actualizar", ["FM-LIQ-15", "FM-LIQ-20"], E(1), seed=[cta_inv("S"), aporte(1, IdMedioDePago=None)], colaboradores=ie_ok(0), excepcion=True)
    # ================================================================== FM-LIQ-18 log
    caso("el-log-se-arma-por-filtro-y-nunca-se-asigna", ["FM-LIQ-18"], entrada(movs=[1], filtros=[filtro(IdInterno=101), filtro(IdInterno=102), filtro(IdInterno=103)]), seed=[aporte(1)], colaboradores=ie_ok(0),
         tags=["LIQ-quirk:log-sin-asignar"])
    caso("el-log-con-un-filtro-sin-id-interno", ["FM-LIQ-18"], entrada(movs=[1], filtros=[filtro(IdInterno=ABSENT)]), seed=[aporte(1)], colaboradores=ie_ok(0), tags=["LIQ-quirk:log-sin-asignar"])
    caso("el-log-lanza-excepcion-despues-de-escribir", ["FM-LIQ-18", "FM-LIQ-20"], E(1), seed=[aporte(1)], colaboradores=mix(ie_ok(0), falla("AgregarLog", "FALLA_SIMULADA_LOG")), excepcion=True, tags=["LIQ-quirk:log-sin-asignar"])
    caso("el-log-no-se-llama-si-tesoreria-devuelve-error", ["FM-LIQ-13", "FM-LIQ-18"], E(1), seed=[aporte(1)], colaboradores=ie_ok(0, contador=1, mensajes=[msg("ERROR", "ERR_T", "x")]), tags=["LIQ-quirk:conteo-por-referencia"])
    caso("el-log-ignora-lo-que-conteste-AgregarLog", ["FM-LIQ-18"], E(1), seed=[aporte(1)], colaboradores=mix(ie_ok(0), agregar_log(3, [msg("ERROR", "ERR_LOG", "ignorado")])), tags=["LIQ-quirk:log-sin-asignar"])
    # ================================================================== FM-LIQ-08 sequences
    caso("sin-solicitudes-no-se-piden-secuencias", ["FM-LIQ-08"], E(77), seed=[aporte(1)], colaboradores=ie_ok())
    caso("con-APV-se-piden-grupos-mas-solicitudes", ["FM-LIQ-08", "FM-LIQ-10"], E(1, 2, 3), seed=[apv("S"), aporte(1), aporte(2), pers(3)], colaboradores=ie_ok(0, 1, 2, 3, 4), tags=["LIQ-quirk:carta-lista-completa"])
    caso("las-secuencias-se-consumen-aunque-despues-falle", ["FM-LIQ-08", "FM-LIQ-13"], E(1, 2), seed=[aporte(1), pers(2)], colaboradores=falla("CrearIngresoEgreso", "FALLA_DESPUES_DE_PEDIR_FOLIOS"), excepcion=True,
         tags=["LIQ-quirk:secuencias-consumidas"])
    caso("los-folios-de-carta-y-de-comprobante-van-en-la-linea-de-tesoreria", ["FM-LIQ-08", "FM-LIQ-09"], E(1, 2), seed=[aporte(1, IdPersonaAGF=7001), aporte(2, IdPersonaAGF=7002)], colaboradores=ie_ok(0, 1),
         tags=["LIQ-quirk:orden-por-hash"])

    # ---- appended after the first recording so that no number moves
    caso("el-filtro-no-trae-solicitudes-de-otra-empresa", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(TipoSolicitud="APO", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")]),
         seed=[empresa_dos(), aporte(1), aporte(2, IdEmpresa=2)], colaboradores=ie_ok(0))
    caso("el-filtro-de-una-empresa-2-no-trae-las-de-la-1", ["FM-LIQ-05"], entrada(movs=[], filtros=[filtro(TipoSolicitud="APO", FechaCertificacion="2026-03-02", FechaLiquidacion="2026-03-03")], empresa=2),
         seed=[empresa_dos(), aporte(1), aporte(2, IdEmpresa=2)], colaboradores=ie_ok(0))
    # ---- appended after the golden mutation (J18, J58 survived: the two sequences gave the same values everywhere, no client line had a medium without modality)
    secuencias_distintas = ['ALTER SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::FolioCartaLiquidacionAporteRescate" RESTART WITH 700;', 'ALTER SEQUENCE "BSCL.FondoMutuo.BD.Secuencias::SecuenciaComprobanteLiquidacion" RESTART WITH 50;']
    caso("la-carta-y-el-comprobante-vienen-de-secuencias-distintas", ["FM-LIQ-08", "FM-LIQ-09", "FM-LIQ-14"], E(1, 2), seed=[secuencias_distintas, aporte(1, IdPersonaAGF=7001), aporte(2, IdPersonaAGF=7002)],
         colaboradores=ie_ok(0, 1), tags=["LIQ-quirk:orden-por-hash"])
    caso("la-cuenta-de-inversion-usa-el-folio-de-la-carta-como-operacion", ["FM-LIQ-08", "FM-LIQ-15"], E(1), seed=[secuencias_distintas, cta_inv("S"), aporte(1, IdMedioDePago="CIN")], colaboradores=mix(ie_ok(0), ca_ok(1)),
         tags=["LIQ-quirk:cin-referencia-0"])
    caso("APV-medio-del-cliente-sin-modalidad-usa-MODCAJ", ["FM-LIQ-09", "FM-LIQ-10"], E(1), seed=[apv("S"), aporte(1, IdMedioDePago="EFE", MontoComision=10000)], colaboradores=ie_ok(0, 1),
         tags=["LIQ-quirk:carta-lista-completa"])

def total():
    return LL._n["n"]
