"""FMM-CER T5 cases, group A of front J: the UPDATE to CRT (FM-CER-61), the asynchronous notification (FM-CER-62) and the OWP notification (FM-CER-65).

Legacy: `CertificarSolicitudAporteRescate.logica` C:835-836, 848-850, `actualizarSolicitudes` C:2738-2750, `notificar` C:2752-2762, `notificarOWP` C:2777-2818 (the re-read with SelectSolicitudes
and one `NotificarEstadoOrdenesOWP.go` for the whole list). Every case is a COMPLETE run of the legacy (A..J); `excepcion=True` marks a run in which the legacy throws (the golden then shows exactly
what had been written and called before the exception: the order of effects, FM-CER-70).

The H2 stand-in of the view FMM.ConsultarSolicitudAporteRescate is a TABLE that every case writes with the same values as the table of the solicitudes (HANA derives it): the UPDATE of FM-CER-61 writes
the TABLE, so the re-read of FM-CER-65 sees the SEED state of the view (`EstadoSolicitud` stays `ING` in the OWP entry). That is a limit of the stand-in, not a rule: the golden pins what the
legacy does on it, and the new code reads the same stand-in.
"""
from cases_cer_t5 import (caso, entrada, ap, aporte, rescate, rescate_tra, origen, mov, err, resp_custodia, res_custodia, calcula_custodia, resp_cuenta, cuenta_script, tesoreria,
                          resp_tesoreria, rech, gen, resp_spot, op, spot, msg, falla, agregar_log, sin_fila_de_tabla, OWP_S, OWP_PARAM_N, CALLBATCH, L, Num, ABSENT, NULL, config, param, ins,
                          solo_tabla, resp_ar, res_ar, ar, destino, TRAS_APV, TRAS_FM)
from cases_cer_t2_d import adv
from cases_cer_t4_i import precios_con, movs, movs_de, R, sw, aporte_rei, aporte_usd
import cases_cer_caso as C

REINV = dict(IdTipoMovimientoFondoMutuo="APOREINV", ValorUFFechaInversion=Num("38000.5"), MontoMonedaSistema=1200000)


