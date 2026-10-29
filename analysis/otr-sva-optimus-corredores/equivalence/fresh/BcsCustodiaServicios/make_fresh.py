#!/usr/bin/env python3
"""Inputs of the FRESH cases of the verify of BcsCustodiaServicios.ConsultarFlujoCustodia (not used in development). Fictitious data only.
Writes fresh/BcsCustodiaServicios/cases/<id>/{case.json,seed.sql}. Never an expected output: the legacy side is recorded by the oracle."""
import pathlib
import shutil
import sys

DEV = pathlib.Path(__file__).resolve().parents[2] / "BcsCustodiaServicios"
sys.path.insert(0, str(DEV))
import caselib  # noqa: E402

caselib.CASES = pathlib.Path(__file__).resolve().parent / "cases"
from caselib import agrupar, caso, cartera, d, entrada, filtro, p_uso, resultado, transito, valorizacion  # noqa: E402
import make_cases as mc  # noqa: E402


def ui(id_uso, tipo, items, **kw):
    return mc.uso_item(id_uso, tipo, items, **kw)


CART = [("INFORMACION_CARTERA", "CARTERA", "+")]
U = []
shutil.rmtree(caselib.CASES, ignore_errors=True)
caselib.CASES.mkdir(parents=True)

U.append(caso("F01-cartera-3-instrumentos-2-dias", ["CUS-FLU-15", "CUS-FLU-28"], "three instruments, two clients, two projected business days",
              entrada([p_uso("CUSTOD")], dias=2,
                      agrupado=[agrupar(si=("ClasificacionTipoInstrumento", "TipoInstrumento", "Instrumento", "Emisor", "Nemotecnico"))]),
              ui(30, "CUSTOD", CART) + [
                  cartera(11, INGCantidad=37.5),
                  cartera(12, cli=501, por=5002, IdInstrumento=1003, IdClasificacionTipoInstrumento="FM", IdTipoInstrumento="CFM",
                          IdMonedaInstrumento="USD", INGCantidad=410.25, IdEmisor="AGF1", Nemotecnico="CFM-1003"),
                  cartera(13, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002, IdMonedaInstrumento="CLF",
                          INGCantidad=2500, INGValorRescate=2500, Nemotecnico="BONO1002", IdEmisor="EMISOR2")]))
U.append(caso("F02-cartera-historica-decimales", ["CUS-FLU-07", "CUS-FLU-15"], "historic portfolio of 2026-02-26 with decimal quantities",
              entrada([p_uso("CUSTOD")], fecha="2026-02-26"),
              ui(30, "CUSTOD", CART) + [
                  cartera(21, historico_fecha="2026-02-26", INGCantidad=12.345678, INGFechaLiquidacion=d("2026-02-19")),
                  cartera(22, historico_fecha="2026-02-26", INGCantidad=0.5, IdInstrumento=1003, INGFechaLiquidacion=d("2026-02-20"),
                          IdMonedaInstrumento="USD")]))
U.append(caso("F03-origen-S-cartera-y-transito", ["CUS-FLU-30", "CUS-FLU-17"], "origin grouped (S): portfolio (+) and transit (-) lines",
              entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento", "ItemOrigen"))]),
              ui(30, "CUSTOD", [("INFORMACION_CARTERA", "CARTERA", "+"), ("INFORMACION_TRANSITO_INGRESO", "TRANSITO", "-")])
              + [cartera(31, INGCantidad=64), transito(32, Cantidad=21.75, IdInstrumento=1001)]))
U.append(caso("F04-garantia-1-dia-calendario", ["CUS-FLU-18", "CUS-FLU-06"], "guarantee current, one calendar day (p_DiaHabil N)",
              entrada([p_uso("CUSTOD")], dias=1, dia_habil="N"),
              ui(30, "CUSTOD", [("INFORMACION_GARANTIA", "GARANTIA", "+")]) + [cartera(41), mc.garantia(42, 41, INGCantidad=18)]))
U.append(caso("F05-uso-cliente-especifico", ["CUS-FLU-10", "CUS-FLU-29"], "use bound to client 501 / portfolio 5002, request for the same client",
              entrada([p_uso("CUSTOD", cli=501, por=5002)]),
              ui(30, "CUSTOD", CART, cli=501, por=5002) + [cartera(51, cli=501, por=5002, INGCantidad=9), cartera(52, INGCantidad=100)]))
U.append(caso("F06-valorizar-tres-lineas", ["CUS-FLU-33", "CUS-FLU-34"], "valuation of three lines, market answer for two of them",
              entrada([p_uso("CUSTOD")], valorizar="S", agrupado=[agrupar()]),
              ui(30, "CUSTOD", CART) + [
                  cartera(61, INGCantidad=15),
                  cartera(62, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002, IdMonedaInstrumento="CLF",
                          INGCantidad=300, INGValorRescate=300),
                  cartera(63, IdInstrumento=1003, IdMonedaInstrumento="USD", INGCantidad=77)],
              {"ValorizarInstrumento": {"defecto": valorizacion([resultado(1, precio="1234.5", monto_ins="18517.5", monto_sis="18517.5"),
                                                                 resultado(3, precio="12.9", monto_ins="993.3", monto_sis="900000")])}}))
