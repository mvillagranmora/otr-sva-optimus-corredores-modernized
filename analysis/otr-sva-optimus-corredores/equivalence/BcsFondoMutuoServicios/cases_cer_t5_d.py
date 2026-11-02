"""FMM-CER T5 cases, group D: FM-CER-64 (the UPDATE of MontoMonedaSistema of the rescate of origin, now WITH front J and the child in the same run: the T4-B harness could not combine them,
Q-T4B-7), the simulation and the notifications together with the child, and a few runs that tie the fronts of the closing to each other.

Legacy: `CertificarSolicitudAporteRescate.logica` C:835-864, `cambiarMontoMonedaSistemaRescatesReinversionInternacionalesSinSpot` C:2820-2835 (it runs AFTER the UPDATE to CRT and the recursion, and
BEFORE the OWP notification and the log: its UPDATE overwrites the value that FM-CER-61 had just written).
"""
from cases_cer_t5 import (caso, entrada, ap, aporte, rescate, rescate_tra, origen, mov, err, resp_custodia, res_custodia, calcula_custodia, tesoreria, resp_tesoreria, gen, resp_spot, op, spot,
                          msg, falla, agregar_log, sin_estado, OWP_S, L, Num, ABSENT, config, param, ins, solo_tabla, resp_ar, res_ar, ar, destino, msg_ar)
from cases_cer_t4_i import precios_con, movs, movs_de, R, RD, sw, aporte_rei, aporte_usd
from cases_cer_t5_c import colab_switch, d_misma


