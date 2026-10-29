#!/usr/bin/env python3
"""Writes the INPUTS of the ConsultarFlujoCustodia golden cases (cases/<id>/case.json + seed.sql). It NEVER writes expected.json: the goldens
are recorded by legacy-oracle/record.sh from the unmodified legacy. Rule ids: analysis/otr-sva-optimus-corredores/RULES_CUSTODIA_FLUJO.md.
Fictitious data only. System date 2026-03-02 (Monday); 03-04 is a holiday; so with business days 03-02 +1 = 03-03, +2 = 03-05, +3 = 03-06.

  py -3 -I make_cases.py
"""
import os
import shutil
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from caselib import (AGRUPAR_CAMPOS, CASES, agrupar, caso, cartera, d, entrada, filtro, insert, item, p_uso, resultado,  # noqa: E402
                     transito, uso, valorizacion)

HIST = "2026-02-25"          # FechaSaldo of the historic cases: max projected date < system date (CUS-FLU-07)


def uso_item(id_uso, tipo, items, **kw):
    """a configured use plus its items: items = [(ItemCalculo, ItemOrigen, FuncionOperador)]"""
    return [uso(id_uso, tipo, **kw)] + [item(id_uso, a, b, c) for a, b, c in items]


def garantia(id_gar, id_cartera, his=None, **over):
    cols = dict(IdGarantia=id_gar, FolioGarantia=id_gar + 50000, IdCarteraOrigen=id_cartera, IdClasificacionTipoInstrumento="RV",
                IdTipoInstrumento="ACC", IdInstrumento=1001, IdEmisor="EMISOR1", Nemotecnico="NEMO1001", NemoSVS="SVS1001", CorteMinimo=1,
                IdMonedaInstrumento="CLP", IdTipoReajuste="CLP", FechaEmisionInstrumento=d("2020-01-01"), FechaVencimientoInstrumento=d("2030-12-31"),
                CodigoIFRS="VR", EsFungible="S", INGFechaLiquidacion=d("2026-02-21"), INGCantidad=30, INGValorRescate=0, INGPrecioGarantia=1450,
                INGTasaGarantia=0, INGMontoCompraActualMonedaInstrumento=43500, INGPrecioMercadoActual=1600, INGTasaMercadoActual=0,
                INGMontoMercadoActualMonedaInstrumento=48000, IdCliente=500, IdPortafolio=5001, IdConcepto="GAR", IdContraparteGarantia=880,
                IdEstado="VIG", IdOrigenGarantia="CUS", IdEmpresa=1)
    cols.update(over)
    if his:
        cols["FechaRespaldo"] = d(his)
        return insert("BSCL.Custodia.BD::HIS.Garantia", **cols)
    return insert("BSCL.Custodia.BD::GTA.Garantia", **cols)


def subyacente(id_sub, folio_cartera, his=None, **over):
    cols = dict(IdSubyacente=id_sub, FolioSubyacente=id_sub + 60000, FolioCarteraRelacionada=folio_cartera, IdClasificacionTipoInstrumento="RF",
                IdTipoInstrumento="BON", IdInstrumento=1002, IdEmisor="EMISOR2", Nemotecnico="BONO1002", NemoSVS="SVS1002", CorteMinimo=1000,
                IdMonedaInstrumento="CLF", IdTipoReajuste="CLF", FechaEmisionInstrumento=d("2021-06-01"), FechaVencimientoInstrumento=d("2031-06-01"),
                EsFungible="N", INGCantidad=5000, INGValorRescate=5000, INGPrecioSubyacente=101.5, INGTasaSubyacente=2.75,
                INGMontoCompraActualMonedaInstrumento=5075, INGPrecioMercadoActual=102, INGTasaMercadoActual=2.7,
                INGMontoMercadoActualMonedaInstrumento=5100, IdMonedaEmision="CLF", IdEstado="VIG")
    cols.update(over)
    if his:
        cols["FechaRespaldo"] = d(his)
        return insert("BSCL.Custodia.BD::HIS.Subyacente", **cols)
    return insert("BSCL.Custodia.BD::CUS.Subyacente", **cols)


def reserva(id_res, id_oper, folio_cartera, his=None, **over):
    cols = dict(IdReserva=id_res, IdOperacion=id_oper, FolioCarteraOrigen=folio_cartera, IdClasificacionTipoInstrumento="RV", IdTipoInstrumento="ACC",
                IdInstrumento=1001, IdEmisor="EMISOR1", CodigoIFRS="VR", EsFungible="S", IdBoveda="DCV", IdBolsa="BCS", IngCantidad=25,
                IngValorRescate=0, Precio=1550, Tasa=0, IngMontoValorizado=38750, IdConcepto="RSV", CorteMinimo=1, IdEstado="VIG")
    cols.update(over)
    if his:
        cols["FechaRespaldo"] = d(his)
        return insert("BSCL.Custodia.BD::HIS.Reserva", **cols)
    return insert("BSCL.Custodia.BD::CUS.Reserva", **cols)


def operacion(id_oper, tipo="COMPAC", **over):
    cols = dict(IdOperacion=id_oper, IdTipoOperacion=tipo, IdMesaOrigen=3, FechaCondicionLiquidacion=d("2026-02-27"),
                FechaCreacion="TIMESTAMP '2026-02-24 10:15:00'", IdCliente=500, IdClienteCorredor=300, IdPortafolio=5001, IdPortafolioCorredor=9001)
    cols.update(over)
    return insert("BSCL.Mesa.BD::MES.Operacion", **cols)


ORDEN_DEF = dict(IdEmpresa=1, IdClasificacionTipoInstrumento="RV", IdTipoInstrumento="ACC", IdInstrumento=1001, IdInstrumentoSubyacente=None,
                 IdEmisor="EMISOR1", Nemotecnico="NEMO1001", IdMonedaOrden="CLP", IdTipoReajuste="CLP", FechaEmision=d("2020-01-01"),
                 FechaVencimiento=d("2030-12-31"), EsFungible="S", IdBoveda="DCV", IdBolsa="BCS", IdTipoOperacion="COMCN",
                 FechaLiquidacion=d("2026-03-02"), Cantidad=15, ValorRescate=0, PrecioOrden=1610, MontoOrden=24150, IdCliente=500, IdPortafolio=5001,
                 IdConcepto="ORD", Accion="COMPRA")


def orden(vista, id_orden, **over):
    cols = dict(IdOrden=id_orden)
    cols.update(ORDEN_DEF)
    if vista == "ingresada":
        cols.update(CantidadDisponible=12, ValorRescateDisponible=0, MontoDisponible=19320)
    cols.update(over)
    t = {"pendiente": "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenPendienteAutorizacionFlujoCustodia",
         "ingresada": "BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenIngresadaFlujoCustodia"}[vista]
    return insert(t, **cols)


