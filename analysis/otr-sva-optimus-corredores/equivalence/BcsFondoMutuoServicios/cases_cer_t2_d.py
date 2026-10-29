"""FMM-CER T2 cases of front F-D (RESCATES: custody and costs): FM-CER-31..33 (custodia disponible: rechazo, advertencia y ajuste, rescate total), FM-CER-34 (GeneraSpot), FM-CER-35 (traspaso),
FM-CER-36 (retencion APV), FM-CER-37 (comision), FM-CER-38 (retencion valida), FM-CER-39, 40 (retiro de custodia en dos llamadas, rescate sin cartera) and FM-CER-41 (costo UF, spread y
rentabilidad real).

Legacy: `CertificarSolicitudAporteRescate.logica` C:582-596 (custodia disponible, GeneraSpot, traspaso, retencion, comision, format, retencion valida) and C:644-676 (retiro de custodia, sin
cartera, costo UF / spread / rentabilidad). Every case is recorded with the unmodified legacy (modo completo); `excepcion=True` marks a case that throws inside F-D."""
import cases_cer_t2 as T
from cases_cer_t2 import caso, entrada, ap, aporte, origen, mov, err, resp_custodia, res_custodia, calcula_custodia, paridad_utm, paridad_comision, L, Num, config, param, ins
from cases_cer_lib import solo_tabla
from cases3a_lib import tramo, comision_maxima
from cases3a_com1 import lote, custodia as flujo_custodia

SIS = "2026-03-02"


def rescate(id_, **c):
    """a rescate of this front: paid by a medium other than the cuenta de inversion (TRA) unless the case says otherwise, so that the legacy sends it to Tesoreria (or Spot) and the golden
    shows, in the pojo it copies from the solicitud, the EXACT amounts that F-D left (no H2 rounding)"""
    c.setdefault("IdMedioDePago", "TRA")
    return T.rescate(id_, **c)
APV = dict(PropositoPortafolio="PROPOSITOAPV", IdMedioDePago="TRA")
COMISION_S = [param("CALCULAR_COMISION_CERTIFICACION", "S")]
TRAMOS = tramo(1, "FM001A", "SALIDA", 0, 30, 2.0) + [comision_maxima("FM001A")]
LOTE = flujo_custodia(lote(5000, "2026-02-10"))


def apv(r="APV-B", **c):
    d = dict(APV)
    d.update(IdRegimenTributario=r)
    d.update(c)
    return d


def adv(id_, disponible=100, cuotas=100, **c):
    """a CalculaCustodiaRescate ADV result: the custody only covers `cuotas`; the amounts are the ones of those quotas"""
    d = dict(Monto=150003.7, NumeroCuotas=cuotas, ValorCuota=1500.0037, MontoDestino=150003.7, ValorParidad=1, CostoEnFactorAjuste=3.8461, ValorFactorAjuste=39000.5, MontoMonedaSistema=150003.7)
    d.update(c)
    return res_custodia(id_, "ADV", disponible, **d)


