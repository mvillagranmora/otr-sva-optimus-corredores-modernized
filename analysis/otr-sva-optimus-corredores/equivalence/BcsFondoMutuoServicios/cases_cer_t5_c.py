"""FMM-CER T5 cases, group C: FM-CER-63 (the facade of the recursion: the child certification of the aportes of switch, WHOLE: the child runs the same class on the same connection and its own
J), the three paths of the review M3 (reinversion of the same family: with the custody withdrawal, without it and with every movement in zero), the depth 2 of the recursion (the only intended
behaviour difference of T5: the new facade FAILS CLOSED, the legacy has no guard) and the big lots (determinism, order).

Legacy: `CertificarSolicitudAporteRescate.logica` C:838-843, `certificarAportesSwitch` C:2764-2775 (the legacy calls `new CertificarSolicitudAporteRescate().logica(con, entradaSwitch)`;
`T4Spy.hijo` records the entry and the answer of the child and moves the calls the child made to its own collaborators into `internas`), `getPojoEntradaSwitch` C:2580.
"""
from cases_cer_t5 import (caso, entrada, ap, aporte, rescate, rescate_tra, origen, mov, err, resp_custodia, res_custodia, calcula_custodia, resp_cuenta, cuenta_script, tesoreria,
                          resp_tesoreria, rech, gen, resp_spot, op, spot, msg, falla, agregar_log, sin_estado, OWP_S, L, Num, ABSENT, NULL, config, param, ins, solo_tabla, TRAS_APV, TRAS_FM,
                          resp_ar, res_ar, ar, destino, msg_ar)
from cases_cer_t2_d import adv
from cases_cer_t4_i import precios_con, movs, movs_de, R, RD, sw, aporte_rei, aporte_usd
import cases_cer_caso as C


def d_misma(r=3001, p=100):
    return destino(1, r, "FM001B", p)


def colab_switch(seed, *resultados, **precios):
    """the script of a request whose AR answers the given results (res_ar) and the prices that make a rescate of 1.000.000 have exactly 100 quotas"""
    return dict(**precios_con(seed, **(precios or {"s3001": 10000})), **ar(resp_ar(list(resultados))))