def asignacion(id_orden, **over):
    cols = dict(IdOrden=id_orden, IdEmpresa=1, IdClasificacionTipoInstrumento="RV", IdTipoInstrumento="ACC", IdInstrumento=1001,
                IdInstrumentoSubyacente=None, IdEmisor="EMISOR1", Nemotecnico="NEMO1001", IdMonedaAsignacion="CLP", IdTipoReajuste="CLP",
                FechaEmision=d("2020-01-01"), FechaVencimiento=d("2030-12-31"), EsFungible="S", IdBoveda="DCV", IdBolsa="BCS",
                IdTipoOperacion="VENCN", FechaLiquidacion=d("2026-03-02"), CantidadAsignada=8, ValorRescateAsignado=0, MontoAsignado=12800,
                IdCliente=500, IdPortafolio=5001, IdConcepto="ASI", Accion="VENTA")
    cols.update(over)
    return insert("BSCL.Orden.BD.Vistas::Flujos.ConsultarAsignacionNoEnviadaFacturarFlujoCustodia", **cols)


def movimiento(id_mov, **over):
    cols = dict(IdMovimientoComprobante=id_mov, IdEmpresa=1, IdClasificacionTipoInstrumento="RV", IdTipoInstrumento="ACC", IdInstrumento=1001,
                IdInstrumentoSubyacente=None, IdEmisor="EMISOR1", Nemotecnico="NEMO1001", NemoSVS="SVS1001", IdMoneda="CLP", IdTipoReajuste="CLP",
                FechaEmision="TIMESTAMP '2020-01-01 00:00:00'", FechaVencimiento="TIMESTAMP '2030-12-31 00:00:00'", CodigoIFRS="VR", EsFungible="S",
                IdBoveda="DCV", IdBolsa="BCS", IdTipoOperacion="COMCN", FechaLiquidacion=d("2026-03-02"), Cantidad=20, ValorRescate=0,
                Precio=1620, Tasa=0, Monto=32400, IdCliente=500, IdPortafolio=5001, IdConceptoMovimiento="FAC", AccionTipoOperacion="COMPRA")
    cols.update(over)
    return insert("BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCustodia", **cols)


def evento(id_reg, tipo="SU", **over):
    cols = dict(IdClasificacionTipoInstrumento="RV", IdTipoInstrumento="ACC", IdInstrumento=1001, NemoNuevo="NEMO1001-OSA", IdMoneda="CLP",
                IdBoveda="DCV", FechaLiquidacion=d("2026-02-28"), Cantidad=10, Precio=1000, Monto=10000, IdCliente=500, IdPortafolio=5001,
                IdRegistro=id_reg, FolioRegistro=id_reg + 1000, IdTipoEventoCapital=tipo, IdRegimenFondoMutuo=None, IdEstado="ING", IdEmpresa=1)
    cols.update(over)
    return insert("BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCustodia", **cols)


def fondo(id_reg, tipo="APORFM", **over):
    cols = dict(IdClasificacionInstrumento="FM", IdTipoInstrumento="CFM", IdInstrumento=1003, IdEmisor="AGF1", Nemotecnico="CFM-1003",
                NemoSVS="SVS1003", IdMonedaInstrumento="USD", IdTipoReajuste="USD", FechaEmisionInstrumento=d("2019-01-01"),
                FechaVencimientoInstrumento=d("2099-12-31"), CodigoIFRS="VR", EsFungible="S", IdBoveda="AGF", IdBolsa="BCS", IdTipoOperacion=tipo,
                FechaLiquidacion=d("2026-03-03"), Cantidad=250.5, ValorRescate=0, PrecioCompra=12.3456, TasaCompra=0, MontoATasaCompra=3092.57,
                IdCliente=500, IdPortafolio=5001, IdRegistro=id_reg, IdConcepto="FFM", IdRegimenTributario="APV", FechaCertificacion=d("2026-03-01"),
                IdEstado="ING", IdEmpresa=1)
    cols.update(over)
    return insert("BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCustodia", **cols)


def param_simultaneas(valor):
    return 'UPDATE "BSCL.Custodia.BD::CUS.Configuracion" SET "Valor" = \'%s\' WHERE "CodigoParametro" = \'CUS.AgregarSimultaneasEventoCapitalCli\';' % valor


def sin(e, *claves):
    return {k: v for k, v in e.items() if k not in claves}


def con(e, **cambios):
    r = dict(e)
    r.update(cambios)
    return r


