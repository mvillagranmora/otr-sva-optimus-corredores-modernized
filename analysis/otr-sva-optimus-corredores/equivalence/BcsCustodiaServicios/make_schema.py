#!/usr/bin/env python3
"""Generates schema-flujo.sql: the H2 tables standing in for the ~45 HANA tables/views read or written by cl.bcs.custodia.ConsultarFlujoCustodia
(29 FlujoCustodia_*.sql) and by the real Plataforma classes it calls (ConsultarEmpresa_Select.sql, ConsultarConfiguracion{Plataforma,Custodia}.sql,
ConsultarDiccionario.sql, ConsultarFeriados.sql). Reads the catalog exports READ ONLY; writes only next to this script.

Every column carries a tag in the generated file:
  REAL    = type taken from hana_qa_results/{cat,fmm}_columnas_{tablas,vistas}.csv (or supuestos_*.csv, which FondoMutuo already labelled ASSUMED);
  ASSUMED = no catalog row: type inferred from the SQL that reads it and from the legacy pojo field it lands in (Long->BIGINT, BigDecimal->
            DECIMAL(26,8) as CUS.Cartera's amounts, java.sql.Date->DATE, String->NVARCHAR(n) with the length of the pojo comment or of a REAL
            homonym). NOT NULL constraints are NOT reproduced anywhere (seeds stay small); no key, no FK.
Views are plain tables (the oracle seeds their rows directly), as in BcsFondoMutuoServicios/schema-fmm.sql.

  py -3 -I make_schema.py
"""
import csv
import pathlib

HERE = pathlib.Path(__file__).resolve().parent
CAT = HERE.parents[1] / "hana_qa_results"

CATALOGS = ["cat_columnas_tablas.csv", "fmm_columnas_tablas.csv", "cat_columnas_vistas.csv", "fmm_columnas_vistas.csv"]
SUPUESTOS = ["supuestos_columnas_tablas.csv", "supuestos_columnas_vistas.csv"]

# ---------------------------------------------------------------- object -> columns the in-scope SQL use (None = take every catalog column)
CUS = "BSCL.Custodia.BD::"
INS = "BSCL.Plataforma.BD::INS."
INSTRUMENTO_DIM = ["IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdEmisor", "Nemotecnico", "NemoSVS", "CorteMinimo",
                   "IdMonedaInstrumento", "IdTipoReajuste", "FechaEmisionInstrumento", "FechaVencimientoInstrumento", "CodigoIFRS", "EsFungible"]
MATRIZ = ["IdCliente", "IdPortafolio", "UsoTipoOperacion", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdEmisor",
          "FechaVencimiento", "IdTipoReajuste", "EsFungible", "IdBoveda", "IdTipoInstrumentoEspecifico", "IdUsoFlujoCustodia", "BusquedaEspecifica",
          "IdEstado", "IdEmpresa", "IdRegimenFondoMutuo", "IdTrabajo"]
TRANSITO = ["IdMovimientoCarteraEnTransito", "IdEmpresa", "IdEstado"] + INSTRUMENTO_DIM + ["IdBoveda", "IdBolsa", "IdTipoOperacion", "IdMesa",
            "FechaLiquidacion", "Cantidad", "ValorRescate", "PrecioCompra", "TasaCompra", "MontoCompraMonedaInstrumento", "IdCliente", "IdPortafolio",
            "IdConcepto", "IdRegimenFondoMutuo", "EsVentaDescubierta", "FechaOrigenCompra", "FolioComprobante", "FolioExternoReferencia",
            "IdTipoComprobante"]
GARANTIA = ["IdGarantia", "FolioGarantia", "IdCarteraOrigen"] + INSTRUMENTO_DIM + ["INGFechaLiquidacion", "INGCantidad", "INGValorRescate",
            "INGPrecioGarantia", "INGTasaGarantia", "INGMontoCompraActualMonedaInstrumento", "INGPrecioMercadoActual", "INGTasaMercadoActual",
            "INGMontoMercadoActualMonedaInstrumento", "IdCliente", "IdPortafolio", "IdConcepto", "IdContraparteGarantia", "IdEstado",
            "IdOrigenGarantia", "IdEmpresa"]
SUBYACENTE = ["IdSubyacente", "FolioSubyacente", "FolioCarteraRelacionada"] + [c for c in INSTRUMENTO_DIM if c != "CodigoIFRS"] + [
            "INGCantidad", "INGValorRescate", "INGPrecioSubyacente", "INGTasaSubyacente", "INGMontoCompraActualMonedaInstrumento",
            "INGPrecioMercadoActual", "INGTasaMercadoActual", "INGMontoMercadoActualMonedaInstrumento", "IdMonedaEmision", "IdEstado"]
