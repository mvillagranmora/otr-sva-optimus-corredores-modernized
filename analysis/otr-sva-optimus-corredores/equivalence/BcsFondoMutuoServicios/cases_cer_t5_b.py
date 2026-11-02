"""FMM-CER T5 cases, group B of front J: the logs (FM-CER-66), the success mark and the output (FM-CER-67), the simulation mode (FM-CER-68), the rejections that do not raise the counter
(FM-CER-69), the order of effects (FM-CER-70) and the exceptions catalogue (FM-CER-71).

Legacy: `CertificarSolicitudAporteRescate.logica` C:853-864 (`agregarLogSistema` C:2837-2871, `marcarSolicitudesExitosas` C:2873-2880, `procesarSalida` C:1580-1611, `insertarLog` C:1613-1643,
the forced counter C:862-864). Every case is a COMPLETE run A..J of the legacy; a run that throws is tagged `excepcion=True` and its golden shows what had been written and called before the
exception.
"""
from cases_cer_t5 import (caso, entrada, ap, aporte, rescate, rescate_tra, origen, mov, err, resp_custodia, res_custodia, calcula_custodia, resp_cuenta, cuenta_script, tesoreria,
                          resp_tesoreria, rech, gen, resp_spot, op, spot, msg, falla, agregar_log, sin_estado, sin_fila_de_tabla, OWP_S, L, Num, ABSENT, NULL, config, param, ins, solo_tabla,
                          TRAS_APV, TRAS_FM, resp_ar, res_ar, ar, destino)
from cases_cer_t2 import fila_saldo
from cases_cer_t2_d import adv
from cases_cer_t2_c import saldo_fijo
from cases_cer_t4_i import precios_con, movs, movs_de, R, sw
import cases_cer_caso as C
from cases_cer_t5_a import facturacion_ok

USD_CLP = dict(IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0)
SPOT_S = param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")
SIN_IVA = ['DELETE FROM "BSCL.Plataforma.BD::COM.Cobro";']


def sin_diccionario(*codigos):
    return ['DELETE FROM "%s" WHERE "CodDiccionario" IN (%s);' % (L.DICC, ", ".join("'%s'" % c for c in codigos))]


def solo_primer_precio(seed):
    """EntregarPreciosTasas that knows only the FIRST solicitud of the seed (the quota value of the others is missing: they are rejected by FM-CER-05 / 08)"""
    sql, specs = C._aplanar(seed)
    return L.precios(C._precio_de(specs[0]))


