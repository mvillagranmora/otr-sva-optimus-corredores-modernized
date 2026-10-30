"""FMM-CER T3 cases of front F-E (CUENTA DE INVERSION de aportes y rescates): FM-CER-29 (cargos y abonos de los aportes), FM-CER-30 (errores de la cuenta, aportes), FM-CER-42 (cargos y
abonos de los rescates) and FM-CER-43 (errores de la cuenta, rescates).

Legacy: `CertificarSolicitudAporteRescate.logica` C:622-640 (aportes, between the custody entry and the rescates) and C:679-694 (rescates, after the retirement of custody and its costs);
`mueveCuentaInversion` C:1741-1935 and `tipoMovimientoFondoMutuoGeneraCargoAbono` C:1938-1951. Every case is recorded with the unmodified legacy (modo completo); `excepcion=True` marks
a case that throws inside F-C..F-H.

Fixed facts of the golden (CrearCargoAbono double): the call carries `p_CargoAbonoAGenerar` (one element per line; for a deferred aporte the principal line goes LAST, for a rescate FIRST),
`p_simularOperacion` "N", `p_EsIngresoManual` "N", `p_FechaSistema` and the company / user / module of the request."""
from cases_cer_t3 import (caso, entrada, ap, aporte, rescate, rescate_tra, origen, reinv, mov, err, resp_custodia, res_custodia, calcula_custodia, fila_saldo, saldo_por_uso, msg, resp_cuenta,
                          cuenta_script, tesoreria, resp_tesoreria, spot, resp_spot, op, L, Num, config, param, ins, solo_tabla, tipo_movimiento, TRAS_APV, TRAS_FM)

SIS = "2026-03-02"
from cases_cer_lib import ABSENT
USD_FONDO_CLP = dict(IdMoneda="USD", CostoMonedaLocal=2000, Monto=0)                      # deferred: the aporte is paid in USD, the fund is in CLP
CLP_FONDO_USD = dict(IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=5000000, Monto=0)   # deferred: paid in CLP, fund in USD