RESERVA = ["IdReserva", "IdOperacion", "FolioCarteraOrigen", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdEmisor",
           "CodigoIFRS", "EsFungible", "IdBoveda", "IdBolsa", "IngCantidad", "IngValorRescate", "Precio", "Tasa", "IngMontoValorizado", "IdConcepto",
           "CorteMinimo", "IdEstado"]
ORDEN = ["IdOrden", "IdEmpresa", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoSubyacente", "IdEmisor",
         "Nemotecnico", "IdMonedaOrden", "IdTipoReajuste", "FechaEmision", "FechaVencimiento", "EsFungible", "IdBoveda", "IdBolsa", "IdTipoOperacion",
         "FechaLiquidacion", "Cantidad", "ValorRescate", "PrecioOrden", "MontoOrden", "IdCliente", "IdPortafolio", "IdConcepto", "Accion"]
TRANSACCION = ["IdTransaccion", "IdCorredorLocal", "IdCorredorContraparte"]

OBJECTS = [
    # --- written by the slice (work table) and the usage configuration
    (CUS + "CUS.FlujoCustodia_MatrizUso", MATRIZ),
    (CUS + "CUS.UsoFlujoCustodia", ["IdUsoFlujoCustodia", "IdUsuario", "UsoTipoOperacion", "IdClasificacionTipoInstrumento", "IdTipoInstrumento",
                                    "IdInstrumento", "IdCliente", "IdPortafolio", "BusquedaEspecifica", "FechaIngresoRegistro", "UsoValorizacion",
                                    "IdEstado", "IdEmpresa"]),
    (CUS + "CUS.ItemsFlujoCustodia", ["IdUsoFlujoCustodia", "ItemCalculo", "ItemOrigen", "Descripcion", "FuncionOperador", "IdEstado"]),
    (CUS + "CUS.Configuracion", ["IdEmpresa", "CodigoParametro", "Valor"]),
    (CUS + "CUS.ConfiguracionMovimientoCustodia", ["IdTipoOperacion", "PoseeInstrumentoSubyacente", "IdAccionOperacion"]),
    # --- sources (current and historic)
    (CUS + "CUS.Cartera", None),
    (CUS + "HIS.Cartera", "HIS"),
    (CUS + "CUS.MovimientoCarteraEnTransito", TRANSITO),
    (CUS + "HIS.MovimientoCarteraEnTransito", TRANSITO + ["FechaRespaldo"]),
    (CUS + "GTA.Garantia", GARANTIA),
    (CUS + "HIS.Garantia", GARANTIA + ["FechaRespaldo"]),
    (CUS + "GTA.TipoInstrumentoParaGarantia", ["IdBolsaTipoInstrumento", "IdTipoInstrumentoPermitido", "IdEstado"]),
    (CUS + "CUS.Subyacente", SUBYACENTE),
    (CUS + "HIS.Subyacente", SUBYACENTE + ["FechaRespaldo"]),
    (CUS + "CUS.Reserva", RESERVA),
    (CUS + "HIS.Reserva", RESERVA + ["FechaRespaldo"]),
    ("BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCustodia",
     ["IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "NemoNuevo", "IdMoneda", "IdBoveda", "FechaLiquidacion", "Cantidad",
      "Precio", "Monto", "IdCliente", "IdPortafolio", "IdRegistro", "FolioRegistro", "IdTipoEventoCapital", "IdRegimenFondoMutuo", "IdEstado",
      "IdEmpresa"]),
    ("BSCL.Mesa.BD::MES.Operacion", ["IdOperacion", "IdTipoOperacion", "IdMesaOrigen", "FechaCondicionLiquidacion", "FechaCreacion", "IdCliente",
                                    "IdClienteCorredor", "IdPortafolio", "IdPortafolioCorredor"]),
    ("BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenPendienteAutorizacionFlujoCustodia", ORDEN),
    ("BSCL.Orden.BD.Vistas::Flujos.ConsultarOrdenIngresadaFlujoCustodia", ORDEN + ["CantidadDisponible", "ValorRescateDisponible", "MontoDisponible"]),
    ("BSCL.Orden.BD.Vistas::Flujos.ConsultarAsignacionNoEnviadaFacturarFlujoCustodia",
     ["IdOrden", "IdEmpresa", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoSubyacente", "IdEmisor",
      "Nemotecnico", "IdMonedaAsignacion", "IdTipoReajuste", "FechaEmision", "FechaVencimiento", "EsFungible", "IdBoveda", "IdBolsa", "IdTipoOperacion",
      "FechaLiquidacion", "CantidadAsignada", "ValorRescateAsignado", "MontoAsignado", "IdCliente", "IdPortafolio", "IdConcepto", "Accion"]),
    ("BSCL.Orden.BD.Vistas::ConsultarTransaccion", TRANSACCION),
    ("BSCL.Orden.BD::ORD.Transaccion", TRANSACCION),
    ("BSCL.Orden.BD::HIS.Transaccion", TRANSACCION),
    ("BSCL.Facturacion.BD::FAC.MovimientoComprobante", None),
    ("BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle", ["IdTipoComprobante", "IdComprobante", "IdInstrumento", "IdInstrumentoSubyacente",
                                                                 "IdTransaccion", "IndicadorOrdenDirecta"]),
    ("BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCustodia",
     ["IdMovimientoComprobante", "IdEmpresa", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdInstrumentoSubyacente",
      "IdEmisor", "Nemotecnico", "NemoSVS", "IdMoneda", "IdTipoReajuste", "FechaEmision", "FechaVencimiento", "CodigoIFRS", "EsFungible", "IdBoveda",
      "IdBolsa", "IdTipoOperacion", "FechaLiquidacion", "Cantidad", "ValorRescate", "Precio", "Tasa", "Monto", "IdCliente", "IdPortafolio",
      "IdConceptoMovimiento", "AccionTipoOperacion"]),
    ("BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateFlujoCustodia",
     ["IdClasificacionInstrumento", "IdTipoInstrumento", "IdInstrumento", "IdEmisor", "Nemotecnico", "NemoSVS", "IdMonedaInstrumento",
      "IdTipoReajuste", "FechaEmisionInstrumento", "FechaVencimientoInstrumento", "CodigoIFRS", "EsFungible", "IdBoveda", "IdBolsa",
      "IdTipoOperacion", "FechaLiquidacion", "Cantidad", "ValorRescate", "PrecioCompra", "TasaCompra", "MontoATasaCompra", "IdCliente",
      "IdPortafolio", "IdRegistro", "IdConcepto", "IdRegimenTributario", "FechaCertificacion", "IdEstado", "IdEmpresa"]),
    ("BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo", None),
    # --- instrument catalog
    (INS + "Instrumento", ["IdInstrumento", "TipoValorizacion", "IdMoneda"]),
    (INS + "TipoOperacion", None),
    (INS + "TipoOperacionTipoInstrumento", None),
    (INS + "BolsaTipoInstrumento", ["IdBolsaTipoInstrumento", "IdTipoInstrumento", "IdBolsa", "ExigeGarantia"]),
    (INS + "TipoInstrumento", None),
    (INS + "Bolsa", None),
    (INS + "TipoInstrumentoSubyacente", ["IdTipoInstrumento", "IdBolsa", "IdTipoInstrumentoSubyacente"]),
    (INS + "InstrumentoSubyacente", ["IdTipoInstrumento", "IdBolsa", "IdTipoInstrumentoSubyacente", "IdInstrumento"]),
    (INS + "Corredora", ["IdCorredora", "IdPersona"]),
    (INS + "CorredoraCodigos", ["IdCorredora", "IdBolsa", "CodigoCorredora"]),
    # --- Plataforma kit (ConfiguracionSistema, Diccionario, ConsultarEmpresa, ObtenerDiaSiguiente) and own-account portfolios
    ("BSCL.Plataforma.BD::PLA.Configuracion", None),
    ("BSCL.Plataforma.BD::PLA.Diccionario", None),
    ("BSCL.Plataforma.BD::PLA.Empresa", None),
    ("BSCL.Plataforma.BD::PLA.Estado", ["IdEstado", "Nombre"]),
    ("BSCL.Plataforma.BD::REG.DiaFeriado", None),
    ("BSCL.Plataforma.BD::REG.Ciudad", ["IdCiudad", "NombreCiudad"]),
    ("BSCL.Plataforma.BD::REG.DivisionPais", ["IdDivisionPais", "NombreDivisionPais"]),
    ("BSCL.Plataforma.BD::REG.Pais", ["IdPais", "CodigoPais", "NombrePais"]),
    ("BSCL.SocioNegocio.BD::SOC.SocioDeNegocio", ["IdSocioDeNegocio", "IdPersona", "IdCliente", "IdContraparte", "Borrado"]),
    ("BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio", None),
    ("BSCL.SocioNegocio.BD::PER.Persona", None),
    ("BSCL.SocioNegocio.BD::PER.DocumentoIdentidad", None),
    ("BSCL.SocioNegocio.BD::PER.DireccionElectronica", ["IdDireccionElectronica", "Direccion"]),
    ("BSCL.SocioNegocio.BD::PER.DireccionFisica", ["IdDireccionFisica", "Calle", "Numero", "Departamento", "Sector", "IdCiudad", "IdDivisionPais"]),
    ("BSCL.SocioNegocio.BD::PER.Telefono", ["IdTelefono", "Numero"]),
    ("BSCL.SocioNegocio.BD::CNT.Contraparte", ["IdContraparte", "CodigoInscripcion"]),
    ("BSCL.SocioNegocio.BD::CLI.Cliente", None),
    ("BSCL.Portafolio.BD::POR.Portafolio", None),
]

