"""FMM-CER T3 cases of front F-F (TESORERIA de los rescates): FM-CER-44 (seleccion de rescates y traspasos), FM-CER-45 (retencion y pago al SII), FM-CER-46 (bonificacion fiscal, TGR) and
FM-CER-47 (invocacion y lectura del resultado: rechazo `RECH` + `NumeroReferencia`, ingresos / egresos generados, contador y mensajes que NO se suman).

Legacy: `CertificarSolicitudAporteRescate.logica` C:698-721 and `mueveTesoreria` C:1953-2054; collaborator `ProcesarIngresoEgresoTesoreria` (gate G1: not ported, a registered double that
records the COMPLETE `p_Solicitudes` the legacy copied, with every null key). The golden of a case shows, for every entry, the pojo `SolicitudAporteRescate` of the legacy
(`getPojoSolicitudAporteRescate` C:2638-2704): the tests compare it by value. Every case is recorded with the unmodified legacy (modo completo); `excepcion=True` marks a case that
throws inside F-C..F-H (the NPE of an APV-AN without SII, a failing Tesoreria, a malformed answer)."""
from cases_cer_t3 import (caso, entrada, ap, aporte, rescate, rescate_tra, origen, mov, err, resp_custodia, res_custodia, calcula_custodia, msg, resp_cuenta, cuenta_script, tesoreria,
                          resp_tesoreria, rech, gen, spot, resp_spot, op, L, Num, config, param, ins, solo_tabla, TRAS_APV, TRAS_FM)

SIS = "2026-03-02"
PCT15 = config("APV.PorcentajeRetencion", "15")
CONCEPTO = param("CONCEPTO_RETENCION_PAGO_SII", "RETSII")
SPOT_S = param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")
USD_PAGADO_CLP = dict(IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0)
MONTO = dict(CostoMonedaLocal=1000000, Monto=1000000)


def apv(r="APV-B", **c):
    d = dict(PropositoPortafolio="PROPOSITOAPV", IdMedioDePago="TRA", IdRegimenTributario=r)
    d.update(c)
    return d


