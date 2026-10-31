"""FMM-CER T4 cases of front F-G (FACTURACION de la comision de los rescates): FM-CER-48 (seleccion de rescates y armado del movimiento), FM-CER-49 (IVA, neto y cobro), FM-CER-50
(invocacion y lectura del resultado) and FM-CER-51 (comprobante automatico de la comision y el contador que se suma).

Legacy: `CertificarSolicitudAporteRescate.logica` C:724-759 and `creaMovimientoFacturacion` C:2159-2309, `creaComprobanteFacturacion` C:2311-2341; collaborators `CrearMovimientoComprobante` and
`CrearComprobante` (Facturacion, gate G6: not ported, registered doubles that record the COMPLETE entry the legacy built: the `MovimientoComprobante_Entrada` of ~80 fields, the cobros and the
forms of payment). Every case is recorded with the legacy (modo completo); `excepcion=True` marks a case that throws inside F-C..F-I.

The rescates of this front carry a commission by SEED (`MontoComisionLocal`, with `CALCULAR_COMISION_CERTIFICACION` = N, the base value: the legacy does not recompute it) so that the
amounts of the movement are exact numbers; one case composes the commission of F-D (CalcularComisiones, real) with the facturacion."""
from cases_cer_t4 import (caso, entrada, ap, aporte, rescate, rescate_tra, origen, mov, err, resp_custodia, res_custodia, calcula_custodia, msg, resp_cuenta, cuenta_script, tesoreria,
                          resp_tesoreria, rech, gen, resp_mov_comp, mov_sal, resp_comprobante, facturacion, msg_fact, ar, resp_ar, res_ar, L, Num, ABSENT, config, param, ins, solo_tabla,
                          TRAS_APV, TRAS_FM, paridad_comision)
from cases_cer_lib import clonar
from cases3a_lib import tramo, comision_maxima
from cases3a_com1 import lote, custodia as flujo_custodia

COBRO = "BSCL.Plataforma.BD::COM.Cobro"
COMP_S = param("RESCATE_COMISION_COMPROBANTE_AUTOMATICO", "S")
SPOT_S = param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")
COMISION_S = [param("CALCULAR_COMISION_CERTIFICACION", "S")]
TRAMOS = tramo(1, "FM001A", "SALIDA", 0, 30, 2.0) + [comision_maxima("FM001A")]
LOTE = flujo_custodia(lote(5000, "2026-02-10"))
APV = dict(PropositoPortafolio="APV")
PROP_APV = dict(PropositoPortafolio="PROPOSITOAPV")      # the value the SQL and the retention of F-D use: Facturacion does NOT take it as APV (D-F3B-51)
COM = dict(MontoComisionLocal=11900)          # 11.900 CLP of commission with IVA 19 % = 10.000 net + 1.900 of IVA


def iva(valor):
    """replaces the IVA row of COM.Cobro (valor None = a row with a NULL ValorCobro)"""
    return ['DELETE FROM "%s";' % COBRO, ins(COBRO, IdCobro="IVA", ValorCobro=valor)]


SIN_IVA = ['DELETE FROM "%s";' % COBRO]
DOS_IVA = [ins(COBRO, IdCobro="IVA", ValorCobro=5)]


