"""FMM-CSR (D): Tesoreria, Spot, writes, AGF, log, FechaFinalReinversion, lots (cards FM-REI-43, 61..65, 68; defects D-F2-4, D-F2-11, D-F2-12, D-F2-19, D-TES-1/2, D-NOR-5/12)."""
import cases_csr_lib as L
from cases_csr_lib import APORTE, sol

DIFERIDO = [L.upd(L.SERIE, {"Diferido": "S"}, '"IdSerieFondo" IN (\'FMX01U\', \'FMN01U\', \'FM002A\')')]
SPOT_S = L.param("APORTE_GENERA_COMPRA_SPOT_DOLAR", "S")
VISTA = "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate"


def tes(resultados=None, mensajes=None, contador=0, generados=None):
    return {"ProcesarIngresoEgresoTesoreria": {"defecto": {"P_OUT_ERRORESCONTADOR": contador, "P_OUT_MENSAJES": mensajes or [], "P_OUT_INGRESOEGRESOGENERADO": generados or [],
                                                           "P_OUT_RESULTADOPROCESO": resultados or []}}}


def spot(operaciones=None, mensajes=None, contador=None):
    d = {"P_OUT_MENSAJES": mensajes or [], "P_OUT_IDOPERACIONSPOT": operaciones or []}
    if contador is not None:
        d["P_OUT_ERRORESCONTADOR"] = contador
    return {"ProcesarIngresoOperacionSpot": {"defecto": d}}


def op(idi, estado="PEN", idop=777):
    return {"IdInterno": str(idi), "Estado": estado, "IdOperacionSpot": idop}


