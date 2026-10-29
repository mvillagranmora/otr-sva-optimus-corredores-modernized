"""Helpers to write the INPUTS of a ConsultarFlujoCustodia golden case: cases/<id>/case.json (request + collaborator script) and seed.sql.
The expected output (expected.json) is NEVER written here: only legacy-oracle/record.sh writes it, from the unmodified legacy.
All data is fictitious (no row copied from any real system; no credential)."""
import json
import pathlib

HERE = pathlib.Path(__file__).resolve().parent
CASES = HERE / "cases"

T_USO = '"BSCL.Custodia.BD::CUS.UsoFlujoCustodia"'
T_ITEM = '"BSCL.Custodia.BD::CUS.ItemsFlujoCustodia"'


def q(v):
    if v is None:
        return "NULL"
    if isinstance(v, bool):
        return "'S'" if v else "'N'"
    if isinstance(v, (int, float)):
        return str(v)
    s = str(v)
    if s.startswith("DATE '") or s.startswith("TIMESTAMP '"):
        return s
    return "'" + s.replace("'", "''") + "'"


def d(s):
    return "DATE '%s'" % s


def insert(table, **cols):
    names = ", ".join('"%s"' % k for k in cols)
    vals = ", ".join(q(v) for v in cols.values())
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (table, names, vals)


def uso(id_uso, tipo, clas=None, tipo_ins=None, ins=None, cli=None, por=None, busq=None, valorizacion="VALCUS", estado="ACT", empresa=1):
    return insert("BSCL.Custodia.BD::CUS.UsoFlujoCustodia", IdUsoFlujoCustodia=id_uso, IdUsuario=1, UsoTipoOperacion=tipo,
                  IdClasificacionTipoInstrumento=clas, IdTipoInstrumento=tipo_ins, IdInstrumento=ins, IdCliente=cli, IdPortafolio=por,
                  BusquedaEspecifica=busq, FechaIngresoRegistro="TIMESTAMP '2026-01-01 00:00:00'", UsoValorizacion=valorizacion, IdEstado=estado,
                  IdEmpresa=empresa)


def item(id_uso, item_calculo, origen, funcion="+", estado="ACT"):
    return insert("BSCL.Custodia.BD::CUS.ItemsFlujoCustodia", IdUsoFlujoCustodia=id_uso, ItemCalculo=item_calculo, ItemOrigen=origen,
                  Descripcion="Item ficticio", FuncionOperador=funcion, IdEstado=estado)


CARTERA_DEF = dict(IdEstado="VIG", IdEmpresa=1, IdConcepto="CUS", IdTipoOperacion="COMCN", IdMesa=7, IdBolsa="BCS", TipoCarteraCorredor="CL",
                   IdClasificacionTipoInstrumento="RV", IdTipoInstrumento="ACC", IdInstrumento=1001, Nemotecnico="NEMO1001", IdEmisor="EMISOR1",
                   NemoSVS="SVS1001", CorteMinimo=1, IdMonedaInstrumento="CLP", IdMonedaEmision="CLP", IdTipoReajuste="CLP",
                   FechaEmisionInstrumento=d("2020-01-01"), FechaVencimientoInstrumento=d("2030-12-31"), FechaVencimientoOperacion=d("2026-12-31"),
                   CodigoIFRS="VR", EsFungible="S", IdBoveda="DCV", INGFechaLiquidacion=d("2026-02-20"), INGCantidad=100, INGValorRescate=0,
                   INGPrecioCompra=1500, INGTasaCompra=0, INGMontoCompraActualMonedaInstrumento=150000, INGPrecioMercadoActual=1600,
                   INGTasaMercadoActual=0, INGMontoMercadoActualMonedaInstrumento=160000, IdRegimenFondoMutuo=None, FechaOrigenCompra=d("2026-02-20"),
                   FechaOperacion=d("2026-02-18"), FolioComprobante=0, FolioExternoReferencia="0", IdTipoComprobante="CMP")


def cartera(id_cartera, cli=500, por=5001, historico_fecha=None, **over):
    cols = dict(IdCartera=id_cartera, FolioCartera=id_cartera + 100000, IdCliente=cli, IdPortafolio=por)
    cols.update(CARTERA_DEF)
    cols.update(over)
    table = "BSCL.Custodia.BD::CUS.Cartera"
    if historico_fecha:
        table = "BSCL.Custodia.BD::HIS.Cartera"
        cols["FechaRespaldo"] = d(historico_fecha)
    return insert(table, **cols)