# ---------------------------------------------------------------- ASSUMED typing
LONG = set("""IdCliente IdPortafolio IdInstrumento IdInstrumentoSubyacente IdUsoFlujoCustodia IdEmpresa IdTrabajo IdUsuario IdMesa
IdMovimientoCarteraEnTransito FolioComprobante IdGarantia FolioGarantia IdCarteraOrigen IdContraparteGarantia IdSubyacente FolioSubyacente
FolioCarteraRelacionada IdReserva IdOperacion FolioCarteraOrigen IdMesaOrigen IdClienteCorredor IdPortafolioCorredor IdBolsaTipoInstrumento
IdComprobante IdTransaccion IdCorredorLocal IdCorredorContraparte IdOrden IdMovimientoComprobante IdRegistro FolioRegistro IdSocioDeNegocio
IdPersona IdContraparte IdCorredora IdPais IdDocumentoIdentidad IdDireccionElectronica IdDireccionFisica IdCiudad IdDivisionPais IdTelefono""".split())
LEN = {"IdEstado": 3, "IdTipoOperacion": 6, "UsoTipoOperacion": 6, "IdTipoInstrumento": 3, "IdTipoInstrumentoEspecifico": 3,
       "IdTipoInstrumentoSubyacente": 3, "IdTipoInstrumentoPermitido": 3, "IdClasificacionTipoInstrumento": 3, "IdClasificacionInstrumento": 3,
       "IdBoveda": 3, "IdBolsa": 3, "IdMoneda": 3, "IdMonedaInstrumento": 3, "IdMonedaOrden": 3, "IdMonedaAsignacion": 3, "IdMonedaEmision": 3,
       "IdTipoReajuste": 3, "EsFungible": 1, "EsVentaDescubierta": 1, "PoseeInstrumentoSubyacente": 1, "ExigeGarantia": 1, "Borrado": 1,
       "FuncionOperador": 1, "IdConcepto": 6, "IdConceptoMovimiento": 6, "IdAccionOperacion": 3, "IdOrigenGarantia": 3, "IdTipoComprobante": 6,
       "IdTipoEventoCapital": 2, "IndicadorOrdenDirecta": 2, "CodigoIFRS": 3, "TipoValorizacion": 10, "Descripcion": 100, "Nombre": 100,
       "Valor": 5000, "CodigoParametro": 40, "IdEmisor": 40, "Nemotecnico": 40, "NemoSVS": 40, "NemoNuevo": 40, "BusquedaEspecifica": 40,
       "ItemCalculo": 40, "ItemOrigen": 40, "UsoValorizacion": 40, "Accion": 40, "AccionTipoOperacion": 40, "IdRegimenFondoMutuo": 40,
       "IdRegimenTributario": 40, "FolioExternoReferencia": 40}