def generar():
    s = [R(3001), d_misma(), sw(4001, 3001)]
    # ================================================================== FM-CER-63 recursion (the child runs for real and its own front J too)
    caso("rec-un-aporte-switch-se-certifica-por-el-hijo-que-hace-su-propio-update-notificacion-y-log", ["FM-CER-63", "FM-CER-58", "FM-CER-61", "FM-CER-62", "FM-CER-66", "D-F3B-02", "matriz:C34"],
         entrada([3001]), seed=s, colaboradores=colab_switch(s, res_ar(1, 4001, "S", canal="OPT")), custodia=movs(3001, 100))
    caso("rec-el-hijo-recibe-la-cabecera-del-padre-con-owp-N-y-simulado-igual-a-la-simulacion", ["FM-CER-63", "matriz:C34"], entrada([3001], canal="AGE", p_UsaReglasNegocio="S"), seed=s,
         colaboradores=colab_switch(s, res_ar(1, 4001, "S")), custodia=movs(3001, 100))
    caso("rec-simulacion-S-el-hijo-simula-y-sus-resultados-salen-con-id-cero-y-todo-se-revierte", ["FM-CER-63", "FM-CER-67", "FM-CER-68", "D-F3B-01", "matriz:C34", "matriz:C40"], entrada([3001], sim="S"),
         seed=s, colaboradores=colab_switch(s, res_ar(1, 4001, "S")), custodia=movs(3001, 100))
    caso("rec-simulacion-N-los-resultados-del-hijo-llevan-su-id", ["FM-CER-63", "FM-CER-67", "matriz:C34"], entrada([3001], sim="N"), seed=s, colaboradores=colab_switch(s, res_ar(1, 4001, "S")),
         custodia=movs(3001, 100))
    caso("rec-simulacion-de-otro-valor-el-hijo-no-simula", ["FM-CER-63", "FM-CER-68", "matriz:C34"], entrada([3001], sim="X"), seed=s, colaboradores=colab_switch(s, res_ar(1, 4001, "S")),
         custodia=movs(3001, 100))
    caso("rec-los-resultados-del-hijo-van-primero-y-despues-los-del-padre", ["FM-CER-63", "FM-CER-67", "matriz:C34"], entrada([3001, 3002]),
         seed=s + [rescate(3002)], colaboradores=colab_switch(s + [rescate(3002)], res_ar(1, 4001, "S")), custodia=movs_de((3001, [100]), (3002, [1])))
    caso("rec-el-padre-rechaza-uno-y-el-hijo-certifica-sus-aportes", ["FM-CER-63", "FM-CER-69", "matriz:C34"], entrada([3001, 3002]),
         seed=s + [rescate(3002, FechaCertificacion="2026-03-09")], colaboradores=colab_switch(s + [rescate(3002)], res_ar(1, 4001, "S")), custodia=movs(3001, 100))
    caso("rec-el-contador-y-los-mensajes-del-hijo-se-suman-a-los-del-padre", ["FM-CER-63", "matriz:C34"], entrada([3001]), seed=s, colaboradores=colab_switch(s, res_ar(1, 4001, "S")),
         custodia=dict(con=movs(3001, 100)["con"], aportes=resp_custodia([], [err(4001, "UI_ERROR_CUSTODIA_APORTE", "Error de custodia en el aporte")], 1)))
    sh = [R(3001), d_misma(), sw(4001, 3001, estado="CRT")]
    caso("rec-el-hijo-rechaza-su-aporte-y-sale-por-la-salida-temprana-con-contador-uno-y-su-log", ["FM-CER-63", "FM-CER-14", "FM-CER-06", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=colab_switch(sh, res_ar(1, 4001, "S")), custodia=movs(3001, 100))
    sh = [R(3001), d_misma(), sw(4001, 3001), sw(4002, 3001, estado="CRT")]
    caso("rec-el-hijo-certifica-uno-y-rechaza-otro-y-el-contador-no-sube", ["FM-CER-63", "FM-CER-69", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=colab_switch(sh, res_ar(1, 4001, "S"), res_ar(2, 4002, "S")), custodia=movs(3001, 100))
    sh = [R(3001), d_misma(), sw(4001, 3001), sw(4002, 3001, IdTipoCanalIngreso="OWP")]
    caso("rec-dos-aportes-switch-van-en-un-solo-llamado-al-hijo-en-el-orden-de-ar", ["FM-CER-63", "FM-CER-58", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=colab_switch(sh, res_ar(1, 4002, "S", canal="OWP"), res_ar(2, 4001, "S", canal="OPT")), custodia=movs(3001, 100))
    caso("rec-un-aporte-switch-con-resultado-N-y-otro-S-solo-recursa-el-S", ["FM-CER-63", "FM-CER-58", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=colab_switch(sh, res_ar(1, 4001, "N"), res_ar(2, 4002, "S", canal="OWP")), custodia=movs(3001, 100))
    sh = [R(3001), d_misma(), sw(4001, 3001, EstadoLiquidacion="LIQ")]
    caso("rec-el-aporte-switch-ya-liquidado-se-certifica-y-llama-a-la-custodia-del-hijo", ["FM-CER-63", "matriz:C34"], entrada([3001]), seed=sh, colaboradores=colab_switch(sh, res_ar(1, 4001, "S")),
         custodia=movs(3001, 100))
    sh = [R(3001), d_misma(), sw(4001, 3001, MueveCta="S", MontoPago=Num("1000000"), MontoPagoLocal=Num("1000000"))]
    caso("rec-el-hijo-mueve-la-cuenta-de-inversion-del-aporte-switch", ["FM-CER-63", "FM-CER-29", "matriz:C34"], entrada([3001]), seed=sh, colaboradores=colab_switch(sh, res_ar(1, 4001, "S")),
         custodia=movs(3001, 100))
    caso("rec-el-hijo-hace-owp-N-aunque-el-parametro-sea-S-y-la-solicitud-del-hijo-sea-owp", ["FM-CER-63", "FM-CER-65", "matriz:C34", "matriz:C38"], entrada([3001]),
         seed=[OWP_S, R(3001), d_misma(), sw(4001, 3001, IdTipoCanalIngreso="OWP")], colaboradores=colab_switch(s, res_ar(1, 4001, "S", canal="OWP")), custodia=movs(3001, 100))
    sh = [R(3001), d_misma(), sw(4001, 3001)]
    caso("rec-los-ids-del-log-de-certificacion-continuan-entre-el-padre-y-el-hijo", ["FM-CER-63", "FM-CER-66", "matriz:C34", "matriz:C39"], entrada([3001, 3002]), seed=sh + [rescate(3002)],
         colaboradores=colab_switch(sh + [rescate(3002)], res_ar(1, 4001, "S")), custodia=movs_de((3001, [100]), (3002, [1])))
    sh = [R(3001), d_misma(), sw(4001, 3001)]
    caso("rec-el-hijo-falla-en-su-notificacion-asincrona-y-la-excepcion-sube-despues-de-su-update", ["FM-CER-63", "FM-CER-62", "FM-CER-70", "FM-CER-71", "matriz:C34", "matriz:C41"], entrada([3001]),
         seed=sh, colaboradores=dict(**colab_switch(sh, res_ar(1, 4001, "S")),
                                     **{"NotificarEstadoOrdenFondoMutuo": {"respuestas": [{"una_vez": True, "respuesta": None}, {"error": "FALLA_NOTIFICACION_DEL_HIJO"}]}}),
         custodia=movs(3001, 100), excepcion=True)
    caso("rec-el-hijo-falla-en-agregarlog-y-la-excepcion-sube-antes-del-owp-del-padre", ["FM-CER-63", "FM-CER-66", "FM-CER-70", "FM-CER-71", "matriz:C34", "matriz:C41"], entrada([3001]),
         seed=[OWP_S, R(3001, IdTipoCanalIngreso="OWP"), d_misma(), sw(4001, 3001)],
         colaboradores=dict(**colab_switch(sh, res_ar(1, 4001, "S")), **{"AgregarLog": {"respuestas": [{"error": "FALLA_AGREGAR_LOG_DEL_HIJO"}]}}), custodia=movs(3001, 100), excepcion=True)
    caso("rec-el-hijo-no-ve-el-aporte-en-la-vista-y-sale-por-la-salida-temprana", ["FM-CER-63", "FM-CER-14", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=colab_switch(sh, res_ar(1, 4999, "S")), custodia=movs(3001, 100))
    caso("rec-un-resultado-de-ar-sin-switch-lanza-antes-de-la-recursion", ["FM-CER-63", "FM-CER-58", "D-F3B-30", "FM-CER-71", "matriz:C41"], entrada([3001]), seed=sh,
         colaboradores=colab_switch(sh, res_ar(1, 4001, ABSENT)), custodia=movs(3001, 100), excepcion=True)
    caso("rec-ar-rechaza-el-aporte-con-contador-uno-pero-el-switch-se-certifica-igual", ["FM-CER-63", "FM-CER-57", "FM-CER-58", "D-F3B-30", "matriz:C34"], entrada([3001]), seed=sh,
         colaboradores=dict(**precios_con(sh, s3001=10000), **ar(resp_ar([res_ar(1, 4001, "S")], [msg_ar(1)], 1))), custodia=movs(3001, 100))
    # --- FM-CER-64 together with the child (the T4-B harness could not run both: Q-T4B-7)
    o = origen(3001, IdTipoMovimientoFondoMutuo="RESREINV", MontoMonedaSistema=111)
    sm = o + [aporte_usd(5001, 3001), R(3002), destino(2, 3002, "FM001B", 100), sw(4001, 3002)]
    caso("rec-fm-cer-64-y-el-hijo-en-la-misma-peticion-el-update-de-monto-sistema-va-despues-del-hijo", ["FM-CER-63", "FM-CER-64", "D-F3B-49", "matriz:C34", "matriz:C37"], entrada([5001, 3002]), seed=sm,
         colaboradores=colab_switch(sm, res_ar(1, 4001, "S"), s3002=10000), custodia=movs(3002, 100))
    # ================================================================== depth of the recursion (D-F3B-50, review M2)
    sp = [R(3001), d_misma(), R(3002), destino(2, 3002, "FM001B", 100)]
    caso("rec-el-hijo-que-llega-a-un-rescate-vuelve-a-ar-y-la-recursion-termina-con-la-respuesta-vacia", ["FM-CER-63", "FM-CER-58", "D-F3B-50", "matriz:C34"], entrada([3001]), seed=sp,
         colaboradores=dict(**precios_con(sp, s3001=10000, s3002=10000), **ar(resp_ar(), [{"una_vez": True, "respuesta": resp_ar([res_ar(1, 3002, "S")])}])),
         custodia=movs_de((3001, [100]), (3002, [100])))
    sg = [R(3001), d_misma(), R(3002), destino(2, 3002, "FM001B", 100), R(3003), destino(3, 3003, "FM001B", 100)]
    caso("rec-profundidad-dos-el-hijo-y-el-nieto-llaman-a-ar-y-el-legacy-recursa-sin-guarda", ["FM-CER-63", "FM-CER-58", "D-F3B-50", "matriz:C34"], entrada([3001]), seed=sg,
         colaboradores=dict(**precios_con(sg, s3001=10000, s3002=10000, s3003=10000),
                            **ar(resp_ar(), [{"una_vez": True, "respuesta": resp_ar([res_ar(1, 3002, "S")])}, {"una_vez": True, "respuesta": resp_ar([res_ar(1, 3003, "S")])}])),
         custodia=movs_de((3001, [100]), (3002, [100]), (3003, [100])), tags=["T5-profundidad"])
    caso("rec-profundidad-dos-con-simulacion-S", ["FM-CER-63", "FM-CER-68", "D-F3B-50", "matriz:C34", "matriz:C40"], entrada([3001], sim="S"), seed=sg,
         colaboradores=dict(**precios_con(sg, s3001=10000, s3002=10000, s3003=10000),
                            **ar(resp_ar(), [{"una_vez": True, "respuesta": resp_ar([res_ar(1, 3002, "S")])}, {"una_vez": True, "respuesta": resp_ar([res_ar(1, 3003, "S")])}])),
         custodia=movs_de((3001, [100]), (3002, [100]), (3003, [100])), tags=["T5-profundidad"])

    # ================================================================== M3: reinversion of the SAME family (review of the architecture of T4-B)
    sm3 = [R(3001), d_misma()]
    caso("m3a-misma-familia-con-retiro-de-custodia-60-y-40-llega-hasta-el-cierre", ["FM-CER-53", "FM-CER-55", "FM-CER-61", "FM-CER-67", "D-F3B-13", "D-F3B-45", "matriz:C31"], entrada([3001]), seed=sm3,
         colaboradores=precios_con(sm3, s3001=10000), custodia=movs(3001, 60, 40), tags=["T5-m3:a"])
    caso("m3b-misma-familia-sin-retiro-de-custodia-del-rescate", ["FM-CER-53", "FM-CER-40", "FM-CER-52", "D-F3B-13", "matriz:C31", "matriz:C41"], entrada([3001]), seed=sm3,
         colaboradores=precios_con(sm3, s3001=10000), custodia=dict(con=resp_custodia([])), tags=["T5-m3:b"])
    caso("m3b-misma-familia-con-retiro-de-custodia-de-otro-rescate-solamente", ["FM-CER-53", "FM-CER-40", "FM-CER-52", "D-F3B-13", "matriz:C31", "matriz:C41"], entrada([3001, 3002]),
         seed=sm3 + [rescate(3002)], colaboradores=precios_con(sm3 + [rescate(3002)], s3001=10000), custodia=dict(con=resp_custodia([mov(3002)])), tags=["T5-m3:b"])
    caso("m3c-misma-familia-con-todos-los-movimientos-en-cero-lanza-IndexOutOfBounds-en-el-primer-aporte", ["FM-CER-53", "FM-CER-55", "FM-CER-71", "D-F3B-13", "D-F3B-45", "matriz:C41"], entrada([3001]),
         seed=sm3, colaboradores=precios_con(sm3, s3001=10000), custodia=movs(3001, 0, 0), excepcion=True, tags=["T5-m3:c"])
    sm4 = [R(3001), destino(1, 3001, "FM003A", 70), destino(2, 3001, "FM005A", 30)]
    caso("m3d-otra-familia-con-dos-destinos-llega-hasta-el-cierre", ["FM-CER-54", "FM-CER-55", "FM-CER-61", "FM-CER-67", "D-F3B-13", "matriz:C32"], entrada([3001]), seed=sm4,
         colaboradores=precios_con(sm4, s3001=10000), custodia=movs(3001, 100), tags=["T5-m3:a"])
    caso("m3e-misma-familia-con-ar-que-rechaza-el-aporte-deja-los-efectos-previos-y-sale-certificada", ["FM-CER-53", "FM-CER-57", "D-F3B-03", "FM-CER-69", "matriz:C33"], entrada([3001, 3002]),
         seed=sm3 + [rescate(3002)], colaboradores=dict(**precios_con(sm3 + [rescate(3002)], s3001=10000), **ar(resp_ar([], [msg_ar(1)], 0))), custodia=movs_de((3001, [60, 40]), (3002, [1])))

    # ================================================================== big lots: determinism of the whole run (HashMap / parallelStream) and the order of everything
    ids30 = [4000 + ((i * 7) % 30) for i in range(30)]
    caso("lote-de-treinta-aportes-con-ids-cruzados-un-solo-update-y-un-solo-log", ["FM-CER-61", "FM-CER-62", "FM-CER-66", "FM-CER-67", "FM-CER-72", "matriz:C03", "matriz:C43"], entrada(ids30),
         seed=[ap(4000 + i, 500 + (i % 3), CodigoPortafolio="PF%d" % (500 + (i % 3))) for i in range(30)])
    caso("lote-de-doce-rescates-y-aportes-con-rechazos-y-notificacion-owp", ["FM-CER-61", "FM-CER-65", "FM-CER-69", "FM-CER-72", "matriz:C05", "matriz:C43"],
         entrada([3012, 4003, 3011, 4001, 3010, 4002, 3009, 4004, 3008, 4005, 3007, 4006]),
         seed=[OWP_S] + [ap(4000 + i, 500, IdTipoCanalIngreso=("OWP" if i % 2 else "OPT"), **(dict(estado="CRT") if i == 4 else {})) for i in range(1, 7)]
         + [rescate(3000 + i, IdTipoCanalIngreso=("OWP" if i % 3 == 0 else "OPT"), **(dict(FechaCertificacion="2026-03-09") if i == 9 else {})) for i in range(7, 13)],
         custodia=movs_de(*[(3000 + i, [1]) for i in range(7, 13)]))
    caso("lote-de-aportes-en-tres-portafolios-con-saldo-por-pares", ["FM-CER-61", "FM-CER-21", "FM-CER-72", "matriz:C03", "matriz:C43"], entrada([4001, 4002, 4003, 4004, 4005, 4006]),
         seed=[ap(4001, 500), ap(4002, 501, IdCliente=101, DocumentoIdentidad="22222222-2"), ap(4003, 502, IdCliente=102, DocumentoIdentidad="33333333-3"), ap(4004, 500), ap(4005, 501, IdCliente=101, DocumentoIdentidad="22222222-2"),
               ap(4006, 502, IdCliente=102, DocumentoIdentidad="33333333-3")])
    caso("lote-con-ids-en-orden-descendente-de-entrada", ["FM-CER-61", "FM-CER-67", "FM-CER-19", "matriz:C44"], entrada([4009, 4008, 4007, 4006, 4005, 4004]),
         seed=[ap(4004), ap(4005), ap(4006), ap(4007), ap(4008), ap(4009)])
    caso("lote-de-rescates-con-comision-spot-y-tesoreria-corre-todo-el-pipeline-sin-rechazos", ["FM-CER-61", "FM-CER-70", "matriz:C03"], entrada([3003, 3001, 3002]),
         seed=[OWP_S, rescate_tra(3001, IdTipoCanalIngreso="OWP"), rescate(3002, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0, MontoComisionLocal=119), rescate(3003),
               param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")],
         colaboradores=dict(**tesoreria(resp_tesoreria(generados=[gen(3001, 8801)])), **spot(resp_spot([op("3002", "ING", 7701)])), **_fact([3002])),
         custodia=movs_de((3001, [1]), (3002, [1]), (3003, [1])), tags=["T5-compuerta:G1", "T5-compuerta:G2"])


def _fact(ids):
    from cases_cer_t4 import facturacion, resp_mov_comp, mov_sal
    return facturacion(resp_mov_comp([mov_sal(i, 7000 + i) for i in ids]))