def generar():
    # ================================================================== FM-CER-61 UPDATE masivo a certificado
    caso("upd-un-aporte-normal-queda-certificado-y-escribe-las-columnas", ["FM-CER-61", "FM-CER-67", "matriz:C03", "matriz:C36"], entrada([4001]), seed=[ap(4001)], tags=["T5-compuerta:G3"])
    caso("upd-un-rescate-normal-queda-certificado-y-escribe-las-columnas", ["FM-CER-61", "FM-CER-67", "matriz:C03", "matriz:C36"], entrada([3001]), seed=[rescate(3001)])
    caso("upd-aporte-y-rescate-en-un-solo-batch", ["FM-CER-61", "FM-CER-62", "FM-CER-66", "FM-CER-67", "matriz:C36"], entrada([4001, 3001]), seed=[ap(4001), rescate(3001)])
    caso("upd-el-spread-de-un-aporte-sembrado-queda-en-cero", ["FM-CER-61", "matriz:C36"], entrada([4001]), seed=[ap(4001, Spread=Num("7.5"))])
    caso("upd-el-spread-del-rescate-se-escribe-el-calculado-de-f-d", ["FM-CER-61", "FM-CER-41", "matriz:C36"], entrada([3001]),
         seed=[rescate(3001, Spread=Num("7.25"), CostoUF=Num("11.5"))], custodia=movs(3001, 100))
    caso("upd-el-costo-uf-del-aporte-es-el-recalculado", ["FM-CER-61", "FM-CER-26", "matriz:C36"], entrada([4001]), seed=[ap(4001, CostoUF=Num("12.5"), ValorUFFechaInversion=Num("39000.25"))])
    caso("upd-el-costo-uf-del-rescate-es-el-de-la-custodia", ["FM-CER-61", "FM-CER-41", "matriz:C36"], entrada([3001]), seed=[rescate(3001, CostoUF=Num("1.5"))],
         custodia=movs(3001, 100))
    caso("upd-tipo-de-solicitud-ajeno-se-marca-exitosa-conserva-el-costo-uf-y-spread-cero", ["FM-CER-61", "FM-CER-67", "D-F3B-16", "matriz:C10"], entrada([3001]),
         seed=[L.sol(3001, "XXX", CostoUF=Num("9.5"), Spread=Num("4.5"))])
    caso("upd-tipo-ajeno-junto-a-un-aporte-y-un-rescate", ["FM-CER-61", "D-F3B-16", "matriz:C10"], entrada([3003, 3001, 3002]), seed=[rescate(3003), L.sol(3001, "XXX"), ap(3002)])
    caso("upd-un-aporte-de-reinversion-escribe-el-valor-uf-de-la-fecha-de-inversion", ["FM-CER-61", "FM-CER-26", "matriz:C36"], entrada([4001]), seed=[ap(4001, **REINV)])
    caso("upd-un-aporte-normal-no-toca-el-valor-uf-de-la-fecha-de-inversion", ["FM-CER-61", "matriz:C36"], entrada([4001]), seed=[ap(4001, ValorUFFechaInversion=Num("1234.5"))])
    caso("upd-el-tipo-ajeno-no-pisa-el-costo-uf-que-la-tabla-ya-tenia", ["FM-CER-61", "D-F3B-16", "matriz:C10"], entrada([3001]),
         seed=[L.sol(3001, "XXX", CostoUF=Num("9.5")), 'UPDATE "%s" SET "CostoUF" = 3 WHERE "IdSolicitudAporteRescate" = 3001;' % L.T_SOL])
    caso("upd-el-aporte-de-reinversion-pisa-el-valor-uf-de-la-tabla-con-el-de-la-vista", ["FM-CER-61", "FM-CER-26", "matriz:C36"], entrada([4001]),
         seed=[ap(4001, **REINV), 'UPDATE "%s" SET "ValorUFFechaInversion" = 999 WHERE "IdSolicitudAporteRescate" = 4001;' % L.T_SOL])
    caso("upd-el-aporte-normal-conserva-el-valor-uf-que-la-tabla-ya-tenia", ["FM-CER-61", "matriz:C36"], entrada([4001]),
         seed=[ap(4001, ValorUFFechaInversion=Num("38000.5")), 'UPDATE "%s" SET "ValorUFFechaInversion" = 999 WHERE "IdSolicitudAporteRescate" = 4001;' % L.T_SOL])
    caso("upd-el-rescate-pisa-el-spread-y-el-costo-uf-de-la-tabla-con-los-calculados", ["FM-CER-61", "FM-CER-41", "matriz:C36"], entrada([3001]),
         seed=[rescate(3001), 'UPDATE "%s" SET "Spread" = 99, "CostoUF" = 77 WHERE "IdSolicitudAporteRescate" = 3001;' % L.T_SOL], custodia=movs(3001, 100))
    caso("upd-sin-filas-de-respuesta-de-paridad-el-run-termina-igual-con-paridad-uno", ["FM-CER-61", "matriz:C36"], entrada([4001]), seed=[ap(4001)],
         colaboradores={"CalcularParidad": {"defecto": {"p_out_CalculosSalida": []}}})
    caso("upd-un-rescate-no-toca-el-valor-uf-de-la-fecha-de-inversion", ["FM-CER-61", "matriz:C36"], entrada([3001]), seed=[rescate(3001, ValorUFFechaInversion=Num("1234.5"))])
    caso("upd-la-paridad-nula-de-la-vista-se-escribe-con-coalesce-a-cero", ["FM-CER-61", "matriz:C36"], entrada([4001]), seed=[ap(4001, ParidadFechaCertificacion=None)])
    caso("upd-la-paridad-del-rescate-fondo-usd-pagado-en-clp", ["FM-CER-61", "matriz:C36"], entrada([3001]),
         seed=[rescate(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=2000000)], custodia=movs(3001, 100))
    caso("upd-el-id-de-ingreso-egreso-de-tesoreria-se-escribe", ["FM-CER-61", "FM-CER-47", "matriz:C36"], entrada([3001]), seed=[rescate_tra(3001)],
         colaboradores=tesoreria(resp_tesoreria(generados=[gen(3001, 8801)])), tags=["T5-compuerta:G1"])
    caso("upd-el-id-de-operacion-spot-se-escribe", ["FM-CER-61", "FM-CER-60", "matriz:C36"], entrada([3001]),
         seed=[rescate(3001, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0), param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")],
         colaboradores=spot(resp_spot([op("3001", "ING", 7701)])), tags=["T5-compuerta:G2"])
    caso("upd-el-id-de-cartera-del-aporte-viene-de-la-custodia", ["FM-CER-61", "FM-CER-28", "matriz:C36"], entrada([4001]), seed=[ap(4001)],
         custodia=dict(aportes=resp_custodia([mov(4001, IdCustodia=9444, FolioCustodia=5444)])), tags=["T5-compuerta:GCUS"])
    caso("upd-el-monto-de-comision-se-escribe", ["FM-CER-61", "FM-CER-37", "matriz:C36"], entrada([3001]), seed=[rescate(3001, MontoComisionLocal=11900, MontoComision=11900)],
         colaboradores=facturacion_ok(), tags=["T5-compuerta:G6"])
    caso("upd-una-advertencia-de-custodia-tambien-se-actualiza", ["FM-CER-61", "FM-CER-31", "FM-CER-62", "matriz:C19"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         calcula=calcula_custodia(adv(3001), res_custodia(3002, "APD", 1000000)), custodia=movs_de((3001, [1]), (3002, [1])))
    caso("upd-una-solicitud-rechazada-no-se-actualiza-y-la-otra-si", ["FM-CER-61", "FM-CER-69", "matriz:C05"], entrada([4001, 4002]), seed=[ap(4001), ap(4002, estado="CRT")])
    caso("upd-un-rechazo-por-fecha-no-se-actualiza", ["FM-CER-61", "FM-CER-69", "matriz:C05"], entrada([4001, 4002]), seed=[ap(4001), ap(4002, FechaCertificacion="2026-03-09")])
    caso("upd-no-hay-guarda-de-estado-en-el-where-la-tabla-en-ANU-queda-certificada", ["FM-CER-61", "D-F3B-16", "matriz:C36"], entrada([4001]), seed=[ap(4001, Estado="ANU")])
    caso("upd-la-solicitud-sin-fila-en-la-tabla-no-falla-y-sale-exitosa", ["FM-CER-61", "FM-CER-67", "matriz:C36"], entrada([4001, 4002]), seed=[ap(4001), ap(4002), sin_fila_de_tabla(4002)])
    caso("upd-el-timestamp-de-certificacion-es-el-reloj-del-sistema", ["FM-CER-61", "matriz:C36"], entrada([4001]), seed=[ap(4001)], reloj="2026-03-02 09:05:07")
    caso("upd-tres-solicitudes-del-mismo-portafolio-y-una-de-otro", ["FM-CER-61", "FM-CER-62", "matriz:C36"], entrada([4003, 4001, 4002, 4004]),
         seed=[ap(4001, 500), ap(4002, 500), ap(4003, 500), ap(4004, 501, CodigoPortafolio="PF501", IdCliente=101, DocumentoIdentidad="22222222-2")])
    caso("upd-un-rescate-con-comision-y-un-aporte-con-moneda-de-sistema-distinta", ["FM-CER-61", "matriz:C36"], entrada([4001]),
         seed=[ap(4001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="USD", IdMonedaSistema="CLP", MontoMonedaSistema=400000, CostoMonedaLocal=5000, Monto=5000)])
    caso("upd-aporte-de-switch-simple-sin-rescate-de-origen", ["FM-CER-61", "FM-CER-63", "matriz:C34"], entrada([4001]), seed=[ap(4001, Switch="S", IdTipoMovimientoFondoMutuo="APOREINV")])

    # ================================================================== FM-CER-62 notificacion asincrona
    caso("notif-asincrona-lleva-los-ids-de-las-actualizadas-en-el-orden-de-lectura", ["FM-CER-62", "FM-CER-67", "D-F3B-02", "matriz:C38"], entrada([4003, 4001, 4002]),
         seed=[ap(4001), ap(4002), ap(4003)], tags=["T5-compuerta:G8"])
    caso("notif-asincrona-no-incluye-a-las-rechazadas", ["FM-CER-62", "FM-CER-69", "matriz:C38"], entrada([4001, 4002, 4003]), seed=[ap(4001), ap(4002, estado="CRT"), ap(4003)])
    caso("notif-asincrona-incluye-las-advertencias", ["FM-CER-62", "FM-CER-31", "matriz:C38"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         calcula=calcula_custodia(adv(3001), res_custodia(3002, "APD", 1000000)), custodia=movs_de((3001, [1]), (3002, [1])))
    caso("notif-asincrona-se-dispara-tambien-en-simulacion-S", ["FM-CER-62", "FM-CER-68", "D-F3B-02", "matriz:C38", "matriz:C40"], entrada([4001], sim="S"), seed=[ap(4001)])
    caso("notif-asincrona-con-todas-las-solicitudes-rechazadas-tarde-va-con-la-lista-vacia-y-no-hay-update", ["FM-CER-62", "FM-CER-61", "FM-CER-31", "FM-CER-69", "matriz:C38"],
         entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)], calcula=calcula_custodia(res_custodia(3001, "RZD", 0), res_custodia(3002, "RZD", 0)))
    caso("notif-asincrona-lleva-la-empresa-el-usuario-y-el-modulo-de-la-entrada", ["FM-CER-62", "matriz:C38"], entrada([4001], p_IdModulo="FMM"), seed=[ap(4001)])
    caso("notif-asincrona-falla-la-excepcion-sube-despues-del-update", ["FM-CER-62", "FM-CER-70", "FM-CER-71", "matriz:C38", "matriz:C41"], entrada([4001]), seed=[ap(4001)],
         colaboradores=falla("NotificarEstadoOrdenFondoMutuo", "FALLA_NOTIFICACION_ASINCRONA"), excepcion=True)
    caso("notif-asincrona-falla-en-un-lote-mixto-deja-el-update-hecho", ["FM-CER-62", "FM-CER-70", "FM-CER-71", "matriz:C38", "matriz:C41"], entrada([4001, 3001]), seed=[ap(4001), rescate(3001)],
         colaboradores=falla("NotificarEstadoOrdenFondoMutuo", "FALLA_NOTIFICACION_ASINCRONA"), excepcion=True)
    caso("notif-asincrona-sin-parametros-de-lote-falla-con-la-excepcion-del-legacy", ["FM-CER-62", "FM-CER-70", "FM-CER-71", "matriz:C38", "matriz:C41"], entrada([4001]), seed=[ap(4001)],
         asyncParametros=True, excepcion=True, tags=["T5-async-parametros"])
    caso("notif-asincrona-con-la-url-pero-sin-token-falla", ["FM-CER-62", "FM-CER-71", "matriz:C38", "matriz:C41"], entrada([4001]), seed=[ap(4001), config("PLA.CallBatchURL", "FAKE-URL-NO-NETWORK")],
         asyncParametros=True, excepcion=True, tags=["T5-async-parametros"])
    caso("notif-asincrona-con-el-token-pero-sin-url-falla", ["FM-CER-62", "FM-CER-71", "matriz:C38", "matriz:C41"], entrada([4001]), seed=[ap(4001), config("PLA.CallBatchToken", "FAKE-TOKEN-0000")],
         asyncParametros=True, excepcion=True, tags=["T5-async-parametros"])
    caso("notif-asincrona-con-los-dos-parametros-llama-y-el-run-termina", ["FM-CER-62", "matriz:C38"], entrada([4001]), seed=[ap(4001), CALLBATCH], asyncParametros=True,
         tags=["T5-async-parametros"])
    caso("notif-asincrona-con-parametros-de-lote-y-solicitudes-rechazadas-tarde-llama-con-lista-vacia", ["FM-CER-62", "FM-CER-31", "matriz:C38"], entrada([3001]), seed=[rescate(3001), CALLBATCH],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0)), asyncParametros=True, tags=["T5-async-parametros"])
    caso("notif-asincrona-no-se-llama-en-la-salida-temprana-de-cero-validas", ["FM-CER-62", "FM-CER-14", "matriz:C06"], entrada([4001, 4002]), seed=[ap(4001, estado="CRT"), ap(4002, estado="CRT")],
         colaboradores=falla("NotificarEstadoOrdenFondoMutuo", "no debe llamarse"))
    caso("notif-asincrona-una-sola-vez-por-peticion-sin-recursion", ["FM-CER-62", "matriz:C38"], entrada([4001, 4002, 4003]), seed=[ap(4001), ap(4002), ap(4003)])

    # ================================================================== FM-CER-65 notificacion OWP
    o = dict(IdTipoCanalIngreso="OWP")
    caso("owp-param-S-y-entrada-S-notifica-la-solicitud-de-canal-owp", ["FM-CER-65", "D-F3B-02", "matriz:C38"], entrada([4001]), seed=[OWP_S, ap(4001, **o)], tags=["T5-compuerta:G8"])
    caso("owp-param-N-y-entrada-S-no-notifica", ["FM-CER-65", "matriz:C38"], entrada([4001]), seed=[OWP_PARAM_N, ap(4001, **o)],
         colaboradores=falla("NotificarEstadoOrdenesOWP", "no debe llamarse"))
    caso("owp-param-S-y-entrada-N-no-notifica", ["FM-CER-65", "matriz:C38"], entrada([4001], p_NotificaOWP="N"), seed=[OWP_S, ap(4001, **o)],
         colaboradores=falla("NotificarEstadoOrdenesOWP", "no debe llamarse"))
    caso("owp-param-N-y-entrada-N-no-notifica", ["FM-CER-65", "matriz:C38"], entrada([4001], p_NotificaOWP="N"), seed=[OWP_PARAM_N, ap(4001, **o)],
         colaboradores=falla("NotificarEstadoOrdenesOWP", "no debe llamarse"))
    caso("owp-entrada-sin-el-campo-toma-el-valor-por-defecto-S-y-notifica", ["FM-CER-65", "matriz:C38"], entrada([4001], p_NotificaOWP=ABSENT), seed=[OWP_S, ap(4001, **o)])
    caso("owp-entrada-con-el-campo-nulo-no-notifica", ["FM-CER-65", "FM-CER-71", "matriz:C38"], entrada([4001], p_NotificaOWP=None), seed=[OWP_S, ap(4001, **o)])
    caso("owp-parametro-en-minuscula-s-notifica-por-equalsIgnoreCase", ["FM-CER-65", "matriz:C38"], entrada([4001]), seed=[config("PLA.NotificarEstadoOrdenOWP", "s"), ap(4001, **o)])
    caso("owp-entrada-en-minuscula-s-notifica-por-equalsIgnoreCase", ["FM-CER-65", "matriz:C38"], entrada([4001], p_NotificaOWP="s"), seed=[OWP_S, ap(4001, **o)])
    caso("owp-entrada-con-otro-valor-no-notifica", ["FM-CER-65", "matriz:C38"], entrada([4001], p_NotificaOWP="X"), seed=[OWP_S, ap(4001, **o)], colaboradores=falla("NotificarEstadoOrdenesOWP", "no debe llamarse"))
    caso("owp-entrada-vacia-no-notifica", ["FM-CER-65", "matriz:C38"], entrada([4001], p_NotificaOWP=""), seed=[OWP_S, ap(4001, **o)], colaboradores=falla("NotificarEstadoOrdenesOWP", "no debe llamarse"))
    caso("owp-parametro-ausente-lanza-la-excepcion-de-parametro-no-encontrado", ["FM-CER-65", "FM-CER-03", "FM-CER-71", "matriz:C02"], entrada([4001]), seed=[config("PLA.NotificarEstadoOrdenOWP", None), ap(4001, **o)],
         excepcion=True)
    caso("owp-sin-solicitudes-de-canal-owp-lee-con-lista-vacia", ["FM-CER-65", "matriz:C38"], entrada([4001]), seed=[OWP_S, ap(4001)])
    caso("owp-mezcla-de-canales-solo-el-owp-se-notifica", ["FM-CER-65", "matriz:C38"], entrada([4001, 4002, 4003]), seed=[OWP_S, ap(4001, **o), ap(4002), ap(4003, IdTipoCanalIngreso="AGE")])
    caso("owp-dos-solicitudes-owp-van-en-una-sola-llamada-con-dos-entradas", ["FM-CER-65", "matriz:C38"], entrada([4002, 4001]), seed=[OWP_S, ap(4001, **o), ap(4002, **o)])
    caso("owp-un-aporte-y-un-rescate-de-canal-owp", ["FM-CER-65", "matriz:C38"], entrada([4001, 3001]), seed=[OWP_S, ap(4001, **o), rescate(3001, **o)])
    caso("owp-el-rescate-de-reinversion-lleva-solsecasoc-vacio", ["FM-CER-65", "matriz:C38"], entrada([3001]), seed=[OWP_S, R(3001, **o)], custodia=movs(3001, 100))
    caso("owp-el-solsecasoc-es-el-rescate-relacionado", ["FM-CER-65", "matriz:C38"], entrada([4001]), seed=[OWP_S, ap(4001, IdSolicitudAporteRescateRelacionado=3000, **o), origen(3000)])
    caso("owp-el-estado-es-el-releido-de-la-vista-y-no-el-nuevo", ["FM-CER-65", "FM-CER-61", "matriz:C38"], entrada([4001]), seed=[OWP_S, ap(4001, **o)])
    caso("owp-una-solicitud-rechazada-de-canal-owp-no-se-notifica", ["FM-CER-65", "FM-CER-69", "matriz:C38"], entrada([4001, 4002]), seed=[OWP_S, ap(4001), ap(4002, estado="CRT", **o)])
    caso("owp-se-dispara-aunque-el-contador-ya-sea-mayor-a-cero", ["FM-CER-65", "FM-CER-70", "D-F3B-02", "matriz:C38"], entrada([4001, 4002]), seed=[OWP_S, ap(4001, **o), ap(4002)],
         custodia=dict(aportes=resp_custodia([mov(4001), mov(4002)], [], 2)))
    caso("owp-se-dispara-tambien-en-simulacion-S", ["FM-CER-65", "FM-CER-68", "D-F3B-02", "matriz:C38", "matriz:C40"], entrada([4001], sim="S"), seed=[OWP_S, ap(4001, **o)])
    caso("owp-con-comision-y-moneda-extranjera-lleva-comision-y-tipo-de-cambio", ["FM-CER-65", "matriz:C38"], entrada([3001]),
         seed=[OWP_S, rescate(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="USD", IdMonedaPago="USD", CostoMonedaLocal=5000, Monto=5000, MontoComisionLocal=119, **o)],
         custodia=movs(3001, 100), colaboradores=facturacion_ok(), tags=["T5-compuerta:G6"])
    caso("owp-falla-la-excepcion-sube-despues-de-la-notificacion-asincrona-y-antes-del-log", ["FM-CER-65", "FM-CER-70", "FM-CER-71", "matriz:C38", "matriz:C41"], entrada([4001]),
         seed=[OWP_S, ap(4001, **o)], colaboradores=falla("NotificarEstadoOrdenesOWP", "FALLA_NOTIFICACION_OWP"), excepcion=True)
    caso("owp-la-lista-de-ids-del-aporte-switch-owp-va-antes-que-las-actualizadas", ["FM-CER-65", "FM-CER-63", "matriz:C38", "matriz:C34"], entrada([3001]),
         seed=[OWP_S, R(3001, **o), destino(1, 3001, "FM001B", 100), sw(4001, 3001, IdTipoCanalIngreso="OWP")],
         colaboradores=dict(**precios_con([R(3001), sw(4001, 3001)], s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S", canal="OWP")]))), custodia=movs(3001, 100))
    caso("owp-el-aporte-switch-de-canal-opt-no-se-notifica", ["FM-CER-65", "FM-CER-63", "matriz:C38", "matriz:C34"], entrada([3001]),
         seed=[OWP_S, R(3001), destino(1, 3001, "FM001B", 100), sw(4001, 3001)],
         colaboradores=dict(**precios_con([R(3001), sw(4001, 3001)], s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S", canal="OPT")]))), custodia=movs(3001, 100))
    caso("owp-el-hijo-no-notifica-owp-solo-lo-hace-el-padre", ["FM-CER-65", "FM-CER-63", "matriz:C38", "matriz:C34"], entrada([3001]),
         seed=[OWP_S, R(3001), destino(1, 3001, "FM001B", 100), sw(4001, 3001, IdTipoCanalIngreso="OWP")],
         colaboradores=dict(**precios_con([R(3001), sw(4001, 3001)], s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S", canal="OWP")]))), custodia=movs(3001, 100))


def facturacion_ok():
    """the commission of the rescate is invoiced: Facturacion answers one movement for the rescate 3001 (benign)"""
    from cases_cer_t4 import facturacion, resp_mov_comp, mov_sal
    return facturacion(resp_mov_comp([mov_sal(3001, 7001)]))