TIMESTAMPS = {"FechaIngresoRegistro", "FechaCreacion"}
# the Facturacion view exposes FechaEmision / FechaVencimiento with the type of FAC.MovimientoComprobante (TIMESTAMP, REAL there): it is read
# through TO_DATE(...) by FlujoCustodia_SelectMovimientos*Facturacion.sql
TIMESTAMP_BY_OBJECT = {"BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteFlujoCustodia": {"FechaEmision", "FechaVencimiento"}}
DECIMAL_HINTS = ("Cantidad", "ValorRescate", "Precio", "Tasa", "Monto", "CorteMinimo")


def assumed_type(obj, col):
    if col in TIMESTAMP_BY_OBJECT.get(obj, set()) or col in TIMESTAMPS:
        return "TIMESTAMP"
    if "Fecha" in col:
        return "DATE"
    if col in LONG:
        return "BIGINT"
    if any(h.lower() in col.lower() for h in DECIMAL_HINTS) and not col.startswith("Id"):
        return "DECIMAL(26,8)"
    return "NVARCHAR(%d)" % LEN.get(col, 40)


def h2_type(dt, length, scale):
    dt = dt.upper()
    if dt in ("NVARCHAR", "VARCHAR", "ALPHANUM", "SHORTTEXT"):
        return "NVARCHAR(%s)" % (length or 5000)
    if dt in ("NCLOB", "CLOB", "TEXT"):
        return "CLOB"
    if dt == "SECONDDATE":
        return "TIMESTAMP(0)"
    if dt == "TIMESTAMP":
        return "TIMESTAMP(7)"
    if dt == "DECIMAL":
        return "DECIMAL(%s,%s)" % (length, scale or 0) if length else "DECFLOAT"
    if dt in ("BIGINT", "INTEGER", "SMALLINT", "TINYINT", "DATE", "DOUBLE", "REAL", "BOOLEAN", "TIME"):
        return dt
    if dt in ("VARBINARY", "BLOB"):
        return "VARBINARY"
    raise ValueError("type %s" % dt)


