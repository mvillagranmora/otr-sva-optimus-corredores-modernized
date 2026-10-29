"""Step 5 coverage cases of Normal (FMM-NOR-61..): the comision-NN success path and the OWP notification path.
INPUTS only (case.json + seed.sql); the goldens are RECORDED by legacy-oracle/record.sh FMM-NOR-6 from the unmodified legacy.

Why they were missing in 4A (D-NOR-14/15): the base seed has only UNITARIO rows in FMM.ConfiguracionFechasFondoMutuo, and the NN rescue of Normal calls
CalcularFechaSolicitudAporteRescate with SolicitudBatch=S (RespetaValutasCargaMasiva=N), which needs the BATCH modality row (D-FEC-2: no row => NPE). The OWP
path re-reads the new request from the view FMM.ConsultarSolicitudAporteRescate (a seeded table in H2) that had no IdOmnibus column (REAL in
hana_qa_results/fmm_columnas_vistas.csv) and no row for the request id the sequence hands out (11413788 for a one-request call).
The column is added by the seed of the OWP cases themselves (ALTER TABLE ... ADD COLUMN, BIGINT NULL as in the catalog), NOT in schema-fmm.sql, so the dev
goldens (case 17 ends in the legacy SQLException there) stay byte identical and are never re-recorded.
"""
import re

import cases_nor_lib as L
from cases_nor_b import PRECIOS2
from cases_nor_lib import APORTE, RESCATE, caso, ins, param, sol, upd

CFG = '"BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"'
VISTA = "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate"
COLS_CFG = ["IdEmpresa", "IdModalidad", "IdTipoFecha", "Descripcion", "CantidadDias", "EsHabil", "IdTipoFechaOrigen", "DebeSerHabilFechaOrigen", "ValutaCurse",
            "ValutaValorCuota", "ValutaCertificacion", "ValutaRetencion", "CalendarioLocal", "CalendarioEmisor", "CalendarioFondo", "CalendarioMoneda", "IdEstado"]


def filas_batch():
    """BATCH modality rows = copies of the UNITARIO rows of the base seed (ASSUMED values: the real configuration is not in the export)."""
    cols = ", ".join('"%s"' % c for c in COLS_CFG)
    sel = ", ".join("'BATCH'" if c == "IdModalidad" else '"%s"' % c for c in COLS_CFG)
    return ["INSERT INTO %s (%s) SELECT %s FROM %s WHERE \"IdModalidad\" = 'UNITARIO';" % (CFG, cols, sel, CFG)]


def fila_vista(**kw):
    """One full row of the seeded view table: every column of schema-fmm.sql gets a neutral value by its type (schema-types.sql), kw overrides."""
    sch = open(L.BASE / "schema-fmm.sql", encoding="utf-8").read()
    typ = open(L.BASE / "schema-types.sql", encoding="utf-8").read()
    bloque = re.search(r'CREATE TABLE "%s" \((.*?)\n\);' % re.escape(VISTA), sch, re.S).group(1)
    valores = {}
    for c in re.findall(r'^\s*"([^"]+)"', bloque, re.M):
        t = re.search(r'CREATE DOMAIN "%s\.%s" AS ([A-Z]+)' % (re.escape(VISTA), re.escape(c)), typ).group(1)
        valores[c] = {"NVARCHAR": "", "BIGINT": 0, "DECIMAL": 0, "INTEGER": 0, "DATE": "2026-03-02", "TIMESTAMP": "2026-03-02 00:00:00"}[t]
    valores.update(kw)
    cols = ", ".join('"%s"' % c for c in valores)
    vals = ", ".join(("DATE '%s'" % v if isinstance(v, str) and re.fullmatch(r"\d{4}-\d\d-\d\d", v) else
                      "TIMESTAMP '%s'" % v if isinstance(v, str) and re.fullmatch(r"\d{4}-\d\d-\d\d \d\d:\d\d:\d\d", v) else L.lit(v)) for v in valores.values())
    return 'INSERT INTO "%s" (%s) VALUES (%s);' % (VISTA, cols, vals)