def generar():
    c = L.caso
    bc = L.base_colab

    def una(slug, ref, seed=None, extra=None, **kw):
        a = [sol(APORTE, **kw.pop("campos", {}))]
        col = bc(a)
        if extra:
            col = L.colab(col, extra)
        return c(slug, ref, a, colaboradores=col, seed=seed, **kw)

    def varias(slug, ref, aportes, seed=None, extra=None, **kw):
        col = bc(aportes)
        if extra:
            col = L.colab(col, extra)
        return c(slug, ref, aportes, colaboradores=col, seed=seed, **kw)

    # ---------------------------------------------------------------- Tesoreria (61)
    una("tesoreria-medio-tra", "FM-REI-61/C53, D-F2-11, D-TES-2: medio TRA: ProcesarIngresoEgresoTesoreria con la solicitud completa (p_EsIngresoAutomatico N); sin RECH la solicitud se inserta con IdIngresoEgreso 0",
        campos=dict(IdMedioDePago="TRA"))
    una("tesoreria-simulacion-no-llama", "FM-REI-61/66: medio TRA en simulacion: no llama a Tesoreria ni escribe", campos=dict(IdMedioDePago="TRA"), p_SimularOperacion="S")
    una("tesoreria-medio-cin-minuscula-no-llama", "FM-REI-52/61: medio 'cin' en minuscula cuenta como CIN (equalsIgnoreCase): sin Tesoreria, Visado N", campos=dict(IdMedioDePago="cin"))
    una("tesoreria-rech-numeroreferencia-rechaza", "D-CSR-02 FM-REI-61/C54, D-F2-11: Resultado RECH + Campo NumeroReferencia con el IdInterno: la solicitud queda RZD, NO se inserta y el contador sigue 0",
        campos=dict(IdMedioDePago="TRA"), extra=tes([{"Id": 1, "Campo": "NumeroReferencia", "Resultado": "RECH"}]))
    una("tesoreria-rech-otro-campo-ignora", "FM-REI-61/C54: RECH de otro Campo (Monto): ignorado, se inserta", campos=dict(IdMedioDePago="TRA"), extra=tes([{"Id": 1, "Campo": "Monto", "Resultado": "RECH"}]))
    una("tesoreria-resultado-ok-ignora", "FM-REI-61: Resultado distinto de RECH (OK): ignorado", campos=dict(IdMedioDePago="TRA"), extra=tes([{"Id": 1, "Campo": "NumeroReferencia", "Resultado": "OK"}]))
    una("tesoreria-error-sin-rech-ignora", "FM-REI-61/C54, D-F2-11, D-TES-1: Tesoreria responde contador 1 y un mensaje ERROR sin RECH: el AR ignora el contador y los mensajes: inserta, contador 0, IdIngresoEgreso 0",
        campos=dict(IdMedioDePago="TRA"), extra=tes(mensajes=[{"id": 1, "tipo": "ERROR", "codigo": "UI_ERROR_INGRESO_EGRESO", "descripcion": "falla"}], contador=1))
    una("tesoreria-generado-ignora", "FM-REI-61, D-F2-11: IngresoEgresoGenerado de Tesoreria ignorado: IdIngresoEgreso queda 0", campos=dict(IdMedioDePago="TRA"),
        extra=tes(generados=[{"NumeroReferencia": 1, "IdIngresoEgreso": 4242}]))
    dos = [sol(APORTE, IdInterno=1, IdMedioDePago="TRA"), sol(APORTE, IdInterno=2, IdMedioDePago="TRA", IdPortafolio=501)]
    varias("tesoreria-dos-aportes-uno-rech", "FM-REI-61: dos aportes TRA, RECH del segundo (Id 2): solo el segundo se rechaza; la lista a Tesoreria sale en el orden del lote", dos,
           extra=tes([{"Id": 2, "Campo": "NumeroReferencia", "Resultado": "RECH"}]))
    mix = [sol(APORTE, IdInterno=1), sol(APORTE, IdInterno=2, IdMedioDePago="TRA")]
    varias("tesoreria-lote-mixto-cin-y-tra", "FM-REI-61: un aporte CIN y uno TRA: solo el TRA va a Tesoreria; SaldoFlujoCaja solo por el CIN", mix)
    una("tesoreria-rechazada-por-validacion-no-se-envia", "FM-REI-61: una solicitud ya rechazada por los requisitos (regimen que no permite aporte) no se envia a Tesoreria", campos=dict(IdMedioDePago="TRA", IdRegimenTributario="NOAP"))
    una("tesoreria-excepcion-del-colaborador", "FM-REI-61/67/68: Tesoreria lanza una excepcion: sale sin catch (el anfitrion revierte)", campos=dict(IdMedioDePago="TRA"),
        extra={"ProcesarIngresoEgresoTesoreria": {"respuestas": [{"error": "falla de tesoreria"}]}})
    una("tesoreria-visado-respeta-entrada", "FM-REI-52: medio TRA: Visado de la entrada (N) se respeta; con CIN siempre N", campos=dict(IdMedioDePago="TRA", Visado="S"))
    # ---------------------------------------------------------------- Spot (51, 63)
    una("spot-clp-a-usd", "FM-REI-51/63/C55, D-F2-4: aporte CLP a serie USD (FMX01U) con el parametro S: GeneraSpot S, IdMonedaPago CLP, IdMoneda USD; forma 'recibe' COMSPT_CIN_CLP_0 (MOVING, MontoDestino) y 'paga' VENSPT_CIN_USD_0 (MOVEGR, Monto)",
        seed=DIFERIDO + SPOT_S, extra=spot([op(1)]), campos=dict(IdSerieFondo="FMX01U"))
    una("spot-usd-a-clp", "FM-REI-51/63/C56, D-F2-4: aporte USD a serie CLP (FM002A): COMSPT_CIN_USD_0 (MOVEGR) y VENSPT_CIN_CLP_0 (MOVING); FechaLiquidacion = FechaAceptacion",
        seed=DIFERIDO + SPOT_S, extra=spot([op(1)]), campos=dict(IdMoneda="USD", Monto=1000, MontoOrigen=1000))
    una("spot-usd-a-clp-con-fechas-del-rescate", "FM-REI-63/C56: aporte USD a serie CLP con FechaCertificaSpotCompra / FechaLiquidaSpotCompra (APOREINV): la liquidacion de las formas = FechaLiquidaSpotCompra; la operacion lleva una COPIA con FechaCertificacion = FechaCertificaSpotCompra",
        seed=DIFERIDO + SPOT_S, extra=spot([op(1)]), campos=dict(IdMoneda="USD", Monto=1000, MontoOrigen=1000, FechaCertificaSpotCompra="2026-03-10", FechaLiquidaSpotCompra="2026-03-11"))
    una("spot-clp-a-usd-con-fechas-del-rescate", "FM-REI-63: aporte CLP a serie USD con FechaCertificaSpotCompra / FechaLiquidaSpotCompra: la copia lleva FechaCertificacion; las formas CLP->USD siguen con FechaAceptacion (solo la rama USD->CLP usa la fecha de liquidacion)",
        seed=DIFERIDO + SPOT_S, extra=spot([op(1)]), campos=dict(IdSerieFondo="FMX01U", FechaCertificaSpotCompra="2026-03-10", FechaLiquidaSpotCompra="2026-03-11"))
    una("spot-solo-fecha-liquida", "FM-REI-63: USD->CLP con FechaLiquidaSpotCompra pero sin FechaCertificaSpotCompra: no hay copia (la operacion lleva la solicitud) pero la forma toma la fecha de liquidacion",
        seed=DIFERIDO + SPOT_S, extra=spot([op(1)]), campos=dict(IdMoneda="USD", Monto=1000, MontoOrigen=1000, FechaLiquidaSpotCompra="2026-03-11"))
    una("spot-operacion-asigna-idoperacionspot", "FM-REI-63: Estado PEN con IdOperacionSpot 777: la solicitud queda con IdOperacionSpot 777 y Estado PEN", seed=DIFERIDO + SPOT_S, extra=spot([op(1, "PEN", 777)]),
        campos=dict(IdSerieFondo="FMX01U"))
    una("spot-rech-sin-mensaje-se-inserta-rzd", "FM-REI-63/C58, D-F2-12: Estado RECH sin mensajes de error: la solicitud queda RZD pero se inserta igual (sigue en solicitudesInsertar)", seed=DIFERIDO + SPOT_S,
        extra=spot([op(1, "RECH", 0)]), campos=dict(IdSerieFondo="FMX01U"))
    una("spot-mensaje-error-corta-el-lote", "FM-REI-63/C57, D-F2-12, D-NOR-5/12: mensaje ERROR de Spot: contador +1, mensaje 'SPT : ...' (id 0), rechaza la primera insertable y `return true`: no inserta nada", seed=DIFERIDO + SPOT_S,
        extra=spot([op(1)], [{"id": 0, "tipo": "ERROR", "codigo": "UI_ERROR_SPOT", "descripcion": "sin liquidez"}]), campos=dict(IdSerieFondo="FMX01U"))
    una("spot-mensaje-advertencia-no-corta", "FM-REI-63: mensaje de tipo ADVERTENCIA de Spot: no cuenta ni corta (solo tipo ERROR)", seed=DIFERIDO + SPOT_S,
        extra=spot([op(1)], [{"id": 0, "tipo": "ADVERTENCIA", "codigo": "UI_SPOT_ADV", "descripcion": "aviso"}]), campos=dict(IdSerieFondo="FMX01U"))
    una("spot-mensaje-error-en-minuscula-corta", "FM-REI-63: tipo 'error' en minuscula: SpotError.equalsIgnoreCase('ERROR') SI cuenta y corta", seed=DIFERIDO + SPOT_S,
        extra=spot([op(1)], [{"id": 0, "tipo": "error", "codigo": "UI_ERROR_SPOT", "descripcion": "sin liquidez"}]), campos=dict(IdSerieFondo="FMX01U"))
    dosspot = [sol(APORTE, IdInterno=1, IdSerieFondo="FMX01U"), sol(APORTE, IdInterno=2, IdSerieFondo="FMX01U", IdPortafolio=501)]
    varias("spot-dos-aportes-error-rechaza-solo-el-primero", "FM-REI-63/C57, D-F2-12: dos aportes con Spot y un error: rechaza solo la PRIMERA solicitud insertable, y el lote entero no inserta", dosspot,
           seed=DIFERIDO + SPOT_S, extra=spot([op(1, "PEN", 11), op(2, "PEN", 12)], [{"id": 0, "tipo": "ERROR", "codigo": "UI_ERROR_SPOT", "descripcion": "sin liquidez"}]))
    varias("spot-dos-aportes-ok", "FM-REI-63: dos aportes con Spot: IdInternoPagoOperacion c++ (0,1,2,3), cada operacion con sus formas; cada solicitud toma su IdOperacionSpot", dosspot,
           seed=DIFERIDO + SPOT_S, extra=spot([op(1, "PEN", 11), op(2, "PEN", 12)]))
    mixspot = [sol(APORTE, IdInterno=1, IdSerieFondo="FMX01U"), sol(APORTE, IdInterno=2)]
    varias("spot-solo-las-que-generan-spot", "FM-REI-63: un aporte con Spot y otro sin: solo el primero va a ProcesarIngresoOperacionSpot (p_DatosOrigenInversion trae la entrada)", mixspot, seed=DIFERIDO + SPOT_S,
           extra=spot([op(1)]))
    una("spot-parametro-n-no-genera", "FM-REI-51/63: parametro N con CLP->USD (Diferido S): GeneraSpot N, sin llamada a Spot, IdMonedaPago = moneda de la serie (USD)", seed=DIFERIDO, campos=dict(IdSerieFondo="FMX01U"))
    una("spot-parametro-minuscula-no-genera", "D-CSR-08 FM-REI-51/63: parametro 's' minuscula: equals('S') falso: no hay Spot", seed=DIFERIDO + L.param("APORTE_GENERA_COMPRA_SPOT_DOLAR", "s"), campos=dict(IdSerieFondo="FMX01U"))
    una("spot-parametro-s-sin-solicitudes-spot", "FM-REI-63/C59: parametro S pero ningun aporte cruza CLP/USD: no hay llamada (el toMap corre igual)", seed=SPOT_S)
    una("spot-parametro-s-moneda-igual-no-genera", "FM-REI-51: parametro S, aporte USD en serie USD: no cruza: sin Spot", seed=SPOT_S, campos=dict(IdSerieFondoOrigen="FMX02U", IdEmisorOrigen="EXT1", IdSerieFondo="FMX01U", IdMoneda="USD", Monto=1000, MontoOrigen=1000))
    una("spot-generaspot-de-la-entrada-se-ignora", "FM-REI-51: GeneraSpot S en la entrada se ignora (se calcula): sin cruce de monedas no hay Spot", campos=dict(GeneraSpot="S"))
    una("spot-simulacion-no-llama", "FM-REI-63/66: simulacion con cruce de monedas: no llama a Spot; el resultado trae GeneraSpot S", seed=DIFERIDO + SPOT_S, campos=dict(IdSerieFondo="FMX01U"), p_SimularOperacion="S")
    una("spot-operacion-con-id-ajeno-se-ignora", "FM-REI-63: Spot responde un IdInterno que no es de ninguna solicitud: se ignora", seed=DIFERIDO + SPOT_S, extra=spot([op(99)]), campos=dict(IdSerieFondo="FMX01U"))
    una("spot-idinterno-no-numerico", "FM-REI-63/67: IdInterno no numerico en la respuesta: NumberFormatException (Long.parseLong)", seed=DIFERIDO + SPOT_S,
        extra=spot([{"IdInterno": "abc", "Estado": "PEN", "IdOperacionSpot": 1}]), campos=dict(IdSerieFondo="FMX01U"))
    una("spot-contador-de-spot-no-se-lee", "FM-REI-63: Spot responde P_OUT_ERRORESCONTADOR 5 sin mensajes de error: el AR solo lee los mensajes: no corta", seed=DIFERIDO + SPOT_S, extra=spot([op(1)], contador=5),
        campos=dict(IdSerieFondo="FMX01U"))
    una("spot-excepcion-del-colaborador", "FM-REI-63/67: Spot lanza una excepcion: sale sin catch", seed=DIFERIDO + SPOT_S, extra={"ProcesarIngresoOperacionSpot": {"respuestas": [{"error": "falla de mesa"}]}},
        campos=dict(IdSerieFondo="FMX01U"))
    una("spot-y-tesoreria-juntos", "FM-REI-61/63/68: medio TRA con cruce CLP->USD: primero Tesoreria y luego Spot; orden de las llamadas", seed=DIFERIDO + SPOT_S, extra=L.colab(spot([op(1)]), tes()),
        campos=dict(IdSerieFondo="FMX01U", IdMedioDePago="TRA"))
    una("spot-con-tesoreria-rech-no-llega-a-spot", "FM-REI-61/63: la solicitud rechazada por Tesoreria (RECH) no entra en solicitudesInsertar: sin llamada a Spot", seed=DIFERIDO + SPOT_S,
        extra=L.colab(spot([op(1)]), tes([{"Id": 1, "Campo": "NumeroReferencia", "Resultado": "RECH"}])), campos=dict(IdSerieFondo="FMX01U", IdMedioDePago="TRA"))
    # ---------------------------------------------------------------- INSERT, FechaFinalReinversion, AGF, log (62, 64, 65)
    una("insert-visado-con-cin-es-n", "FM-REI-52/C79: Visado S de la entrada con medio CIN: la solicitud queda Visado N", campos=dict(Visado="S"))
    una("insert-visado-nulo-no-cin", "FM-REI-52: Visado nulo con medio TRA: queda nulo (el INSERT acepta o no segun la columna)", campos=dict(IdMedioDePago="TRA", Visado=None))
    una("insert-pk-duplicada", "FM-REI-64/67/68: el id de la secuencia (11413788) ya existe en la tabla: el INSERT falla con SQLException tras Tesoreria",
        seed=[L.rescate_relacionado(11413788, "GEN")], campos=dict(IdMedioDePago="TRA"))
    dosf = [sol(APORTE, IdInterno=1, FolioContrato=0), sol(APORTE, IdInterno=2, FolioContrato=900, IdPortafolio=501)]
    varias("insert-folios-por-posicion", "FM-REI-52: un folio informado (> 0) y otro de la secuencia: la secuencia se pide para todo el lote y se toma por posicion del aporte en la lista", dosf)
    una("fechafinal-actualiza-la-fila-de-reinversion", "FM-REI-64/C77: la fila 1 de ReinversionSerieFondoDestino (rescate 9001, FM002A, GEN, FechaFinal 2026-03-02) se actualiza a la FechaCertificacion del aporte", seed=[])
    una("fechafinal-sin-cambio-no-actualiza", "FM-REI-64: la FechaFinalReinversion ya es igual a la FechaCertificacion (2026-03-03): la fila no se lee ni se actualiza",
        seed=[L.upd(L.REINV, {"FechaFinalReinversion": "2026-03-03"}, '"IdReinversionSerieFondo" = 1')])
    una("fechafinal-relacionado-sin-filas", "FM-REI-64: rescate relacionado 8888 sin filas de reinversion: ninguna actualizacion", campos=dict(IdSolicitudAporteRescateRelacionado=8888), extra=L.saldo(50000000, fecha="2026-03-03"))
    dosr = [sol(APORTE, IdInterno=1, FechaEmision="2026-03-02"), sol(APORTE, IdInterno=2, FechaEmision="2026-03-06")]
    varias("fechafinal-varios-aportes-misma-fila-gana-el-ultimo", "FM-REI-64/C77, D-F2-4: dos aportes a la misma fila (rescate 9001, FM002A, GEN) con distinta FechaCertificacion: la SELECT trae la fila dos veces y el ultimo UPDATE gana", dosr)
    dosr2 = [sol(APORTE, IdInterno=1, IdSerieFondo="FM002A"), sol(APORTE, IdInterno=2, IdSerieFondo="FMX01U", IdMoneda="USD", Monto=1000, MontoOrigen=1000, IdSerieFondoOrigen="FMX02U", IdEmisorOrigen="EXT1")]
    varias("fechafinal-dos-filas-del-mismo-rescate", "FM-REI-64: dos aportes del mismo rescate a series distintas: se actualizan las dos filas de reinversion (FM002A y FMX01U)", dosr2)
    una("fechafinal-lote-batch-sin-agf", "FM-REI-62/64: SolicitudBatch S: no entra a la lista AGF pero igual se actualiza FechaFinalReinversion", campos=dict(SolicitudBatch="S"))
    vista = L.fila_solicitud(L.VISTA_SOLICITUD, IdSolicitudAporteRescate=11413788, IdTipoCanalIngreso="AGE", IdCliente=100, IdPortafolio=500, TipoSolicitud="APO", IdSerieFondo="FM002A",
                             IdTipoMovimientoFondoMutuo="APOREINV", DocumentoIdentidad="11111111-1", CodigoPortafolio="PF500", NombreCompleto="Cliente Ficticio Uno",
                             NumeroCuotas=83.2986, ValorCuotaSolicitud=1200.5, Monto=100000, CalculoPorMontoCuota="M", IdMoneda="CLP", IdRegimenTributario="GEN",
                             EstadoSolicitud="ING", IdEstadoSerieFondo="ACT", FechaLiquidacion="2026-03-03", FechaAceptacion="2026-03-02", IdUsuario=7,
                             IdSolicitudAporteRescateRelacionado=9001)
    relaja = ['ALTER TABLE "BSCL.FondoMutuo.BD::FMM.InterfazAgf" ALTER COLUMN "%s" SET NULL;' % n for n, _ in L.columnas('"BSCL.FondoMutuo.BD::FMM.InterfazAgf"') if n != "IdInterfazAgf"]
    una("agf-h2-standin-estricto-falla", "FM-REI-65/C76 (limite H2): con UsaReplicaSigaFondoMutuo S y la solicitud nueva en la vista, el INSERT del beneficiario falla en H2 porque la tabla de paso InterfazAgf junta las columnas NOT NULL de beneficiario y solicitud: SQLException tras el INSERT de la solicitud",
        seed=[vista] + L.param("UsaReplicaSigaFondoMutuo", "S"))
    una("agf-con-fila-en-la-vista", "FM-REI-65/C76: con UsaReplicaSigaFondoMutuo S, la solicitud nueva (id 11413788) en la vista y InterfazAgf sin NOT NULL (limite H2): CrearSolicitudAGF inserta beneficiario y solicitud",
        seed=relaja + [vista] + L.param("UsaReplicaSigaFondoMutuo", "S"))
    una("agf-batch-no-entra", "FM-REI-62/65: SolicitudBatch S: no se pide AGF aunque la replica este activa", seed=[vista] + L.param("UsaReplicaSigaFondoMutuo", "S"), campos=dict(SolicitudBatch="S"))
    una("log-con-cliente-y-portafolio", "FM-REI-65: AgregarLog por solicitud insertada (IdTipoRegistro SOLICITUD_APORTE_REINVERSION, TipoCambio = ParidadFechaInversion.doubleValue())", campos=dict(IdSerieFondo="FMN01U", IdMoneda="USD", Monto=1000, MontoOrigen=1000, IdSerieFondoOrigen="FMX01U", IdEmisorOrigen="EXT1"),
        seed=DIFERIDO)
    dosl = [sol(APORTE, IdInterno=1), sol(APORTE, IdInterno=2, IdPortafolio=501, FolioContrato=77)]
    varias("log-dos-solicitudes-en-un-solo-llamado", "FM-REI-65: AgregarLog recibe UN llamado con una entrada por solicitud insertada (en el orden de insercion)", dosl)
    una("log-excepcion-tras-insertar", "FM-REI-65/67: AgregarLog lanza: la excepcion sale tras el INSERT (el anfitrion revierte)", extra={"AgregarLog": {"respuestas": [{"error": "falla log"}]}})
    una("log-no-se-llama-sin-insertables", "FM-REI-65: nada insertable (rechazada por requisitos): ni INSERT, ni AGF, ni log", campos=dict(IdRegimenTributario="NOAP"))