def load(files, tag):
    out = {}
    for f in files:
        with open(CAT / f, encoding="utf-8") as fh:
            for row in csv.DictReader(fh):
                name = row.get("TABLE_NAME") or row.get("VIEW_NAME")
                out.setdefault(name, {})
                if row["COLUMN_NAME"] not in out[name]:
                    out[name][row["COLUMN_NAME"]] = (h2_type(row["DATA_TYPE_NAME"], row["LENGTH"], row["SCALE"]), tag + " (" + f + ")",
                                                     int(row["POSITION"]))
    return out


def main():
    real = load(CATALOGS, "REAL")
    sup = load(SUPUESTOS, "ASSUMED-FM")
    lines = ["-- GENERATED by make_schema.py (do not edit by hand). H2 stand-in for the HANA objects of cl.bcs.custodia.ConsultarFlujoCustodia.",
             "-- REAL = catalog export type; ASSUMED-FM = type already assumed by BcsFondoMutuoServicios (supuestos_*.csv);",
             "-- ASSUMED = no catalog row: inferred from the SQL and the legacy pojo field type. NOT NULL / keys are not reproduced.", ""]
    resumen = []
    for obj, cols in OBJECTS:
        cat = real.get(obj) or sup.get(obj)
        if cols == "HIS":   # HIS.Cartera = the CUS.Cartera row shape + FechaRespaldo (backup date); ASSUMED (no catalog row for HIS.*)
            base = real[CUS + "CUS.Cartera"]
            spec = [(c, t, "ASSUMED (shape of CUS.Cartera, REAL there)") for c, (t, _, _) in sorted(base.items(), key=lambda kv: kv[1][2])]
            spec.append(("FechaRespaldo", "DATE", "ASSUMED"))
        elif cols is None:
            if not cat:
                raise SystemExit("no catalog for " + obj)
            spec = [(c, t, tag) for c, (t, tag, _) in sorted(cat.items(), key=lambda kv: kv[1][2])]
        else:
            spec = []
            for c in cols:
                if cat and c in cat:
                    spec.append((c, cat[c][0], cat[c][1]))
                else:
                    spec.append((c, assumed_type(obj, c), "ASSUMED"))
        tags = {s[2].split(" ")[0] for s in spec}
        resumen.append("-- %-100s %s" % (obj, "/".join(sorted(tags))))
        lines.append('CREATE TABLE "%s" (' % obj)
        lines.append(",\n".join('    "%s" %s -- %s' % (c, t, tag) if False else '    "%s" %s' % (c, t) for c, t, tag in spec))
        lines.append(");")
        for c, t, tag in spec:
            if not tag.startswith("REAL"):
                lines.append("-- %s.%s %s: %s" % (obj, c, t, tag))
        lines.append("")
    lines.append("-- sequences: no catalog export for BSCL.Custodia.BD.Secuencias::*; START values ASSUMED (distinct ranges so that the dump shows")
    lines.append("-- which sequence a value came from)")
    lines.append('CREATE SEQUENCE "BSCL.Custodia.BD.Secuencias::FlujoCustodia" START WITH 7000001 INCREMENT BY 1;')
    lines.append('CREATE SEQUENCE "BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia" START WITH 90000001 INCREMENT BY 1;')
    lines.append("")
    lines.append("-- summary (object, origin of its column types)")
    lines.extend(resumen)
    (HERE / "schema-flujo.sql").write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
    print("schema-flujo.sql:", len(OBJECTS), "objects")


if __name__ == "__main__":
    main()