def generar():
    f = dict(front="F-F")
    # ================================================================== FM-CER-44 seleccion de rescates
    caso("rescate-pagado-por-transferencia-va-a-tesoreria-por-la-rama-a", ["FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)], **f)
    caso("rescate-pagado-por-la-cuenta-de-inversion-no-va-a-tesoreria", ["FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **MONTO)], **f)
    caso("rescate-con-medio-vacio-va-a-tesoreria", ["FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate(3001, IdMedioDePago="", **MONTO)], **f)
    caso("rescate-con-medio-nulo-de-la-vista-va-a-tesoreria", ["FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate(3001, IdMedioDePago=None, **MONTO)], **f)
    caso("rescate-con-medio-cheque-va-a-tesoreria-con-ese-medio", ["FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate(3001, IdMedioDePago="CHE", **MONTO)], **f)
    caso("rescate-de-reinversion-no-va-a-tesoreria", ["FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, IdTipoMovimientoFondoMutuo="RESREINV", **MONTO)], **f)
    caso("rescate-mlt-S-no-va-a-tesoreria", ["FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, IndicadorMLT="S", **MONTO)], **f)
    caso("rescate-mlt-minuscula-s-no-es-N-y-no-va-a-tesoreria", ["FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, IndicadorMLT="s", **MONTO)], **f)
    caso("rescate-con-spot-no-va-a-tesoreria-va-a-spot", ["FM-CER-44", "FM-CER-34", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, **USD_PAGADO_CLP), SPOT_S], **f)
    caso("los-aportes-no-pasan-por-tesoreria", ["FM-CER-44", "matriz:C27"], entrada([3001, 3002]), seed=[ap(3001, 500, IdMedioDePago="TRA", **MONTO), rescate_tra(3002, **MONTO)], **f)
    caso("rescate-rechazado-por-custodia-no-va-a-tesoreria", ["FM-CER-44", "FM-CER-31", "matriz:C27"], entrada([3001, 3002]), seed=[rescate_tra(3001, **MONTO), rescate_tra(3002, **MONTO)],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0), res_custodia(3002, "APD", 1000000)), **f)
    caso("rescate-sin-cartera-retirada-no-va-a-tesoreria", ["FM-CER-44", "FM-CER-40", "matriz:C27"], entrada([3001, 3002]), seed=[rescate_tra(3001, **MONTO), rescate_tra(3002, **MONTO)],
         custodia=dict(con=resp_custodia([mov(3002)])), **f)
    caso("lote-de-rescates-llega-en-orden-de-id-aunque-la-solicitud-este-invertida", ["FM-CER-44", "FM-CER-19", "matriz:C44"], entrada([3003, 3001, 3002]),
         seed=[rescate_tra(3001, **MONTO), rescate(3002, **MONTO), rescate_tra(3003, **MONTO)], **f)
    caso("rescate-ADV-envia-los-montos-ajustados-de-custodia", ["FM-CER-44", "FM-CER-32", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001)],
         calcula=calcula_custodia(res_custodia(3001, "ADV", 100, Monto=150003.7, NumeroCuotas=100, ValorCuota=1500.0037, MontoDestino=150003.7, ValorParidad=1, CostoEnFactorAjuste=3.8461,
                                               ValorFactorAjuste=39000.5, MontoMonedaSistema=150003.7)), **f)
    caso("fecha-de-emision-es-la-de-liquidacion-aunque-la-de-sistema-sea-posterior", ["FM-CER-44", "D-F3B-07", "matriz:C27"], entrada([3001]),
         seed=[rescate_tra(3001, FechaLiquidacion="2026-03-01", **MONTO)], **f)
    caso("fecha-de-emision-es-la-de-liquidacion-cuando-es-posterior-al-sistema", ["FM-CER-44", "D-F3B-07", "matriz:C27"], entrada([3001]),
         seed=[rescate_tra(3001, FechaLiquidacion="2026-03-09", **MONTO)], **f)
    caso("el-pojo-copia-la-preferencia-de-pago-en-el-autorizado-de-origen", ["FM-CER-44", "D-F3B-41", "matriz:C27"], entrada([3001]),
         seed=[rescate_tra(3001, IdPreferenciaPagoPortafolio=77, IdAutorizadoOrigen=9, **MONTO)], **f)
    caso("el-pojo-no-copia-el-valor-cuota-de-la-vista-ni-la-empresa", ["FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, FechaValorCuota="2026-03-02", IdEmpleadoEjecutivo=44, **MONTO)], **f)
    caso("simulacion-S-tesoreria-recibe-simular-N-y-las-reglas-de-negocio-de-la-entrada", ["FM-CER-47", "D-F3B-01", "matriz:C40"], entrada([3001], sim="S", p_UsaReglasNegocio="S"),
         seed=[rescate_tra(3001, **MONTO)], **f)
    caso("ingreso-automatico-N-y-usuario-y-modulo-de-la-entrada", ["FM-CER-47", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)], **f)

    # ================================================================== FM-CER-44 (b) traspaso
    caso("traspaso-apv-con-medio-cin-va-a-tesoreria-por-la-rama-b", ["FM-CER-44", "FM-CER-35", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **MONTO), TRAS_APV], **f)
    caso("traspaso-apv-con-medio-tra-va-por-la-rama-b-y-no-por-la-a", ["FM-CER-44", "FM-CER-35", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, **MONTO), TRAS_APV], **f)
    caso("traspaso-no-apv-va-a-tesoreria-con-la-cuenta-de-la-entidad", ["FM-CER-44", "FM-CER-35", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **MONTO), TRAS_FM], **f)
    caso("traspaso-de-la-solicitud-relacionada-va-a-tesoreria", ["FM-CER-44", "FM-CER-35", "matriz:C27"], entrada([3002]),
         seed=[rescate(3002, IdSolicitudAporteRescateRelacionado=3001, **MONTO), solo_tabla(3001, "RES", "CRT"), TRAS_FM], **f)
    caso("traspaso-sin-cuenta-bancaria-envia-ceros-y-vacios", ["FM-CER-44", "FM-CER-35", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **MONTO), [ins(L.TRA_APV, IdTraspaso=71, RazonRetiro="TRA", IdPersona=105, DocumentoEntidad="88888885-5"), ins(L.TRA_ORDEN, IdSolicitudAporteRescate=3001, IdTraspaso=71),
                                         ins("BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona", IdPersona=105, NombreCompleto="Persona sin cuenta", DocumentoIdentidad="88888885-5")]], **f)
    caso("traspaso-con-spot-no-va-a-tesoreria", ["FM-CER-44", "FM-CER-34", "FM-CER-35", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **USD_PAGADO_CLP), SPOT_S, TRAS_APV], **f)
    caso("traspaso-solo-envia-a-la-solicitud-que-lo-tiene", ["FM-CER-44", "FM-CER-35", "matriz:C27"], entrada([3001, 3002]),
         seed=[rescate(3001, **MONTO), rescate_tra(3002, **MONTO), TRAS_APV], **f)
    caso("traspaso-con-mlt-S-va-a-tesoreria-por-la-rama-b", ["FM-CER-44", "FM-CER-35", "matriz:C27"], entrada([3001]), seed=[rescate(3001, IndicadorMLT="S", **MONTO), TRAS_APV], **f)
    caso("traspaso-de-un-rescate-de-reinversion-va-por-la-rama-b", ["FM-CER-44", "FM-CER-35", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESREINV", **MONTO), TRAS_APV], **f)
    caso("traspaso-del-rescate-rechazado-por-custodia-no-va", ["FM-CER-44", "FM-CER-31", "D-F3B-38", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **MONTO), TRAS_APV],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0)), **f)
    caso("traspaso-apv-b-no-aplica-retencion-ni-pago-al-sii", ["FM-CER-44", "FM-CER-36", "FM-CER-45", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-B", **MONTO)), PCT15, CONCEPTO, TRAS_APV], **f)

    # ================================================================== FM-CER-45 retencion y pago al SII
    caso("apv-b-resta-la-retencion-y-agrega-el-pago-al-sii", ["FM-CER-45", "FM-CER-36", "D-F3B-09", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **apv("APV-B", **MONTO)), PCT15, CONCEPTO], **f)
    caso("apv-an-resta-la-retencion-y-agrega-el-pago-al-sii", ["FM-CER-45", "FM-CER-36", "D-F3B-08", "D-F3B-09", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-AN", **MONTO)), PCT15, CONCEPTO], **f)
    caso("apv-an-sin-sii-configurada-lanza-NPE-despues-de-escribir-custodia-y-cuenta", ["FM-CER-45", "D-F3B-08", "D-F3B-03", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-AN", **MONTO)), PCT15, CONCEPTO, config("APV.DocumentoIdentidadPersonaSII", None)], excepcion=True, **f)
    caso("apv-an-sin-sii-pero-con-medio-cin-no-llega-a-tesoreria", ["FM-CER-45", "D-F3B-08", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-AN", IdMedioDePago="CIN", **MONTO)), PCT15, CONCEPTO, config("APV.DocumentoIdentidadPersonaSII", None)], **f)
    caso("apv-b-sin-retencion-porcentaje-cero-igual-envia-la-entrada-del-sii", ["FM-CER-45", "FM-CER-36", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-B", **MONTO)), config("APV.PorcentajeRetencion", "0"), CONCEPTO], **f)
    caso("apv-an-sin-retencion-porcentaje-cero-igual-envia-la-entrada-del-sii", ["FM-CER-45", "FM-CER-36", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-AN", **MONTO)), config("APV.PorcentajeRetencion", "0"), CONCEPTO], **f)
    caso("apv-b-con-medio-cin-no-va-a-tesoreria-ni-al-sii", ["FM-CER-45", "FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **apv("APV-B", IdMedioDePago="CIN", **MONTO)), PCT15, CONCEPTO], **f)
    caso("apv-b-con-spot-no-va-a-tesoreria-ni-al-sii", ["FM-CER-45", "FM-CER-34", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-B", **USD_PAGADO_CLP)), PCT15, CONCEPTO, SPOT_S], **f)
    caso("apv-b-fondo-en-usd-resta-el-monto-del-fondo-del-pago-local", ["FM-CER-45", "D-F3B-09", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-B", IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=5000000, Monto=0)), PCT15, CONCEPTO], **f)
    caso("apv-b-retencion-con-decimales-exactos", ["FM-CER-45", "FM-CER-36", "D-F3B-09", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-B", CostoMonedaLocal=Num("1000.37"), Monto=Num("1000.37"))), config("APV.PorcentajeRetencion", "7"), CONCEPTO], **f)
    caso("apv-b-sin-concepto-configurado-lanza-excepcion-antes-de-llamar-a-tesoreria", ["FM-CER-45", "D-F3B-03", "matriz:C27", "D-F3B-44"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-B", **MONTO)), PCT15, param("CONCEPTO_RETENCION_PAGO_SII", None)], excepcion=True, **f)
    caso("apv-b-y-rescate-normal-orden-de-las-entradas-del-lote", ["FM-CER-45", "FM-CER-44", "matriz:C27"], entrada([3002, 3001]),
         seed=[rescate(3001, **apv("APV-B", **MONTO)), rescate_tra(3002, **MONTO), PCT15, CONCEPTO], **f)
    caso("dos-apv-cada-uno-con-su-entrada-del-sii-despues-de-la-propia", ["FM-CER-45", "FM-CER-44", "matriz:C27"], entrada([3001, 3002]),
         seed=[rescate(3001, **apv("APV-B", **MONTO)), rescate(3002, **apv("APV-AN", **MONTO)), PCT15, CONCEPTO], **f)
    caso("apv-b-con-comision-calculada-resta-la-retencion-bruta-del-pago-neto", ["FM-CER-45", "FM-CER-37", "D-F3B-09", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-B", **MONTO)), PCT15, CONCEPTO, param("CALCULAR_COMISION_CERTIFICACION", "S")] + T2COM_(), colaboradores=T2LOTE_(), paridad=[T2PARIDAD_(3001, 19998)], **f)

    # ================================================================== FM-CER-46 bonificacion fiscal (TGR)
    caso("bonificacion-fiscal-usa-la-persona-y-la-cuenta-de-la-tgr", ["FM-CER-46", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **apv("BF", **MONTO))], **f)
    caso("bonificacion-fiscal-con-medio-cin-no-va-a-tesoreria", ["FM-CER-46", "FM-CER-44", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **apv("BF", IdMedioDePago="CIN", **MONTO))], **f)
    caso("bonificacion-fiscal-con-traspaso-usa-la-rama-b-y-no-la-tgr", ["FM-CER-46", "FM-CER-44", "FM-CER-35", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **apv("BF", **MONTO)), TRAS_APV], **f)
    caso("bonificacion-fiscal-sin-retencion-aunque-haya-porcentaje", ["FM-CER-46", "FM-CER-36", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **apv("BF", **MONTO)), PCT15, CONCEPTO], **f)
    caso("bonificacion-fiscal-sin-tgr-configurada-se-rechaza-antes-de-tesoreria", ["FM-CER-46", "FM-CER-13", "matriz:C27"], entrada([3001, 3002]),
         seed=[rescate(3001, **apv("BF", **MONTO)), rescate_tra(3002, **MONTO), config("APV.DocumentoIdentidadPersonaTGR", None)], **f)

    # ================================================================== FM-CER-47 lectura del resultado
    r_ok = tesoreria(resp_tesoreria())
    caso("tesoreria-sin-rechazos-ni-generados-no-cambia-nada", ["FM-CER-47", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)], colaboradores=r_ok, **f)
    caso("tesoreria-RECH-de-NumeroReferencia-rechaza-al-rescate-y-deja-los-efectos-previos", ["FM-CER-47", "D-F3B-03", "matriz:C28", "D-F3B-43"], entrada([3001, 3002]),
         seed=[rescate_tra(3001, **MONTO), rescate_tra(3002, **MONTO)], colaboradores=tesoreria(resp_tesoreria([rech(3001)])), **f)
    caso("tesoreria-RECH-con-id-negativo-de-la-entrada-del-sii-rechaza-al-rescate", ["FM-CER-47", "FM-CER-45", "D-F3B-03", "matriz:C28", "D-F3B-43"], entrada([3001, 3002]),
         seed=[rescate(3001, **apv("APV-B", **MONTO)), rescate_tra(3002, **MONTO), PCT15, CONCEPTO], colaboradores=tesoreria(resp_tesoreria([rech(-3001)])), **f)
    caso("tesoreria-RECH-de-otro-campo-no-rechaza", ["FM-CER-47", "matriz:C28"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)], colaboradores=tesoreria(resp_tesoreria([rech(3001, "IdCuentaBancaria")])), **f)
    caso("tesoreria-resultado-en-minuscula-no-rechaza-por-equals", ["FM-CER-47", "matriz:C28"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)],
         colaboradores=tesoreria(resp_tesoreria([rech(3001, "NumeroReferencia", "rech")])), **f)
    caso("tesoreria-resultado-que-no-es-RECH-no-rechaza", ["FM-CER-47", "matriz:C28"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)],
         colaboradores=tesoreria(resp_tesoreria([rech(3001, "NumeroReferencia", "ING")])), **f)
    caso("tesoreria-RECH-de-un-id-sin-rescate-se-ignora", ["FM-CER-47", "matriz:C28"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)], colaboradores=tesoreria(resp_tesoreria([rech(9999)])), **f)
    caso("tesoreria-RECH-sin-campo-con-id-del-rescate-lanza-NPE", ["FM-CER-47", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)],
         colaboradores=tesoreria(resp_tesoreria([rech(3001, None)])), excepcion=True, **f)
    caso("tesoreria-RECH-sin-campo-con-id-ajeno-no-evalua-el-campo", ["FM-CER-47", "matriz:C28"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)], colaboradores=tesoreria(resp_tesoreria([rech(9999, None)])), **f)
    caso("tesoreria-RECH-de-todos-los-rescates-no-deja-ninguno-para-certificar", ["FM-CER-47", "D-F3B-03", "matriz:C28", "D-F3B-43"], entrada([3001, 3002]),
         seed=[rescate_tra(3001, **MONTO), rescate_tra(3002, **MONTO)], colaboradores=tesoreria(resp_tesoreria([rech(3001), rech(3002)])), **f)
    caso("tesoreria-el-contador-y-los-mensajes-no-se-suman-a-la-salida", ["FM-CER-47", "D-F3B-03", "matriz:C28"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)],
         colaboradores=tesoreria(resp_tesoreria([], [], 3, [msg(3001, "UI_ERROR_TESORERIA", "Error de tesoreria"), msg(0, "UI_ERROR_TESORERIA", "Error de tesoreria")])), **f)
    caso("tesoreria-generado-asigna-el-ingreso-egreso-a-la-solicitud", ["FM-CER-47", "matriz:C27"], entrada([3001, 3002]), seed=[rescate_tra(3001, **MONTO), rescate_tra(3002, **MONTO)],
         colaboradores=tesoreria(resp_tesoreria([], [gen(3001, 501), gen(3002, 502)])), **f)
    caso("tesoreria-generado-de-una-referencia-sin-rescate-se-ignora", ["FM-CER-47", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)],
         colaboradores=tesoreria(resp_tesoreria([], [gen(9999, 509)])), **f)
    caso("tesoreria-dos-generados-de-la-misma-referencia-gana-el-ultimo", ["FM-CER-47", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)],
         colaboradores=tesoreria(resp_tesoreria([], [gen(3001, 501), gen(3001, 502)])), **f)
    caso("tesoreria-generado-sin-numero-de-referencia-lanza-NPE", ["FM-CER-47", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)],
         colaboradores=tesoreria(resp_tesoreria([], [gen(None, 501)])), excepcion=True, **f)
    caso("tesoreria-generado-y-RECH-del-mismo-rescate-deja-el-rescate-rechazado", ["FM-CER-47", "D-F3B-03", "matriz:C28", "D-F3B-43"], entrada([3001, 3002]),
         seed=[rescate_tra(3001, **MONTO), rescate_tra(3002, **MONTO)], colaboradores=tesoreria(resp_tesoreria([rech(3001)], [gen(3001, 501), gen(3002, 502)])), **f)
    caso("tesoreria-con-pago-al-sii-el-generado-del-sii-no-coincide-con-la-solicitud", ["FM-CER-47", "FM-CER-45", "matriz:C27"], entrada([3001]),
         seed=[rescate(3001, **apv("APV-B", **MONTO)), PCT15, CONCEPTO], colaboradores=tesoreria(resp_tesoreria([], [gen(3001, 501), gen(-3001, 502)])), **f)
    caso("tesoreria-falla-y-propaga-la-excepcion", ["FM-CER-47", "matriz:C27"], entrada([3001]), seed=[rescate_tra(3001, **MONTO)], colaboradores=L.falla("ProcesarIngresoEgresoTesoreria", "tesoreria no disponible"),
         excepcion=True, **f)
    caso("tesoreria-sin-entradas-no-se-llama", ["FM-CER-47", "matriz:C27"], entrada([3001]), seed=[rescate(3001, **MONTO)], colaboradores=L.falla("ProcesarIngresoEgresoTesoreria", "no debe llamarse"), **f)
    caso("tesoreria-RECH-del-primero-y-el-segundo-se-certifica-con-su-ingreso-egreso", ["FM-CER-47", "FM-CER-69", "matriz:C28", "D-F3B-43"], entrada([3001, 3002]),
         seed=[rescate_tra(3001, **MONTO), rescate_tra(3002, **MONTO)], colaboradores=tesoreria(resp_tesoreria([rech(3001)], [gen(3002, 502)])), **f)


# ---- helpers of the commission case (REAL CalcularComisiones with the scripted flow of custody)
def T2COM_():
    from cases_cer_t3_e import T2COM
    return T2COM


def T2LOTE_():
    from cases_cer_t3_e import T2LOTE
    return T2LOTE


def T2PARIDAD_(i, m):
    from cases_cer_t3_e import T2PARIDAD
    return T2PARIDAD(i, m)