def generar():
    # ================================================================== FM-CER-66 logs
    caso("log-sistema-un-registro-fijo-por-agregarlog-con-el-usuario-y-la-fecha-de-sistema", ["FM-CER-66", "matriz:C39"], entrada([4001]), seed=[ap(4001)], tags=["T5-compuerta:G5"])
    caso("log-sistema-con-fecha-de-sistema-distinta-a-la-del-dia", ["FM-CER-66", "matriz:C39"], entrada([4001], fecha="2026-03-05"),
         seed=[ap(4001, FechaCertificacion="2026-03-05"), config("PLA.FechaSistema", "2026-03-05")], reloj="2026-03-10 08:00:00")
    caso("log-sistema-se-escribe-tambien-con-solicitudes-rechazadas-tarde", ["FM-CER-66", "FM-CER-31", "matriz:C39"], entrada([3001]), seed=[rescate(3001)],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0)))
    caso("log-sistema-el-resultado-de-agregarlog-con-error-no-se-revisa", ["FM-CER-66", "matriz:C39"], entrada([4001]), seed=[ap(4001)],
         colaboradores=agregar_log(3, [msg(0, "UI_ERROR_LOG", "Error del log", "ERROR")]))
    caso("log-sistema-falla-la-excepcion-sube-antes-de-marcar-y-del-log-de-certificacion", ["FM-CER-66", "FM-CER-70", "FM-CER-71", "matriz:C39", "matriz:C41"], entrada([4001]), seed=[ap(4001)],
         colaboradores=falla("AgregarLog", "FALLA_AGREGAR_LOG"), excepcion=True, tags=["T5-compuerta:G5"])
    caso("log-sistema-falla-despues-de-owp-y-del-update", ["FM-CER-66", "FM-CER-70", "FM-CER-71", "matriz:C39", "matriz:C41"], entrada([4001]), seed=[OWP_S, ap(4001, IdTipoCanalIngreso="OWP")],
         colaboradores=falla("AgregarLog", "FALLA_AGREGAR_LOG"), excepcion=True)
    caso("log-certificacion-una-fila-por-solicitud-con-los-montos-y-la-simulacion", ["FM-CER-66", "FM-CER-67", "matriz:C39"], entrada([4001, 3001]), seed=[ap(4001), rescate(3001)])
    caso("log-certificacion-incluye-las-rechazadas-en-el-orden-de-la-lectura", ["FM-CER-66", "FM-CER-69", "matriz:C39"], entrada([4003, 4001, 4002]),
         seed=[ap(4001), ap(4002, estado="CRT"), ap(4003)])
    caso("log-certificacion-valor-cuota-y-numero-de-cuotas-nulos-se-escriben-en-cero", ["FM-CER-66", "matriz:C39"], entrada([4001, 4002]),
         seed=[ap(4001), ap(4002, estado="CRT", ValorCuota=None, NumeroCuotas=None)])
    caso("log-certificacion-con-simulacion-S-queda-en-la-fila-y-se-pierde-con-la-reversa", ["FM-CER-66", "FM-CER-68", "matriz:C39", "matriz:C40"], entrada([4001], sim="S"), seed=[ap(4001)])
    caso("log-certificacion-con-simulacion-de-otro-valor-se-copia-tal-cual", ["FM-CER-66", "FM-CER-68", "matriz:C39"], entrada([4001], sim="X"), seed=[ap(4001)])
    caso("log-certificacion-el-tipo-de-cambio-es-la-paridad-y-el-monto-original-es-el-costo-local", ["FM-CER-66", "matriz:C39"], entrada([3001]),
         seed=[rescate(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=2000000)], custodia=movs(3001, 100))
    caso("log-certificacion-usa-el-usuario-y-la-empresa-de-la-entrada", ["FM-CER-66", "matriz:C39"], entrada([4001]), seed=[ap(4001)])
    caso("log-certificacion-la-salida-temprana-escribe-el-log-y-no-llama-a-agregarlog", ["FM-CER-66", "FM-CER-14", "matriz:C06", "matriz:C39"], entrada([4001, 4002]),
         seed=[ap(4001, estado="CRT"), ap(4002, estado="CRT")], colaboradores=falla("AgregarLog", "no debe llamarse"))
    caso("log-certificacion-ids-de-la-secuencia-uno-a-n-por-llamada", ["FM-CER-66", "matriz:C39"], entrada([4001, 4002, 4003, 4004, 4005]), seed=[ap(4001), ap(4002), ap(4003), ap(4004), ap(4005)])

    # ================================================================== FM-CER-67 marca de exito y salida
    caso("salida-certificada-sale-aprobada-con-ui-exito-y-la-descripcion-con-parametros", ["FM-CER-67", "matriz:C03"], entrada([4001]), seed=[ap(4001)])
    caso("salida-rechazada-por-fecha-sale-rzd-con-su-codigo-y-descripcion", ["FM-CER-67", "FM-CER-07", "FM-CER-69", "matriz:C05"], entrada([4001, 4002]), seed=[ap(4001), ap(4002, FechaCertificacion="2026-03-09")])
    caso("salida-el-estado-apd-no-esta-en-pla-estado-lanza-NullPointerException-despues-de-todos-los-efectos", ["FM-CER-67", "FM-CER-70", "FM-CER-71", "matriz:C41"], entrada([4001]),
         seed=[ap(4001), sin_estado("APD")], excepcion=True)
    caso("salida-el-estado-rzd-no-esta-en-pla-estado-lanza-NullPointerException", ["FM-CER-67", "FM-CER-71", "matriz:C41"], entrada([4001, 4002]), seed=[ap(4001), ap(4002, estado="CRT"), sin_estado("RZD")],
         excepcion=True)
    caso("salida-el-estado-adv-no-esta-en-pla-estado-lanza-NullPointerException", ["FM-CER-67", "FM-CER-71", "matriz:C41"], entrada([3001]), seed=[rescate(3001), sin_estado("ADV")],
         calcula=calcula_custodia(adv(3001)), custodia=movs(3001, 100), excepcion=True)
    caso("salida-una-advertencia-de-custodia-sale-adv-con-el-codigo-del-colaborador", ["FM-CER-67", "FM-CER-31", "matriz:C19"], entrada([3001]), seed=[rescate(3001)],
         calcula=calcula_custodia(adv(3001)), custodia=movs(3001, 100))
    caso("salida-sin-ninguna-fila-de-pla-estado-lanza-NullPointerException", ["FM-CER-67", "FM-CER-71", "matriz:C41"], entrada([4001]), seed=[ap(4001), 'DELETE FROM "%s";' % L.ESTADO], excepcion=True)
    caso("salida-el-codigo-sin-traduccion-sale-con-el-texto-del-kit", ["FM-CER-67", "matriz:C05"], entrada([4001, 4002]), seed=[ap(4001), ap(4002, estado="CRT"), sin_diccionario("UI_ESTADO_INCORRECTO_CERTIFICACION")])
    caso("salida-ui-exito-sin-traduccion-sale-con-el-texto-del-kit", ["FM-CER-67", "matriz:C03"], entrada([4001]), seed=[ap(4001), sin_diccionario("UI_EXITO")])
    caso("salida-la-descripcion-del-colaborador-tiene-prioridad-sobre-el-diccionario", ["FM-CER-67", "FM-CER-27", "FM-CER-69", "matriz:C17"], entrada([4001, 4002]), seed=[ap(4001), ap(4002)],
         custodia=dict(aportes=resp_custodia([mov(4001)], [err(4002, "UI_ERROR_CUSTODIA_APORTE", "Texto propio del colaborador", "ERROR")], 0)))
    caso("salida-la-descripcion-lleva-el-id-del-comprobante-no-liquidado", ["FM-CER-67", "FM-CER-10", "FM-CER-69", "matriz:C05"], entrada([4001, 4002]), seed=[ap(4001), ap(4002), L.comprobante_no_liquidado(4002, 7001, "PEN")])
    caso("salida-la-descripcion-lleva-el-rescate-relacionado-de-la-asociada", ["FM-CER-67", "FM-CER-12", "FM-CER-69", "matriz:C05"], entrada([3001, 3002]),
         seed=[rescate(3001), rescate(3002, IdRegimenTributario="REGSA", IdSolicitudAporteRescateRelacionado=2999), solo_tabla(2999, "RES", "ING", EstadoLiquidacion="ING")])
    caso("salida-las-filas-siguen-el-orden-de-lectura-no-el-de-la-entrada", ["FM-CER-67", "matriz:C03"], entrada([4005, 4002, 3001, 4001]), seed=[ap(4001), ap(4002), rescate(3001), ap(4005)])
    caso("salida-un-lote-con-las-tres-clases-certificada-advertida-y-rechazada", ["FM-CER-67", "FM-CER-69", "FM-CER-31", "matriz:C05"], entrada([4001, 3001, 3002]),
         seed=[ap(4001, estado="CRT"), rescate(3001), rescate(3002)], calcula=calcula_custodia(adv(3001), res_custodia(3002, "APD", 1000000)), custodia=movs_de((3001, [100]), (3002, [1])))
    caso("salida-una-solicitud-con-documento-y-portafolio-distintos", ["FM-CER-67", "matriz:C03"], entrada([4001]), seed=[ap(4001, 777, DocumentoIdentidad="33333333-3", IdCliente=102, CodigoPortafolio="PF777")])

    # ================================================================== FM-CER-68 modo simulacion
    caso("simulacion-S-fuerza-el-contador-a-uno-sin-errores-y-el-host-revierte", ["FM-CER-68", "D-F3B-01", "matriz:C40"], entrada([4001, 3001], sim="S"), seed=[ap(4001), rescate(3001)])
    caso("simulacion-S-pisa-el-contador-acumulado-por-los-colaboradores", ["FM-CER-68", "FM-CER-70", "matriz:C40"], entrada([4001], sim="S"), seed=[ap(4001)],
         custodia=dict(aportes=resp_custodia([mov(4001)], [], 2)))
    caso("simulacion-N-con-el-contador-acumulado-conserva-el-valor-y-el-host-revierte", ["FM-CER-68", "FM-CER-70", "matriz:C40"], entrada([4001], sim="N"), seed=[ap(4001)],
         custodia=dict(aportes=resp_custodia([mov(4001)], [], 2)))
    caso("simulacion-N-sin-errores-el-contador-queda-en-cero-y-el-host-confirma", ["FM-CER-68", "matriz:C40"], entrada([4001], sim="N"), seed=[ap(4001)])
    caso("simulacion-vacia-del-job-no-fuerza-el-contador", ["FM-CER-68", "matriz:C40"], entrada([4001, 3001], sim=""), seed=[ap(4001), rescate(3001)])
    caso("simulacion-en-minuscula-s-no-fuerza-el-contador", ["FM-CER-68", "matriz:C40"], entrada([4001], sim="s"), seed=[ap(4001)])
    caso("simulacion-de-otro-valor-no-fuerza-el-contador", ["FM-CER-68", "matriz:C40"], entrada([4001], sim="X"), seed=[ap(4001)])
    caso("simulacion-ausente-toma-el-valor-por-defecto-N", ["FM-CER-68", "matriz:C40"], entrada([4001], sim=ABSENT), seed=[ap(4001)])
    caso("simulacion-S-con-un-rechazo-parcial-igual-sale-con-contador-uno", ["FM-CER-68", "FM-CER-69", "matriz:C40"], entrada([4001, 4002], sim="S"), seed=[ap(4001), ap(4002, estado="CRT")])
    caso("simulacion-S-con-todas-rechazadas-sale-por-la-salida-temprana-con-contador-uno", ["FM-CER-68", "FM-CER-14", "matriz:C40"], entrada([4001], sim="S"), seed=[ap(4001, estado="CRT")])
    caso("simulacion-S-todos-los-colaboradores-reciben-N-y-las-notificaciones-salen", ["FM-CER-68", "D-F3B-01", "D-F3B-02", "matriz:C40"], entrada([4001, 3001], sim="S"),
         seed=[OWP_S, ap(4001, IdTipoCanalIngreso="OWP"), rescate_tra(3001)], colaboradores=tesoreria(resp_tesoreria(generados=[gen(3001, 8801)])), tags=["T5-compuerta:G1"])
    caso("simulacion-S-con-comision-y-facturacion-corre-de-verdad", ["FM-CER-68", "D-F3B-01", "matriz:C40"], entrada([3001], sim="S"), seed=[rescate(3001, MontoComisionLocal=11900)],
         colaboradores=facturacion_ok(), tags=["T5-compuerta:G6"])
    caso("simulacion-S-el-estado-de-la-tabla-queda-escrito-hasta-la-reversa-del-host", ["FM-CER-68", "FM-CER-61", "matriz:C40"], entrada([4001], sim="S"), seed=[ap(4001, Estado="ING")])

    # ================================================================== FM-CER-69 rechazos que no suben el contador (un lote: una certificada y una rechazada)
    def mixto(slug, ref, rechazada, extra=(), **kw):
        caso("mixto-" + slug, ["FM-CER-69", "FM-CER-14"] + ref, entrada([4001, 4002]) if kw.pop("aportes", True) else entrada([3001, 3002]), seed=[ap(4001)] + list(rechazada) + list(extra), **kw)
    mixto("rechazo-por-estado-de-la-solicitud", ["FM-CER-06", "matriz:C04"], [ap(4002, estado="CRT")])
    mixto("rechazo-por-fecha-de-certificacion", ["FM-CER-07", "matriz:C04"], [ap(4002, FechaCertificacion="2026-03-09")])
    mixto("rechazo-por-valor-cuota-faltante", ["FM-CER-08", "matriz:C04"], [ap(4002)], colaboradores=solo_primer_precio([ap(4001), ap(4002)]))
    mixto("rechazo-por-omnibus-no-creado", ["FM-CER-09", "D-F3B-31", "matriz:C04"], [ap(4002, IdTipoMovimientoFondoMutuo="APOOMNI", IdOmnibus=None)])
    mixto("rechazo-por-comprobante-no-liquidado", ["FM-CER-10", "matriz:C04"], [ap(4002), L.comprobante_no_liquidado(4002, 7001, "PEN")])
    caso("mixto-rechazo-por-liquidacion-previa-faltante", ["FM-CER-69", "FM-CER-11", "matriz:C04"], entrada([4001, 4002]),
         seed=[param("VALIDAR_CERTIFICACION", "S"), ap(4001, EstadoLiquidacion="LIQ"), ap(4002, EstadoLiquidacion="ING", FolioCartaLiquidacion=0)])
    caso("mixto-rechazo-por-asociada-no-certificada", ["FM-CER-69", "FM-CER-12", "matriz:C04"], entrada([3003, 3002]),
         seed=[rescate(3003), rescate(3002, IdRegimenTributario="REGSA", IdSolicitudAporteRescateRelacionado=2001), solo_tabla(2001, "RES", "ING", EstadoLiquidacion="ING")])
    caso("mixto-rechazo-por-tgr-no-configurada", ["FM-CER-69", "FM-CER-13", "matriz:C04"], entrada([3003, 3002]),
         seed=[rescate(3003), rescate(3002, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="BF"), config("APV.DocumentoIdentidadPersonaTGR", "99999990-0")])
    caso("mixto-rechazo-por-sii-no-configurada", ["FM-CER-69", "FM-CER-13", "matriz:C04"], entrada([3003, 3002]),
         seed=[rescate(3003), rescate(3002, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="APV-B"), config("APV.DocumentoIdentidadPersonaSII", "99999990-0")])
    caso("mixto-rechazo-por-precio-cero-en-el-calculo-de-cuotas", ["FM-CER-69", "FM-CER-16", "matriz:C08"], entrada([3001, 3002]),
         seed=[rescate(3001, CalculoPorMontoCuota="C", NumeroCuotas=100, CostoMonedaLocal=0), rescate(3002)], colaboradores=L.precios(C.L.precio(3001, 0), C.L.precio(3002)))
    caso("mixto-rechazo-por-saldo-insuficiente-de-un-portafolio-y-el-otro-pasa", ["FM-CER-69", "FM-CER-21", "matriz:C12"], entrada([4001, 4002]),
         seed=[ap(4001, 500, CostoMonedaLocal=300000, Monto=300000), ap(4002, 501, IdCliente=101, DocumentoIdentidad="22222222-2", CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 999999), fila_saldo(501, "CLP", 100)))
    caso("rechazo-por-saldo-insuficiente-de-dos-aportes-del-mismo-portafolio-rechaza-a-los-dos-y-sale-por-la-salida-temprana", ["FM-CER-14", "FM-CER-21", "matriz:C12"], entrada([4001, 4002]),
         seed=[ap(4001, 500, CostoMonedaLocal=300000, Monto=300000), ap(4002, 500, CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 400000)))
    caso("mixto-rechazo-por-custodia-insuficiente-del-rescate", ["FM-CER-69", "FM-CER-31", "matriz:C19"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         calcula=calcula_custodia(res_custodia(3001, "APD", 1000000), res_custodia(3002, "RZD", 0)), custodia=movs_de((3001, [1]), (3002, [1])))
    caso("mixto-rechazo-por-un-rescate-sin-cartera-retirada", ["FM-CER-69", "FM-CER-40", "matriz:C24"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         custodia=dict(con=resp_custodia([mov(3001)])))
    caso("mixto-rechazo-de-tesoreria-no-sube-el-contador-y-deja-los-efectos", ["FM-CER-69", "FM-CER-47", "D-F3B-03", "matriz:C28"], entrada([3001, 3002]),
         seed=[rescate_tra(3001, CostoMonedaLocal=1000000, Monto=1000000), rescate_tra(3002, CostoMonedaLocal=1000000, Monto=1000000)],
         colaboradores=tesoreria(resp_tesoreria([rech(3001)])), tags=["T5-compuerta:G1"])
    caso("mixto-rechazo-de-spot-no-sube-el-contador-y-deja-los-efectos", ["FM-CER-69", "FM-CER-60", "D-F3B-03", "matriz:C35"], entrada([3001, 3002]),
         seed=[rescate(3001, **USD_CLP), rescate(3002, **USD_CLP), SPOT_S], colaboradores=spot(resp_spot([op("3001", "RECH"), op("3002", "ING", 8802)])), tags=["T5-compuerta:G2"])
    caso("mixto-rechazo-de-la-custodia-del-aporte-sube-el-contador-del-colaborador", ["FM-CER-69", "FM-CER-27", "matriz:C17"], entrada([4001, 4002]), seed=[ap(4001), ap(4002)],
         custodia=dict(aportes=resp_custodia([mov(4001)], [err(4002, "UI_ERROR_CUSTODIA_APORTE", "Error de custodia en el aporte")], 1)))
    caso("mixto-seis-solicitudes-cuatro-certificadas-y-dos-rechazadas", ["FM-CER-69", "FM-CER-62", "FM-CER-66", "matriz:C05"], entrada([4006, 3001, 4002, 3002, 4004, 4005]),
         seed=[ap(4002), ap(4004, estado="CRT"), ap(4005), ap(4006), rescate(3001), rescate(3002, FechaCertificacion="2026-03-09")])
    caso("mixto-todas-rechazadas-por-motivos-distintos-salen-por-la-salida-temprana", ["FM-CER-14", "FM-CER-69", "FM-CER-66", "D-F3B-31", "matriz:C06"], entrada([4001, 4002, 4003]),
         seed=[ap(4001, estado="CRT"), ap(4002, FechaCertificacion="2026-03-09"), ap(4003, IdTipoMovimientoFondoMutuo="APOOMNI", IdOmnibus=None)])

    # ================================================================== FM-CER-71 exceptions catalogue (whole run) and FM-CER-70 order of effects
    caso("aporte-con-folio-nulo-se-lee-como-cero-y-la-validacion-de-liquidacion-lo-rechaza", ["FM-CER-11", "FM-CER-14", "matriz:C04"], entrada([4001]),
         seed=[param("VALIDAR_CERTIFICACION", "S"), ap(4001, EstadoLiquidacion="ING", FolioCartaLiquidacion=None)])
    caso("excepcion-rescate-sin-resultado-de-custodia-lanza-NullPointerException", ["FM-CER-71", "FM-CER-31", "D-F3B-05", "matriz:C41"], entrada([3001]), seed=[rescate(3001)],
         calcula=calcula_custodia(), excepcion=True)
    caso("excepcion-sin-fila-de-iva-lanza-IndexOutOfBoundsException-despues-de-escribir", ["FM-CER-71", "FM-CER-70", "D-F3B-47", "matriz:C41"], entrada([3001]), seed=[rescate(3001, MontoComisionLocal=11900), SIN_IVA],
         excepcion=True)
    caso("excepcion-resultado-de-ar-con-switch-nulo-lanza-NullPointerException", ["FM-CER-71", "FM-CER-58", "D-F3B-30", "matriz:C41"], entrada([3001]),
         seed=[R(3001), destino(1, 3001, "FM001B", 100), sw(4001, 3001)],
         colaboradores=dict(**precios_con([R(3001), sw(4001, 3001)], s3001=10000), **ar(resp_ar([res_ar(1, 4001, None)]))), custodia=movs(3001, 100), excepcion=True)
    caso("excepcion-indicadores-duplicados-en-la-vista-lanzan", ["FM-CER-71", "D-NOR-9", "matriz:C41"], entrada([4001]), seed=[ap(4001), L.indicador_portafolio(500, "S"), L.indicador_portafolio(500, "N")], excepcion=True)
    caso("excepcion-estado-duplicado-en-pla-estado-lanza-por-claves-repetidas", ["FM-CER-71", "D-NOR-9", "matriz:C41"], entrada([4001]), seed=[ap(4001), L.estado_pla("ING", "Ingresada otra vez")], excepcion=True)
    caso("excepcion-parametro-obligatorio-ausente-lanza-la-excepcion-del-legacy", ["FM-CER-71", "FM-CER-03", "matriz:C02"], entrada([4001]), seed=[ap(4001), param("TipoComprobanteFacturaNoApv", None)], excepcion=True)
    caso("excepcion-la-lista-de-solicitudes-nula-lanza-NullPointerException", ["FM-CER-71", "FM-CER-02", "matriz:C01"], entrada(None), excepcion=True)
    caso("excepcion-solicitud-duplicada-sale-con-contador-uno-sin-escribir", ["FM-CER-71", "FM-CER-02", "matriz:C01"], entrada([4001, 4001]), seed=[ap(4001)])
    caso("excepcion-usuario-inexistente-sale-con-contador-uno-sin-escribir", ["FM-CER-71", "FM-CER-01", "matriz:C01"], entrada([4001], p_IdUsuario=999), seed=[ap(4001)])
    caso("excepcion-ninguna-solicitud-existe-sale-con-contador-uno", ["FM-CER-71", "FM-CER-04", "matriz:C01"], entrada([4999]), seed=[ap(4001)])
    caso("orden-de-efectos-la-excepcion-de-salida-llega-despues-del-update-las-notificaciones-y-el-log-de-sistema", ["FM-CER-70", "FM-CER-67", "FM-CER-71", "matriz:C41"], entrada([4001]),
         seed=[OWP_S, ap(4001, IdTipoCanalIngreso="OWP"), sin_estado("APD")], excepcion=True)
    caso("orden-de-efectos-un-lote-con-un-rescate-y-un-aporte-escribe-la-custodia-la-cuenta-el-update-y-el-log-en-ese-orden", ["FM-CER-70", "FM-CER-61", "FM-CER-66", "matriz:C03"], entrada([3001, 4001]),
         seed=[ap(4001), rescate(3001)])
    caso("orden-de-efectos-con-todo-un-rescate-con-comision-spot-y-tesoreria", ["FM-CER-70", "FM-CER-47", "FM-CER-51", "FM-CER-59", "matriz:C03"], entrada([3001]),
         seed=[OWP_S, rescate_tra(3001, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0, MontoComisionLocal=119, IdTipoCanalIngreso="OWP"), SPOT_S],
         colaboradores=dict(**tesoreria(resp_tesoreria(generados=[gen(3001, 8801)])), **spot(resp_spot([op("3001", "ING", 7701)])), **facturacion_ok()),
         tags=["T5-compuerta:G2"])