def main():
    CASES.mkdir(exist_ok=True)
    for old in CASES.iterdir():          # inputs are regenerated; goldens are kept (record.sh overwrites them)
        if old.is_dir():
            for f in ("case.json", "seed.sql"):
                if (old / f).exists():
                    (old / f).unlink()
    U = []
    CART = [("INFORMACION_CARTERA", "CARTERA", "+")]

    # ================================================================ 1. entry and transport
    U.append(caso("CUS-FLU-02_uso-vacio", ["CUS-FLU-02", "CUS-FLU-01"],
                  "p_Uso empty: one ERROR message SIN_INFORMAR_USO_FLUJO_CUSTODIA (dictionary text ignored: valores null), nothing read or written",
                  entrada([]), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("D-CUS-01_uso-nulo", ["CUS-FLU-03", "D-CUS-01"], "p_Uso null: NullPointerException, nothing written",
                  entrada(None), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-05_parametro-custodia-ausente", ["CUS-FLU-05"],
                  "CUS.AgregarSimultaneasEventoCapitalCli not configured (the module has another parameter): plain Exception from ConfiguracionSistema",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", CART) + [cartera(1),
                  "UPDATE \"BSCL.Custodia.BD::CUS.Configuracion\" SET \"CodigoParametro\" = 'CUS.OtroParametro';"]))
    U.append(caso("CUS-FLU-05_modulo-custodia-sin-filas", ["CUS-FLU-05"],
                  "CUS.Configuracion has no row at all for the company: NullPointerException in ConfiguracionSistema (not the 'no encontrado' Exception)",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", CART) + [cartera(1),
                  'DELETE FROM "BSCL.Custodia.BD::CUS.Configuracion";']))
    U.append(caso("CUS-FLU-05_empresa-sin-configuracion", ["CUS-FLU-05"],
                  "p_IdEmpresa 2 has no PLA.Configuracion row: NullPointerException in ConfiguracionSistema (registros.get(..) null)",
                  con(entrada([p_uso("CUSTOD")]), p_IdEmpresa=2), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-06_dias-proyeccion-nulo", ["CUS-FLU-06", "CUS-FLU-36", "CUS-FLU-14"],
                  "p_DiasProyeccion null: NPE after the FlujoCustodia key was drawn and before the usage matrix is written",
                  con(entrada([p_uso("CUSTOD")]), p_DiasProyeccion=None), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-33_valorizar-ausente-npe", ["CUS-FLU-33", "CUS-FLU-14"],
                  "p_ValorizarFlujo absent (no default in the pojo): NPE at the very end; the usage matrix is still deleted in finally",
                  sin(entrada([p_uso("CUSTOD")]), "p_ValorizarFlujo"), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-04_pais-explicito-sin-feriados", ["CUS-FLU-04", "CUS-FLU-06", "CUS-FLU-31"],
                  "p_IdPais 77 (no holiday, no weekend) instead of the company country: 03-02 +1 +2 = 03-03, 03-04",
                  entrada([p_uso("CUSTOD")], dias=2, pais=77), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-06_dia-habil-N-calendario", ["CUS-FLU-06", "CUS-FLU-04"],
                  "p_DiaHabil N: calendar days 03-03, 03-04 (holiday kept), 03-05",
                  entrada([p_uso("CUSTOD")], dias=3, dia_habil="N"), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-06_dia-habil-defecto-S", ["CUS-FLU-06", "CUS-FLU-04", "CUS-FLU-31"],
                  "p_DiaHabil absent = S: business days of country 44 skip the 03-04 holiday and the weekend (03-03, 03-05, 03-06)",
                  entrada([p_uso("CUSTOD")], dias=3), uso_item(10, "CUSTOD", CART) + [cartera(1)]))

    # ================================================================ 3. usage matrix / similarity
    U.append(caso("CUS-FLU-09_sin-uso-configurado", ["CUS-FLU-09", "CUS-FLU-36", "CUS-FLU-14"],
                  "requested type has only an INACTIVE use and another type is active: no winner, nothing read, empty outputs, key still drawn",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", CART, estado="INA") + uso_item(11, "OTRO", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-10_similitud-gana-mayor", ["CUS-FLU-10", "CUS-FLU-11", "CUS-FLU-24"],
                  "two uses of the type: generic (+) and RV-specific (-); filter RV: the RV use (higher score) wins -> negative quantity",
                  entrada([p_uso("CUSTOD")], [filtro(clas="RV")]),
                  uso_item(10, "CUSTOD", CART) + uso_item(11, "CUSTOD", [("INFORMACION_CARTERA", "CARTERA", "-")], clas="RV") + [cartera(1)]))
    U.append(caso("CUS-FLU-11_similitud-empate-primero", ["CUS-FLU-11"],
                  "two equivalent uses (same score): the first one returned by FlujoCustodia_SelectUsoFlujoCustodia.sql wins (strict <)",
                  entrada([p_uso("CUSTOD")]),
                  uso_item(10, "CUSTOD", CART) + uso_item(11, "CUSTOD", [("INFORMACION_CARTERA", "CARTERA", "-")]) + [cartera(1)]))
    U.append(caso("CUS-FLU-10_similitud-instrumento-configurado", ["CUS-FLU-10", "CUS-FLU-11"],
                  "uses with IdInstrumento 1002 (+) and 1001 (-), filter instrument 1001: the equal one scores +2 and wins",
                  entrada([p_uso("CUSTOD")], [filtro(ins=1001)]),
                  uso_item(10, "CUSTOD", CART, ins=1002) + uso_item(11, "CUSTOD", [("INFORMACION_CARTERA", "CARTERA", "-")], ins=1001)
                  + [cartera(1)]))
    U.append(caso("D-CUS-02_npe-instrumento-configurado-nulo", ["D-CUS-02", "CUS-FLU-10"],
                  "configured use with IdInstrumento null, filter instrument 1001: ufc.IdInstrumento.equals -> NullPointerException",
                  entrada([p_uso("CUSTOD")], [filtro(ins=1001)]), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("D-CUS-02_npe-cliente-configurado-nulo", ["D-CUS-02", "CUS-FLU-10"],
                  "configured use with IdCliente null, requested client 500: ufc.IdCliente.equals -> NullPointerException",
                  entrada([p_uso("CUSTOD", cli=500)]), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-25_tipo-operacion-nulo-npe", ["CUS-FLU-25", "CUS-FLU-10"],
                  "requested UsoTipoOperacion null matching a use with a null type: u.UsoTipoOperacion.equals(\"\") -> NPE; this is why the "
                  "discard CONFIGURACION_TIPO_OPERACION_EXCLUYE_INSTRUMENTO (null type) can never be reached",
                  entrada([p_uso(None)]), uso_item(10, None, CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-08_uso-y-filtro-duplicados", ["CUS-FLU-08", "CUS-FLU-14"],
                  "the same use and the same filter twice: distinct -> one usage-matrix row, one detail row",
                  entrada([p_uso("CUSTOD"), p_uso("CUSTOD")], [filtro(clas="RV"), filtro(clas="RV")]), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-08_cruce-dos-usos-dos-filtros", ["CUS-FLU-08", "CUS-FLU-12", "CUS-FLU-14", "D-CUS-04"],
                  "2 requested uses x 2 filters (RV, RF) = 4 winners written to the matrix; current CARTERA predicates from the first winner only",
                  entrada([p_uso("CUSTOD"), p_uso("GARANT")], [filtro(clas="RV"), filtro(clas="RF")]),
                  uso_item(10, "CUSTOD", CART) + uso_item(20, "GARANT", [("INFORMACION_CARTERA", "CARTERA", "-")])
                  + [cartera(1), cartera(2, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002)]))

    # ================================================================ 4. sources: CARTERA (CUS-FLU-15) and the date switch (CUS-FLU-07)
    U.append(caso("CUS-FLU-07_cartera-fecha-maxima-igual-sistema", ["CUS-FLU-07", "CUS-FLU-15", "CUS-FLU-27", "CUS-FLU-31", "CUS-FLU-36"],
                  "max projected date = system date (03-02): CURRENT CUS.Cartera (a HIS.Cartera row with the same date is ignored)",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", CART) + [cartera(1), cartera(2, historico_fecha="2026-03-02", INGCantidad=999)]))
    U.append(caso("CUS-FLU-07_cartera-fecha-maxima-posterior", ["CUS-FLU-07", "CUS-FLU-15", "CUS-FLU-28"],
                  "max projected date 03-03 > system date: current table; the row is projected to 03-03",
                  entrada([p_uso("CUSTOD")], dias=1), uso_item(10, "CUSTOD", CART) + [cartera(1), cartera(2, historico_fecha="2026-03-03", INGCantidad=999)]))
    U.append(caso("CUS-FLU-07_cartera-historica", ["CUS-FLU-07", "CUS-FLU-15"],
                  "FechaSaldo 02-25 (max < system date): HIS.Cartera with FechaRespaldo = 02-25 (rows of other backup dates and CUS.Cartera ignored)",
                  entrada([p_uso("CUSTOD")], fecha=HIST),
                  uso_item(10, "CUSTOD", CART) + [cartera(1, INGCantidad=111), cartera(2, historico_fecha=HIST, INGFechaLiquidacion=d("2026-02-20")),
                                                   cartera(3, historico_fecha="2026-02-24", INGCantidad=333)]))
    U.append(caso("CUS-FLU-15_cartera-sin-predicados-1igual1", ["CUS-FLU-15", "D-CUS-04"],
                  "first winner without any dimension: the dynamic join becomes (1==1) (bridged to (1=1) on H2): every row x every matrix row",
                  entrada([p_uso("CUSTOD")]),
                  uso_item(10, "CUSTOD", CART) + [cartera(1), cartera(2, cli=501, por=5002, IdInstrumento=1003, IdMonedaInstrumento="USD")]))
    U.append(caso("CUS-FLU-15_cartera-emisor-rtrim-actual", ["CUS-FLU-15"],
                  "filter IdEmisor EMISOR1, row IdEmisor 'EMISOR1   ': the current query compares rtrim(CAR.IdEmisor): the row is read",
                  entrada([p_uso("CUSTOD")], [filtro(emisor="EMISOR1")]), uso_item(10, "CUSTOD", CART) + [cartera(1, IdEmisor="EMISOR1   ")]))
    U.append(caso("CUS-FLU-15_cartera-emisor-sin-rtrim-historico", ["CUS-FLU-15", "CUS-FLU-07"],
                  "same filter on the historic table: no rtrim there, the padded IdEmisor does not match",
                  entrada([p_uso("CUSTOD")], [filtro(emisor="EMISOR1")], fecha=HIST),
                  uso_item(10, "CUSTOD", CART) + [cartera(1, historico_fecha=HIST, IdEmisor="EMISOR1   "),
                                                   cartera(2, historico_fecha=HIST, IdEmisor="EMISOR1")]))
    U.append(caso("CUS-FLU-15_vencimiento-RF-instrumento", ["CUS-FLU-15"],
                  "filter RF + FechaVencimiento 2031-06-01: the current query compares the INSTRUMENT maturity",
                  entrada([p_uso("CUSTOD")], [filtro(clas="RF", venc="2031-06-01")]),
                  uso_item(10, "CUSTOD", CART)
                  + [cartera(1, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002, FechaVencimientoInstrumento=d("2031-06-01"),
                             FechaVencimientoOperacion=d("2026-06-01")),
                     cartera(2, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002, FechaVencimientoInstrumento=d("2026-06-01"),
                             FechaVencimientoOperacion=d("2031-06-01"))]))
    U.append(caso("CUS-FLU-15_vencimiento-RV-operacion", ["CUS-FLU-15"],
                  "filter RV + FechaVencimiento 2026-12-31: the current query compares the OPERATION maturity",
                  entrada([p_uso("CUSTOD")], [filtro(clas="RV", venc="2026-12-31")]),
                  uso_item(10, "CUSTOD", CART) + [cartera(1, FechaVencimientoOperacion=d("2026-12-31")),
                                                   cartera(2, FechaVencimientoInstrumento=d("2026-12-31"), FechaVencimientoOperacion=d("2027-01-31"))]))
    U.append(caso("D-CUS-04_cartera-predicados-primer-ganador", ["D-CUS-04", "CUS-FLU-15", "CUS-FLU-29"],
                  "two winners of the same use (filter instrument 1001 / filter classification RF): only matrizUso.get(0) shapes the current join",
                  entrada([p_uso("CUSTOD")], [filtro(ins=1001), filtro(clas="RF")]),
                  uso_item(10, "CUSTOD", CART, ins=0)
                  + [cartera(1), cartera(2, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002)]))
    U.append(caso("D-CUS-04_historico-todos-los-ganadores", ["D-CUS-04", "CUS-FLU-07"],
                  "same two winners on the historic table: its fixed join takes every matrix row",
                  entrada([p_uso("CUSTOD")], [filtro(ins=1001), filtro(clas="RF")], fecha=HIST),
                  uso_item(10, "CUSTOD", CART, ins=0)
                  + [cartera(1, historico_fecha=HIST),
                     cartera(2, historico_fecha=HIST, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002)]))

    # ================================================================ other sources, current and historic
    TRA = [("INFORMACION_TRANSITO_INGRESO", "TRANSITO", "+"), ("INFORMACION_TRANSITO_RETIRO", "TRANSITO", "-"),
           ("MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA", "TRANSITO", "-")]
    tra_rows = [transito(1), transito(2, IdTipoOperacion="RETTIT", Cantidad=10), transito(3, IdTipoOperacion="RETTIT", Cantidad=5, EsVentaDescubierta="S")]
    U.append(caso("CUS-FLU-17_transito-actual", ["CUS-FLU-17", "CUS-FLU-24"],
                  "TRANSITO current: ingreso (+), retiro (-), retiro of an uncovered sale -> MOV_TRANSITO_RETIRO_VENTA_DESCUBIERTA",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento", "ItemOrigen", "TipoOperacion"))]),
                  uso_item(10, "CUSTOD", TRA) + tra_rows))
    U.append(caso("CUS-FLU-17_transito-historico", ["CUS-FLU-17", "CUS-FLU-07"],
                  "TRANSITO historic: HIS.MovimientoCarteraEnTransito joined to its current row (EsVentaDescubierta of the current one)",
                  entrada([p_uso("CUSTOD")], fecha=HIST),
                  uso_item(10, "CUSTOD", TRA)
                  + [transito(1, historico_fecha=HIST, FechaLiquidacion=d("2026-02-24")),
                     transito(2, historico_fecha="2026-02-20", IdTipoOperacion="RETTIT", FechaLiquidacion=d("2026-02-19"))]))
    CTR = [("CARTERA_TRANSITO_COMPRA", "CARTERA", "+")]
    ctr_rows = [cartera(1, INGFechaLiquidacion=d("2026-03-05")), cartera(2, IdTipoOperacion="VENCN", INGFechaLiquidacion=d("2026-03-05"), INGCantidad=7)]
    U.append(caso("CUS-FLU-16_cartera-transito-actual", ["CUS-FLU-16", "CUS-FLU-25"],
                  "CARTERA_TRANSITO current (liquidation after the max date 03-02): COMPRA row kept, VENTA row has no operator -> discarded",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", CTR) + ctr_rows))
    U.append(caso("CUS-FLU-16_cartera-transito-historico", ["CUS-FLU-16", "CUS-FLU-07"],
                  "CARTERA_TRANSITO historic: HIS.Cartera rows of FechaRespaldo 02-25 liquidating after it",
                  entrada([p_uso("CUSTOD")], fecha=HIST),
                  uso_item(10, "CUSTOD", CTR) + [cartera(1, historico_fecha=HIST, INGFechaLiquidacion=d("2026-02-27")),
                                                  cartera(2, INGFechaLiquidacion=d("2026-02-27"), INGCantidad=55)]))
    GAR = [("INFORMACION_GARANTIA", "GARANTIA", "+")]
    U.append(caso("CUS-FLU-18_garantia-actual", ["CUS-FLU-18"], "GARANTIA current: GTA.Garantia joined to its origin CUS.Cartera row",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", GAR) + [cartera(1), garantia(1, 1), garantia(2, 1, IdOrigenGarantia="EXT")]))
    U.append(caso("CUS-FLU-18_garantia-historica", ["CUS-FLU-18", "CUS-FLU-07"], "GARANTIA historic: HIS.Garantia + HIS.Cartera of the same backup date",
                  entrada([p_uso("CUSTOD")], fecha=HIST),
                  uso_item(10, "CUSTOD", GAR) + [cartera(1, historico_fecha=HIST), garantia(1, 1, his=HIST), garantia(2, 1)]))
    SUB = [("VENTA_PACTO", "SUBYACENTE", "-"), ("SIMULTANEA_COMPRA", "SUBYACENTE", "+")]
    U.append(caso("CUS-FLU-19_subyacente-actual", ["CUS-FLU-19", "CUS-FLU-05"],
                  "SUBYACENTE current (CUS.AgregarSimultaneasEventoCapitalCli = S): VENPAC underlying -> VENTA_PACTO (liquidation = operation date)",
                  entrada([p_uso("CUSTOD")]),
                  uso_item(10, "CUSTOD", SUB) + [cartera(1, IdTipoOperacion="VENPAC", IdTipoInstrumento="PAC"), subyacente(1, 100001)]))
    U.append(caso("CUS-FLU-19_subyacente-historico", ["CUS-FLU-19", "CUS-FLU-07"], "SUBYACENTE historic: HIS.Subyacente + HIS.Cartera",
                  entrada([p_uso("CUSTOD")], fecha=HIST),
                  uso_item(10, "CUSTOD", SUB) + [cartera(1, historico_fecha=HIST, IdTipoOperacion="VENPAC", IdTipoInstrumento="PAC"),
                                                  subyacente(1, 100001, his=HIST)]))
    U.append(caso("D-CUS-04_subyacente-lista-evento-capital", ["D-CUS-04", "CUS-FLU-19"],
                  "first winner BusquedaEspecifica FLUJO_CLIENTES_DERECHO_CAPITAL: COMSIM is in the operation list -> SIMULTANEA_COMPRA read",
                  entrada([p_uso("FLEVCP", busq="FLUJO_CLIENTES_DERECHO_CAPITAL")]),
                  uso_item(10, "FLEVCP", SUB, busq="FLUJO_CLIENTES_DERECHO_CAPITAL")
                  + [cartera(1, IdTipoOperacion="COMSIM", IdTipoInstrumento="SIM", FechaVencimientoOperacion=d("2026-04-30")), subyacente(1, 100001)]))
    U.append(caso("D-CUS-04_subyacente-lista-normal", ["D-CUS-04", "CUS-FLU-19"],
                  "other BusquedaEspecifica: COMSIM is NOT in the default list -> nothing read",
                  entrada([p_uso("CUSTOD")]),
                  uso_item(10, "CUSTOD", SUB)
                  + [cartera(1, IdTipoOperacion="COMSIM", IdTipoInstrumento="SIM", FechaVencimientoOperacion=d("2026-04-30")), subyacente(1, 100001)]))
    RSV = [("RESERVA_COMPRA_PACTOS", "RESERVA", "-"), ("RESERVA_VENTA_TERMINO", "RESERVA", "-")]
    rsv_rows = [cartera(1), operacion(1, "COMPAC"), reserva(1, 1, 100001), operacion(2, "VENTER"), reserva(2, 2, 100001, IngCantidad=4)]
    U.append(caso("CUS-FLU-20_cartera-reservada-actual", ["CUS-FLU-20"],
                  "CARTERA_RESERVADA current: COMPAC (liquidation = creation date, client of the operation) and VENTER (condition date, broker client)",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento", "ItemOrigen", "TipoOperacion"))]), uso_item(10, "CUSTOD", RSV) + rsv_rows))
    U.append(caso("CUS-FLU-20_cartera-reservada-historica", ["CUS-FLU-20", "CUS-FLU-07"],
                  "CARTERA_RESERVADA historic: HIS.Reserva + HIS.Cartera; the client is always the operation's (no broker switch)",
                  entrada([p_uso("CUSTOD")], fecha=HIST),
                  uso_item(10, "CUSTOD", RSV) + [cartera(1, historico_fecha=HIST), operacion(1, "COMPAC"), reserva(1, 1, 100001, his=HIST),
                                                  operacion(2, "VENTER", FechaCondicionLiquidacion=d("2026-02-23")), reserva(2, 2, 100001, his=HIST)]))
    U.append(caso("D-CUS-03_subyacente-cae-en-reservada", ["D-CUS-03", "CUS-FLU-19", "CUS-FLU-20", "CUS-FLU-05"],
                  "CUS.AgregarSimultaneasEventoCapitalCli = N and only SUBYACENTE items: the switch falls through and reads the reserved portfolio "
                  "(its rows have no operator -> discarded); the underlying itself is not read",
                  entrada([p_uso("CUSTOD")]),
                  uso_item(10, "CUSTOD", SUB) + rsv_rows + [cartera(2, IdTipoOperacion="VENPAC", IdTipoInstrumento="PAC"), subyacente(1, 100002),
                                                            param_simultaneas("N")]))
    U.append(caso("D-CUS-03_reservada-leida-dos-veces", ["D-CUS-03", "CUS-FLU-20", "CUS-FLU-31"],
                  "parameter N with a SUBYACENTE item AND a reserve item: the reserved portfolio is read twice (fall-through + its own case)",
                  entrada([p_uso("CUSTOD")]),
                  uso_item(10, "CUSTOD", SUB + [("RESERVA_COMPRA_PACTOS", "RESERVA", "-")]) + [cartera(1), operacion(1, "COMPAC"),
                                                                                               reserva(1, 1, 100001), param_simultaneas("N")]))
    U.append(caso("CUS-FLU-21_ordenes-por-autorizar", ["CUS-FLU-21"], "ORDENES_POR_AUTORIZAR: ORDENES_COMPRA_PENDIENTES; a later liquidation is not read",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", [("ORDENES_COMPRA_PENDIENTES", "ORDENES", "+")])
                  + [orden("pendiente", 1), orden("pendiente", 2, FechaLiquidacion=d("2026-03-09"))]))
    U.append(caso("CUS-FLU-21_ordenes-ingresadas", ["CUS-FLU-21", "CUS-FLU-24"],
                  "ORDENES_INGRESADAS: available quantities; a VENTA liquidates at FechaMinima whatever its own date; CantidadDisponible 0 skipped",
                  entrada([p_uso("CUSTOD")], dias=1),
                  uso_item(10, "CUSTOD", [("ORDENES_COMPRA_INGRESADAS", "ORDENES", "+"), ("ORDENES_VENTA_INGRESADAS", "ORDENES", "-")])
                  + [orden("ingresada", 1), orden("ingresada", 2, Accion="VENTA", IdTipoOperacion="VENCN", FechaLiquidacion=d("2026-03-20")),
                     orden("ingresada", 3, CantidadDisponible=0)]))
    U.append(caso("CUS-FLU-21_ordenes-reporte-por-autorizar", ["CUS-FLU-21"],
                  "ORDENES_REPORTE_POR_AUTORIZAR: RE/RRV orders, instrument = underlying, currency of INS.Instrumento",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento",))]),
                  uso_item(10, "CUSTOD", [("ORDENES_REPORTE_COMPRA_PENDIENTES", "ORDENES", "+")])
                  + [orden("pendiente", 1, IdClasificacionTipoInstrumento="RE", IdTipoInstrumento="RRV", IdInstrumento=4001, IdInstrumentoSubyacente=1002),
                     orden("pendiente", 2)]))
    U.append(caso("CUS-FLU-21_ordenes-reporte-ingresadas", ["CUS-FLU-21"], "ORDENES_REPORTE_INGRESADAS: RE/RRF orders with availability",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento",))]),
                  uso_item(10, "CUSTOD", [("ORDENES_REPORTE_VENTA_INGRESADAS", "ORDENES", "-")])
                  + [orden("ingresada", 1, IdClasificacionTipoInstrumento="RE", IdTipoInstrumento="RRF", IdInstrumento=4002, IdInstrumentoSubyacente=1002,
                           Accion="VENTA")]))
    U.append(caso("CUS-FLU-21_asignaciones-no-enviadas", ["CUS-FLU-21"],
                  "ASIGNACIONES_NO_ENVIAS_A_FACTURAR: VENTA assignment; a SIM assignment takes its underlying and maturity 0001-01-01",
                  entrada([p_uso("CUSTOD")]),
                  uso_item(10, "CUSTOD", [("ASIGNACIONES_VENTA_PENDIENTE", "ASIGNACIONES", "-"), ("ASIGNACIONES_COMPRA_PENDIENTE", "ASIGNACIONES", "+")])
                  + [asignacion(1), asignacion(2, IdTipoInstrumento="SIM", IdInstrumento=5001, IdInstrumentoSubyacente=1001, Accion="COMPRA",
                                               IdTipoOperacion="COMSIM")]))
    U.append(caso("CUS-FLU-21_asignaciones-reporte", ["CUS-FLU-21"], "ASIGNACIONES_REPORTE_NO_ENVIAS_A_FACTURAR: RE/RRF assignment",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento",))]),
                  uso_item(10, "CUSTOD", [("ASIGNACIONES_REPORTE_VENTA_PENDIENTE", "ASIGNACIONES", "-")])
                  + [asignacion(1, IdClasificacionTipoInstrumento="RE", IdTipoInstrumento="RRF", IdInstrumento=4003, IdInstrumentoSubyacente=1002)]))
    U.append(caso("CUS-FLU-21_movimientos-facturacion", ["CUS-FLU-21", "CUS-FLU-25"],
                  "MOVIMIENTOS_FACTURACION: COMPRA movement; a movement without action -> item MOVIMIENTOS__FACTURACION (no operator, discarded)",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", [("MOVIMIENTOS_COMPRA_FACTURACION", "FACTURACION", "+")])
                  + [movimiento(1), movimiento(2, AccionTipoOperacion=None)]))
    U.append(caso("CUS-FLU-21_movimientos-reporte-facturacion", ["CUS-FLU-21"], "MOVIMIENTOS_REPORTE_FACTURACION: RE/RRV movement",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento",))]),
                  uso_item(10, "CUSTOD", [("MOVIMIENTOS_REPORTE_COMPRA_FACTURACION", "FACTURACION", "+")])
                  + [movimiento(1, IdClasificacionTipoInstrumento="RE", IdTipoInstrumento="RRV", IdInstrumento=4004, IdInstrumentoSubyacente=1002)]))
    U.append(caso("CUS-FLU-22_eventos-de-capital", ["CUS-FLU-22", "CUS-FLU-29"],
                  "EVENTOS_DE_CAPITAL SU/SL/PB: ItemOrigen forced to the item name; the SL item is configured with another origin -> its row is in the "
                  "detail but never grouped (empty totalized line)",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento", "ItemOrigen"))]),
                  uso_item(10, "CUSTOD", [("SUBSCRIPCIONES", "SUBSCRIPCIONES", "+"), ("SORTEO_LETRAS", "EVENTOS", "-")])
                  + [evento(1, "SU"), evento(2, "SL"), evento(3, "PB"), evento(4, "SU", IdEstado="ANU")]))
    U.append(caso("CUS-FLU-23_fondos-mutuos", ["CUS-FLU-23"],
                  "FONDOS_MUTUOS: APORFM (own liquidation) and RESCFM (liquidation = FechaMinima); ItemOrigen forced to FONDOSMUTUOS",
                  entrada([p_uso("CUSTOD")], dias=1, agrupado=[agrupar(si=("Instrumento", "IdRegimenFondoMutuo", "GrupoRegimen"))]),
                  uso_item(10, "CUSTOD", [("APORTE_FONDOS_MUTUOS", "FONDOSMUTUOS", "+"), ("RESCATE_FONDOS_MUTUOS", "FONDOSMUTUOS", "-")])
                  + [fondo(1), fondo(2, "RESCFM", FechaLiquidacion=d("2026-03-10"), Cantidad=40), fondo(3, IdEstado="CUR")]))
    U.append(caso("CUS-FLU-21_fuentes-sin-historico-con-fecha-pasada", ["CUS-FLU-21", "CUS-FLU-07", "CUS-FLU-22", "CUS-FLU-23"],
                  "max date 02-25 < system date: orders, assignments, movements, capital events and funds have no historic variant (current views)",
                  entrada([p_uso("CUSTOD")], fecha=HIST, agrupado=[agrupar(si=("Instrumento", "ItemOrigen"))]),
                  uso_item(10, "CUSTOD", [("ORDENES_COMPRA_PENDIENTES", "ORDENES", "+"), ("ASIGNACIONES_VENTA_PENDIENTE", "ASIGNACIONES", "-"),
                                          ("MOVIMIENTOS_COMPRA_FACTURACION", "FACTURACION", "+"), ("SUBSCRIPCIONES", "SUBSCRIPCIONES", "+"),
                                          ("APORTE_FONDOS_MUTUOS", "FONDOSMUTUOS", "+")])
                  + [orden("pendiente", 1, FechaLiquidacion=d("2026-02-24")), asignacion(1, FechaLiquidacion=d("2026-02-24")),
                     movimiento(1, FechaLiquidacion=d("2026-02-24")), evento(1, "SU", FechaLiquidacion=d("2026-02-24")),
                     fondo(1, FechaLiquidacion=d("2026-02-24"))]))
    U.append(caso("D-CUS-07_item-rpa-nunca-leido", ["D-CUS-07", "CUS-FLU-13"],
                  "item CARTERA_TRANSITO_VENTA_RPA maps to CARTERA_VENTA_TRANSITO_RPA, which has no switch case: nothing is read",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", [("CARTERA_TRANSITO_VENTA_RPA", "CARTERA", "+")])
                  + [cartera(1, IdTipoOperacion="VENCN", INGFechaLiquidacion=d("2026-03-05"))]))
    U.append(caso("CUS-FLU-13_item-fuera-del-mapa", ["CUS-FLU-13", "CUS-FLU-30"],
                  "an item outside the 42-item map reads nothing: one empty totalized line for it",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", [("ITEM_INVENTADO", "OTRO", "+")]) + [cartera(1)]))

    # ================================================================ 5. discard reasons (CUS-FLU-25/26)
    U.append(caso("CUS-FLU-25_cartera-comprometida", ["CUS-FLU-25"], "TipoCarteraCorredor CC -> SE_EXCLUYE_CARTERA_COMPROMETIDA (not in the detail)",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", CART) + [cartera(1), cartera(2, TipoCarteraCorredor="CC", INGCantidad=70)]))
    U.append(caso("CUS-FLU-25_solo-garantizable", ["CUS-FLU-25"],
                  "SOLO_GARANTIZABLE, especifico SIM: ACC#SIM is a guarantee pair (kept); BON#SIM is not -> CONFIGURACION_GARANTIA_EXCLUYE_INSTRUMENTO",
                  entrada([p_uso("GARANT", busq="SOLO_GARANTIZABLE", especifico="SIM")]),
                  uso_item(10, "GARANT", CART, busq="SOLO_GARANTIZABLE")
                  + [cartera(1), cartera(2, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002)]))
    U.append(caso("CUS-FLU-25_solo-garantizable-INGGPR", ["CUS-FLU-25"], "same rows with UsoTipoOperacion INGGPR: the guarantee discard does not apply",
                  entrada([p_uso("INGGPR", busq="SOLO_GARANTIZABLE", especifico="SIM")]),
                  uso_item(10, "INGGPR", CART, busq="SOLO_GARANTIZABLE")
                  + [cartera(1), cartera(2, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002)]))
    U.append(caso("CUS-FLU-26_solo-subyacente", ["CUS-FLU-26"],
                  "SOLO_SUBYACENTE, especifico SIM: SIM#ACC#1001 is an underlying (kept); 1002 is not -> INSTRUMENTO_NO_SUBYACENTE",
                  entrada([p_uso("CUSTOD", busq="SOLO_SUBYACENTE", especifico="SIM")]),
                  uso_item(10, "CUSTOD", CART, busq="SOLO_SUBYACENTE") + [cartera(1), cartera(2, IdInstrumento=1002)]))
    U.append(caso("CUS-FLU-26_solo-subyacente-sin-coincidencias", ["CUS-FLU-26"],
                  "SOLO_SUBYACENTE without especifico: no underlying found at all -> nothing is discarded",
                  entrada([p_uso("CUSTOD", busq="SOLO_SUBYACENTE")]),
                  uso_item(10, "CUSTOD", CART, busq="SOLO_SUBYACENTE") + [cartera(1), cartera(2, IdInstrumento=1002)]))
    U.append(caso("CUS-FLU-24_portafolio-cuenta-propia", ["CUS-FLU-24", "CUS-FLU-27"],
                  "own-account portfolio 9001: EsCuentaPropia = S is set on the row but no output field carries it (unobservable)",
                  entrada([p_uso("CUSTOD")]), uso_item(10, "CUSTOD", CART) + [cartera(1, cli=300, por=9001), cartera(2)]))

    # ================================================================ projection (CUS-FLU-28)
    U.append(caso("CUS-FLU-28_proyeccion-3-dias", ["CUS-FLU-28", "CUS-FLU-24", "CUS-FLU-31", "CUS-FLU-36"],
                  "3 projected days: a row liquidated before FechaSaldo (all dates carry it) and a row liquidating 03-05 (zero before)",
                  entrada([p_uso("CUSTOD")], dias=3, agrupado=[agrupar(si=("Instrumento", "FechaLiquidacionInstrumento"))]),
                  uso_item(10, "CUSTOD", CART) + [cartera(1), cartera(2, INGFechaLiquidacion=d("2026-03-05"), INGCantidad=60)]))
    U.append(caso("CUS-FLU-28_proyeccion-1-dia-signo-menos", ["CUS-FLU-28", "CUS-FLU-24"],
                  "1 projected day, operator '-': quantity and rescue value negated on the row and its projection",
                  entrada([p_uso("CUSTOD")], dias=1),
                  uso_item(10, "CUSTOD", [("INFORMACION_CARTERA", "CARTERA", "-")])
                  + [cartera(1, IdClasificacionTipoInstrumento="IF", IdTipoInstrumento="DPF", IdInstrumento=2001, INGValorRescate=98765.4321)]))

    # ================================================================ grouping (CUS-FLU-29..32)
    U.append(caso("CUS-FLU-32_sin-agrupacion", ["CUS-FLU-32", "CUS-FLU-27"], "p_EsAgrupadoPor empty: totalized output empty, detail still filled",
                  entrada([p_uso("CUSTOD")], agrupado=[]), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-32_agrupacion-nula", ["CUS-FLU-32"], "p_EsAgrupadoPor null: same as empty",
                  con(entrada([p_uso("CUSTOD")]), p_EsAgrupadoPor=None), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-31_agrupar-todo-S", ["CUS-FLU-31"], "every grouping flag S: one line per row (folio, mesa, bolsa, dates...)",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=tuple(AGRUPAR_CAMPOS))]),
                  uso_item(10, "CUSTOD", CART) + [cartera(1), cartera(2, INGCantidad=5, IdMesa=8)]))
    U.append(caso("CUS-FLU-31_agrupar-todo-N-suma", ["CUS-FLU-31"], "every flag N: rows of two instruments summed in one line per date",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=())]),
                  uso_item(10, "CUSTOD", CART) + [cartera(1), cartera(2, IdInstrumento=1003, INGCantidad=5.5, CorteMinimo=0)]))
    U.append(caso("CUS-FLU-31_agrupar-flags-nulos", ["CUS-FLU-31"], "null flags behave as N",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento",), nulos=("ClasificacionTipoInstrumento", "TipoInstrumento", "ItemOrigen"))]),
                  uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-31_dos-agrupaciones-instancia-compartida", ["CUS-FLU-31"],
                  "two p_EsAgrupadoPor entries (instrument S / instrument N): the legacy reuses ONE line object per row across entries, so both keys "
                  "show the values of the LAST entry",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento", "Nemotecnico")), agrupar(si=())]),
                  uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    U.append(caso("CUS-FLU-31_agrupacion-duplicada-suma-doble", ["CUS-FLU-31"],
                  "the same p_EsAgrupadoPor entry twice: same key twice -> the same line object counted twice (quantity doubled)",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(), agrupar()]), uso_item(10, "CUSTOD", CART) + [cartera(1)]))
    two_items_same_origin = [("INFORMACION_CARTERA", "CARTERA", "+"), ("CARTERA_TRANSITO_COMPRA", "CARTERA", "+")]
    two_items_other_origin = [("INFORMACION_CARTERA", "CARTERA", "+"), ("CARTERA_TRANSITO_COMPRA", "TRANSITO", "+")]
    U.append(caso("CUS-FLU-30_considerar-origen-S-sin-registros", ["CUS-FLU-30"],
                  "ItemOrigen S, no row in the origin (SItemOrigen = 0): empty lines are considered (S)",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("ItemOrigen",))]), uso_item(10, "CUSTOD", two_items_same_origin)))
    U.append(caso("CUS-FLU-30_considerar-origen-S-item-vacio", ["CUS-FLU-30"],
                  "ItemOrigen S, rows only for one of two items of the same origin: the empty item line is dropped (N)",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("ItemOrigen",))]), uso_item(10, "CUSTOD", two_items_same_origin) + [cartera(1)]))
    U.append(caso("CUS-FLU-30_considerar-origen-N-sreg", ["CUS-FLU-30"],
                  "ItemOrigen N, rows for one item, the other item (other origin) empty: SREG > 0 and SItemCalculo = 0 -> dropped (N)",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=())]), uso_item(10, "CUSTOD", two_items_other_origin) + [cartera(1)]))
    U.append(caso("CUS-FLU-30_considerar-origen-N-sin-registros", ["CUS-FLU-30"],
                  "ItemOrigen N and no row at all (SREG = 0): the empty lines are kept (S)",
                  entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=())]), uso_item(10, "CUSTOD", two_items_other_origin)))
    U.append(caso("CUS-FLU-29_dimension-cliente-de-la-base", ["CUS-FLU-29", "CUS-FLU-10"],
                  "requested client 500 (use configured for client 500): only that client's rows group; IdCliente of the line from the base row",
                  entrada([p_uso("CUSTOD", cli=500)]), uso_item(10, "CUSTOD", CART, cli=500) + [cartera(1), cartera(2, cli=501, por=5002)]))

    # ================================================================ 6. valuation (CUS-FLU-33..35)
    VAL_ROWS = [cartera(1), cartera(2, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002, INGCantidad=2000,
                                       INGValorRescate=2050.5, INGTasaMercadoActual=2.5, IdMonedaInstrumento="CLF")]
    U.append(caso("CUS-FLU-34_valorizar-merc-y-no-merc", ["CUS-FLU-33", "CUS-FLU-34"],
                  "valuation S: the MERC result overwrites price/rate/amounts of its line; a COMP result is ignored",
                  entrada([p_uso("CUSTOD")], valorizar="S"), uso_item(10, "CUSTOD", CART) + VAL_ROWS,
                  {"ValorizarInstrumento": {"defecto": valorizacion([resultado(1), resultado(2, dimension="COMP", precio="9", tasa="9", monto_ins="9",
                                                                                                monto_sis="9")])}}))
    U.append(caso("CUS-FLU-35_valorizar-con-errores", ["CUS-FLU-35", "D-CUS-10"],
                  "valuation with errors: messages REPLACED by the valuation's; call recomputes the counter (1 ERROR message -> 1)",
                  entrada([p_uso("CUSTOD")], valorizar="S"), uso_item(10, "CUSTOD", CART) + VAL_ROWS,
                  {"ValorizarInstrumento": {"defecto": valorizacion([resultado(1)], errores=1, mensajes=[
                      {"id": 2, "tipo": "ERROR", "codigo": "SIN_PRECIO", "descripcion": "Instrumento sin precio (ficticio)"}])}}))
    U.append(caso("CUS-FLU-35_valorizar-errores-solo-advertencias", ["CUS-FLU-35", "D-CUS-10"],
                  "valuation counter 1 but only ADVERTENCIA messages: they replace the messages and call recomputes the counter to 0",
                  entrada([p_uso("CUSTOD")], valorizar="S"), uso_item(10, "CUSTOD", CART) + VAL_ROWS,
                  {"ValorizarInstrumento": {"defecto": valorizacion([], errores=1, mensajes=[
                      {"id": 1, "tipo": "ADVERTENCIA", "codigo": "FUENTE_ALTERNATIVA", "descripcion": "Fuente ficticia"}])}}))
    U.append(caso("CUS-FLU-33_valorizar-excluye-cero-e-IF-sin-rescate", ["CUS-FLU-33", "CUS-FLU-28"],
                  "not valued: the zero line before a future liquidation (Cantidad 0 scale 0) and an IF line with rescue value 0",
                  entrada([p_uso("CUSTOD")], dias=1, valorizar="S"),
                  uso_item(10, "CUSTOD", CART) + [cartera(1, INGFechaLiquidacion=d("2026-03-03")),
                                                   cartera(2, IdClasificacionTipoInstrumento="IF", IdTipoInstrumento="DPF", IdInstrumento=2001)],
                  {"ValorizarInstrumento": {"defecto": valorizacion([resultado(1)])}}))
    U.append(caso("CUS-FLU-33_valorizar-cantidad-cero-con-escala", ["CUS-FLU-33"],
                  "+100 and -100 of the same instrument: the sum is 0E-8 (scale 8), BigDecimal.equals(ZERO) is false -> the line IS valued",
                  entrada([p_uso("CUSTOD")], valorizar="S"),
                  uso_item(10, "CUSTOD", [("INFORMACION_CARTERA", "CARTERA", "+"), ("CARTERA_TRANSITO_COMPRA", "CARTERA", "-")])
                  + [cartera(1), cartera(2, INGFechaLiquidacion=d("2026-03-05"))],
                  {"ValorizarInstrumento": {"defecto": valorizacion([])}}))
    U.append(caso("CUS-FLU-33_valorizar-nada-que-valorizar", ["CUS-FLU-33"], "valuation S but no line with quantity: ValorizarInstrumento is not called",
                  entrada([p_uso("CUSTOD")], valorizar="S"), uso_item(10, "CUSTOD", CART)))
    U.append(caso("CUS-FLU-14_valorizar-lanza-excepcion", ["CUS-FLU-14", "CUS-FLU-33"],
                  "the valuation collaborator throws: the call fails with that Exception and the usage matrix is deleted in finally",
                  entrada([p_uso("CUSTOD")], valorizar="S"), uso_item(10, "CUSTOD", CART) + [cartera(1)],
                  {"ValorizarInstrumento": {"respuestas": [{"error": "valorizador no disponible (guion)"}]}}))
    U.append(caso("CUS-FLU-33_valorizar-sin-instrumento-agrupado-npe", ["CUS-FLU-33", "CUS-FLU-31"],
                  "valuation S with Instrumento N grouping: line IdInstrumento 0, instrumentos.get(0) is null -> NullPointerException",
                  entrada([p_uso("CUSTOD")], valorizar="S", agrupado=[agrupar(si=())]), uso_item(10, "CUSTOD", CART) + [cartera(1)],
                  {"ValorizarInstrumento": {"defecto": valorizacion([])}}))
    U.append(caso("CUS-FLU-33_valorizar-multiuso", ["CUS-FLU-33", "CUS-FLU-08", "CUS-FLU-34"],
                  "two requested uses (UsoValorizacion VALCUS / VALGAR): each line takes the source table of its own use",
                  entrada([p_uso("CUSTOD"), p_uso("GARANT")], valorizar="S", agrupado=[agrupar()]),
                  uso_item(10, "CUSTOD", CART) + uso_item(20, "GARANT", GAR, valorizacion="VALGAR") + [cartera(1), garantia(1, 1)],
                  {"ValorizarInstrumento": {"defecto": valorizacion([resultado(1), resultado(2, precio="1700")])}}))

    print(len(U), "cases written")
    for old in CASES.iterdir():
        if old.is_dir() and old.name not in U:
            shutil.rmtree(old)
            print("removed stale case", old.name)


if __name__ == "__main__":
    main()
