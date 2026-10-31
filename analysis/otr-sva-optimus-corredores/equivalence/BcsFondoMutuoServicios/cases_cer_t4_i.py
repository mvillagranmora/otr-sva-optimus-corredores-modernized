"""FMM-CER T4 cases of front F-I (REINVERSION y SWITCH): FM-CER-52 (seleccion y lectura), FM-CER-53 (misma familia y moneda: un aporte por cartera retirada), FM-CER-54 (otra familia o
moneda: un aporte por destino), FM-CER-55 (ajuste de redondeo), FM-CER-56 (fechas del aporte y del Spot), FM-CER-57 (llamada a AR y mapeo de ids), FM-CER-58 (aportes de switch),
FM-CER-63 (certificacion recursiva de los aportes de switch) and FM-CER-64 (monto en moneda de sistema del rescate de reinversion internacional sin Spot).

Legacy: `CertificarSolicitudAporteRescate.logica` C:762-787 and `crearAporteReinversion` C:2343-2557; the recursion C:838-843, 2764-2775 and `cambiarMontoMonedaSistemaRescatesReinversionInternacionalesSinSpot` C:845,
2820-2835. Collaborators: `CrearSolicitudAporteReinversion` (AR: not ported in this tanda; a registered double scripted by the case, or the REAL class with `arReal`; the golden holds the COMPLETE
call and the answer) and the CHILD certification (the legacy calls itself: T4Spy records its entry and its answer; the child runs for real over the SAME session on the aportes of switch the
script of AR names, which the case seeds). Every case is recorded with the legacy (modo completo); `excepcion=True` marks a case that throws inside F-C..F-I.

Numbers: the base price of the series is replaced (`precios_con`) so that a rescate of 1.000.000 has EXACTLY 100 quotas (price 10.000): the custody movements are scripted as quantities of that
total (60 + 40 ...), and the amounts of the aportes are exact. Series: FM001B, FM005A = family FAM1 (CLP funds), FM003A = FAM2, FM002A, FM004A = FAM3 (USD funds, FondoExtranjero S)."""
from cases_cer_t4 import (caso, entrada, ap, aporte, rescate as _rescate, rescate_tra, origen, mov, err, resp_custodia, res_custodia, calcula_custodia, msg, resp_cuenta, cuenta_script,
                          tesoreria, resp_tesoreria, rech, gen, resp_mov_comp, mov_sal, resp_comprobante, facturacion, msg_fact, ar, resp_ar, res_ar, msg_ar, destino, L, Num, ABSENT, config,
                          param, ins, solo_tabla, TRAS_APV, TRAS_FM)
import cases_cer_caso as C

SIS = "2026-03-02"
SPOT_S = param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")
COMP_S = param("RESCATE_COMISION_COMPROBANTE_AUTOMATICO", "S")


def precios_con(seed, **valores):
    """EntregarPreciosTasas of the case with the price of some solicitudes replaced (keyword = the id, as text with a leading `s`, e.g. s3001=10000)"""
    sql, specs = C._aplanar(seed)
    filas = []
    for s in specs:
        p = C._precio_de(s)
        v = valores.get("s%d" % s["IdSolicitudAporteRescate"])
        if v is not None:
            p["Precio"] = v
        filas.append(p)
    return L.precios(*filas)


def R(id_, **c):
    """a rescate of reinversion (RESREINV) of the CLP fund FM001A, family FAM1, 1.000.000 CLP: with the price 10.000 it has 100 quotas and MontoPagoLocal 1.000.000"""
    d = dict(IdTipoMovimientoFondoMutuo="RESREINV", IdFamiliaFondo="FAM1")
    d.update(c)
    return _rescate(id_, **d)


def RD(id_, **c):
    """a rescate of reinversion of the USD fund FM002A (FondoExtranjero S), family FAM3"""
    d = dict(IdTipoMovimientoFondoMutuo="RESREINV", IdFamiliaFondo="FAM3", IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="USD", IdMonedaPago="USD", CostoMonedaLocal=5000,
             Monto=5000)
    d.update(c)
    return _rescate(id_, **d)


def _costo(q):
    return Num(("%.6f" % (float(q.texto if isinstance(q, Num) else q) / 10)).rstrip("0").rstrip(".") or "0")


def movs(id_, *cantidades, **c):
    """a custody answer (rescates WITH regime: the base regime GEN) with one movement of the rescate per quantity; each carries cost UF = quantity / 10"""
    return dict(con=resp_custodia([mov(id_, Cantidad=q, CostoInversionEnFactorAjusteARetirar=_costo(q), **c) for q in cantidades]))


def movs_de(*por_rescate):
    """the same for several rescates: (id, [quantities])"""
    filas = []
    for id_, cantidades in por_rescate:
        filas += [mov(id_, Cantidad=q, CostoInversionEnFactorAjusteARetirar=_costo(q)) for q in cantidades]
    return dict(con=resp_custodia(filas))


def sw(id_, rel, **c):
    """an aporte of switch (APOREINV, Switch S) of the rescate `rel` that the script of AR names: the child certifies it for real"""
    d = dict(IdTipoMovimientoFondoMutuo="APOREINV", IdSolicitudAporteRescateRelacionado=rel, Switch="S", IdSerieFondo="FM001B", IdInstrumento=901, ValorUFFechaInversion=Num("38000.5"),
             MontoMonedaSistema=1000000, CostoUF=Num("26.3"), CostoMonedaLocal=1000000)
    d.update(c)
    return aporte(id_, **d)


def aporte_rei(id_, rel, **c):
    """an APOREINV aporte (not switch) of the rescate `rel` that is certified in the request (front FM-CER-64)"""
    d = dict(IdTipoMovimientoFondoMutuo="APOREINV", IdSolicitudAporteRescateRelacionado=rel, IdSerieFondo="FM001B", IdInstrumento=901, ValorUFFechaInversion=Num("38000.5"),
             MontoMonedaSistema=1000000, CostoUF=Num("26.3"), CostoMonedaLocal=1000000)
    d.update(c)
    return aporte(id_, **d)