def generar():
    # ================================================================== FM-CER-29 cuenta de inversion de los aportes
    caso("aporte-normal-un-cargo-en-la-moneda-del-fondo", ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-sin-mueve-cuenta-no-llama-a-la-cuenta", ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[aporte(3001, IdPortafolio=500, CodigoPortafolio="PF500", MueveCta="N", CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-de-origen-no-manual-no-llama-a-la-cuenta", ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, OrigenOperacion="A", CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-con-tipo-que-no-genera-cargo-abono-no-llama", ["FM-CER-29", "matriz:C18"], entrada([3001]),
         seed=[ap(3001, 500, IdTipoMovimientoFondoMutuo="APOSINCTA", CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-con-tipo-sin-fila-se-trata-como-N", ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, IdTipoMovimientoFondoMutuo="APOXXX", CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-diferido-usd-en-fondo-clp-tres-lineas-con-el-principal-al-final", ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, **USD_FONDO_CLP)])
    caso("aporte-diferido-clp-en-fondo-usd-tres-lineas", ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, **CLP_FONDO_USD)])
    caso("aporte-switch-S-toma-la-fecha-de-sistema-aunque-la-liquidacion-sea-posterior", ["FM-CER-29", "matriz:C18"], entrada([3001]),
         seed=[ap(3001, 500, Switch="S", FechaLiquidacion="2026-03-05", CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-switch-minuscula-s-tambien-por-equalsignorecase", ["FM-CER-29", "matriz:C18"], entrada([3001]),
         seed=[ap(3001, 500, Switch="s", FechaLiquidacion="2026-03-05", CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-con-liquidacion-posterior-al-sistema-usa-su-fecha", ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, FechaLiquidacion="2026-03-05", CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-con-liquidacion-igual-al-sistema-usa-su-fecha", ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, FechaLiquidacion=SIS, CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-con-liquidacion-anterior-al-sistema-usa-la-fecha-de-sistema", ["FM-CER-29", "matriz:C18"], entrada([3001]),
         seed=[ap(3001, 500, FechaLiquidacion="2026-03-01", CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-con-liquidacion-anterior-y-switch-S", ["FM-CER-29", "matriz:C18"], entrada([3001]),
         seed=[ap(3001, 500, FechaLiquidacion="2026-03-01", Switch="S", CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-fecha-de-certificacion-posterior-fija-fecha-de-operacion-y-comprobante", ["FM-CER-29", "FM-CER-18", "matriz:C09"], entrada([3001], fecha="2026-03-05"),
         seed=[ap(3001, 500, FechaCertificacion="2026-03-05", CostoMonedaLocal=250000, Monto=250000)])

    # conceptos de cuenta por tipo de movimiento (el CASE de SelectSolicitudes)
    for tipo, concepto in (("APODIV", "APODIV"), ("APOOVER", "APOOVE"), ("APORESPRO", "APOPRO"), ("APOTRASP", "APOTRS"), ("APODIVCFI", "APOCFI")):
        caso("aporte-%s-usa-el-concepto-%s" % (tipo.lower(), concepto.lower()), ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, IdTipoMovimientoFondoMutuo=tipo, CostoMonedaLocal=250000, Monto=250000)])
    caso("aporte-de-reinversion-usa-el-concepto-aporei", ["FM-CER-29", "FM-CER-26", "matriz:C18"], entrada([3002]),
         seed=[origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5")), reinv(3002, 3001, IdRegimenTributario="BF", CostoMonedaLocal=1000000, Monto=1000000)])
    caso("aporte-de-reinversion-diferido-hereda-el-concepto-en-las-tres-lineas", ["FM-CER-29", "FM-CER-26", "matriz:C18"], entrada([3002]),
         seed=[origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5")), reinv(3002, 3001, IdRegimenTributario="BF", **USD_FONDO_CLP)])

    # el lote: correlativo de lineas, orden ascendente, solo los aportes aceptados
    caso("lote-de-aportes-numera-las-lineas-correlativamente-normal-diferido-normal", ["FM-CER-29", "matriz:C18"], entrada([3003, 3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000), ap(3002, 501, **USD_FONDO_CLP), ap(3003, 502, CostoMonedaLocal=100000, Monto=100000)])
    caso("lote-de-aportes-uno-sin-mueve-cuenta-no-consume-numero-de-linea", ["FM-CER-29", "matriz:C18"], entrada([3001, 3002, 3003]),
         seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000), aporte(3002, IdPortafolio=501, CodigoPortafolio="PF501", MueveCta="N", CostoMonedaLocal=100000, Monto=100000),
               ap(3003, 502, CostoMonedaLocal=100000, Monto=100000)])
    caso("aporte-rechazado-por-saldo-no-se-envia-a-la-cuenta", ["FM-CER-29", "FM-CER-21", "matriz:C18"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=900000, Monto=900000), ap(3002, 501, CostoMonedaLocal=100000, Monto=100000), L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N")],
         saldo=L.saldo_flujo(fila_saldo(500, "CLP", 1000), fila_saldo(501, "CLP", 500000)))
    caso("aporte-rechazado-por-custodia-no-se-envia-a-la-cuenta", ["FM-CER-29", "FM-CER-27", "matriz:C18"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000), ap(3002, 501, CostoMonedaLocal=100000, Monto=100000)],
         custodia=dict(aportes=resp_custodia([mov(3002)], [err(3001, "UI_ERROR_CUSTODIA_APORTE", "Error de custodia en el aporte")], 1)))
    caso("todos-los-aportes-rechazados-por-F-A-no-llaman-a-la-cuenta", ["FM-CER-29", "FM-CER-06", "matriz:C18"], entrada([3001, 3002]),
         seed=[ap(3001, 500, estado="CRT", CostoMonedaLocal=250000, Monto=250000), rescate(3002, CostoMonedaLocal=1000000, Monto=1000000)])
    caso("simulacion-S-la-cuenta-recibe-simular-N-y-las-reglas-de-negocio-de-la-entrada", ["FM-CER-29", "D-F3B-01", "matriz:C40"], entrada([3001], sim="S", p_UsaReglasNegocio="S"),
         seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000)])
    caso("aportes-y-rescates-hacen-dos-llamadas-y-la-numeracion-reinicia", ["FM-CER-29", "FM-CER-42", "matriz:C18"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000), rescate(3002, CostoMonedaLocal=1000000, Monto=1000000)])
    caso("aporte-con-decimales-exactos-en-el-pago-de-un-fondo-usd", ["FM-CER-29", "FM-CER-23", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, **CLP_FONDO_USD)])
    caso("aporte-con-ejecutivo-y-portafolio-propios-en-cada-linea", ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[ap(3001, 777, IdEmpleadoEjecutivo=33, CostoMonedaLocal=250000, Monto=250000)])

    # ================================================================== FM-CER-30 errores de la cuenta de inversion (aportes)
    caso("aporte-error-de-la-cuenta-rechaza-al-aporte-con-el-mensaje", ["FM-CER-30", "D-F3B-03", "matriz:C18"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000), ap(3002, 501, CostoMonedaLocal=100000, Monto=100000)],
         colaboradores=cuenta_script(aportes=resp_cuenta([msg(1)], 1)))
    caso("aporte-error-en-una-linea-secundaria-del-diferido-rechaza-al-mismo-aporte", ["FM-CER-30", "matriz:C18"], entrada([3001, 3002]),
         seed=[ap(3001, 500, **USD_FONDO_CLP), ap(3002, 501, CostoMonedaLocal=100000, Monto=100000)],
         colaboradores=cuenta_script(aportes=resp_cuenta([msg(1, "UI_SALDO_CUENTA_INSUFICIENTE", "Saldo insuficiente en la cuenta de inversion")], 1)))
    caso("aporte-errores-en-dos-aportes-rechazan-a-ambos-y-suman-el-contador", ["FM-CER-30", "matriz:C18"], entrada([3001, 3002, 3003]),
         seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000), ap(3002, 501, CostoMonedaLocal=100000, Monto=100000), ap(3003, 502, CostoMonedaLocal=100000, Monto=100000)],
         colaboradores=cuenta_script(aportes=resp_cuenta([msg(0), msg(2, "UI_SALDO_CUENTA_INSUFICIENTE", "Saldo insuficiente en la cuenta de inversion")], 2)))
    caso("aporte-dos-errores-del-mismo-aporte-agregan-los-dos-mensajes", ["FM-CER-30", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, **USD_FONDO_CLP)],
         colaboradores=cuenta_script(aportes=resp_cuenta([msg(0), msg(2, "UI_SALDO_CUENTA_INSUFICIENTE", "Saldo insuficiente en la cuenta de inversion")], 2)))
    caso("aporte-mensaje-que-no-es-ERROR-no-rechaza", ["FM-CER-30", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000)],
         colaboradores=cuenta_script(aportes=resp_cuenta([msg(0, "UI_AVISO", "aviso de la cuenta", "ADVERTENCIA")], 0)))
    caso("aporte-mensaje-ERROR-en-minuscula-no-rechaza-por-equals", ["FM-CER-30", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000)],
         colaboradores=cuenta_script(aportes=resp_cuenta([msg(0, "UI_ERROR_CUENTA_INVERSION", "error en minuscula", "error")], 0)))
    caso("aporte-el-contador-de-la-cuenta-se-suma-aunque-no-haya-mensajes", ["FM-CER-30", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000)],
         colaboradores=cuenta_script(aportes=resp_cuenta([], 3)))
    caso("aporte-error-con-linea-desconocida-lanza-NPE-y-aborta-el-lote", ["FM-CER-30", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000)],
         colaboradores=cuenta_script(aportes=resp_cuenta([msg(99)], 1)), excepcion=True)
    caso("aporte-error-sin-id-lanza-NPE", ["FM-CER-30", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000)],
         colaboradores=cuenta_script(aportes=resp_cuenta([msg(ABSENT)], 1)), excepcion=True)
    caso("aporte-la-cuenta-falla-y-propaga-la-excepcion", ["FM-CER-29", "matriz:C18"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000)],
         colaboradores=L.falla("CrearCargoAbono", "cuenta de inversion no disponible"), excepcion=True)
    caso("aporte-rechazado-por-la-cuenta-deja-la-custodia-ya-ingresada", ["FM-CER-30", "FM-CER-27", "D-F3B-03", "matriz:C18"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000), ap(3002, 501, CostoMonedaLocal=100000, Monto=100000)],
         colaboradores=cuenta_script(aportes=resp_cuenta([msg(1)], 1)))

    # ================================================================== FM-CER-42 cuenta de inversion de los rescates
    caso("rescate-normal-un-abono-en-la-moneda-del-fondo", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, CostoMonedaLocal=1000000, Monto=1000000)])
    caso("rescate-no-exige-mueve-cuenta-ni-origen-manual", ["FM-CER-42", "D-F3B-17", "matriz:C26"], entrada([3001]),
         seed=[rescate(3001, MueveCta="N", OrigenOperacion="A", IdMedioDePago="TRA", CostoMonedaLocal=1000000, Monto=1000000)])
    caso("rescate-con-tipo-que-no-genera-cargo-abono-no-llama", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESSINCTA")])
    caso("rescate-diferido-usd-en-fondo-clp-abono-cargo-abono", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, **USD_FONDO_CLP)])
    caso("rescate-diferido-clp-en-fondo-usd", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, **CLP_FONDO_USD)])
    caso("rescate-de-reinversion-diferido-con-comision-local-cuatro-lineas", ["FM-CER-42", "matriz:C26"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESREINV", MontoComisionLocal=5000, **USD_FONDO_CLP)])
    caso("rescate-de-reinversion-con-comision-local-agrega-el-cargo-de-comision", ["FM-CER-42", "matriz:C26"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESREINV", MontoComisionLocal=5000, CostoMonedaLocal=1000000, Monto=1000000)])
    caso("rescate-de-reinversion-sin-comision-no-agrega-el-cargo", ["FM-CER-42", "matriz:C26"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESREINV", MontoComisionLocal=0, CostoMonedaLocal=1000000, Monto=1000000)])
    caso("rescate-RESCOM-con-comision-local-agrega-el-cargo-y-usa-el-concepto-rescom", ["FM-CER-42", "matriz:C26"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCOM", MontoComisionLocal=5000, CostoMonedaLocal=1000000, Monto=1000000)])
    caso("rescate-normal-con-comision-local-no-agrega-cargo-de-comision", ["FM-CER-42", "matriz:C26"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESNORM", MontoComisionLocal=5000, CostoMonedaLocal=1000000, Monto=1000000)])
    caso("rescate-de-reinversion-usa-el-concepto-resrei", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESREINV")])
    caso("rescate-de-traspaso-usa-el-concepto-restrs", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESTRASP")])
    caso("rescate-nn-usa-el-concepto-rescfm", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCNN")])
    caso("rescate-switch-S-toma-la-fecha-de-sistema-aunque-la-liquidacion-sea-posterior", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, Switch="S", FechaLiquidacion="2026-03-05")])
    caso("rescate-con-liquidacion-posterior-al-sistema-usa-su-fecha", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, FechaLiquidacion="2026-03-05")])
    caso("rescate-con-liquidacion-igual-al-sistema-usa-su-fecha", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, FechaLiquidacion=SIS)])
    caso("rescate-con-liquidacion-anterior-al-sistema-usa-la-fecha-de-sistema", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, FechaLiquidacion="2026-03-01")])
    caso("rescate-la-fecha-de-operacion-es-la-de-aceptacion", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001, FechaAceptacion="2026-02-20")])
    caso("rescate-rechazado-por-custodia-no-se-envia-a-la-cuenta", ["FM-CER-42", "FM-CER-31", "matriz:C26"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0), res_custodia(3002, "APD", 1000000)))
    caso("rescate-sin-cartera-retirada-no-se-envia-a-la-cuenta", ["FM-CER-42", "FM-CER-40", "matriz:C26"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         custodia=dict(con=resp_custodia([mov(3002)])))
    caso("rescate-advertido-por-custodia-usa-los-montos-ajustados-en-la-cuenta", ["FM-CER-42", "FM-CER-32", "matriz:C26"], entrada([3001]), seed=[rescate(3001)],
         calcula=calcula_custodia(res_custodia(3001, "ADV", 100, Monto=150003.7, NumeroCuotas=100, ValorCuota=1500.0037, MontoDestino=150003.7, ValorParidad=1, CostoEnFactorAjuste=3.8461,
                                               ValorFactorAjuste=39000.5, MontoMonedaSistema=150003.7)))
    caso("lote-de-rescates-numera-las-lineas-correlativamente-normal-diferido-comision", ["FM-CER-42", "matriz:C26"], entrada([3003, 3001, 3002]),
         seed=[rescate(3001), rescate(3002, **USD_FONDO_CLP), rescate(3003, IdTipoMovimientoFondoMutuo="RESREINV", MontoComisionLocal=7000, CostoMonedaLocal=1000000, Monto=1000000)])
    caso("lote-de-rescates-uno-sin-genera-cargo-abono-no-consume-numero-de-linea", ["FM-CER-42", "matriz:C26"], entrada([3001, 3002, 3003]),
         seed=[rescate(3001), rescate(3002, IdTipoMovimientoFondoMutuo="RESSINCTA"), rescate(3003)])
    caso("rescate-con-spot-tambien-mueve-la-cuenta-de-inversion", ["FM-CER-42", "FM-CER-34", "matriz:C26"], entrada([3001]),
         seed=[rescate(3001, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0), param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")])
    caso("rescate-con-retencion-apv-mueve-la-cuenta-con-el-pago-completo", ["FM-CER-42", "FM-CER-36", "D-F3B-09", "matriz:C26"], entrada([3001]),
         seed=[rescate(3001, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="APV-B", IdMedioDePago="TRA", CostoMonedaLocal=1000000, Monto=1000000), config("APV.PorcentajeRetencion", "15"),
               param("CONCEPTO_RETENCION_PAGO_SII", "RETSII")])
    caso("rescate-con-comision-calculada-de-certificacion-reduce-el-pago-en-la-cuenta", ["FM-CER-42", "FM-CER-37", "matriz:C26"], entrada([3001]),
         seed=[rescate(3001, CostoMonedaLocal=1000000, Monto=1000000), param("CALCULAR_COMISION_CERTIFICACION", "S")] + T2COM, colaboradores=T2LOTE,
         paridad=[T2PARIDAD(3001, 19998)])
    caso("simulacion-S-la-cuenta-de-los-rescates-recibe-simular-N", ["FM-CER-42", "D-F3B-01", "matriz:C40"], entrada([3001], sim="S", p_UsaReglasNegocio="S"), seed=[rescate(3001)])

    # ================================================================== FM-CER-43 errores de la cuenta de inversion (rescates)
    caso("rescate-error-de-la-cuenta-rechaza-al-rescate-con-el-mensaje", ["FM-CER-43", "D-F3B-03", "matriz:C26"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         colaboradores=cuenta_script(rescates=resp_cuenta([msg(0)], 1)))
    caso("rescate-error-en-una-linea-secundaria-del-diferido-rechaza-al-mismo-rescate", ["FM-CER-43", "matriz:C26"], entrada([3001, 3002]), seed=[rescate(3001, **USD_FONDO_CLP), rescate(3002)],
         colaboradores=cuenta_script(rescates=resp_cuenta([msg(2, "UI_SALDO_CUENTA_INSUFICIENTE", "Saldo insuficiente en la cuenta de inversion")], 1)))
    caso("rescate-errores-en-dos-rescates-rechazan-a-ambos", ["FM-CER-43", "matriz:C26"], entrada([3001, 3002, 3003]), seed=[rescate(3001), rescate(3002), rescate(3003)],
         colaboradores=cuenta_script(rescates=resp_cuenta([msg(0), msg(2)], 2)))
    caso("rescate-error-con-linea-desconocida-lanza-NPE-y-aborta-el-lote", ["FM-CER-43", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=[rescate(3001)],
         colaboradores=cuenta_script(rescates=resp_cuenta([msg(99)], 1)), excepcion=True)
    caso("rescate-mensaje-que-no-es-ERROR-no-rechaza", ["FM-CER-43", "matriz:C26"], entrada([3001]), seed=[rescate(3001)],
         colaboradores=cuenta_script(rescates=resp_cuenta([msg(0, "UI_AVISO", "aviso de la cuenta", "ADVERTENCIA")], 0)))
    caso("rescate-el-contador-de-la-cuenta-se-suma", ["FM-CER-43", "matriz:C26"], entrada([3001]), seed=[rescate(3001)], colaboradores=cuenta_script(rescates=resp_cuenta([], 2)))
    caso("rescate-la-cuenta-falla-y-propaga-la-excepcion", ["FM-CER-42", "matriz:C26"], entrada([3001]), seed=[rescate(3001)],
         colaboradores=L.falla("CrearCargoAbono", "cuenta de inversion no disponible"), excepcion=True)
    caso("aporte-y-rescate-con-error-cada-uno-rechazan-en-su-llamada", ["FM-CER-30", "FM-CER-43", "matriz:C26"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=250000, Monto=250000), rescate(3002)],
         colaboradores=cuenta_script(aportes=resp_cuenta([msg(0)], 1), rescates=resp_cuenta([msg(0, "UI_SALDO_CUENTA_INSUFICIENTE", "Saldo insuficiente en la cuenta de inversion")], 1)))
    caso("rescate-rechazado-por-la-cuenta-no-llega-a-tesoreria", ["FM-CER-43", "FM-CER-44", "D-F3B-03", "matriz:C27"], entrada([3001, 3002]), seed=[rescate_tra(3001), rescate_tra(3002)],
         colaboradores=cuenta_script(rescates=resp_cuenta([msg(0)], 1)), front="F-F")
    caso("rescate-rechazado-por-la-cuenta-deja-la-custodia-ya-retirada", ["FM-CER-43", "FM-CER-39", "D-F3B-03", "matriz:C26"], entrada([3001, 3002]), seed=[rescate(3001), rescate(3002)],
         colaboradores=cuenta_script(rescates=resp_cuenta([msg(1)], 1)))


# ---- helpers of the commission case (REAL CalcularComisiones with the scripted flow of custody)
from cases3a_lib import tramo, comision_maxima  # noqa: E402
from cases3a_com1 import lote, custodia as flujo_custodia  # noqa: E402
from cases_cer_t2 import paridad_comision as T2PARIDAD  # noqa: E402,F401

T2COM = [tramo(1, "FM001A", "SALIDA", 0, 30, 2.0) + [comision_maxima("FM001A")]]
T2LOTE = flujo_custodia(lote(5000, "2026-02-10"))