def generar():
    f = dict(front="F-G")
    # ================================================================== FM-CER-48 seleccion y armado del movimiento
    caso("fact-rescate-normal-con-comision-arma-el-movimiento-foe-con-iva-19", ["FM-CER-48", "FM-CER-49", "FM-CER-50", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **COM)], **f)
    caso("fact-rescate-normal-de-portafolio-apv-usa-el-concepto-cosapv", ["FM-CER-48", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **APV, **COM)], **f)
    caso("fact-rescate-de-traspaso-restrapor-no-crea-forma-de-pago", ["FM-CER-48", "FM-CER-49", "matriz:C29"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESTRAPOR", **COM)], **f)
    caso("fact-rescate-restrapor-apv-usa-el-concepto-cosapv", ["FM-CER-48", "matriz:C29"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESTRAPOR", **APV, **COM)], **f)
    caso("fact-rescate-con-comision-rescom-usa-el-comprobante-no-apv-y-el-monto-del-rescate", ["FM-CER-48", "FM-CER-49", "matriz:C29"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCOM", **COM)], **f)
    caso("fact-rescom-de-portafolio-apv-usa-el-comprobante-apv-y-el-concepto-cocapv", ["FM-CER-48", "matriz:C29"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCOM", **APV, **COM)], **f)
    caso("fact-rescom-con-costo-cero-crea-el-movimiento-sin-cobro", ["FM-CER-48", "FM-CER-49", "matriz:C29"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCOM", CostoMonedaLocal=0, Monto=0, **COM)], **f)
    caso("fact-rescate-normal-con-proposito-propositoapv-no-es-apv-para-la-facturacion", ["FM-CER-48", "D-F3B-51", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **PROP_APV, **COM)], **f)
    caso("fact-rescom-con-proposito-propositoapv-usa-el-comprobante-y-concepto-no-apv", ["FM-CER-48", "D-F3B-51", "matriz:C29"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCOM", **PROP_APV, **COM)], **f)
    caso("fact-rescate-nn-no-se-factura", ["FM-CER-48", "matriz:C29"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCNN", **COM)],
         colaboradores=L.falla("CrearMovimientoComprobante", "no debe llamarse"), **f)
    caso("fact-rescate-de-reinversion-no-se-factura", ["FM-CER-48", "matriz:C29"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESREINV", **COM)],
         colaboradores=L.falla("CrearMovimientoComprobante", "no debe llamarse"), **f)
    caso("fact-sin-comision-no-se-factura", ["FM-CER-48", "matriz:C29"], entrada([3001]), seed=[rescate(3001, MontoComisionLocal=0)],
         colaboradores=L.falla("CrearMovimientoComprobante", "no debe llamarse"), **f)
    caso("fact-comision-fraccionaria-en-fondo-usd-se-factura", ["FM-CER-48", "FM-CER-49", "matriz:C29"], entrada([3001]),
         seed=[rescate(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="USD", IdMonedaPago="USD", CostoMonedaLocal=1000, Monto=1000, MontoComisionLocal=Num("0.01"))], **f)
    caso("fact-comision-que-el-formato-lleva-a-cero-no-se-factura", ["FM-CER-48", "FM-CER-23", "matriz:C29"], entrada([3001]),
         seed=[rescate(3001, MontoComisionLocal=Num("0.4"))], colaboradores=L.falla("CrearMovimientoComprobante", "no debe llamarse"), **f)
    caso("fact-rescate-con-spot-no-se-factura", ["FM-CER-48", "FM-CER-34", "matriz:C29"], entrada([3001]),
         seed=[rescate(3001, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0, MontoComisionLocal=100), SPOT_S],
         colaboradores=L.falla("CrearMovimientoComprobante", "no debe llamarse"), **f)
    caso("fact-rescate-con-traspaso-no-se-factura", ["FM-CER-48", "FM-CER-35", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **COM), TRAS_APV],
         colaboradores=L.falla("CrearMovimientoComprobante", "no debe llamarse"), **f)
    caso("fact-rescate-rechazado-por-custodia-no-se-factura", ["FM-CER-48", "FM-CER-31", "matriz:C29"], entrada([3001, 3002]), seed=[rescate(3001, **COM), rescate(3002, **COM)],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0), res_custodia(3002, "APD", 1000000)), **f)
    caso("fact-rescate-sin-cartera-retirada-no-se-factura", ["FM-CER-48", "FM-CER-40", "matriz:C29"], entrada([3001, 3002]), seed=[rescate(3001, **COM), rescate(3002, **COM)],
         custodia=dict(con=resp_custodia([mov(3002)])), **f)
    caso("fact-rescate-rechazado-por-la-cuenta-no-se-factura", ["FM-CER-48", "FM-CER-43", "matriz:C29"], entrada([3001, 3002]), seed=[rescate(3001, **COM), rescate(3002, **COM)],
         colaboradores=cuenta_script(rescates=resp_cuenta([msg(0)], 1)), **f)
    caso("fact-rescate-rechazado-por-tesoreria-no-se-factura", ["FM-CER-48", "FM-CER-47", "D-F3B-43", "matriz:C29"], entrada([3001, 3002]), seed=[rescate_tra(3001, **COM), rescate_tra(3002, **COM)],
         colaboradores=tesoreria(resp_tesoreria([rech(3001)], [gen(3002, 502)])), **f)
    caso("fact-los-aportes-no-se-facturan", ["FM-CER-48", "matriz:C29"], entrada([3001, 3002]), seed=[ap(3001, 500, **COM), rescate(3002, MontoComisionLocal=0)],
         colaboradores=L.falla("CrearMovimientoComprobante", "no debe llamarse"), **f)
    caso("fact-sin-rescates-no-se-lee-el-iva-ni-se-llama", ["FM-CER-48", "FM-CER-49", "matriz:C29"], entrada([3001]), seed=[ap(3001, 500), SIN_IVA],
         colaboradores=L.falla("CrearMovimientoComprobante", "no debe llamarse"), **f)
    caso("fact-rescate-con-medio-transferencia-tambien-se-factura", ["FM-CER-48", "matriz:C29"], entrada([3001]), seed=[rescate_tra(3001, **COM)], **f)
    caso("fact-dos-rescates-llegan-en-orden-de-id-con-sus-cobros-y-formas", ["FM-CER-48", "FM-CER-19", "matriz:C44"], entrada([3002, 3001]),
         seed=[rescate(3001, **COM), rescate(3002, IdTipoMovimientoFondoMutuo="RESCOM", MontoComisionLocal=23800)], **f)
    caso("fact-lote-mixto-solo-factura-los-que-tienen-comision", ["FM-CER-48", "matriz:C29"], entrada([3001, 3002, 3003]),
         seed=[rescate(3001, **COM), rescate(3002, MontoComisionLocal=0), rescate(3003, IdTipoMovimientoFondoMutuo="RESTRAPOR", MontoComisionLocal=5950)], **f)
    caso("fact-movimiento-usa-las-fechas-de-liquidacion-y-certificacion-del-rescate", ["FM-CER-48", "matriz:C29"], entrada([3001], fecha="2026-03-05"),
         seed=[rescate(3001, FechaCertificacion="2026-03-05", FechaLiquidacion="2026-03-09", FechaAceptacion="2026-03-04", **COM)], **f)
    caso("fact-comision-calculada-por-f-d-alimenta-el-movimiento", ["FM-CER-48", "FM-CER-37", "FM-COM-02", "matriz:C29"], entrada([3001]), seed=[rescate(3001), COMISION_S, TRAMOS],
         colaboradores=LOTE, paridad=[paridad_comision(3001, 19998)], **f)
    caso("fact-rescate-en-fondo-usd-lleva-la-moneda-en-el-movimiento-y-el-cobro", ["FM-CER-48", "FM-CER-49", "matriz:C29"], entrada([3001]),
         seed=[rescate(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="USD", IdMonedaPago="USD", CostoMonedaLocal=5000, Monto=5000, MontoComisionLocal=Num("119.00"))], **f)

    # ================================================================== FM-CER-49 IVA, neto y cobro
    caso("fact-iva-cero-no-cambia-el-neto-y-agrega-un-cobro-en-cero", ["FM-CER-49", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **COM), iva(0)], **f)
    caso("fact-iva-diez-por-ciento", ["FM-CER-49", "matriz:C29"], entrada([3001]), seed=[rescate(3001, MontoComisionLocal=11000), iva(10)], **f)
    caso("fact-iva-con-decimales-divide-sin-redondeo-exacto", ["FM-CER-49", "matriz:C29"], entrada([3001]), seed=[rescate(3001, MontoComisionLocal=11950), iva(Num("19.5"))], **f)
    caso("fact-neto-no-exacto-usa-34-digitos-decimal128", ["FM-CER-49", "matriz:C29"], entrada([3001]), seed=[rescate(3001, MontoComisionLocal=100)], **f)
    caso("fact-neto-no-exacto-del-rescom-usa-34-digitos", ["FM-CER-49", "matriz:C29"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCOM", MontoComisionLocal=100, CostoMonedaLocal=999)], **f)
    caso("fact-comision-de-un-peso-deja-un-neto-menor-a-uno-y-agrega-cobro", ["FM-CER-49", "matriz:C29", "D-F3B-48"], entrada([3001]), seed=[rescate(3001, MontoComisionLocal=1)], **f)
    caso("fact-iva-con-dos-filas-usa-la-primera", ["FM-CER-49", "D-F3B-12", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **COM), DOS_IVA], **f)
    caso("fact-sin-fila-de-iva-lanza-IndexOutOfBoundsException-aunque-no-haya-comision", ["FM-CER-49", "FM-CER-71", "matriz:C41", "D-F3B-47"], entrada([3001]), seed=[rescate(3001, MontoComisionLocal=0), SIN_IVA],
         excepcion=True, **f)
    caso("fact-iva-con-valor-nulo-lanza-NullPointerException", ["FM-CER-49", "FM-CER-71", "matriz:C41"], entrada([3001]), seed=[rescate(3001, **COM), iva(None)], excepcion=True, **f)

    # ================================================================== FM-CER-50 invocacion y resultado
    caso("fact-simulacion-S-la-facturacion-recibe-simular-N-y-reglas-S", ["FM-CER-50", "D-F3B-01", "matriz:C40"], entrada([3001], sim="S"), seed=[rescate(3001, **COM)], **f)
    caso("fact-reglas-de-negocio-S-de-la-entrada-no-cambian-el-S-fijo", ["FM-CER-50", "matriz:C29"], entrada([3001], p_UsaReglasNegocio="S"), seed=[rescate(3001, **COM)], **f)
    caso("fact-error-del-movimiento-rechaza-al-rescate-con-su-codigo", ["FM-CER-50", "matriz:C29"], entrada([3001, 3002]), seed=[rescate(3001, **COM), rescate(3002, **COM)],
         colaboradores=facturacion(resp_mov_comp([], [msg_fact(3002)], 1)), **f)
    caso("fact-error-con-id-negativo-usa-el-valor-absoluto", ["FM-CER-50", "matriz:C29"], entrada([3001, 3002]), seed=[rescate(3001, **COM), rescate(3002, **COM)],
         colaboradores=facturacion(resp_mov_comp([], [msg_fact(-3002)], 1)), **f)
    caso("fact-error-con-id-ajeno-no-rechaza-a-nadie-pero-queda-en-la-salida", ["FM-CER-50", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **COM)],
         colaboradores=facturacion(resp_mov_comp([], [msg_fact(9999)], 1)), **f)
    caso("fact-mensaje-que-no-es-ERROR-no-rechaza-ni-se-agrega", ["FM-CER-50", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **COM)],
         colaboradores=facturacion(resp_mov_comp([], [msg_fact(3001, "UI_AVISO", "aviso de facturacion", "ADVERTENCIA")], 0)), **f)
    caso("fact-mensaje-ERROR-en-minuscula-no-se-reconoce-por-equals", ["FM-CER-50", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **COM)],
         colaboradores=facturacion(resp_mov_comp([], [msg_fact(3001, tipo="error")], 1)), **f)
    caso("fact-mensaje-ERROR-sin-id-lanza-NullPointerException", ["FM-CER-50", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=[rescate(3001, **COM)],
         colaboradores=facturacion(resp_mov_comp([], [msg_fact(ABSENT)], 1)), excepcion=True, **f)
    caso("fact-el-contador-del-movimiento-se-suma-a-la-salida", ["FM-CER-50", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **COM)], colaboradores=facturacion(resp_mov_comp([], [], 3)), **f)
    caso("fact-dos-errores-con-el-mismo-id-rechazan-solo-al-primero-pero-quedan-los-dos-mensajes", ["FM-CER-50", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **COM)],
         colaboradores=facturacion(resp_mov_comp([], [msg_fact(3001, descripcion="primero"), msg_fact(3001, "UI_OTRO", "segundo")], 2)), **f)
    caso("fact-un-error-rechaza-pero-el-otro-rescate-se-certifica", ["FM-CER-50", "FM-CER-69", "matriz:C29"], entrada([3001, 3002]), seed=[rescate(3001, **COM), rescate(3002, **COM)],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001), mov_sal(3002, 7002)], [msg_fact(3001)], 0)), **f)
    caso("fact-falla-y-propaga-la-excepcion", ["FM-CER-50", "matriz:C29"], entrada([3001]), seed=[rescate(3001, **COM)], colaboradores=L.falla("CrearMovimientoComprobante", "facturacion no disponible"),
         excepcion=True, **f)
    caso("fact-el-rechazo-deja-la-custodia-la-cuenta-y-tesoreria-ya-escritas", ["FM-CER-50", "FM-CER-47", "D-F3B-03", "matriz:C28"], entrada([3001]), seed=[rescate_tra(3001, **COM)],
         colaboradores=dict(**facturacion(resp_mov_comp([], [msg_fact(3001)], 1)), **tesoreria(resp_tesoreria([], [gen(3001, 501)]))), **f)
    caso("fact-rescate-rechazado-por-facturacion-no-llega-a-spot-ni-a-reinversion", ["FM-CER-50", "FM-CER-59", "matriz:C29"], entrada([3001, 3002]),
         seed=[rescate(3001, **COM), rescate(3002, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0), SPOT_S], colaboradores=facturacion(resp_mov_comp([], [msg_fact(3001)], 1)), **f)

    # ================================================================== FM-CER-51 comprobante automatico
    caso("comprobante-S-llama-con-el-movimiento-generado-y-la-fecha-de-sistema", ["FM-CER-51", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001)])), **f)
    caso("comprobante-N-no-llama-al-comprobante", ["FM-CER-51", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM)],
         colaboradores=dict(**facturacion(resp_mov_comp([mov_sal(3001, 7001)])), **L.falla("CrearComprobante", "no debe llamarse")), **f)
    caso("comprobante-parametro-en-minuscula-s-tambien-llama", ["FM-CER-51", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM), param("RESCATE_COMISION_COMPROBANTE_AUTOMATICO", "s")],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001)])), **f)
    caso("comprobante-S-sin-movimientos-no-llama", ["FM-CER-51", "matriz:C30"], entrada([3001]), seed=[rescate(3001, MontoComisionLocal=0), COMP_S],
         colaboradores=L.falla("CrearComprobante", "no debe llamarse"), **f)
    caso("comprobante-S-con-respuesta-sin-movimientos-no-llama", ["FM-CER-51", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=dict(**facturacion(resp_mov_comp([])), **L.falla("CrearComprobante", "no debe llamarse")), **f)
    caso("comprobante-restrapor-y-rescom-quedan-fuera-aunque-tengan-movimiento", ["FM-CER-51", "matriz:C30"], entrada([3001, 3002, 3003]),
         seed=[rescate(3001, **COM), rescate(3002, IdTipoMovimientoFondoMutuo="RESTRAPOR", **COM), rescate(3003, IdTipoMovimientoFondoMutuo="RESCOM", **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001), mov_sal(3002, 7002), mov_sal(3003, 7003)])), **f)
    caso("comprobante-de-un-rescate-de-reinversion-con-movimiento-scripted-si-se-incluye", ["FM-CER-51", "matriz:C30"], entrada([3001, 3002]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESREINV", **COM), rescate(3002, **COM), COMP_S], colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001), mov_sal(3002, 7002)])), **f)
    caso("comprobante-de-un-movimiento-sin-rescate-en-el-lote-lanza-NullPointerException", ["FM-CER-51", "FM-CER-71", "matriz:C41"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(9999, 7009)])), excepcion=True, **f)
    caso("comprobante-de-un-movimiento-con-numero-nulo-lanza-NullPointerException", ["FM-CER-51", "FM-CER-71", "matriz:C41"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([{"IdMovimientoComprobante": 7001}])), excepcion=True, **f)
    caso("comprobante-de-un-rescate-con-traspaso-no-se-incluye", ["FM-CER-51", "FM-CER-35", "matriz:C30"], entrada([3001, 3002]), seed=[rescate(3001, **COM), rescate(3002, **COM), TRAS_APV, COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001), mov_sal(3002, 7002)])), **f)
    caso("comprobante-de-un-rescate-rechazado-por-la-facturacion-no-se-incluye", ["FM-CER-51", "FM-CER-50", "matriz:C30"], entrada([3001, 3002]), seed=[rescate(3001, **COM), rescate(3002, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001), mov_sal(3002, 7002)], [msg_fact(3001)], 0)), **f)
    caso("comprobante-se-suma-dos-veces-el-contador-del-movimiento-y-nunca-el-del-comprobante", ["FM-CER-51", "D-F3B-04", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001)], [], 2), resp_comprobante([], 5)), **f)
    caso("comprobante-con-contador-del-movimiento-cero-no-suma-el-del-comprobante", ["FM-CER-51", "D-F3B-04", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001)], [], 0), resp_comprobante([], 3)), **f)
    caso("comprobante-error-rechaza-al-rescate-con-su-codigo-aunque-el-contador-no-sube", ["FM-CER-51", "D-F3B-04", "matriz:C30"], entrada([3001, 3002]),
         seed=[rescate(3001, **COM), rescate(3002, **COM), COMP_S], colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001), mov_sal(3002, 7002)]),
                                                                                               resp_comprobante([msg_fact(3002, "UI_ERROR_COMPROBANTE", "Error al crear el comprobante")], 1)), **f)
    caso("comprobante-error-con-id-negativo-usa-el-valor-absoluto", ["FM-CER-51", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001)]), resp_comprobante([msg_fact(-3001, "UI_ERROR_COMPROBANTE", "Error al crear el comprobante")], 1)), **f)
    caso("comprobante-error-con-id-ajeno-queda-en-la-salida-y-no-rechaza", ["FM-CER-51", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001)]), resp_comprobante([msg_fact(9999, "UI_ERROR_COMPROBANTE", "Error al crear el comprobante")], 1)), **f)
    caso("comprobante-mensaje-que-no-es-ERROR-no-se-agrega", ["FM-CER-51", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001)]), resp_comprobante([msg_fact(3001, "UI_AVISO", "aviso", "ADVERTENCIA")], 0)), **f)
    caso("comprobante-mensaje-ERROR-sin-id-lanza-NullPointerException", ["FM-CER-51", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001)]), resp_comprobante([msg_fact(ABSENT)], 1)), excepcion=True, **f)
    caso("comprobante-falla-y-propaga-la-excepcion", ["FM-CER-51", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=dict(**facturacion(resp_mov_comp([mov_sal(3001, 7001)])), **L.falla("CrearComprobante", "comprobante no disponible")), excepcion=True, **f)
    caso("comprobante-dos-movimientos-van-en-el-orden-de-la-respuesta", ["FM-CER-51", "matriz:C30"], entrada([3001, 3002]), seed=[rescate(3001, **COM), rescate(3002, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3002, 7002), mov_sal(3001, 7001)])), **f)
    caso("comprobante-la-fecha-es-la-de-sistema-aunque-la-certificacion-sea-posterior", ["FM-CER-51", "matriz:C30"], entrada([3001], fecha="2026-03-05"),
         seed=[rescate(3001, FechaCertificacion="2026-03-05", **COM), COMP_S], colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001)])), **f)
    caso("comprobante-simulacion-S-recibe-simular-N-y-reglas-S", ["FM-CER-51", "D-F3B-01", "matriz:C40"], entrada([3001], sim="S"), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001)])), **f)
    caso("comprobante-el-movimiento-repetido-en-la-respuesta-se-envia-dos-veces", ["FM-CER-51", "matriz:C30"], entrada([3001]), seed=[rescate(3001, **COM), COMP_S],
         colaboradores=facturacion(resp_mov_comp([mov_sal(3001, 7001), mov_sal(3001, 7001)])), **f)
    caso("comprobante-rescate-rechazado-por-tesoreria-con-movimiento-scripted-no-se-incluye", ["FM-CER-51", "FM-CER-47", "matriz:C30"], entrada([3001, 3002]),
         seed=[rescate_tra(3001, **COM), rescate_tra(3002, **COM), COMP_S], colaboradores=dict(**tesoreria(resp_tesoreria([rech(3001)], [gen(3002, 502)])),
                                                                                              **facturacion(resp_mov_comp([mov_sal(3001, 7001), mov_sal(3002, 7002)]))), **f)