def aporte_usd(id_, rel, **c):
    """an APOREINV aporte of a USD fund paid in USD (the system currency is CLP): the rule of FM-CER-64 applies to it"""
    d = dict(IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="USD", IdMonedaSistema="CLP", MontoMonedaSistema=400000, CostoMonedaLocal=5000, Monto=5000)
    d.update(c)
    return aporte_rei(id_, rel, **d)


def generar():
    f = dict(front="F-I")
    d_misma = lambda r=3001, p=100: destino(1, r, "FM001B", p)   # noqa: E731
    # ================================================================== FM-CER-52 seleccion y lectura
    s = [R(3001), d_misma()]
    caso("reinv-un-rescate-de-reinversion-con-destino-llama-una-vez-a-ar", ["FM-CER-52", "FM-CER-57", "matriz:C31"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 60, 40), **f)
    s = [R(3001)]
    caso("reinv-sin-destinos-no-llama-a-ar", ["FM-CER-52", "matriz:C31"], entrada([3001]), seed=s, colaboradores=dict(**precios_con(s, s3001=10000), **L.falla("CrearSolicitudAporteReinversion", "no debe llamarse")),
         **f)
    s = [_rescate(3001, **{}), destino(1, 3001, "FM001B", 100)]
    caso("reinv-solo-los-rescates-de-tipo-reinversion-cuentan", ["FM-CER-52", "matriz:C31"], entrada([3001]), seed=s, colaboradores=L.falla("CrearSolicitudAporteReinversion", "no debe llamarse"), **f)
    s = [R(3001, IdTipoMovimientoFondoMutuo="RESCOM"), destino(1, 3001, "FM001B", 100)]
    caso("reinv-un-rescate-rescom-con-destino-no-reinvierte", ["FM-CER-52", "matriz:C31"], entrada([3001]), seed=s, colaboradores=L.falla("CrearSolicitudAporteReinversion", "no debe llamarse"), **f)
    s = [R(3001), d_misma()]
    caso("reinv-rescate-rechazado-por-custodia-no-reinvierte", ["FM-CER-52", "FM-CER-31", "matriz:C31"], entrada([3001]), seed=s, calcula=calcula_custodia(res_custodia(3001, "RZD", 0)),
         colaboradores=L.falla("CrearSolicitudAporteReinversion", "no debe llamarse"), **f)
    s = [R(3001), R(3002), destino(1, 3001, "FM001B", 100), destino(2, 3002, "FM001B", 100)]
    caso("reinv-solo-el-rescate-no-rechazado-reinvierte", ["FM-CER-52", "FM-CER-31", "matriz:C31"], entrada([3001, 3002]), seed=s, colaboradores=precios_con(s, s3001=10000, s3002=10000),
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0), res_custodia(3002, "APD", 1000000)), custodia=movs(3002, 100), **f)
    s = [R(3001), d_misma()]
    caso("reinv-rescate-sin-cartera-retirada-no-reinvierte", ["FM-CER-52", "FM-CER-40", "matriz:C31"], entrada([3001, 3002]), seed=s + [R(3002)], colaboradores=L.falla("CrearSolicitudAporteReinversion", "no debe llamarse"),
         custodia=dict(con=resp_custodia([mov(3002)])), **f)
    s = [ap(3001, 500), destino(1, 3001, "FM001B", 100)]
    caso("reinv-un-lote-solo-de-aportes-no-llama-a-ar", ["FM-CER-52", "matriz:C31"], entrada([3001]), seed=s, colaboradores=L.falla("CrearSolicitudAporteReinversion", "no debe llamarse"), **f)
    s = [R(3001), R(3002), destino(1, 3001, "FM001B", 100), destino(2, 3002, "FM003A", 100)]
    caso("reinv-dos-rescates-con-destino-van-en-una-sola-llamada-numerados-por-el-orden-del-mapa", ["FM-CER-52", "FM-CER-57", "D-F3B-32", "matriz:C31"], entrada([3002, 3001]), seed=s,
         colaboradores=precios_con(s, s3001=10000, s3002=10000), custodia=movs_de((3001, [60, 40]), (3002, [100])), **f)
    s = [R(3007), R(3008), destino(1, 3007, "FM003A", 100), destino(2, 3008, "FM003A", 100)]
    caso("reinv-el-orden-del-hashmap-pone-el-3008-antes-que-el-3007", ["FM-CER-52", "D-F3B-32", "matriz:C31"], entrada([3007, 3008]), seed=s, colaboradores=precios_con(s, s3007=10000, s3008=10000),
         custodia=movs_de((3007, [100]), (3008, [100])), **f)
    s = [R(3001), destino(1, 3001, "FMXXXX", 100)]
    caso("reinv-destino-sin-serie-conocida-va-por-la-rama-de-otra-familia", ["FM-CER-52", "FM-CER-54", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 100), **f)
    s = [R(3001, IdFamiliaFondo=""), d_misma()]
    caso("reinv-familia-vacia-del-rescate-va-por-la-rama-de-otra-familia", ["FM-CER-52", "FM-CER-54", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 60, 40), **f)
    s = [R(3001, IdFamiliaFondo=""), destino(1, 3001, "FM005A", 100), "UPDATE \"%s\" SET \"IdFamiliaFondo\" = '' WHERE \"IdSerieFondo\" = 'FM005A';" % L.SERIE]
    caso("reinv-familia-vacia-en-el-rescate-y-en-el-destino-no-es-misma-familia", ["FM-CER-52", "FM-CER-54", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 60, 40), **f)
    s = [R(3001, IdFamiliaFondo=None), d_misma()]
    caso("reinv-familia-nula-del-rescate-en-la-vista", ["FM-CER-52", "FM-CER-71", "matriz:C41"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 60, 40), excepcion=True, **f)
    s = [R(3001, IdFamiliaFondo="FAM3"), destino(1, 3001, "FM002A", 100)]
    caso("reinv-misma-familia-pero-fondo-en-otra-moneda-va-por-la-rama-de-otra-moneda", ["FM-CER-52", "FM-CER-54", "FM-CER-56", "matriz:C32"], entrada([3001]), seed=s,
         colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 100), **f)
    s = [R(3001), R(3002), destino(1, 3001, "FM001B", 100)]
    caso("reinv-un-rescate-con-destino-y-otro-sin-llama-solo-con-el-primero", ["FM-CER-52", "matriz:C31"], entrada([3001, 3002]), seed=s, colaboradores=precios_con(s, s3001=10000, s3002=10000),
         custodia=movs_de((3001, [100]), (3002, [100])), **f)

    # ================================================================== FM-CER-53 misma familia y misma moneda
    s = [R(3001), d_misma()]
    caso("reinv-misma-familia-destino-al-100-con-dos-movimientos-60-y-40-un-aporte-por-cartera", ["FM-CER-53", "FM-REI-41", "matriz:C31"], entrada([3001]), seed=s,
         colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 60, 40), **f)
    s = [R(3001), destino(1, 3001, "FM001B", 50)]
    caso("reinv-misma-familia-destino-al-50-por-ciento", ["FM-CER-53", "matriz:C31"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 60, 40), **f)
    s = [R(3001), destino(1, 3001, "FM001B", Num("33.3333"))]
    caso("reinv-misma-familia-porcentaje-con-decimales", ["FM-CER-53", "matriz:C31"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 60, 40), **f)
    s = [R(3001), d_misma()]
    caso("reinv-misma-familia-tres-movimientos-con-residuo-de-redondeo-va-al-primer-aporte", ["FM-CER-53", "FM-CER-55", "D-F3B-13", "matriz:C31"], entrada([3001]), seed=s,
         colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, Num("33.3333"), Num("33.3333"), Num("33.3334")), **f)
    s = [R(3001), d_misma()]
    caso("reinv-misma-familia-tres-movimientos-iguales-el-residuo-es-un-peso", ["FM-CER-53", "FM-CER-55", "D-F3B-13", "matriz:C31"], entrada([3001]), seed=s,
         colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, Num("33.3333"), Num("33.3333"), Num("33.3333")), **f)
    s = [R(3001), d_misma()]
    caso("reinv-misma-familia-un-movimiento-con-monto-cero-no-genera-aporte", ["FM-CER-53", "matriz:C31"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 0, 100), **f)
    caso("reinv-misma-familia-todos-los-movimientos-en-cero-lanza-IndexOutOfBoundsException", ["FM-CER-53", "FM-CER-55", "D-F3B-13", "matriz:C41"], entrada([3001]), seed=s,
         colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 0, 0), excepcion=True, **f)
    s = [R(3001, CostoMonedaLocal=1000000000), d_misma()]
    caso("reinv-misma-familia-ratio-de-34-digitos-con-un-monto-grande", ["FM-CER-53", "matriz:C31"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=333333333.3333), custodia=movs(3001, 1, 1, 1), **f)
    s = [R(3001), d_misma()]
    caso("reinv-misma-familia-fecha-de-origen-y-costo-uf-salen-de-cada-cartera", ["FM-CER-53", "matriz:C31"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=dict(con=resp_custodia([mov(3001, Cantidad=60, CostoInversionEnFactorAjusteARetirar=Num("6.5"), FechaOrigenCompra="2025-12-20"),
                                          mov(3001, Cantidad=40, CostoInversionEnFactorAjusteARetirar=Num("3.25"), FechaOrigenCompra="2026-02-01")])), **f)
    caso("reinv-misma-familia-un-movimiento-sin-fecha-de-origen-lanza-NullPointerException", ["FM-CER-53", "FM-CER-71", "matriz:C41"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=dict(con=resp_custodia([mov(3001, Cantidad=100, FechaOrigenCompra=None)])), excepcion=True, **f)
    s = [R(3001, IdAgrupador=55, IngresoAutomatico="S", SolicitudBatch="S", Glosa="glosa de la reinversion", FolioContrato=777, IndicadorMLT="S", IdMedioDePago="CIN", IdTipoCanalIngreso="OWP"),
         destino(1, 3001, "FM001B", 100, regimen="APV-A")]
    caso("reinv-misma-familia-copia-los-campos-del-rescate-y-el-regimen-del-destino", ["FM-CER-53", "FM-CER-57", "matriz:C33"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 100), **f)
    s = [R(3001), destino(1, 3001, "FM001B", 60), destino(2, 3001, "FM005A", 40)]
    caso("reinv-misma-familia-dos-destinos-60-y-40-uno-por-destino-y-cartera", ["FM-CER-53", "FM-CER-55", "matriz:C31"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 60, 40), **f)
    caso("reinv-misma-familia-dos-destinos-el-ajuste-del-segundo-va-al-primer-aporte-del-primero", ["FM-CER-53", "FM-CER-55", "D-F3B-13", "matriz:C31", "D-F3B-45"], entrada([3001]), seed=s,
         colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, Num("33.3333"), Num("33.3333"), Num("33.3333")), **f)
    s = [R(3001), destino(1, 3001, "FM001B", 100), destino(2, 3001, "FM005A", 0)]
    caso("reinv-misma-familia-un-destino-al-cero-por-ciento-no-genera-aportes-y-el-ajuste-lo-toma-el-primero", ["FM-CER-53", "FM-CER-55", "matriz:C31"], entrada([3001]), seed=s,
         colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 100), **f)
    s = [R(3001, MontoComisionLocal=0, CostoUF=Num("26.5")), d_misma()]
    caso("reinv-misma-familia-el-costo-uf-del-rescate-no-se-usa-en-esta-rama", ["FM-CER-53", "matriz:C31"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 100), **f)

    # ================================================================== FM-CER-54 otra familia o moneda
    s = [R(3001), destino(1, 3001, "FM003A", 100)]
    caso("reinv-otra-familia-un-aporte-por-destino-con-el-costo-uf-del-rescate", ["FM-CER-54", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 60, 40), **f)
    s = [R(3001, CostoUF=Num("26.5")), destino(1, 3001, "FM003A", 100)]
    caso("reinv-otra-familia-usa-el-costo-uf-del-rescate-despues-de-f-d", ["FM-CER-54", "FM-CER-41", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 60, 40), **f)
    s = [R(3001), destino(1, 3001, "FM003A", 70), destino(2, 3001, "FM005A", 30)]
    caso("reinv-otra-familia-dos-destinos-70-y-30", ["FM-CER-54", "FM-CER-55", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 100), **f)
    s = [R(3001), destino(1, 3001, "FM003A", 60), destino(2, 3001, "FM001B", 30)]
    caso("reinv-porcentajes-que-no-suman-100-el-residuo-completo-va-al-primer-aporte", ["FM-CER-54", "FM-CER-55", "D-F3B-13", "matriz:C32", "D-F3B-46"], entrada([3001]), seed=s,
         colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 100), **f)
    s = [R(3001), destino(1, 3001, "FM003A", 70), destino(2, 3001, "FM001B", 50)]
    caso("reinv-porcentajes-que-suman-mas-de-100-el-ajuste-es-negativo", ["FM-CER-54", "FM-CER-55", "D-F3B-13", "matriz:C32", "D-F3B-46"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 100), **f)
    s = [R(3001), destino(1, 3001, "FM003A", 0)]
    caso("reinv-otra-familia-destino-al-cero-por-ciento-agrega-un-aporte-en-cero", ["FM-CER-54", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 100), **f)
    s = [R(3001), destino(1, 3001, "FM003A", 33), destino(2, 3001, "FM001B", 33), destino(3, 3001, "FM005A", 34)]
    caso("reinv-tres-destinos-numerados-1-2-3-en-el-orden-de-la-consulta", ["FM-CER-54", "FM-CER-53", "FM-CER-57", "matriz:C31"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 60, 40), **f)
    s = [R(3001), destino(1, 3001, "FM001B", 60), destino(2, 3001, "FM003A", 40)]
    caso("reinv-mezcla-de-ramas-en-el-mismo-rescate", ["FM-CER-53", "FM-CER-54", "FM-CER-55", "D-F3B-13", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 60, 40), **f)
    s = [R(3001, CostoMonedaLocal=1000003), destino(1, 3001, "FM003A", Num("33.3333")), destino(2, 3001, "FM001B", Num("33.3333")), destino(3, 3001, "FM005A", Num("33.3334"))]
    caso("reinv-otra-familia-tres-destinos-con-residuo-de-redondeo", ["FM-CER-54", "FM-CER-55", "D-F3B-13", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 100), **f)
    s = [RD(3001), destino(1, 3001, "FM004A", 100)]
    caso("reinv-fondo-usd-a-fondo-usd-misma-familia-conserva-las-fechas-de-certificacion", ["FM-CER-53", "FM-CER-56", "matriz:C32"], entrada([3001]), seed=s,
         colaboradores=precios_con(s, s3001=10.1234), custodia=movs(3001, 100), **f)

    # ================================================================== FM-CER-56 fechas
    s = [R(3001, FechaAceptacion="2026-02-27", FechaLiquidacion="2026-03-05"), d_misma()]
    caso("reinv-fechas-misma-moneda-nacional-usan-la-liquidacion-y-la-aceptacion-del-spot", ["FM-CER-56", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 100), **f)
    s = [RD(3001, FechaAceptacion="2026-02-27", FechaLiquidacion="2026-03-05", IdSerieFondoDestino="FM004A"), destino(1, 3001, "FM004A", 100)]
    caso("reinv-fechas-internacional-a-internacional-usa-la-certificacion-en-la-emision", ["FM-CER-56", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10.1234),
         custodia=movs(3001, 100), **f)
    s = [RD(3001, FechaAceptacion="2026-02-27", FechaLiquidacion="2026-03-05", IdSerieFondoDestino="FM004A"), destino(1, 3001, "FM004A", 100, switch="S")]
    caso("reinv-fechas-internacional-con-switch-usa-la-liquidacion-en-la-emision", ["FM-CER-56", "FM-CER-58", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10.1234),
         custodia=movs(3001, 100), **f)
    s = [RD(3001, FechaAceptacion="2026-02-27", FechaLiquidacion="2026-03-05", IdSerieFondoDestino="FM002A"), destino(1, 3001, "FM002A", 100)]
    caso("reinv-fechas-internacional-a-la-misma-serie-usa-la-certificacion", ["FM-CER-56", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10.1234),
         custodia=movs(3001, 100), **f)
    s = [RD(3001, FechaAceptacion="2026-02-27", FechaLiquidacion="2026-03-05", IdMonedaSistema="USD"), destino(1, 3001, "FM004A", 100)]
    s = [RD(3001, FechaAceptacion="2026-02-27", FechaLiquidacion="2026-03-05"), destino(1, 3001, "FM004A", 100)]
    caso("reinv-las-banderas-internacionales-salen-de-la-serie-destino-de-la-solicitud-y-no-de-la-tabla-de-destinos", ["FM-CER-56", "D-F3B-52", "matriz:C32"], entrada([3001]), seed=s,
         colaboradores=precios_con(s, s3001=10.1234), custodia=movs(3001, 100), **f)
    s = [RD(3001, FechaAceptacion="2026-02-27", FechaLiquidacion="2026-03-05", IdMonedaSistema="USD", IdSerieFondoDestino="FM004A"), destino(1, 3001, "FM004A", 100)]
    caso("reinv-fecha-spot-con-moneda-de-sistema-igual-a-la-del-rescate-usa-la-aceptacion", ["FM-CER-56", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10.1234),
         custodia=movs(3001, 100), **f)
    s = [R(3001, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0, FechaAceptacion="2026-02-27", FechaLiquidacion="2026-03-05"), destino(1, 3001, "FM003A", 100)]
    caso("reinv-rescate-en-usd-fecha-spot-es-la-certificacion-y-la-liquidacion", ["FM-CER-56", "matriz:C32"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 100), **f)
    s = [R(3001, FechaAceptacion="2026-02-27"), destino(1, 3001, "FM003A", 100)]
    caso("reinv-otra-familia-fechas-del-rescate-origen-y-fecha-de-emision-por-moneda", ["FM-CER-56", "FM-CER-54", "matriz:C32"], entrada([3001], fecha="2026-03-04"),
         seed=[R(3001, FechaAceptacion="2026-02-27", FechaCertificacion="2026-03-04", FechaLiquidacion="2026-03-06", FechaValorCuota="2026-03-04"), destino(1, 3001, "FM003A", 100)],
         colaboradores=precios_con([R(3001, FechaValorCuota="2026-03-04")], s3001=10000), custodia=movs(3001, 100), **f)

    # ================================================================== FM-CER-57 llamada a AR y mapeo de ids
    s = [R(3001), d_misma()]
    caso("ar-simulacion-S-se-llama-igual-con-simular-N-canal-AGE-y-las-reglas-de-la-entrada", ["FM-CER-57", "D-F3B-01", "matriz:C33", "matriz:C40"], entrada([3001], sim="S", p_UsaReglasNegocio="S"), seed=s,
         colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 100), **f)
    caso("ar-las-reglas-de-negocio-N-de-la-entrada-pasan-a-ar", ["FM-CER-57", "matriz:C33"], entrada([3001]), seed=s, colaboradores=precios_con(s, s3001=10000), custodia=movs(3001, 100), **f)
    caso("ar-el-canal-de-la-entrada-no-cambia-el-canal-AGE-de-la-llamada", ["FM-CER-57", "matriz:C33"], entrada([3001], canal="OWP"), seed=s, colaboradores=precios_con(s, s3001=10000),
         custodia=movs(3001, 100), **f)
    s = [R(3001), R(3002), destino(1, 3001, "FM001B", 100), destino(2, 3002, "FM001B", 100)]
    caso("ar-error-con-el-correlativo-del-aporte-se-mapea-al-rescate-y-lo-rechaza", ["FM-CER-57", "matriz:C33"], entrada([3001, 3002]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000, s3002=10000), **ar(resp_ar([], [msg_ar(2)], 1))), custodia=movs_de((3001, [100]), (3002, [100])), **f)
    caso("ar-error-del-primer-correlativo-rechaza-al-primero-y-el-otro-se-certifica", ["FM-CER-57", "FM-CER-69", "matriz:C33"], entrada([3001, 3002]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000, s3002=10000), **ar(resp_ar([], [msg_ar(1)], 1))), custodia=movs_de((3001, [100]), (3002, [100])), **f)
    s = [R(3001), d_misma()]
    caso("ar-error-con-correlativo-que-no-existe-queda-con-su-id-y-no-rechaza", ["FM-CER-57", "matriz:C33"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([], [msg_ar(99)], 1))), custodia=movs(3001, 100), **f)
    caso("ar-error-con-id-negativo-no-se-mapea-y-no-rechaza-a-nadie", ["FM-CER-57", "matriz:C33"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([], [msg_ar(-1)], 1))), custodia=movs(3001, 100), **f)
    caso("ar-error-con-id-igual-al-del-rescate-lo-rechaza-aunque-no-sea-un-correlativo", ["FM-CER-57", "matriz:C33"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([], [msg_ar(3001)], 1))), custodia=movs(3001, 100), **f)
    caso("ar-mensaje-ERROR-sin-id-lanza-NullPointerException", ["FM-CER-57", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([], [msg_ar(ABSENT)], 1))), custodia=movs(3001, 100), excepcion=True, **f)
    caso("ar-mensaje-sin-id-que-no-es-ERROR-no-lanza", ["FM-CER-57", "matriz:C33"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([], [msg_ar(ABSENT, "UI_AVISO", "aviso", "ADVERTENCIA")], 0))), custodia=movs(3001, 100), **f)
    caso("ar-mensaje-que-no-es-ERROR-no-rechaza-ni-se-agrega", ["FM-CER-57", "matriz:C33"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([], [msg_ar(1, "UI_AVISO", "aviso de ar", "ADVERTENCIA")], 0))), custodia=movs(3001, 100), **f)
    caso("ar-mensaje-ERROR-en-minuscula-no-se-reconoce-por-equals", ["FM-CER-57", "matriz:C33"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([], [msg_ar(1, tipo="error")], 1))), custodia=movs(3001, 100), **f)
    caso("ar-el-contador-de-ar-se-suma-aunque-no-haya-mensajes", ["FM-CER-57", "matriz:C33"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([], [], 2))), custodia=movs(3001, 100), **f)
    caso("ar-dos-errores-con-el-mismo-id-rechazan-solo-al-primero", ["FM-CER-57", "matriz:C33"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([], [msg_ar(1, descripcion="primero"), msg_ar(1, "UI_OTRO", "segundo")], 2))), custodia=movs(3001, 100), **f)
    caso("ar-falla-y-propaga-la-excepcion", ["FM-CER-57", "matriz:C33"], entrada([3001]), seed=s, colaboradores=dict(**precios_con(s, s3001=10000), **L.falla("CrearSolicitudAporteReinversion", "ar no disponible")),
         custodia=movs(3001, 100), excepcion=True, **f)
    caso("ar-el-rechazo-deja-la-custodia-la-cuenta-ya-escritas", ["FM-CER-57", "FM-CER-42", "D-F3B-03", "matriz:C33"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([], [msg_ar(1)], 1))), custodia=movs(3001, 100), **f)
    s = [R(3001), R(3002), destino(1, 3001, "FM001B", 100), destino(2, 3002, "FM003A", 100)]
    caso("ar-resultados-con-correlativo-se-reemplazan-por-el-id-del-rescate", ["FM-CER-57", "FM-CER-58", "matriz:C33"], entrada([3001, 3002]), seed=s + [sw(4001, 3001), sw(4002, 3002)],
         colaboradores=dict(**precios_con(s + [sw(4001, 3001), sw(4002, 3002)], s3001=10000, s3002=10000), **ar(resp_ar([res_ar(1, 4001, "S"), res_ar(2, 4002, "S")]))),
         custodia=movs_de((3001, [100]), (3002, [100])), **f)
    s = [R(3001), d_misma(), COMP_S]
    caso("ar-sigue-a-la-facturacion-y-precede-a-spot-en-el-orden-de-las-llamadas", ["FM-CER-57", "FM-CER-51", "FM-CER-59", "matriz:C33"], entrada([3001, 3002, 3003]),
         seed=s + [R(3002, MontoComisionLocal=11900, IdTipoMovimientoFondoMutuo="RESNORM", IdFamiliaFondo=""), _rescate(3003, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0), SPOT_S],
         colaboradores=dict(**precios_con(s + [R(3002), _rescate(3003)], s3001=10000), **facturacion(resp_mov_comp([mov_sal(3002, 7002)]))), custodia=movs_de((3001, [100]), (3002, [1]), (3003, [1])), **f)

    # ================================================================== FM-CER-58 aportes de switch
    s = [R(3001), d_misma(), sw(4001, 3001)]
    caso("switch-resultado-S-se-certifica-por-recursion-con-el-aporte-nombrado", ["FM-CER-58", "FM-CER-63", "matriz:C34"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S", canal="OPT")]))), custodia=movs(3001, 100), **f)
    caso("switch-resultado-N-no-se-agrega-y-no-hay-recursion", ["FM-CER-58", "matriz:C34"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "N")]))), custodia=movs(3001, 100), **f)
    caso("switch-resultado-minuscula-s-no-se-agrega-por-equals", ["FM-CER-58", "matriz:C34"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "s")]))), custodia=movs(3001, 100), **f)
    caso("switch-resultado-sin-switch-lanza-NullPointerException", ["FM-CER-58", "D-F3B-30", "matriz:C41"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, ABSENT_SWITCH)]))), custodia=movs(3001, 100), excepcion=True, **f)
    caso("switch-resultado-con-switch-nulo-lanza-NullPointerException", ["FM-CER-58", "D-F3B-30", "matriz:C41"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, None)]))), custodia=movs(3001, 100), excepcion=True, **f)
    s2 = s + [sw(4002, 3001, IdTipoCanalIngreso="OWP")]
    caso("switch-dos-resultados-S-van-en-un-solo-llamado-al-hijo-en-orden", ["FM-CER-58", "FM-CER-63", "matriz:C34"], entrada([3001]), seed=s2,
         colaboradores=dict(**precios_con(s2, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S", canal="OPT"), res_ar(2, 4002, "S", canal="OWP")]))), custodia=movs(3001, 100), **f)
    caso("switch-solo-los-resultados-S-de-varios-van-al-hijo", ["FM-CER-58", "matriz:C34"], entrada([3001]), seed=s2,
         colaboradores=dict(**precios_con(s2, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "N"), res_ar(2, 4002, "S", canal="OWP")]))), custodia=movs(3001, 100), **f)
    caso("switch-el-aporte-de-un-rescate-rechazado-por-ar-igual-se-certifica", ["FM-CER-58", "FM-CER-57", "D-F3B-30", "matriz:C34"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")], [msg_ar(1)], 1))), custodia=movs(3001, 100), **f)
    caso("switch-resultado-con-canal-nulo-lleva-el-canal-nulo-al-hijo", ["FM-CER-58", "FM-CER-63", "matriz:C34"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S", canal=None)]))), custodia=movs(3001, 100), **f)
    caso("switch-resultado-con-canal-vacio", ["FM-CER-58", "FM-CER-63", "matriz:C34"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S", canal="")]))), custodia=movs(3001, 100), **f)
    caso("switch-resultado-sin-id-de-aporte-lleva-el-id-nulo-al-hijo", ["FM-CER-58", "FM-CER-63", "matriz:C34"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, ABSENT_SWITCH, "S")]))), custodia=movs(3001, 100), **f)
    caso("switch-ar-sin-resultados-no-llama-al-hijo", ["FM-CER-58", "FM-CER-63", "matriz:C34"], entrada([3001]), seed=s, colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar())),
         custodia=movs(3001, 100), **f)

    # ================================================================== FM-CER-63 certificacion recursiva
    caso("switch-el-hijo-recibe-la-cabecera-del-padre-con-owp-N-y-simulado-N", ["FM-CER-63", "matriz:C34"], entrada([3001], canal="AGE"), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")]))), custodia=movs(3001, 100), **f)
    caso("switch-simulacion-S-el-hijo-recibe-simulado-S-y-sus-resultados-salen-con-id-cero", ["FM-CER-63", "D-F3B-01", "matriz:C34", "matriz:C40"], entrada([3001], sim="S", p_UsaReglasNegocio="S"), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")]))), custodia=movs(3001, 100), **f)
    caso("switch-las-reglas-de-negocio-de-la-entrada-llegan-al-hijo", ["FM-CER-63", "matriz:C34"], entrada([3001], p_UsaReglasNegocio="S"), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")]))), custodia=movs(3001, 100), **f)
    caso("switch-el-hijo-sin-canal-de-entrada-lo-lleva-nulo", ["FM-CER-63", "matriz:C34"], entrada([3001]), seed=s, colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")]))),
         custodia=movs(3001, 100), **f)
    sh = [R(3001), d_misma(), sw(4001, 3001, EstadoLiquidacion="LIQ")]
    caso("switch-el-hijo-con-un-aporte-ya-liquidado-lo-certifica-y-llama-a-su-custodia", ["FM-CER-63", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=dict(**precios_con(sh, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")]))), custodia=movs(3001, 100), **f)
    sh = [R(3001), d_misma(), sw(4001, 3001, MueveCta="S", MontoPago=Num("1000000"), MontoPagoLocal=Num("1000000"))]
    caso("switch-el-hijo-mueve-la-cuenta-de-inversion-del-aporte", ["FM-CER-63", "FM-CER-29", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=dict(**precios_con(sh, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")]))), custodia=movs(3001, 100), **f)
    caso("switch-el-contador-y-los-mensajes-del-hijo-se-suman-a-los-del-padre", ["FM-CER-63", "matriz:C34"], entrada([3001]), seed=s,
         colaboradores=dict(**precios_con(s, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")]))),
         custodia=dict(con=movs(3001, 100)["con"], aportes=resp_custodia([], [err(4001, "UI_ERROR_CUSTODIA_APORTE", "Error de custodia en el aporte")], 1)), **f)
    sh = [R(3001), d_misma(), sw(4001, 3001, estado="CRT")]
    caso("switch-el-hijo-rechaza-un-aporte-que-no-esta-ingresado-y-cuenta-uno", ["FM-CER-63", "FM-CER-14", "FM-CER-06", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=dict(**precios_con(sh, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")]))), custodia=movs(3001, 100), **f)
    sh = [R(3001), d_misma(), sw(4001, 3001, FechaCertificacion="2026-03-09")]
    caso("switch-el-hijo-rechaza-por-fecha-de-certificacion-distinta", ["FM-CER-63", "FM-CER-07", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=dict(**precios_con(sh, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")]))), custodia=movs(3001, 100), **f)
    sh = [R(3001), d_misma(), sw(4001, 3001), sw(4002, 3001, estado="CRT")]
    caso("switch-el-hijo-certifica-uno-y-rechaza-otro-y-sus-resultados-se-agregan", ["FM-CER-63", "FM-CER-69", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=dict(**precios_con(sh, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S"), res_ar(2, 4002, "S")]))), custodia=movs(3001, 100), **f)
    sh = [R(3001), d_misma(), sw(4001, 3001), R(3002), destino(2, 3002, "FM001B", 100)]
    caso("switch-el-hijo-que-llega-a-un-rescate-llama-otra-vez-a-ar-y-la-recursion-termina-con-la-respuesta-vacia", ["FM-CER-63", "FM-CER-58", "matriz:C34", "D-F3B-50"], entrada([3001]), seed=sh,
         colaboradores=dict(**precios_con(sh, s3001=10000, s3002=10000),
                            **ar(resp_ar(), [{"una_vez": True, "respuesta": resp_ar([res_ar(1, 3002, "S")])}])), custodia=movs_de((3001, [100]), (3002, [100])), **f)
    sh = [R(3001), d_misma(), sw(4001, 3001)]
    caso("switch-el-hijo-no-ve-el-aporte-si-no-esta-en-la-vista-y-sale-por-la-salida-temprana", ["FM-CER-63", "FM-CER-14", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=dict(**precios_con(sh, s3001=10000), **ar(resp_ar([res_ar(1, 4999, "S")]))), custodia=movs(3001, 100), **f)
    caso("switch-la-facturacion-del-padre-precede-al-hijo-en-el-orden-de-las-llamadas", ["FM-CER-63", "FM-CER-50", "matriz:C34"], entrada([3001, 3002]),
         seed=[R(3001), d_misma(), sw(4001, 3001), _rescate(3002, MontoComisionLocal=11900)],
         colaboradores=dict(**precios_con([R(3001), sw(4001, 3001), _rescate(3002)], s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")])), **facturacion(resp_mov_comp([mov_sal(3002, 7002)]))),
         custodia=movs_de((3001, [100]), (3002, [1])), **f)
    caso("switch-spot-del-padre-precede-al-hijo-en-el-orden-de-las-llamadas", ["FM-CER-63", "FM-CER-59", "matriz:C34"], entrada([3001, 3002]),
         seed=[R(3001), d_misma(), sw(4001, 3001), _rescate(3002, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0), SPOT_S],
         colaboradores=dict(**precios_con([R(3001), sw(4001, 3001), _rescate(3002)], s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")]))), custodia=movs_de((3001, [100]), (3002, [1])), **f)

    # ================================================================== FM-CER-64 monto en moneda de sistema del rescate de reinversion internacional
    # the aportes are in a USD fund paid in USD while the system currency is CLP: after F-B the system currency of the solicitud is CLP != USD (the seed value is overwritten), so the rule applies
    o = origen(3001, IdTipoMovimientoFondoMutuo="RESREINV", MontoMonedaSistema=111)
    s = o + [aporte_usd(5001, 3001)]
    caso("monto-sistema-un-aporte-internacional-escribe-su-suma-en-el-rescate-de-origen", ["FM-CER-64", "matriz:C37"], entrada([5001]), seed=s, **f)
    s = o + [aporte_usd(5001, 3001), solo_tabla(5002, "APO", "CRT", IdTipoMovimientoFondoMutuo="APOREINV", IdSolicitudAporteRescateRelacionado=3001, MontoMonedaSistema=250000)]
    caso("monto-sistema-la-suma-incluye-los-aportes-hermanos-que-no-estan-en-el-lote", ["FM-CER-64", "matriz:C37", "D-F3B-49"], entrada([5001]), seed=s, **f)
    s = o + [aporte_rei(5001, 3001, MontoMonedaSistema=400000)]
    caso("monto-sistema-con-moneda-de-sistema-igual-a-la-del-aporte-no-escribe", ["FM-CER-64", "matriz:C37", "D-F3B-48"], entrada([5001]), seed=s, **f)
    s = o + [aporte_usd(5001, 0)]
    caso("monto-sistema-sin-rescate-relacionado-no-escribe", ["FM-CER-64", "matriz:C37"], entrada([5001]), seed=s, **f)
    s = o + [ap(5001, 500, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="USD", IdMonedaSistema="CLP", MontoMonedaSistema=400000, CostoMonedaLocal=5000, Monto=5000,
                IdSolicitudAporteRescateRelacionado=3001)]
    caso("monto-sistema-un-aporte-que-no-es-de-reinversion-no-escribe", ["FM-CER-64", "matriz:C37"], entrada([5001]), seed=s, **f)
    s = o + [aporte_usd(5001, 3001), aporte_usd(5002, 3001, estado="CRT", MontoMonedaSistema=999)]
    caso("monto-sistema-un-aporte-rechazado-por-estado-no-escribe", ["FM-CER-64", "FM-CER-06", "matriz:C37"], entrada([5001, 5002]), seed=s, **f)
    s = o + origen(3002, IdTipoMovimientoFondoMutuo="RESREINV", MontoMonedaSistema=222) + [aporte_usd(5001, 3001), aporte_usd(5002, 3002, CostoMonedaLocal=7000, Monto=7000)]
    caso("monto-sistema-dos-rescates-de-origen-se-actualizan-con-sus-propias-sumas", ["FM-CER-64", "matriz:C37"], entrada([5001, 5002]), seed=s, **f)
    s = o + [aporte_usd(5001, 3001), aporte_usd(5002, 3001, CostoMonedaLocal=3000, Monto=3000)]
    caso("monto-sistema-dos-aportes-del-mismo-rescate-se-suman-y-el-rescate-se-lee-una-vez", ["FM-CER-64", "matriz:C37"], entrada([5001, 5002]), seed=s, **f)
    s = o + [solo_tabla(0, "RES", "CRT", MontoMonedaSistema=7), aporte_usd(5001, 0)]
    caso("monto-sistema-sin-rescate-relacionado-aunque-exista-una-fila-con-id-cero-no-escribe", ["FM-CER-64", "matriz:C37"], entrada([5001]), seed=s, **f)
    s = [aporte_usd(5001, 3999)]
    caso("monto-sistema-sin-fila-del-rescate-de-origen-el-update-no-toca-nada", ["FM-CER-64", "matriz:C37"], entrada([5001]), seed=s, **f)
    s = o + [aporte_usd(5001, 3001, CostoMonedaLocal=Num("5000.37"), Monto=Num("5000.37"))]
    caso("monto-sistema-el-aporte-con-decimales-se-formatea-antes-de-sumar", ["FM-CER-64", "matriz:C37"], entrada([5001]), seed=s, **f)
    s = o + [aporte_usd(5001, 3001)]
    caso("monto-sistema-simulacion-S-escribe-igual-porque-el-final-fuerza-el-contador", ["FM-CER-64", "D-F3B-01", "matriz:C37", "matriz:C40"], entrada([5001], sim="S"), seed=s, **f)
    s = o + [aporte_usd(5001, 3001), solo_tabla(5002, "APO", "CRT", IdTipoMovimientoFondoMutuo="APOREINV", IdSolicitudAporteRescateRelacionado=3001, MontoMonedaSistema=Num("250000.123456"))]
    caso("monto-sistema-la-suma-conserva-los-decimales-de-los-hermanos", ["FM-CER-64", "matriz:C37", "D-F3B-49"], entrada([5001]), seed=s, **f)
    s = o + origen(3002, IdTipoMovimientoFondoMutuo="RESREINV", MontoMonedaSistema=222) + [aporte_usd(5001, 3001), aporte_usd(5002, 3002, estado="CRT", MontoMonedaSistema=999)]
    caso("monto-sistema-el-aporte-rechazado-de-otro-rescate-no-actualiza-a-su-origen", ["FM-CER-64", "FM-CER-06", "matriz:C37"], entrada([5001, 5002]), seed=s, **f)
    s = o + [aporte_usd(5001, 3001), aporte_usd(5002, 3001, estado="CRT", MontoMonedaSistema=999)]
    caso("monto-sistema-el-aporte-rechazado-igual-suma-su-valor-de-la-tabla", ["FM-CER-64", "FM-CER-06", "matriz:C37"], entrada([5001, 5002]), seed=s, **f)
    s = o + [aporte_usd(5001, 3001), aporte_usd(5002, 3001, IdSerieFondo="FM001B", IdInstrumento=901, IdMonedaFondo="CLP", IdMoneda="CLP", IdMonedaSistema="CLP", CostoMonedaLocal=3000, Monto=3000)]
    caso("monto-sistema-solo-los-aportes-de-moneda-distinta-activan-pero-la-suma-incluye-a-todos", ["FM-CER-64", "matriz:C37"], entrada([5001, 5002]), seed=s, **f)


ABSENT_SWITCH = ABSENT
