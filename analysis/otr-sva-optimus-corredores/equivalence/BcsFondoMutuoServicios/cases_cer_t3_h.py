"""FMM-CER T3 cases of front F-H (SPOT de los rescates): FM-CER-59 (construccion del pojo y de las formas de pago "recibe" / "paga", `RESCATE_GENERA_VENTA_SPOT_DOLAR`, GeneraSpot) and
FM-CER-60 (lectura del resultado: `RECH` -> `ERROR_CREAR_SPOT`, ids de operacion, mensajes ERROR que rechazan al primero si el id es 0, contador).

Legacy: `CertificarSolicitudAporteRescate.logica` C:798-829 (the call and the reading, AFTER facturacion and reinversion) and `mueveSpot` C:2057-2157; collaborator
`ProcesarIngresoOperacionSpot` (gate G2: not ported, a registered double that records the COMPLETE `p_DatosOperacion` (the pojo of the solicitud) and `p_DatosFormaPago`). Every case
is recorded with the unmodified legacy (modo completo); `excepcion=True` marks a case that throws inside F-C..F-H.

NOT BUILT (unreachable): the `VENSPT` branch of `idTipoOperacionSpot` (an aporte of reinversion in USD): `mueveSpot` is only called with the RESCATES, so the type is always `COMSPT`."""
from cases_cer_t3 import (caso, entrada, ap, aporte, rescate, rescate_tra, origen, mov, err, resp_custodia, res_custodia, calcula_custodia, msg, resp_cuenta, cuenta_script, tesoreria,
                          resp_tesoreria, rech, gen, spot, resp_spot, op, L, Num, config, param, ins, solo_tabla, TRAS_APV, TRAS_FM)

SPOT_S = param("RESCATE_GENERA_VENTA_SPOT_DOLAR", "S")
PER = ins(L.COMISION, IdSerieFondo="FM001A", TipoComision="PER")
TRI = ins(L.COMISION, IdSerieFondo="FM001A", TipoComision="TRI")
USD = dict(IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0)         # the rescate in dollars paid in pesos: GeneraSpot S with the parameter


def sp(id_, **c):
    """a rescate that generates a Spot (USD paid in CLP) paid through `medio` (default the cuenta de inversion)"""
    d = dict(USD)
    d.update(c)
    return rescate(id_, **d)