TRANSITO_DEF = dict(IdEmpresa=1, IdEstado="PEN", IdClasificacionTipoInstrumento="RV", IdTipoInstrumento="ACC", IdInstrumento=1001,
                    IdEmisor="EMISOR1", Nemotecnico="NEMO1001", NemoSVS="SVS1001", CorteMinimo=1, IdMonedaInstrumento="CLP", IdTipoReajuste="CLP",
                    FechaEmisionInstrumento=d("2020-01-01"), FechaVencimientoInstrumento=d("2030-12-31"), CodigoIFRS="VR", EsFungible="S",
                    IdBoveda="DCV", IdBolsa="BCS", IdTipoOperacion="INGTIT", IdMesa=7, FechaLiquidacion=d("2026-02-26"), Cantidad=40,
                    ValorRescate=0, PrecioCompra=1400, TasaCompra=0, MontoCompraMonedaInstrumento=56000, IdConcepto="TRA", IdRegimenFondoMutuo=None,
                    EsVentaDescubierta="N", FechaOrigenCompra=d("2026-02-26"), FolioComprobante=0, FolioExternoReferencia="0", IdTipoComprobante="CMP")


def transito(id_mov, cli=500, por=5001, historico_fecha=None, **over):
    cols = dict(IdMovimientoCarteraEnTransito=id_mov, IdCliente=cli, IdPortafolio=por)
    cols.update(TRANSITO_DEF)
    cols.update(over)
    out = [insert("BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito", **cols)]
    if historico_fecha:
        h = dict(cols)
        h["FechaRespaldo"] = d(historico_fecha)
        out.append(insert("BSCL.Custodia.BD::HIS.MovimientoCarteraEnTransito", **h))
    return "\n".join(out)


def entrada(usos, filtros=None, agrupado=None, fecha="2026-03-02", dias=0, valorizar="N", pais=-1, dia_habil=None, **extra):
    e = {"p_IdEmpresa": 1, "p_IdUsuario": 77, "p_IdModulo": "CUS", "p_FechaSaldo": fecha, "p_IdPais": pais, "p_DiasProyeccion": dias,
         "p_ValorizarFlujo": valorizar, "p_Uso": usos, "p_FiltrosBusqueda": filtros if filtros is not None else [filtro()],
         "p_EsAgrupadoPor": agrupado if agrupado is not None else [agrupar()]}
    if dia_habil is not None:
        e["p_DiaHabil"] = dia_habil
    e.update(extra)
    return e


def p_uso(tipo, cli=None, por=None, busq="", especifico=None):
    return {"UsoTipoOperacion": tipo, "IdCliente": cli, "IdPortafolio": por, "BusquedaEspecifica": busq, "IdTipoInstrumentoEspecifico": especifico}


def filtro(clas=None, tipo=None, ins=None, emisor=None, venc=None, reajuste=None, fungible=None, boveda=None, regimen=None):
    return {"IdClasificacionTipoInstrumento": clas, "IdTipoInstrumento": tipo, "IdInstrumento": ins, "IdEmisor": emisor, "FechaVencimiento": venc,
            "IdTipoReajuste": reajuste, "EsFungible": fungible, "IdBoveda": boveda, "IdRegimenFondoMutuo": regimen}


AGRUPAR_CAMPOS = ["ClasificacionTipoInstrumento", "TipoInstrumento", "Instrumento", "Emisor", "Nemotecnico", "NemoSVS", "FechaEmisionInstrumento",
                  "FechaVencimientoInstrumento", "FechaLiquidacionInstrumento", "TipoReajuste", "EsFungible", "CodigoIFRS", "Boveda", "Bolsa",
                  "TipoOperacion", "Mesa", "FolioRegistro", "ItemOrigen", "IdRegimenFondoMutuo", "GrupoRegimen", "MonedaInstrumento"]


def agrupar(si=("ClasificacionTipoInstrumento", "TipoInstrumento", "Instrumento"), nulos=()):
    """one p_EsAgrupadoPor entry: the fields in `si` are "S", the ones in `nulos` are null, every other one "N" """
    return {c: (None if c in nulos else ("S" if c in si else "N")) for c in AGRUPAR_CAMPOS}


def valorizacion(resultados, errores=0, mensajes=None):
    """a scripted ValorizarInstrumentoSalida"""
    return {"P_OUT_INSTRUMENTOSVALORIZADOS": resultados, "P_OUT_MENSAJES": mensajes or [], "P_OUT_ERRORVALORIZACIONES": [],
            "P_OUT_ERRORESCONTADOR": errores}


def resultado(id_val, dimension="MERC", precio="1650.5", tasa="0", monto_ins="165050", monto_sis="165050"):
    return {"IdValorizacion": id_val, "DimensionTasa": dimension, "Precio": precio, "Tasa": tasa, "MontoMonedaInstrumento": monto_ins,
            "MontoMonedaSistema": monto_sis}


def caso(cid, reglas, descripcion, entrada_json, seed_lines=(), colaboradores=None):
    dd = CASES / cid
    dd.mkdir(parents=True, exist_ok=True)
    c = {"id": cid, "reglas": list(reglas), "descripcion": descripcion, "entrada": entrada_json}
    if colaboradores:
        c["colaboradores"] = colaboradores
    (dd / "case.json").write_text(json.dumps(c, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")
    (dd / "seed.sql").write_text("-- inputs of case " + cid + " (fictitious)\n" + "\n".join(seed_lines) + "\n", encoding="utf-8", newline="\n")
    return cid