def generar():
    # ================================================================== FM-CER-64 monto en moneda de sistema (whole run: the UPDATE of FM-CER-61, the child, then FM-CER-64, OWP and the log)
    o = origen(3001, IdTipoMovimientoFondoMutuo="RESREINV", MontoMonedaSistema=111)
    caso("fm64-un-aporte-internacional-escribe-la-suma-en-el-rescate-de-origen-despues-del-update-a-crt", ["FM-CER-64", "FM-CER-61", "D-F3B-49", "matriz:C37"], entrada([5001]), seed=o + [aporte_usd(5001, 3001)])
    caso("fm64-la-suma-incluye-al-hermano-que-no-esta-en-el-lote", ["FM-CER-64", "D-F3B-49", "matriz:C37"], entrada([5001]),
         seed=o + [aporte_usd(5001, 3001), solo_tabla(5002, "APO", "CRT", IdTipoMovimientoFondoMutuo="APOREINV", IdSolicitudAporteRescateRelacionado=3001, MontoMonedaSistema=250000)])
    caso("fm64-simulacion-S-escribe-igual-y-el-host-lo-revierte", ["FM-CER-64", "FM-CER-68", "D-F3B-01", "matriz:C37", "matriz:C40"], entrada([5001], sim="S"), seed=o + [aporte_usd(5001, 3001)])
    caso("fm64-el-update-del-rescate-de-origen-precede-a-owp-y-al-log", ["FM-CER-64", "FM-CER-65", "FM-CER-66", "FM-CER-70", "matriz:C37"], entrada([5001]),
         seed=[OWP_S] + o + [aporte_usd(5001, 3001, IdTipoCanalIngreso="OWP")])
    caso("fm64-dos-rescates-de-origen-y-dos-aportes-del-lote", ["FM-CER-64", "matriz:C37"], entrada([5001, 5002]),
         seed=o + origen(3002, IdTipoMovimientoFondoMutuo="RESREINV", MontoMonedaSistema=222) + [aporte_usd(5001, 3001), aporte_usd(5002, 3002, CostoMonedaLocal=7000, Monto=7000)])
    caso("fm64-un-aporte-rechazado-del-lote-no-actualiza-su-origen-y-el-certificado-si", ["FM-CER-64", "FM-CER-69", "matriz:C37"], entrada([5001, 5002]),
         seed=o + origen(3002, IdTipoMovimientoFondoMutuo="RESREINV", MontoMonedaSistema=222) + [aporte_usd(5001, 3001), aporte_usd(5002, 3002, estado="CRT", MontoMonedaSistema=999)])
    # ================================================================== simulation / notifications / child together
    s = [R(3001), d_misma(), sw(4001, 3001, IdTipoCanalIngreso="OWP")]
    caso("rec-simulacion-S-con-owp-y-el-hijo-todo-sale-aunque-se-revierte", ["FM-CER-63", "FM-CER-65", "FM-CER-68", "D-F3B-01", "D-F3B-02", "matriz:C34", "matriz:C38", "matriz:C40"], entrada([3001], sim="S"),
         seed=[OWP_S] + s, colaboradores=colab_switch(s, res_ar(1, 4001, "S", canal="OWP")), custodia=movs(3001, 100))
    sh = [R(3001), d_misma(), sw(4001, 3001, estado="CRT")]
    caso("rec-simulacion-S-el-hijo-rechaza-todo-y-los-resultados-del-hijo-salen-con-id-cero", ["FM-CER-63", "FM-CER-68", "FM-CER-14", "FM-CER-67", "matriz:C34", "matriz:C40"], entrada([3001], sim="S"), seed=sh,
         colaboradores=colab_switch(sh, res_ar(1, 4001, "S")), custodia=movs(3001, 100))
    caso("rec-ar-sin-resultados-no-hay-hijo-ni-registro-del-hijo", ["FM-CER-63", "FM-CER-58", "matriz:C34"], entrada([3001]), seed=[R(3001), d_misma()],
         colaboradores=dict(**precios_con([R(3001)], s3001=10000), **ar(resp_ar())), custodia=movs(3001, 100))
    caso("rec-ar-con-resultado-N-no-hay-hijo", ["FM-CER-63", "FM-CER-58", "matriz:C34"], entrada([3001]), seed=[R(3001), d_misma(), sw(4001, 3001)],
         colaboradores=colab_switch([R(3001), sw(4001, 3001)], res_ar(1, 4001, "N")), custodia=movs(3001, 100))
    s2 = [R(3001), d_misma(), sw(4001, 3001), sw(4002, 3001)]
    caso("rec-el-hijo-tiene-dos-aportes-y-un-solo-log-de-sistema-del-hijo", ["FM-CER-63", "FM-CER-66", "matriz:C34"], entrada([3001]), seed=s2,
         colaboradores=colab_switch(s2, res_ar(1, 4001, "S"), res_ar(2, 4002, "S")), custodia=movs(3001, 100))
    caso("rec-la-notificacion-asincrona-del-padre-sale-antes-del-hijo-y-la-del-hijo-despues", ["FM-CER-62", "FM-CER-63", "FM-CER-70", "D-F3B-02", "matriz:C38", "matriz:C34"], entrada([3001, 3002]),
         seed=s2 + [rescate(3002)], colaboradores=colab_switch(s2 + [rescate(3002)], res_ar(1, 4001, "S"), res_ar(2, 4002, "S")), custodia=movs_de((3001, [100]), (3002, [1])))
    caso("rec-el-padre-con-tesoreria-spot-y-facturacion-y-un-hijo", ["FM-CER-63", "FM-CER-70", "FM-CER-47", "FM-CER-59", "FM-CER-50", "matriz:C34"], entrada([3001, 3002, 3003]),
         seed=[R(3001), d_misma(), sw(4001, 3001), rescate_tra(3002, MontoComisionLocal=119), rescate(3003, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0), param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")],
         colaboradores=dict(**colab_switch([R(3001), sw(4001, 3001)], res_ar(1, 4001, "S")), **tesoreria(resp_tesoreria(generados=[gen(3002, 8802)])), **spot(resp_spot([op("3003", "ING", 7703)])),
                            **_fact([3002])), custodia=movs_de((3001, [100]), (3002, [1]), (3003, [1])), tags=[])
    # ================================================================== a few more runs of the closing
    caso("upd-el-rescate-de-reinversion-con-aportes-escribe-su-propio-update-sin-el-de-los-aportes-creados-por-ar", ["FM-CER-61", "FM-CER-57", "matriz:C31"], entrada([3001]), seed=[R(3001), d_misma()],
         colaboradores=dict(**precios_con([R(3001)], s3001=10000), **ar(resp_ar([res_ar(1, 4001, "N")]))), custodia=movs(3001, 60, 40))
    caso("salida-el-rescate-advertido-y-el-aporte-certificado-con-owp-y-simulacion-N", ["FM-CER-67", "FM-CER-65", "FM-CER-31", "matriz:C19"], entrada([3001, 4001]),
         seed=[OWP_S, rescate(3001, IdTipoCanalIngreso="OWP"), ap(4001, IdTipoCanalIngreso="OWP")], calcula=calcula_custodia(_adv(3001)), custodia=movs_de((3001, [100])))
    caso("log-el-aporte-rechazado-por-saldo-y-el-rescate-certificado-llevan-su-fila-de-log", ["FM-CER-66", "FM-CER-21", "FM-CER-69", "matriz:C39"], entrada([4001, 3001]),
         seed=[ap(4001, 500, CostoMonedaLocal=300000, Monto=300000), rescate(3001), L.opera_saldo_negativo(500, "N")], saldo=_saldo_corto())
    caso("simulacion-S-con-un-rescate-con-comision-spot-y-tesoreria-corre-todo-y-revierte", ["FM-CER-68", "D-F3B-01", "D-F3B-02", "matriz:C40"], entrada([3001], sim="S"),
         seed=[OWP_S, rescate_tra(3001, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0, MontoComisionLocal=119, IdTipoCanalIngreso="OWP"), param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")],
         colaboradores=dict(**tesoreria(resp_tesoreria(generados=[gen(3001, 8801)])), **spot(resp_spot([op("3001", "ING", 7701)])), **_fact([3001])),
         tags=["T5-compuerta:G2"])
    caso("orden-de-efectos-la-excepcion-de-spot-no-deja-update-ni-notificaciones", ["FM-CER-70", "FM-CER-71", "matriz:C41"], entrada([3001]),
         seed=[rescate(3001, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0), param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")], colaboradores=falla("ProcesarIngresoOperacionSpot", "FALLA_SPOT"),
         excepcion=True)
    caso("orden-de-efectos-la-excepcion-de-tesoreria-deja-la-custodia-y-la-cuenta-escritas", ["FM-CER-70", "FM-CER-71", "D-F3B-03", "matriz:C41"], entrada([3001]), seed=[rescate_tra(3001)],
         colaboradores=falla("ProcesarIngresoEgresoTesoreria", "FALLA_TESORERIA"), excepcion=True)
    caso("notif-asincrona-y-owp-con-el-mismo-aporte-owp-y-dos-aportes-opt", ["FM-CER-62", "FM-CER-65", "matriz:C38"], entrada([4001, 4002, 4003]),
         seed=[OWP_S, ap(4001), ap(4002, IdTipoCanalIngreso="OWP"), ap(4003)])
    # golden-mutation J08: the OWP entry reads NTitulos from NumeroCuotas and Precio from ValorCuota; every other case seeds both at 0, so a swap of the two was invisible
    caso("owp-la-cantidad-es-el-numero-de-cuotas-y-el-precio-el-valor-cuota-con-valores-distintos", ["FM-CER-65", "matriz:C38"], entrada([4001]),
         seed=[OWP_S, ap(4001, IdTipoCanalIngreso="OWP", NumeroCuotas=7, ValorCuota=3)])


def _adv(id_):
    from cases_cer_t2_d import adv
    return adv(id_)


def _saldo_corto():
    from cases_cer_t2_c import saldo_fijo
    from cases_cer_t2 import fila_saldo
    return saldo_fijo(fila_saldo(500, "CLP", 100))


def _fact(ids):
    from cases_cer_t4 import facturacion, resp_mov_comp, mov_sal
    return facturacion(resp_mov_comp([mov_sal(i, 7000 + i) for i in ids]))