def generar():
    f = dict(front="F-H")
    # ================================================================== FM-CER-59 construccion
    caso("spot-rescate-usd-pagado-en-clp-con-medio-cin", ["FM-CER-59", "FM-CER-34", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S], **f)
    caso("spot-medio-transferencia-afecta-tesoreria-en-la-forma-que-paga", ["FM-CER-59", "matriz:C35"], entrada([3001]), seed=[sp(3001, IdMedioDePago="TRA"), SPOT_S], **f)
    caso("spot-medio-vacio-se-toma-como-cin", ["FM-CER-59", "matriz:C35"], entrada([3001]), seed=[sp(3001, IdMedioDePago=""), SPOT_S], **f)
    caso("spot-medio-nulo-de-la-vista-se-toma-como-cin", ["FM-CER-59", "matriz:C35"], entrada([3001]), seed=[sp(3001, IdMedioDePago=None), SPOT_S], **f)
    caso("spot-medio-cheque-arma-la-forma-de-pago-con-ese-medio", ["FM-CER-59", "matriz:C35"], entrada([3001]), seed=[sp(3001, IdMedioDePago="CHE"), SPOT_S], **f)
    caso("spot-rescate-nn-afecta-tesoreria-en-la-forma-que-recibe", ["FM-CER-59", "FM-CER-34", "matriz:C35"], entrada([3001]), seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCNN"), SPOT_S], **f)
    caso("spot-rescate-nn-con-comision-periodica-usa-la-forma-de-transferencia", ["FM-CER-59", "matriz:C35"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESCNN"), SPOT_S, PER], **f)
    caso("spot-con-comision-periodica-usa-la-forma-comspt-tra", ["FM-CER-59", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S, PER], **f)
    caso("spot-con-comision-no-periodica-usa-la-forma-comspt-cin", ["FM-CER-59", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S, TRI], **f)
    caso("spot-la-fecha-de-liquidacion-de-las-dos-formas-es-la-del-rescate", ["FM-CER-59", "matriz:C35"], entrada([3001]), seed=[sp(3001, FechaLiquidacion="2026-03-09"), SPOT_S], **f)
    caso("spot-la-fecha-de-liquidacion-anterior-al-sistema-se-envia-tal-cual", ["FM-CER-59", "matriz:C35"], entrada([3001]), seed=[sp(3001, FechaLiquidacion="2026-03-01"), SPOT_S], **f)
    caso("spot-la-forma-que-recibe-lleva-el-monto-del-rescate-con-decimales", ["FM-CER-59", "matriz:C35"], entrada([3001]),
         seed=[rescate(3001, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=Num("2000.37"), Monto=0), SPOT_S], **f)
    caso("spot-dos-rescates-llegan-por-id-con-sus-dos-formas-cada-uno", ["FM-CER-59", "matriz:C35"], entrada([3002, 3001]),
         seed=[sp(3001), sp(3002, IdMedioDePago="TRA"), SPOT_S], **f)
    caso("spot-un-rescate-con-spot-y-otro-sin-llama-solo-con-el-primero", ["FM-CER-59", "FM-CER-34", "matriz:C35"], entrada([3001, 3002]), seed=[sp(3001), rescate(3002), SPOT_S], **f)
    caso("spot-junto-a-tesoreria-cada-rescate-va-a-su-colaborador", ["FM-CER-59", "FM-CER-44", "matriz:C35"], entrada([3001, 3002]), seed=[sp(3001), rescate_tra(3002), SPOT_S], **f)
    caso("spot-parametro-N-no-genera-spot-y-el-rescate-va-a-tesoreria", ["FM-CER-59", "FM-CER-34", "matriz:C35"], entrada([3001]), seed=[sp(3001, IdMedioDePago="TRA")], **f)
    caso("spot-simulacion-S-recibe-calcula-margen-S-y-las-reglas-de-negocio-de-la-entrada", ["FM-CER-59", "D-F3B-01", "matriz:C40"], entrada([3001], sim="S", p_UsaReglasNegocio="S"),
         seed=[sp(3001), SPOT_S], **f)
    caso("spot-rescate-rechazado-por-custodia-no-se-envia", ["FM-CER-59", "FM-CER-31", "matriz:C35"], entrada([3001, 3002]), seed=[sp(3001), sp(3002), SPOT_S],
         calcula=calcula_custodia(res_custodia(3001, "RZD", 0), res_custodia(3002, "APD", 1000000)), **f)
    caso("spot-rescate-sin-cartera-retirada-no-se-envia", ["FM-CER-59", "FM-CER-40", "matriz:C35"], entrada([3001, 3002]), seed=[sp(3001), sp(3002), SPOT_S],
         custodia=dict(con=resp_custodia([mov(3002)])), **f)
    caso("spot-rescate-rechazado-por-la-cuenta-no-se-envia", ["FM-CER-59", "FM-CER-43", "D-F3B-03", "matriz:C35"], entrada([3001, 3002]), seed=[sp(3001), sp(3002), SPOT_S],
         colaboradores=cuenta_script(rescates=resp_cuenta([msg(0)], 1)), **f)
    caso("spot-ADV-envia-los-montos-ajustados-de-custodia", ["FM-CER-59", "FM-CER-32", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S],
         calcula=calcula_custodia(res_custodia(3001, "ADV", 100, Monto=150003.7, NumeroCuotas=100, ValorCuota=1500.0037, MontoDestino=150003.7, ValorParidad=1, CostoEnFactorAjuste=3.8461,
                                               ValorFactorAjuste=39000.5, MontoMonedaSistema=150003.7)), **f)
    caso("spot-el-pojo-copia-la-preferencia-de-pago-en-el-autorizado-de-origen", ["FM-CER-59", "D-F3B-41", "matriz:C35"], entrada([3001]), seed=[sp(3001, IdPreferenciaPagoPortafolio=77, IdAutorizadoOrigen=9), SPOT_S], **f)
    caso("spot-sin-rescates-con-spot-no-se-llama-al-colaborador", ["FM-CER-59", "matriz:C35"], entrada([3001]), seed=[rescate(3001)], colaboradores=L.falla("ProcesarIngresoOperacionSpot", "no debe llamarse"), **f)
    caso("spot-un-aporte-no-genera-spot", ["FM-CER-59", "matriz:C35"], entrada([3001]), seed=[ap(3001, 500, IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0), SPOT_S],
         colaboradores=L.falla("ProcesarIngresoOperacionSpot", "no debe llamarse"), **f)

    # ================================================================== FM-CER-60 lectura del resultado
    caso("spot-resultado-ING-asigna-la-operacion-spot-a-la-solicitud", ["FM-CER-60", "matriz:C35"], entrada([3001, 3002]), seed=[sp(3001), sp(3002), SPOT_S],
         colaboradores=spot(resp_spot([op("3001", "ING", 8801), op("3002", "ING", 8802)])), **f)
    caso("spot-resultado-RECH-rechaza-con-error-crear-spot-y-deja-los-efectos-previos", ["FM-CER-60", "D-F3B-03", "matriz:C35"], entrada([3001, 3002]), seed=[sp(3001), sp(3002), SPOT_S],
         colaboradores=spot(resp_spot([op("3001", "RECH"), op("3002", "ING", 8802)])), **f)
    caso("spot-resultado-con-id-interno-sin-rescate-se-ignora", ["FM-CER-60", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S], colaboradores=spot(resp_spot([op("9999", "RECH")])), **f)
    caso("spot-resultado-con-id-interno-no-numerico-lanza-NumberFormatException", ["FM-CER-60", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([op("ABC", "ING", 8801)])), excepcion=True, **f)
    caso("spot-resultado-con-id-interno-nulo-lanza-NumberFormatException", ["FM-CER-60", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([{"Estado": "ING", "IdOperacionSpot": 8801}])), excepcion=True, **f)
    caso("spot-resultado-estado-rech-en-minuscula-no-rechaza-por-equals", ["FM-CER-60", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([op("3001", "rech", 8801)])), **f)
    caso("spot-resultado-RECH-y-luego-ING-de-la-misma-solicitud-deja-la-operacion-en-la-rechazada", ["FM-CER-60", "D-F3B-42", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([op("3001", "RECH"), op("3001", "ING", 8801)])), **f)
    caso("spot-resultado-ING-y-luego-RECH-de-la-misma-solicitud-la-deja-rechazada", ["FM-CER-60", "D-F3B-42", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([op("3001", "ING", 8801), op("3001", "RECH")])), **f)
    caso("spot-resultado-sin-operaciones-ni-mensajes-no-cambia-nada", ["FM-CER-60", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S], colaboradores=spot(resp_spot()), **f)
    caso("spot-mensaje-ERROR-con-id-del-rescate-lo-rechaza-con-su-codigo", ["FM-CER-60", "matriz:C35"], entrada([3001, 3002]), seed=[sp(3001), sp(3002), SPOT_S],
         colaboradores=spot(resp_spot([], [msg(3002, "UI_ERROR_SPOT_GENERAL", "Error general de la operacion spot")], 1)), **f)
    caso("spot-mensaje-ERROR-con-id-negativo-usa-el-valor-absoluto", ["FM-CER-60", "matriz:C35"], entrada([3001, 3002]), seed=[sp(3001), sp(3002), SPOT_S],
         colaboradores=spot(resp_spot([], [msg(-3002, "UI_ERROR_SPOT_GENERAL", "Error general de la operacion spot")], 1)), **f)
    caso("spot-mensaje-ERROR-con-id-cero-rechaza-al-primero-de-la-lista-aunque-no-sea-el-culpable", ["FM-CER-60", "D-F3B-14", "matriz:C35"], entrada([3002, 3001]),
         seed=[sp(3001), sp(3002), SPOT_S], colaboradores=spot(resp_spot([], [msg(0, "UI_ERROR_SPOT_GENERAL", "Error general de la operacion spot")], 1)), **f)
    caso("spot-mensaje-ERROR-con-id-cero-y-un-solo-rescate-lo-rechaza", ["FM-CER-60", "D-F3B-14", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([], [msg(0, "UI_ERROR_SPOT_GENERAL", "Error general de la operacion spot")], 1)), **f)
    caso("spot-mensaje-ERROR-con-id-cero-rechaza-al-primer-rescate-de-la-lista-que-no-genera-spot", ["FM-CER-60", "D-F3B-14", "matriz:C35"], entrada([3001, 3002]),
         seed=[rescate(3001), sp(3002), SPOT_S], colaboradores=spot(resp_spot([], [msg(0, "UI_ERROR_SPOT_GENERAL", "Error general de la operacion spot")], 1)), **f)
    caso("spot-mensaje-ERROR-en-minuscula-rechaza-por-equalsignorecase", ["FM-CER-60", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([], [msg(3001, "UI_ERROR_SPOT_GENERAL", "Error general de la operacion spot", "error")], 1)), **f)
    caso("spot-mensaje-que-no-es-ERROR-no-rechaza", ["FM-CER-60", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([], [msg(3001, "UI_AVISO", "aviso del spot", "ADVERTENCIA")], 0)), **f)
    caso("spot-mensaje-ERROR-con-id-de-un-rescate-ajeno-no-rechaza-a-nadie", ["FM-CER-60", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([], [msg(9999, "UI_ERROR_SPOT_GENERAL", "Error general de la operacion spot")], 1)), **f)
    caso("spot-mensaje-ERROR-sin-id-lanza-NPE", ["FM-CER-60", "D-NOR-9", "matriz:C41"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([], [msg(ABSENT_, "UI_ERROR_SPOT_GENERAL", "Error general de la operacion spot")], 1)), excepcion=True, **f)
    caso("spot-el-contador-del-colaborador-se-suma-a-la-salida", ["FM-CER-60", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S], colaboradores=spot(resp_spot([], [], 2)), **f)
    caso("spot-sin-contador-no-suma-nada", ["FM-CER-60", "D-F3B-14", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S], colaboradores=spot(resp_spot([], [], 0, True)), **f)
    caso("spot-falla-y-propaga-la-excepcion", ["FM-CER-60", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S], colaboradores=L.falla("ProcesarIngresoOperacionSpot", "spot no disponible"),
         excepcion=True, **f)
    caso("spot-RECH-de-uno-y-el-otro-se-certifica-con-su-operacion", ["FM-CER-60", "FM-CER-69", "matriz:C35"], entrada([3001, 3002]), seed=[sp(3001), sp(3002), SPOT_S],
         colaboradores=spot(resp_spot([op("3001", "RECH"), op("3002", "ING", 8802)])), **f)
    caso("spot-RECH-con-tesoreria-ok-del-otro-rescate-mantiene-sus-ingresos-egresos", ["FM-CER-60", "FM-CER-47", "D-F3B-03", "matriz:C35"], entrada([3001, 3002]),
         seed=[sp(3001), rescate_tra(3002), SPOT_S], colaboradores=dict(**spot(resp_spot([op("3001", "RECH")])), **tesoreria(resp_tesoreria([], [gen(3002, 502)]))), **f)
    caso("spot-RECH-deja-la-custodia-y-la-cuenta-ya-escritas", ["FM-CER-60", "FM-CER-42", "FM-CER-39", "D-F3B-03", "matriz:C35"], entrada([3001]), seed=[sp(3001), SPOT_S],
         colaboradores=spot(resp_spot([op("3001", "RECH")], [], 0)), **f)


ABSENT_ = __import__("cases_cer_lib").ABSENT


def generar_extra():
    f = dict(front="F-H")
    caso("spot-fondo-usd-el-monto-del-rescate-y-el-de-moneda-de-sistema-difieren", ["FM-CER-59", "matriz:C35"], entrada([3001]),
         seed=[rescate(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", IdTipoMovimientoFondoMutuo="RESCNN", CostoMonedaLocal=5000000, Monto=0), SPOT_S], **f)