def generar():
    # ================================================================== FM-CER-31 custodia disponible: rechazo
    caso("custodia-suficiente-no-cambia-los-montos", ["FM-CER-31", "matriz:C19"], entrada([3001]), seed=[rescate(3001)],
         calcula=calcula_custodia(res_custodia(3001, "APD", 1000000, Monto=111, NumeroCuotas=1, ValorCuota=1, MontoDestino=111, ValorParidad=1, MontoMonedaSistema=111)), front="F-D")
    caso("custodia-RZD-rechaza-con-su-descripcion", ["FM-CER-31", "D-F3B-06", "matriz:C19"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0), res_custodia(3002, "APD", 1000000)), front="F-D")
    caso("custodia-ADV-con-omnibus-procesado-rechaza", ["FM-CER-31", "matriz:C19"], entrada([3001]), seed=[rescate(3001, IdOmnibus=55)],
         calcula=calcula_custodia(adv(3001)), front="F-D")
    caso("custodia-sin-resultado-lanza-NPE-y-aborta-el-lote", ["FM-CER-31", "D-F3B-05", "matriz:C19"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         calcula=calcula_custodia(res_custodia(3001, "APD", 1000000)), excepcion=True, front="F-D")
    caso("custodia-sin-ningun-resultado-lanza-NPE", ["FM-CER-31", "D-F3B-05"], entrada([3001]), seed=[rescate(3001)], calcula=calcula_custodia(), excepcion=True, front="F-D")
    caso("custodia-RZD-sin-cantidad-disponible-lanza-NPE", ["FM-CER-31", "D-F3B-05"], entrada([3001]), seed=[rescate(3001)],
         calcula=calcula_custodia({"IdSolicitudAporteRescate": 3001, "Estado": "RZD"}), excepcion=True, front="F-D")
    caso("custodia-estado-desconocido-se-trata-como-aprobado", ["FM-CER-31", "FM-CER-32", "D-F3B-39"], entrada([3001]), seed=[rescate(3001)],
         calcula=calcula_custodia(res_custodia(3001, "XYZ", 5)), front="F-D")
    caso("custodia-el-calculo-falla-y-propaga", ["FM-CER-31", "matriz:C19"], entrada([3001]), seed=[rescate(3001)], colaboradores=L.falla("CalculaCustodiaRescate", "custodia no disponible"),
         excepcion=True, front="F-D")
    caso("custodia-solo-ve-los-rescates-vigentes-en-orden-de-id", ["FM-CER-31", "FM-CER-19", "matriz:C44"], entrada([3003, 3001, 3002]),
         seed=[rescate(3001), rescate(3002, estado="CRT"), rescate(3003)], front="F-D")
    caso("todos-los-rescates-rechazados-por-F-A-no-llaman-a-custodia", ["FM-CER-31", "FM-CER-06"], entrada([3001, 3002]),
         seed=[rescate(3001, estado="CRT"), aporte(3002)], front="F-D")

    # ================================================================== FM-CER-32 advertencia y ajuste
    caso("custodia-ADV-ajusta-los-montos-y-advierte", ["FM-CER-32", "matriz:C19"], entrada([3001]), seed=[rescate(3001)], calcula=calcula_custodia(adv(3001)), front="F-D")
    caso("custodia-ADV-con-omnibus-en-cero-sigue-y-ajusta", ["FM-CER-32", "matriz:C19"], entrada([3001]), seed=[rescate(3001, IdOmnibus=0)], calcula=calcula_custodia(adv(3001)), front="F-D")
    caso("custodia-ADV-con-omnibus-nulo-sigue-y-ajusta", ["FM-CER-32", "matriz:C19"], entrada([3001]), seed=[rescate(3001, IdOmnibus=None)], calcula=calcula_custodia(adv(3001)), front="F-D")
    caso("custodia-ADV-del-rescate-total-ajusta", ["FM-CER-32", "FM-CER-33"], entrada([3001]), seed=[rescate(3001, CalculoPorMontoCuota="T", NumeroCuotas=Num("120"), CostoMonedaLocal=0, Monto=0)],
         calcula=calcula_custodia(adv(3001)), front="F-D")
    caso("lote-mixto-aprobado-advertido-y-rechazado-por-custodia", ["FM-CER-31", "FM-CER-32", "FM-CER-69", "matriz:C19"], entrada([3003, 3001, 3002, 3004]),
         seed=[rescate(3001), rescate(3002), rescate(3003), aporte(3004)],
         calcula=calcula_custodia(res_custodia(3001, "APD", 1000000), adv(3002), res_custodia(3003, "RZD", 0)), front="F-D")

    # ================================================================== FM-CER-33 rescate total
    total = dict(CalculoPorMontoCuota="T", NumeroCuotas=Num("100.25"), CostoMonedaLocal=0, Monto=0)
    toma = res_custodia(3001, "APD", 1000000, Monto=150000, NumeroCuotas=99.5, ValorCuota=1507.5377, MontoDestino=150000, ValorParidad=1, CostoEnFactorAjuste=3.8461, ValorFactorAjuste=39000.5,
                        MontoMonedaSistema=150000)
    caso("total-con-omnibus-en-cero-toma-los-montos-de-custodia", ["FM-CER-33", "matriz:C19"], entrada([3001]), seed=[rescate(3001, IdOmnibus=0, **total)], calcula=calcula_custodia(toma), front="F-D")
    caso("total-con-omnibus-nulo-toma-los-montos-de-custodia", ["FM-CER-33", "matriz:C19"], entrada([3001]), seed=[rescate(3001, IdOmnibus=None, **total)], calcula=calcula_custodia(toma), front="F-D")
    caso("total-con-omnibus-procesado-conserva-los-montos", ["FM-CER-33", "matriz:C19"], entrada([3001]), seed=[rescate(3001, IdOmnibus=77, **total)], calcula=calcula_custodia(toma), front="F-D")
    caso("no-total-ignora-los-montos-de-custodia-aprobada", ["FM-CER-33"], entrada([3001]), seed=[rescate(3001, IdOmnibus=0)], calcula=calcula_custodia(toma), front="F-D")

    # ================================================================== FM-CER-34 GeneraSpot
    spot_s = [param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")]
    usd = dict(IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0)
    caso("spot-S-rescate-en-usd-pagado-en-clp", ["FM-CER-34", "matriz:C20"], entrada([3001]), seed=[rescate(3001, **usd), spot_s], front="F-D")
    caso("spot-S-rescate-nn-pagado-en-clp", ["FM-CER-34", "matriz:C20"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCNN"), spot_s], front="F-D")
    caso("spot-parametro-N-no-marca", ["FM-CER-34", "matriz:C20"], entrada([3001]), seed=[rescate(3001, **usd)], front="F-D")
    caso("spot-parametro-minuscula-s-marca-por-equalsignorecase", ["FM-CER-34", "matriz:C20"], entrada([3001]), seed=[rescate(3001, **usd), param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "s")], front="F-D")
    caso("spot-S-pagado-en-usd-no-marca", ["FM-CER-34", "matriz:C20"], entrada([3001]), seed=[rescate(3001, **dict(usd, IdMonedaPago="USD")), spot_s], front="F-D")
    caso("spot-S-rescate-normal-en-clp-no-marca", ["FM-CER-34", "matriz:C20"], entrada([3001]), seed=[rescate(3001), spot_s], front="F-D")
    caso("spot-S-un-rescate-con-spot-y-otro-sin", ["FM-CER-34", "matriz:C20"], entrada([3001, 3002]), seed=[rescate(3001, **usd), rescate(3002), spot_s], front="F-D")

    # ================================================================== FM-CER-35 rescate relacionado con un traspaso
    tras_apv = [ins(L.TRA_APV, IdTraspaso=71, RazonRetiro="TRA", IdPersona=101, DocumentoEntidad="88888881-1"), ins(L.TRA_ORDEN, IdSolicitudAporteRescate=3001, IdTraspaso=71),
                ins("BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona", IdPersona=101, NombreCompleto="Persona APV", DocumentoIdentidad="88888881-1")]
    tras_fm = [ins(L.ENT_REC, IdEntidadRecaudadora=5, Nombre="Entidad", IdPersona=202), ins(L.TRA_FM, IdTraspaso=81, IdTipoMovimientoTraspaso="TRA", IdEntidadRecaudadora=5),
               ins(L.TRA_DET, IdSolicitudAporteRescate=3001, IdTraspaso=81)]
    caso("traspaso-apv-de-la-solicitud-marca-la-relacion", ["FM-CER-35", "matriz:C21"], entrada([3001]), seed=[rescate(3001), tras_apv], front="F-D")
    caso("traspaso-no-apv-de-la-solicitud-marca-la-relacion", ["FM-CER-35", "matriz:C21"], entrada([3001]), seed=[rescate(3001), tras_fm], front="F-D")
    caso("traspaso-de-la-solicitud-relacionada-marca-la-relacion", ["FM-CER-35", "matriz:C21"], entrada([3002]),
         seed=[rescate(3002, IdSolicitudAporteRescateRelacionado=3001), solo_tabla(3001, "RES", "CRT"), tras_fm], front="F-D")
    caso("sin-traspaso-no-marca-la-relacion", ["FM-CER-35", "matriz:C21"], entrada([3001]), seed=[rescate(3001, IdMedioDePago="TRA")], front="F-D")
    caso("traspaso-solo-marca-a-la-solicitud-que-lo-tiene", ["FM-CER-35", "FM-CER-44", "matriz:C21"], entrada([3001, 3002]),
         seed=[rescate(3001, IdMedioDePago="TRA"), rescate(3002, IdMedioDePago="TRA"), tras_apv], front="F-D")
    caso("traspaso-del-rescate-rechazado-por-custodia-igual-se-marca", ["FM-CER-35", "FM-CER-31", "D-F3B-38"], entrada([3001]), seed=[rescate(3001), tras_apv],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0)), front="F-D")

    # ================================================================== FM-CER-36 / 38 retencion APV (sobre el monto BRUTO, antes de la comision)
    pct15 = config("APV.PorcentajeRetencion", "15")
    concepto = param("CONCEPTO_RETENCION_PAGO_SII", "RETSII")
    caso("retencion-apv-b-al-quince-por-ciento", ["FM-CER-36", "D-F3B-09", "matriz:C22"], entrada([3001]), seed=[rescate(3001, **apv("APV-B")), pct15, concepto], front="F-D")
    caso("retencion-apv-an-al-quince-por-ciento", ["FM-CER-36", "D-F3B-08", "D-F3B-09", "matriz:C22"], entrada([3001]), seed=[rescate(3001, **apv("APV-AN")), pct15, concepto], front="F-D")
    caso("retencion-porcentaje-cero-no-aplica", ["FM-CER-36", "matriz:C22"], entrada([3001]), seed=[rescate(3001, **apv("APV-B")), config("APV.PorcentajeRetencion", "0"), concepto], front="F-D")
    caso("retencion-parametro-ausente-no-aplica", ["FM-CER-36", "D-F3B-09", "matriz:C22"], entrada([3001]), seed=[rescate(3001, **apv("APV-B")), concepto], front="F-D")
    caso("retencion-parametro-no-numerico-no-aplica", ["FM-CER-36", "D-F3B-09"], entrada([3001]), seed=[rescate(3001, **apv("APV-B")), config("APV.PorcentajeRetencion", "quince"), concepto], front="F-D")
    caso("retencion-medio-cin-no-aplica", ["FM-CER-36", "matriz:C22"], entrada([3001]), seed=[rescate(3001, **apv("APV-B", IdMedioDePago="CIN")), pct15, concepto], front="F-D")
    caso("retencion-con-traspaso-no-aplica", ["FM-CER-36", "FM-CER-35", "matriz:C22"], entrada([3001]), seed=[rescate(3001, **apv("APV-B")), pct15, concepto, tras_apv], front="F-D")
    caso("retencion-con-spot-no-aplica", ["FM-CER-36", "FM-CER-34", "matriz:C22"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-B", IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0)), pct15, concepto, spot_s], front="F-D")
    caso("retencion-regimen-que-no-es-apv-no-aplica", ["FM-CER-36", "matriz:C22"], entrada([3001]), seed=[rescate(3001, IdMedioDePago="TRA"), pct15, concepto], front="F-D")
    caso("retencion-regimen-bf-no-aplica", ["FM-CER-36", "FM-CER-46", "matriz:C22"], entrada([3001]), seed=[rescate(3001, **apv("BF")), pct15, concepto], front="F-D")
    caso("retencion-tipo-distinto-de-rescate-normal-no-aplica", ["FM-CER-36", "matriz:C22"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCOM", **apv("APV-B")), pct15, concepto],
         front="F-D")
    caso("retencion-regimen-vacio-toma-el-codigo-de-portafolio", ["FM-CER-36", "FM-CER-13", "matriz:C22"], entrada([3001]),
         seed=[rescate(3001, CodigoPortafolio="APV-B", **apv("")), pct15, concepto], front="F-D")
    caso("retencion-se-calcula-sobre-el-monto-bruto-antes-de-la-comision", ["FM-CER-36", "FM-CER-37", "D-F3B-09", "matriz:C22"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-B")), pct15, concepto, COMISION_S, TRAMOS], colaboradores=LOTE, paridad=[paridad_comision(3001, 19998)], front="F-D")
    caso("retencion-fondo-en-usd-resta-el-monto-del-fondo-del-pago-local", ["FM-CER-36", "D-F3B-09", "matriz:C22"], entrada([3001]),
         seed=[rescate(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=5000000, Monto=0, **apv("APV-B")), pct15, concepto],
         front="F-D")
    caso("retencion-con-decimales-exactos-sin-contexto-de-redondeo", ["FM-CER-36", "D-F3B-09"], entrada([3001]),
         seed=[rescate(3001, CostoMonedaLocal=Num("1000.37"), Monto=Num("1000.37"), **apv("APV-B")), config("APV.PorcentajeRetencion", "7"), concepto], front="F-D")
    caso("retencion-cero-despues-del-formato-deja-de-ser-retencion", ["FM-CER-38", "FM-CER-36", "matriz:C22"], entrada([3001]),
         seed=[rescate(3001, CostoMonedaLocal=3, Monto=3, **apv("APV-B")), pct15, concepto], front="F-D")
    caso("retencion-dos-rescates-uno-con-retencion-y-otro-cin", ["FM-CER-36", "FM-CER-38"], entrada([3001, 3002]),
         seed=[rescate(3001, **apv("APV-B")), rescate(3002, **apv("APV-AN", IdMedioDePago="CIN")), pct15, concepto], front="F-D")

    # ================================================================== FM-CER-37 comision de certificacion
    caso("comision-S-calcula-y-descuenta-del-pago", ["FM-CER-37", "FM-COM-02", "matriz:C23"], entrada([3001]), seed=[rescate(3001), COMISION_S, TRAMOS], colaboradores=LOTE, paridad=[paridad_comision(3001, 19998)], front="F-D")
    caso("comision-N-no-calcula", ["FM-CER-37", "matriz:C23"], entrada([3001]), seed=[rescate(3001), TRAMOS], colaboradores=LOTE, front="F-D")
    caso("comision-parametro-en-minuscula-no-calcula", ["FM-CER-37", "matriz:C23"], entrada([3001]), seed=[rescate(3001), param("CALCULAR_COMISION_CERTIFICACION", "s"), TRAMOS], colaboradores=LOTE,
         front="F-D")
    caso("comision-con-comision-nn-relacionada-se-calcula-pero-no-se-aplica", ["FM-CER-37", "FM-CER-17", "matriz:C23"], entrada([3001]),
         seed=[rescate(3001, MontoComision=Num("11.5"), MontoComisionLocal=Num("9.5"), MontoPago=Num("999000"), MontoPagoLocal=Num("999000")),
               solo_tabla(3002, "RES", "ING", IdTipoMovimientoFondoMutuo="RESCNN", IdSolicitudAporteRescateRelacionado=3001), COMISION_S, TRAMOS], colaboradores=LOTE, paridad=[paridad_comision(3001, 0)], front="F-D")
    caso("comision-solo-de-los-rescates-no-rechazados-por-custodia", ["FM-CER-37", "FM-CER-31", "matriz:C23"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002), COMISION_S, TRAMOS],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0), res_custodia(3002, "APD", 1000000)), colaboradores=LOTE, paridad=[paridad_comision(3002, 19998)], front="F-D")
    caso("comision-todos-los-rescates-rechazados-no-llama-al-flujo-de-custodia", ["FM-CER-37", "FM-CER-31"], entrada([3001]), seed=[rescate(3001), COMISION_S, TRAMOS],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0)), colaboradores=LOTE, front="F-D")
    caso("comision-con-rescate-advertido-usa-las-cuotas-ajustadas", ["FM-CER-37", "FM-CER-32", "matriz:C23"], entrada([3001]), seed=[rescate(3001), COMISION_S, TRAMOS],
         calcula=calcula_custodia(adv(3001, cuotas=300)), colaboradores=LOTE, paridad=[paridad_comision(3001, 9000)], front="F-D")
    caso("comision-sin-lotes-de-custodia-es-cero", ["FM-CER-37", "FM-COM-02", "matriz:C23"], entrada([3001]), seed=[rescate(3001), COMISION_S, TRAMOS], colaboradores=flujo_custodia(), paridad=[paridad_comision(3001, 0)], front="F-D")
    caso("comision-falla-el-flujo-de-custodia-y-propaga", ["FM-CER-37", "matriz:C23"], entrada([3001]), seed=[rescate(3001), COMISION_S, TRAMOS],
         colaboradores=flujo_custodia(error="custodia no disponible"), excepcion=True, front="F-D")
    caso("comision-rescate-con-traspaso-no-cambia-el-calculo", ["FM-CER-37", "FM-CER-35", "D-COM-1", "matriz:C23"], entrada([3001]), seed=[rescate(3001), COMISION_S, TRAMOS, tras_apv],
         colaboradores=LOTE, paridad=[paridad_comision(3001, 19998)], front="F-D")
    caso("comision-dos-rescates-de-distinto-portafolio", ["FM-CER-37", "matriz:C23"], entrada([3001, 3002]),
         seed=[rescate(3001), rescate(3002, IdPortafolio=501, CodigoPortafolio="PF501"), COMISION_S, TRAMOS],
         colaboradores=flujo_custodia(lote(5000, "2026-02-10"), lote(5000, "2026-02-12", port=501)), paridad=[paridad_comision(3001, 19998, None, (3002, 19998))], front="F-D")
    caso("comision-fondo-usd-formatea-con-la-moneda-del-fondo", ["FM-CER-37", "FM-CER-23", "matriz:C23"], entrada([3001]),
         seed=[rescate(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=5000000, Monto=0), COMISION_S,
               tramo(2, "FM002A", "SALIDA", 0, 30, 2.0, id_comision=2), [comision_maxima("FM002A", 2)]], colaboradores=flujo_custodia(lote(600, "2026-02-10", precio=10, inst=903)), paridad=[paridad_comision(3001, 108000, 108000)], front="F-D")

    # ================================================================== FM-CER-39 / 40 retiro de custodia
    caso("retiro-con-regimen-y-sin-regimen-son-dos-llamadas", ["FM-CER-39", "D-F3B-01", "matriz:C24"], entrada([3001, 3002, 3003]),
         seed=[rescate(3001, IdRegimenTributario="GEN"), rescate(3002, IdRegimenTributario=""), rescate(3003, IdRegimenTributario="APV-A")], front="F-D")
    caso("retiro-solo-con-regimen-una-llamada", ["FM-CER-39", "matriz:C24"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)], front="F-D")
    caso("retiro-solo-sin-regimen-una-llamada", ["FM-CER-39", "matriz:C24"], entrada([3001]), seed=[rescate(3001, IdRegimenTributario="")], front="F-D")
    caso("retiro-del-rescate-rechazado-por-custodia-no-se-envia", ["FM-CER-39", "FM-CER-31", "matriz:C24"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002, IdRegimenTributario="")],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0), res_custodia(3002, "APD", 1000000)), front="F-D")
    caso("retiro-todos-rechazados-por-custodia-no-llama", ["FM-CER-39", "FM-CER-31"], entrada([3001]), seed=[rescate(3001)], calcula=calcula_custodia(res_custodia(3001, "RZD", 0)), front="F-D")
    caso("retiro-campos-del-movimiento-de-un-rescate", ["FM-CER-39", "matriz:C24"], entrada([3001]),
         seed=[rescate(3001, IdEmpleadoEjecutivo=33, TipoCarteraNormativa="TCN1", IdRegimenTributario="APV-A", FechaAceptacion="2026-02-27")], front="F-D")
    caso("retiro-error-rechaza-al-rescate-por-su-id", ["FM-CER-39", "matriz:C24"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         custodia=dict(con=resp_custodia([mov(3001), mov(3002)], [err(3002, "UI_ERROR_CUSTODIA_RESCATE", "Error de custodia en el rescate")], contador=1)), front="F-D")
    caso("retiro-error-con-id-negativo-en-la-llamada-sin-regimen", ["FM-CER-39", "matriz:C24"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002, IdRegimenTributario="")],
         custodia=dict(sin=resp_custodia([mov(3002)], [err(-3002, "UI_ERROR_CUSTODIA_RESCATE", "Error de custodia en el rescate")], contador=1)), front="F-D")
    caso("retiro-error-de-un-id-ajeno-solo-queda-el-mensaje", ["FM-CER-39"], entrada([3001]), seed=[rescate(3001)],
         custodia=dict(con=resp_custodia([mov(3001)], [err(9999, "UI_ERROR_CUSTODIA_GENERAL", "Error general de custodia")], contador=1)), front="F-D")
    caso("retiro-los-contadores-de-las-dos-llamadas-se-suman", ["FM-CER-39", "matriz:C24"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002, IdRegimenTributario="")],
         custodia=dict(con=resp_custodia([mov(3001)], [err(3001, "UI_ERROR_CUSTODIA_RESCATE", "Error de custodia en el rescate")], contador=2),
                       sin=resp_custodia([mov(3002)], [err(3002, "UI_ERROR_CUSTODIA_GENERAL", "Error general de custodia")], contador=3)), front="F-D")
    caso("retiro-mensaje-que-no-es-error-no-rechaza", ["FM-CER-39"], entrada([3001]), seed=[rescate(3001)],
         custodia=dict(con=resp_custodia([mov(3001)], [err(3001, "UI_ERROR_CUSTODIA_RESCATE", "Error de custodia en el rescate", tipo="ADVERTENCIA")])), front="F-D")
    caso("retiro-la-segunda-llamada-falla-despues-de-la-primera", ["FM-CER-39", "D-F3B-01"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002, IdRegimenTributario="")],
         colaboradores={"ProcesarCustodiaFondoMutuo": {"respuestas": [{"cuando": {"0.p_Movimientos.0.IdRegimenTributario": ""}, "error": "custodia no disponible"},
                                                                       {"cuando": {"0.p_Movimientos.0.IdTipoOperacion": "RESCFM"}, "respuesta": resp_custodia([mov(3001)])}]}},
         excepcion=True, front="F-D")
    caso("retiro-mensaje-sin-id-lanza-NPE", ["FM-CER-39", "D-NOR-9"], entrada([3001]), seed=[rescate(3001)],
         custodia=dict(con=resp_custodia([mov(3001)], [{"tipo": "ERROR", "codigo": "UI_ERROR_CUSTODIA_GENERAL", "descripcion": "Error general de custodia"}], contador=1)), excepcion=True, front="F-D")
    caso("sin-cartera-retirada-rechaza-al-rescate", ["FM-CER-40", "matriz:C24"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)], custodia=dict(con=resp_custodia([mov(3001)])), front="F-D")
    caso("sin-cartera-con-cartera-de-id-ajeno-rechaza", ["FM-CER-40"], entrada([3001]), seed=[rescate(3001)], custodia=dict(con=resp_custodia([mov(9999)])), front="F-D")
    caso("sin-cartera-de-todos-los-rescates-contador-cero-y-commit", ["FM-CER-40", "FM-CER-69", "D-F3B-03"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002, IdRegimenTributario="")],
         custodia=dict(con=resp_custodia(), sin=resp_custodia()), front="F-D")
    caso("sin-cartera-rescate-con-error-de-custodia-queda-con-el-codigo-del-error", ["FM-CER-40", "FM-CER-39"], entrada([3001]), seed=[rescate(3001)],
         custodia=dict(con=resp_custodia([], [err(3001, "UI_ERROR_CUSTODIA_RESCATE", "Error de custodia en el rescate")], contador=1)), front="F-D")

    # ================================================================== FM-CER-41 costo UF, spread y rentabilidad real
    nn = lambda i: solo_tabla(i, "RES", "ING", IdTipoMovimientoFondoMutuo="RESCNN", IdSolicitudAporteRescateRelacionado=3001)   # keeps the amounts of the database (FM-CER-17)
    base41 = dict(CostoUF=16, MontoMonedaSistema=600000, CostoMonedaLocal=1000000, Monto=1000000, MontoPago=1000000, MontoPagoLocal=1000000, ValorCuota=Num("1500.1234"), NumeroCuotas=Num("666.6118"))
    c10_6 = mov(3001, IdCustodia=9101, CostoInversionEnFactorAjusteARetirar=Num("10.5"), MontoCompraFactorARetirar=Num("6.0"))
    c4_3 = mov(3001, IdCustodia=9102, CostoInversionEnFactorAjusteARetirar=Num("4.5"), MontoCompraFactorARetirar=Num("3.5"))
    caso("costo-uf-spread-y-rentabilidad-ejemplo-de-la-tarjeta", ["FM-CER-41", "FM-CER-17", "matriz:C25"], entrada([3001]), seed=[rescate(3001, **base41), nn(3002)],
         custodia=dict(con=resp_custodia([c10_6, c4_3])), paridad=[paridad_utm(60000)], front="F-D")
    caso("costo-uf-spread-y-rentabilidad-sin-comision-nn-recalculados-por-F-B", ["FM-CER-41", "matriz:C25"], entrada([3001]), seed=[rescate(3001)],
         custodia=dict(con=resp_custodia([c10_6])), paridad=[paridad_utm(60000)], front="F-D")
    caso("utm-con-resultado-distinto-de-OK-usa-uno", ["FM-CER-41", "D-F3B-11", "matriz:C25"], entrada([3001]), seed=[rescate(3001, **base41), nn(3002)],
         custodia=dict(con=resp_custodia([c10_6])), paridad=[paridad_utm(60000, resultado="ERROR")], front="F-D")
    caso("utm-sin-salida-para-la-fecha-usa-uno", ["FM-CER-41", "D-F3B-11", "matriz:C25"], entrada([3001]), seed=[rescate(3001, **base41), nn(3002)],
         custodia=dict(con=resp_custodia([c10_6])),
         paridad=[{"cuando": {"0.p_IdTrabajo": 1, "0.p_CalculosEntrada.0.IdMonedaOrigen": "UTM"}, "respuesta": {"p_out_CalculosSalida": []}}], front="F-D")
    caso("utm-con-paridad-que-falla-propaga", ["FM-CER-41", "D-F3B-11"], entrada([3001]), seed=[rescate(3001, **base41), nn(3002)],
         paridad=[{"cuando": {"0.p_IdTrabajo": 1, "0.p_CalculosEntrada.0.IdMonedaOrigen": "UTM"}, "error": "paridad no disponible"}], excepcion=True, front="F-D")
    caso("costo-uf-dos-rescates-comparten-la-fecha-y-la-utm", ["FM-CER-41", "D-F3B-11", "matriz:C25"], entrada([3001, 3002]), seed=[rescate(3001, **base41), rescate(3002, IdRegimenTributario="", **base41), nn(3003)],
         custodia=dict(con=resp_custodia([c10_6]), sin=resp_custodia([mov(3002, CostoInversionEnFactorAjusteARetirar=Num("7.25"), MontoCompraFactorARetirar=Num("2.125"))])),
         paridad=[paridad_utm(60000)], front="F-D")
    caso("costo-uf-movimiento-con-costo-nulo-lanza-NPE", ["FM-CER-41", "D-F3B-11"], entrada([3001]), seed=[rescate(3001, **base41), nn(3002)],
         custodia=dict(con=resp_custodia([mov(3001, CostoInversionEnFactorAjusteARetirar=None)])), excepcion=True, front="F-D")
    caso("costo-uf-un-rescate-sin-cartera-queda-rechazado-y-el-otro-se-calcula", ["FM-CER-40", "FM-CER-41", "D-F3B-11", "D-F3B-38"], entrada([3001, 3002]), seed=[rescate(3001, **base41), rescate(3002, **base41), nn(3003)],
         custodia=dict(con=resp_custodia([c10_6])), paridad=[paridad_utm(60000)], front="F-D")

    # ================================================================== mixed lots (parallel streams of the legacy: the result must not depend on the thread order)
    caso("lote-de-cuarenta-rescates-aprobados", ["FM-CER-31", "FM-CER-39", "FM-CER-41", "D-F3B-06", "matriz:C43"], entrada([4000 + ((i * 7) % 40) for i in range(40)]),
         seed=[rescate(4000 + i, IdPortafolio=500 + (i % 5), CodigoPortafolio="PF%d" % (500 + (i % 5)), IdRegimenTributario=("" if i % 3 == 0 else "GEN"), CostoMonedaLocal=100000 + i * 1000, Monto=100000 + i * 1000)
               for i in range(40)], front="F-D")
    caso("lote-mixto-de-aportes-y-rescates-con-rechazos-de-todo-tipo", ["FM-CER-21", "FM-CER-27", "FM-CER-31", "FM-CER-32", "FM-CER-39", "FM-CER-40", "FM-CER-69", "matriz:C05"],
         entrada([3009, 3001, 3008, 3002, 3007, 3003, 3006, 3004, 3005]),
         seed=[ap(3001, 500, CostoMonedaLocal=900000, Monto=900000), ap(3002, 501, CostoMonedaLocal=100000, Monto=100000), rescate(3003), rescate(3004), rescate(3005, IdRegimenTributario=""),
               rescate(3006), rescate(3007, IdRegimenTributario=""), ap(3008, 502, CostoMonedaLocal=50000, Monto=50000), rescate(3009),
               L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N"), L.opera_saldo_negativo(502, "S")],
         saldo=L.saldo_flujo(T.fila_saldo(500, "CLP", 1), T.fila_saldo(501, "CLP", 5000000), T.fila_saldo(502, "CLP", 1)),
         calcula=calcula_custodia(res_custodia(3003, "APD", 1000000), adv(3004), res_custodia(3005, "RZD", 0), res_custodia(3006, "APD", 1000000), res_custodia(3007, "APD", 1000000),
                                  res_custodia(3009, "APD", 1000000)),
         custodia=dict(con=resp_custodia([mov(3003), mov(3004), mov(3006), mov(3009)]), sin=resp_custodia([mov(3007)])), front="F-D")
    # ================================================================== added after the first recording (the numbering of the earlier cases is not touched)
    caso("custodia-aporte-reinversion-fecha-original-centinela-usa-fecha-valor-cuota", ["FM-CER-27", "D-FEC-6", "matriz:C17"], entrada([3002]),
         seed=[T.reinv(3002, 3001, IdRegimenTributario="BF", FechaOriginalInversion="0000-12-30"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))], front="F-C")
    # simulation S: the legacy runs for real and only forces the counter at the very end (F-J): every collaborator of F-C / F-D still receives "N" (D-F3B-01)
    caso("simulacion-S-la-custodia-y-la-comision-reciben-N-y-no-hay-contador", ["FM-CER-27", "FM-CER-37", "FM-CER-39", "D-F3B-01", "matriz:C40"], entrada([3001, 3002], sim="S"),
         seed=[ap(3001, 500, CostoMonedaLocal=300000, Monto=300000), rescate(3002), COMISION_S, TRAMOS, L.opera_saldo_negativo(500, "N")], colaboradores=LOTE,
         saldo=L.saldo_flujo(T.fila_saldo(500, "CLP", 500000)), paridad=[paridad_comision(3002, 19998)], front="F-D")
    caso("simulacion-S-con-error-de-custodia-el-contador-final-sigue-siendo-uno", ["FM-CER-27", "D-F3B-01", "matriz:C40"], entrada([3001], sim="S"), seed=[ap(3001, 500)],
         custodia=dict(aportes=resp_custodia([mov(3001)], [err(3001, "UI_ERROR_CUSTODIA_APORTE", "Error de custodia en el aporte")], contador=3)), front="F-C")
    # a traspaso rescate paid to the cuenta de inversion still goes to Tesoreria through the traspaso entry (FM-CER-44 b)
    caso("traspaso-con-medio-cin-igual-va-a-tesoreria-por-traspaso", ["FM-CER-35", "FM-CER-44", "matriz:C21"], entrada([3001]), seed=[rescate(3001, IdMedioDePago="CIN"), tras_apv], front="F-D")
    # D-F3B-08: APV-AN with no SII configured passes F-A and F-D (retention computed) and the legacy fails LATER, in Tesoreria, after custody was written
    caso("apv-an-sin-sii-configurada-pasa-f-d-y-falla-despues-en-tesoreria", ["FM-CER-36", "D-F3B-08", "D-F3B-09", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-AN")), pct15, concepto, config("APV.DocumentoIdentidadPersonaSII", None)], excepcion="posterior", front="F-D")
    caso("rescate-de-reinversion-no-paga-comision", ["FM-CER-37", "D-COM-1", "matriz:C23"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESREINV"), COMISION_S, TRAMOS], colaboradores=LOTE,
         paridad=[paridad_comision(3001, 0)], front="F-D")
    # the order of the calls of the five steps (front E, the cuenta de inversion of the aportes, sits between the third and the fourth)
    caso("orden-de-las-llamadas-de-aportes-y-rescates-con-comision-y-reinversion", ["FM-CER-21", "FM-CER-24", "FM-CER-27", "FM-CER-31", "FM-CER-37", "FM-CER-39", "FM-CER-41", "matriz:C19"],
         entrada([3009, 3001, 3002, 3003]),
         seed=[ap(3001, 500, CostoMonedaLocal=300000, Monto=300000), T.reinv(3002, 3009, IdRegimenTributario="GEN", CostoMonedaLocal=100000, Monto=100000),
               rescate(3003, IdRegimenTributario=""), rescate(3009, IdRegimenTributario="APV-A", CostoUF=40, ValorUFFechaInversion=Num("39000.5"), MontoMonedaSistema=1200000), COMISION_S, TRAMOS,
               L.opera_saldo_negativo(500, "N")],
         colaboradores=LOTE, saldo=L.saldo_flujo(T.fila_saldo(500, "CLP", 500000)), paridad=[T.paridad_uf(39500.25), paridad_comision(3003, 19998, None, (3009, 19998))], front="F-D")
    # D-COM-5 seen from F-D: when the parity has no row for the commission, CalcularComisiones values the local commission at 1
    caso("comision-sin-fila-de-paridad-la-comision-local-vale-uno", ["FM-CER-37", "D-COM-5", "matriz:C23"], entrada([3001]), seed=[rescate(3001), COMISION_S, TRAMOS], colaboradores=LOTE, front="F-D")
    # forty aportes of five portfolios: the totals per portfolio are exact (equal saldo passes, one peso less rejects all the aportes of the portfolio): HashMap of totals and ordered lists of the legacy
    tot = {p: sum(100000 + i * 1000 for i in range(40) if i % 5 == p) for p in range(5)}
    caso("lote-de-cuarenta-aportes-de-cinco-portafolios-con-saldos-exactos", ["FM-CER-21", "FM-CER-27", "FM-CER-19", "D-NOR-16", "matriz:C12"], entrada([6000 + ((i * 11) % 40) for i in range(40)]),
         seed=[ap(6000 + i, 500 + (i % 5), CostoMonedaLocal=100000 + i * 1000, Monto=100000 + i * 1000) for i in range(40)] + [L.opera_saldo_negativo(500 + p, "N") for p in range(5)],
         saldo=L.saldo_flujo(T.fila_saldo(500, "CLP", tot[0] - 1), T.fila_saldo(501, "CLP", 99999999), T.fila_saldo(502, "CLP", tot[2]), T.fila_saldo(503, "CLP", tot[3] - 1),
                             T.fila_saldo(504, "CLP", tot[4])), front="F-C")
