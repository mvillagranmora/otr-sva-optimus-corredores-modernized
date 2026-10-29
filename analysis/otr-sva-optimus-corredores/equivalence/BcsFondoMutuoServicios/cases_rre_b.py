"""FMM-RRE-22..36: types of reinversion, batch, groups (D-F2-6), 1:N, BF, regimes, MLT, grouped amounts (FM-REI-07..08, 12, 16..21, 25, 31)."""
import cases_rre_lib as L
from cases_rre_lib import RESCATE, APORTE, sol

# the BATCH modality rows the base universe lacks (Step 5 root cause of D-NOR-14): copies of the UNITARIO ones (ASSUMED values)
BATCH_FECHAS = [L.clonar(L.FECHAS, "IdModalidad", "UNITARIO", IdModalidad="BATCH")]


def par(r, a, ids=(1,), extra=None):
    """scripted collaborators of a pair rescue series r -> contribution series a (ids of the rescues; contributions are -id)."""
    pares = {}
    for i in ids:
        pares[i] = r
        pares[-i] = a
    custodia = [(L.SERIES[r][0], "GEN", 1000)]
    return L.colab(L.precios(pares), L.custodia(*custodia), L.paridad(list(ids) + [-i for i in ids]), extra or {})


def regimen(rid, grupo="DC", **kw):
    """a regime (clone of GEN) for the series FM001A and FM002A, with its purpose configuration for PROPGEN."""
    s = [L.clonar(L.REGIMEN, "IdRegimenFondoMutuo", "GEN", IdRegimenFondoMutuo=rid, GrupoRegimen=grupo, **kw)]
    for sid in ("FM001A", "FM002A"):
        s.append(L.clonar(L.REGSERIE, "IdSerieFondo", sid, IdRegimenFondoMutuo=rid))
    s.append(L.ins(L.PROPOSITO, {"IdRegimenFondoMutuo": rid, "IdPropositoPortafolio": "PROPGEN"}))
    return s