def generar():
    import cases3a_lib as L3
    tramos = L3.tramo(10, "FM001A", "PER", 0, 9999, 1.2) + [L3.comision_maxima("FM001A")]
    lote = {"IdCliente": 100, "IdPortafolio": 500, "IdInstrumento": 900, "Cantidad": 100, "PrecioCompra": 1500, "IdConcepto": "APORFM",
            "FechaOrigenCompra": "2026-02-10 00:00:00.000", "FechaOperacion": "2026-02-09 00:00:00.000"}
    flujo = {"P_OUT_ERRORESCONTADOR": 0, "p_out_FlujoDetallado": [lote],
             "p_out_FlujoTotalizado": [{"IdCliente": 100, "IdPortafolio": 500, "IdInstrumento": 900, "IdRegimenFondoMutuo": "GEN", "Cantidad": 1000,
                                        "MontoAMercadoMonedaInstrumento": 1500123}]}
    tipo_nn = ins('"BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo"', {"IdTipoMovimientoFondoMutuo": "RESCNN", "Descripcion": "RESCNN", "IndicadorAporteRescate": "RES", "IdTipoOperacion": "RESCFM",
                                                                          "TieneCarga": "", "TieneIngresoManual": "", "TieneCertificacion": "", "IdEstado": "ACT", "GeneraOmnibus": ""})
    seed_nn = [param("CALCULAR_COMISION_CERTIFICACION", "S"), tipo_nn] + tramos + filas_batch()
    caso("FMM-NOR-61_comision-nn-ruta-feliz", "FM-NOR-16 D-NOR-6/15 (step 5): comision trimestral PER con la modalidad BATCH sembrada: CrearSolicitudRescateComisionNN crea el rescate NN y su resultado se agrega a la salida",
         [sol(RESCATE, NumeroCuotas=10)], seed=seed_nn, colab={"ConsultarFlujoCustodia": {"defecto": flujo}})
    caso("FMM-NOR-62_comision-nn-y-batch-valutas", "FM-NOR-12 D-NOR-14 (step 5): SolicitudBatch=S con la modalidad BATCH sembrada (antes NPE): fechas de carga masiva sin valutas",
         [sol(RESCATE, SolicitudBatch="S")], seed=filas_batch())
    alter = 'ALTER TABLE "%s" ADD COLUMN "IdOmnibus" BIGINT;' % VISTA   # REAL: fmm_columnas_vistas.csv ConsultarSolicitudAporteRescate.IdOmnibus BIGINT nullable
    seed_owp = [alter, upd(L.PLACONF, {"Valor": "S"}, "\"CodigoParametro\" = 'PLA.NotificarEstadoOrdenOWP'"),
                fila_vista(IdSolicitudAporteRescate=11413788, IdEmpresa=1, IdSerieFondo="FM001A", IdCliente=100, IdPortafolio=500, TipoSolicitud="RES",
                           IdMoneda="CLP", NumeroCuotas=10, ValorCuota=1500.1234, EstadoSolicitud="ACP", IdTipoCanalIngreso="OWP", FechaAceptacion="2026-03-02")]
    caso("FMM-NOR-63_canal-owp-notificacion-completa", "FM-NOR-17 (step 5): canal OWP con PLA.NotificarEstadoOrdenOWP=S y la vista sembrada con IdOmnibus (REAL en el catalogo): se arma y se envia la notificacion (doble)",
         [sol(RESCATE)], p_IdTipoCanalIngreso="OWP", seed=seed_owp, colab={"NotificarEstadoOrdenesOWP": {"defecto": {}}})
    caso("FMM-NOR-64_canal-owp-aporte-dos-solicitudes", "FM-NOR-17 (step 5): canal OWP, dos solicitudes distintas: la lista de ids no se repite; la vista solo trae la primera",
         [sol(APORTE, IdInterno=1), sol(APORTE, IdInterno=2, Monto=250000)], p_IdTipoCanalIngreso="OWP", colab=dict(PRECIOS2, NotificarEstadoOrdenesOWP={"defecto": {}}, **L.SALDO_OK),
         seed=[seed_owp[0], seed_owp[1], fila_vista(IdSolicitudAporteRescate=11413788, IdEmpresa=1, IdSerieFondo="FM001A", IdCliente=100, IdPortafolio=500, TipoSolicitud="APO",
                                       IdMoneda="CLP", NumeroCuotas=66, ValorCuota=1500.1234, EstadoSolicitud="ACP", IdTipoCanalIngreso="OWP", FechaAceptacion="2026-03-02")])


if __name__ == "__main__":
    generar()
    print("coverage cases written")