U.append(caso("F07-valorizar-con-error-y-aviso", ["CUS-FLU-35", "D-CUS-10"], "valuation answers with an error and a warning: messages replaced",
              entrada([p_uso("CUSTOD")], valorizar="S"),
              ui(30, "CUSTOD", CART) + [cartera(71, INGCantidad=5)],
              {"ValorizarInstrumento": {"defecto": valorizacion([resultado(1, precio="10")], errores=1, mensajes=[
                  {"id": 4, "tipo": "ERROR", "codigo": "SIN_TASA", "descripcion": "sin tasa (ficticio)"},
                  {"id": 5, "tipo": "ADVERTENCIA", "codigo": "AVISO", "descripcion": "aviso (ficticio)"}])}}))
U.append(caso("F08-eventos-capital-SL-PB", ["CUS-FLU-22"], "capital events: lottery of bills and bond prepayment",
              entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento", "ItemOrigen"))]),
              ui(30, "CUSTOD", [("SORTEO_LETRAS", "EVENTOS", "+"), ("PREPAGO_BONO", "EVENTOS", "-")])
              + [mc.evento(81, "SL", Cantidad=44, FechaLiquidacion=d("2026-03-01")), mc.evento(82, "PB", Cantidad=6, IdInstrumento=1002)]))
U.append(caso("F09-fondos-mutuos-aporte-rescate", ["CUS-FLU-23"], "mutual funds: contribution and redemption",
              entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento", "ItemOrigen", "TipoOperacion"))]),
              ui(30, "CUSTOD", [("APORTE_FONDOS_MUTUOS", "FFMM", "+"), ("RESCATE_FONDOS_MUTUOS", "FFMM", "-")])
              + [mc.fondo(91, "APORFM", Cantidad=100.125), mc.fondo(92, "RESCFM", Cantidad=40)]))
U.append(caso("F10-proyeccion-5-dias-liquidacion-futura", ["CUS-FLU-28", "CUS-FLU-06"],
              "five projected days, liquidation inside the window: zero before it",
              entrada([p_uso("CUSTOD")], dias=5, agrupado=[agrupar(si=("Instrumento", "FechaLiquidacionInstrumento"))]),
              ui(30, "CUSTOD", CART) + [cartera(101, INGFechaLiquidacion=d("2026-03-05"), INGCantidad=88.5)]))
U.append(caso("F11-ordenes-y-asignaciones", ["CUS-FLU-21"], "orders entered and unsent assignments",
              entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento", "ItemOrigen"))]),
              ui(30, "CUSTOD", [("ORDENES_COMPRA_INGRESADAS", "ORDENES", "-"), ("ASIGNACIONES_VENTA_PENDIENTE", "ASIG", "+")])
              + [mc.orden("ingresada", 111, Cantidad=33, CantidadDisponible=29), mc.asignacion(112, CantidadAsignada=14)]))
U.append(caso("F12-dos-usos-emisor-renta-fija", ["CUS-FLU-08", "CUS-FLU-15"], "two requested uses, fixed-income filter by issuer and maturity",
              entrada([p_uso("CUSTOD"), p_uso("GARANT")], [filtro(clas="RF", emisor="EMISOR2", venc="2031-06-01")]),
              ui(30, "CUSTOD", CART) + ui(31, "GARANT", [("INFORMACION_CARTERA", "CARTERA", "-")])
              + [cartera(121, IdClasificacionTipoInstrumento="RF", IdTipoInstrumento="BON", IdInstrumento=1002, IdEmisor="EMISOR2",
                         FechaVencimientoInstrumento=d("2031-06-01"), INGCantidad=12000, INGValorRescate=12000)]))
U.append(caso("F13-dos-agrupaciones-flags-mixtos", ["CUS-FLU-31"], "two groupings, one with null flags, sharing the line instances",
              entrada([p_uso("CUSTOD")], agrupado=[agrupar(si=("Instrumento",)),
                                                   agrupar(si=("ClasificacionTipoInstrumento", "Emisor"), nulos=("Boveda", "Bolsa"))]),
              ui(30, "CUSTOD", CART) + [cartera(131, INGCantidad=3),
                                        cartera(132, IdInstrumento=1003, IdClasificacionTipoInstrumento="FM", INGCantidad=8, IdEmisor="AGF1")]))
U.append(caso("F14-solo-garantizable-INGGPR", ["CUS-FLU-25"], "specific search SOLO_GARANTIZABLE with operation INGGPR",
              entrada([p_uso("INGGPR", busq="SOLO_GARANTIZABLE", especifico="SIM")]),
              ui(30, "INGGPR", CART, busq="SOLO_GARANTIZABLE") + [cartera(141, INGCantidad=22), cartera(142, IdTipoInstrumento="BON", INGCantidad=1)]))
U.append(caso("F15-instrumento-sin-filas-linea-vacia", ["CUS-FLU-29"], "filter for an instrument with no rows: the base row yields an empty line",
              entrada([p_uso("CUSTOD")], [filtro(ins=1003)], dias=1),
              ui(30, "CUSTOD", CART, ins=0) + [cartera(151)]))
print(len(U), "fresh cases written")