def escribir():
    c = L.caso
    usd_r = sol(RESCATE, IdSerieFondo="FMX01U", IdMoneda="USD")
    c("FMM-RRE-22_switch-mismo-emisor-extranjero", "FM-REI-07/08/12/31/C09: SWITCH (extranjeros, mismo emisor): valutas 2/1/2, curse 0, Switch S en fila y solicitud",
      [usd_r], [sol(APORTE, IdSerieFondo="FMX02U", IdEmisor="EXT1", IdMoneda="USD")], colaboradores=par("FMX01U", "FMX02U"))
    c("FMM-RRE-23_internacional-distinto-emisor", "FM-REI-07/08/C10: INTERNACIONAL: retencion 4", [usd_r],
      [sol(APORTE, IdSerieFondo="FMX03U", IdEmisor="EXT2", IdMoneda="USD")], colaboradores=par("FMX01U", "FMX03U"))
    c("FMM-RRE-24_extranjero-a-nacional-spot-S", "FM-REI-07/08/C11: extranjero -> nacional con monedas distintas y RESCATE_GENERA_VENTA_SPOT_DOLAR S: "
      "RescateGeneraSpotCertificacion S", [usd_r], [sol(APORTE, IdSerieFondo="FM001A", IdEmisor="AGF1", IdMoneda="CLP")],
      colaboradores=par("FMX01U", "FM001A"), seed=[L.param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")])
    c("FMM-RRE-25_extranjero-a-nacional-diferido", "FM-REI-20/21/C29: aporte USD hacia serie CLP no diferida con parametro Spot N: EsDiferido N", [usd_r],
      [sol(APORTE, IdSerieFondo="FM002A", IdMoneda="USD")], colaboradores=par("FMX01U", "FM002A"))
    c("FMM-RRE-26_misma-serie-origen-destino", "FM-REI-20/C12: serie de rescate = serie destino: EsFondoValido N", [sol(RESCATE)],
      [sol(APORTE, IdSerieFondo="FM001A", IdEmisor="AGF1")], colaboradores=par("FM001A", "FM001A"))
    c("FMM-RRE-27_batch-car-respeta-N", "FM-REI-08/12/32/C03/C13: batch S, origen CAR, RespetaValutasCargaMasiva N: fondo abierto y valutas 0; sin AGF",
      [sol(RESCATE, SolicitudBatch="S")], [sol(APORTE)], colaboradores=par("FM001A", "FM002A"), seed=BATCH_FECHAS, p_IdOrigenProceso="CAR")
    c("FMM-RRE-28_batch-respeta-S", "FM-REI-08/12/C13: batch S con RespetaValutasCargaMasiva S: valutas de la serie", [sol(RESCATE, SolicitudBatch="S")],
      [sol(APORTE)], colaboradores=par("FM001A", "FM002A"), seed=BATCH_FECHAS + [L.param("RespetaValutasCargaMasiva", "S")])
    c("FMM-RRE-29_dos-rescates-agrupador-cero", "FM-REI-16/25/31/C22, D-F2-6: 2 rescates con IdAgrupador 0: una sola solicitud (el 2o se descarta sin mensaje), "
      "2 filas de destino", [sol(RESCATE), sol(RESCATE, IdInterno=2, NumeroCuotas=5)], [sol(APORTE), sol(APORTE, IdInterno=2, IdSerieFondo="FMX02U",
                                                                                                     IdEmisor="EXT1", IdMoneda="USD")],
      colaboradores=L.colab(L.precios({1: "FM001A", 2: "FM001A", -1: "FM002A", -2: "FMX02U"}), L.custodia((900, "GEN", 1000)), L.paridad([1, 2, -1, -2])))
    c("FMM-RRE-30_dos-grupos", "FM-REI-16/25/31/C23: 2 rescates en grupos 1 y 2: 2 solicitudes, 2 agrupadores, una fila de destino cada una",
      [sol(RESCATE, IdAgrupador=1), sol(RESCATE, IdInterno=2, NumeroCuotas=5, IdAgrupador=2)],
      [sol(APORTE, IdAgrupador=1), sol(APORTE, IdInterno=2, IdAgrupador=2)],
      colaboradores=L.colab(L.precios({1: "FM001A", 2: "FM001A", -1: "FM002A", -2: "FM002A"}), L.custodia((900, "GEN", 1000)), L.paridad([1, 2, -1, -2])))
    tres_r = [sol(RESCATE, IdInterno=i, IdAgrupador=7, NumeroCuotas=10) for i in (1, 2, 3)]
    tres_a = [sol(APORTE, IdInterno=1, IdAgrupador=7, Porcentaje=50, PorcentajeReal=50.12345678),
              sol(APORTE, IdInterno=2, IdAgrupador=7, Porcentaje=30, PorcentajeReal=29.87654321, IdSerieFondo="FMX02U", IdEmisor="EXT1",
                  IdMoneda="USD"),
              sol(APORTE, IdInterno=3, IdAgrupador=7, Porcentaje=20, PorcentajeReal=20, IdSerieFondo="FMX03U", IdEmisor="EXT2", IdMoneda="USD")]
    tres_c = L.colab(L.precios({1: "FM001A", 2: "FM001A", 3: "FM001A", -1: "FM002A", -2: "FMX02U", -3: "FMX03U"}), L.custodia((900, "GEN", 1000)),
                     L.paridad([1, 2, 3, -1, -2, -3]))
    c("FMM-RRE-31_uno-a-n-tres-destinos", "FM-REI-14/31/C24, D-F2-6: 1:N (3 rescates del agrupador 7, 3 aportes 50/30/20 con PorcentajeReal DECIMAL(26,8)): "
      "1 RESREINV y 3 filas de destino en orden del HashMap", tres_r, tres_a, colaboradores=tres_c)
    c("FMM-RRE-32_bf-cuotas-cero", "FM-REI-19/27/C26, D-F2-17: regimen BF con cuotas 0: se omite sin mensaje tras el bloqueo; nada por procesar",
      [sol(RESCATE, IdRegimenTributario="BF", NumeroCuotas=0)], [sol(APORTE, IdRegimenTributario="BF")], colaboradores=par("FM001A", "FM002A"),
      seed=regimen("BF"))
    c("FMM-RRE-33_regimen-destino-sin-configuracion", "FM-REI-20/C27: regimen del aporte distinto sin fila en ConfiguracionRegimenFondoMutuo: "
      "EsRegimenDestinoValido N", [sol(RESCATE)], [sol(APORTE, IdRegimenTributario="APVX")], colaboradores=par("FM001A", "FM002A"),
      seed=regimen("APVX", "APV"))
    c("FMM-RRE-34_regimen-destino-configurado-apv", "FM-REI-18/20: regimen distinto con configuracion GEN->APVX: pasa; tributacion por GrupoRegimen del rescate",
      [sol(RESCATE)], [sol(APORTE, IdRegimenTributario="APVX")], colaboradores=par("FM001A", "FM002A"),
      seed=regimen("APVX", "APV") + [L.ins(L.CONFREG, {"IdRegimenFondoMutuoOrigen": "GEN", "IdRegimenFondoMutuoDestino": "APVX"})])
    c("FMM-RRE-35_mlt-sin-mandato", "FM-REI-20/21/C28, D-F2-14: aporte MLT: PermiteMLT del regimen y mandato (rMandato nunca nulo)", [sol(RESCATE)],
      [sol(APORTE, IndicadorMLT="S")], colaboradores=par("FM001A", "FM002A"),
      seed=[L.ins(L.MANDATO, {"IdEmpresa": 1, "IdSerieFondo": "FM001A", "RescateConMandato": "S", "IdEstadoRegimenSerieDeFondo": "ACT"})])
    c("FMM-RRE-36_serie-sin-cambio-fondo-nada-por-procesar", "FM-REI-24/27/C32/C33: la serie de rescate no permite cambio de fondo: error del lado rescate, "
      "rechazo, nada por procesar, bloqueo eliminado", [sol(RESCATE)], [sol(APORTE)], colaboradores=par("FM001A", "FM002A"),
      seed=[L.upd(L.SERIE, {"CambioFondo": "N"}, "\"IdSerieFondo\" = 'FM001A'")])
    return 15
