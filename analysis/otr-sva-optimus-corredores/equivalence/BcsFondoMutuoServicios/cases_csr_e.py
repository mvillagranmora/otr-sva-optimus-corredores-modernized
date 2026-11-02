"""FMM-CSR (E): the contract Certificar uses (crearAporteReinversion, C:2343-2560 and 762-787), lots of 2.000 and the cut by `return true` (D-F2-12, D-F2-19), a larger mixed lot."""
import cases_csr_lib as L
from cases_csr_lib import APORTE, APORTE_CERTIFICAR as AC, sol

DIFERIDO = [L.upd(L.SERIE, {"Diferido": "S"}, '"IdSerieFondo" IN (\'FMX01U\', \'FMN01U\', \'FM002A\')')]
SPOT_S = L.param("APORTE_GENERA_COMPRA_SPOT_DOLAR", "S")
USD_ = dict(IdMoneda="USD", Monto=1000, MontoOrigen=1000)
CABECERA = {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "N", "p_UsaReglasNegocio": "S", "p_IdTipoCanalIngreso": "AGE"}


def op(idi, estado="PEN", idop=777):
    return {"IdInterno": str(idi), "Estado": estado, "IdOperacionSpot": idop}


def generar():
    c = L.caso
    bc = L.base_colab

    def cert(slug, ref, aportes, seed=None, extra=None, **kw):
        col = bc(aportes)
        if extra:
            col = L.colab(col, extra)
        return c(slug, ref, aportes, colaboradores=col, seed=seed, **kw)

    cert("certificar-un-aporte", "FM-CER-crearAporteReinversion (C:2385-2442) / FM-REI-41: el aporte tal como lo arma Certificar (todos sus campos, canal AGE, simulacion N, FechaValorCuota ausente): resultado con IdInterno, IdSolicitudAporteRescate, "
         "IdTipoCanalIngreso y Switch que Certificar lee", [sol(AC)])
    cert("certificar-aporte-switch", "FM-CER (C:780-787) / FM-REI-48: aporte Switch S de Certificar (fechas del rescate de origen): el resultado trae Switch S (Certificar lo agrega a aportesSwitch con IdSolicitudAporteRescate e IdTipoCanalIngreso)",
         [sol(AC, Switch="S", IdSerieFondoOrigen="FMX01U", IdEmisorOrigen="EXT1", IdSerieFondo="FMX02U", IdMoneda="USD", Monto=1000, MontoOrigen=1000, MontoTotalInversion=1000,
              FechaCertificacionRescateOrigen="2026-03-04", FechaLiquidacionRescateOrigen="2026-03-04", FechaValorCuotaRescateOrigen="2026-03-03")])
    cert("certificar-aporte-usd-a-serie-clp-con-spot", "FM-CER (C:2436-2441) / FM-REI-63: aporte USD con las dos fechas de Spot de Certificar (FechaCertificacion y FechaLiquidacion del rescate) hacia una serie en pesos: Spot de compra con esas fechas",
         [sol(AC, IdMoneda="USD", Monto=1000, MontoOrigen=1000, MontoTotalInversion=1000, FechaCertificaSpotCompra="2026-03-03", FechaLiquidaSpotCompra="2026-03-04")], seed=DIFERIDO + SPOT_S, extra={
             "ProcesarIngresoOperacionSpot": {"defecto": {"P_OUT_MENSAJES": [], "P_OUT_IDOPERACIONSPOT": [op(1)]}}})
    cert("certificar-aporte-clp-a-serie-usd-con-spot", "FM-CER (C:2441-2447) / FM-REI-63: aporte en la moneda del sistema (CLP) a una serie USD: Certificar fija las dos fechas de Spot en la FechaAceptacion del rescate",
         [sol(AC, IdSerieFondo="FMX01U", FechaCertificaSpotCompra="2026-03-02", FechaLiquidaSpotCompra="2026-03-02")], seed=DIFERIDO + SPOT_S, extra={
             "ProcesarIngresoOperacionSpot": {"defecto": {"P_OUT_MENSAJES": [], "P_OUT_IDOPERACIONSPOT": [op(1)]}}})
    tres = [sol(AC, IdInterno=1, Monto=33333, MontoOrigen=33333, MontoTotalInversion=100000), sol(AC, IdInterno=2, Monto=33333, MontoOrigen=33333, IdSerieFondo="FMX01U", IdMoneda="CLP", MontoTotalInversion=100000),
            sol(AC, IdInterno=3, Monto=33334, MontoOrigen=33334, MontoTotalInversion=100000)]
    cert("certificar-tres-aportes-de-un-rescate", "FM-CER (C:2452-2460) / FM-REI-52/62: tres aportes del mismo rescate 9001 (el ultimo lleva la diferencia): un solo lote, folios de la secuencia salvo el informado (7000)", tres,
         seed=DIFERIDO)
    cert("certificar-rechazo-por-requisitos-vuelve-con-su-idinterno", "FM-CER (C:764-777) / FM-REI-55: un aporte rechazado por los requisitos (regimen NOAP): el mensaje trae el IdInterno del aporte (Certificar lo traduce al rescate con su mapa) y no hay resultado",
         [sol(AC, IdRegimenTributario="NOAP")])
    dos = [sol(AC, IdInterno=1), sol(AC, IdInterno=2, IdRegimenTributario="NOAP", IdSerieFondo="FMX01U")]
    cert("certificar-lote-con-un-rechazo", "FM-CER (C:762-787) / FM-REI-55/62: dos aportes, el segundo rechazado: contador 1, un resultado (el primero); como hay error el anfitrion revierte (ROLLBACK)", dos, seed=DIFERIDO)
    cert("certificar-canal-del-rescate-gana", "FM-CER (C:2429) / FM-REI-52: Canal OPT del rescate con p_IdTipoCanalIngreso AGE: la solicitud nace con el canal del rescate", [sol(AC, Canal="OPT")])
    cert("certificar-ingreso-automatico-s", "FM-CER (C:2431) / FM-REI-52: IngresoAutomatico S de la solicitud origen: se copia", [sol(AC, IngresoAutomatico="S")])
    cert("certificar-monto-total-de-inversion-distinto", "FM-CER (C:2434) / FM-REI-46/56: MontoTotalInversion (100000 * porcentaje) distinto de Monto (50000): la paridad se calcula sobre el total y ValidaMontoInversion usa su MontoDestino",
         [sol(AC, Monto=50000, MontoOrigen=50000, MontoTotalInversion=100000)])
    cert("certificar-portafolio-apv-hereda-costouf", "FM-CER (C:2412) / FM-REI-50: portafolio APV-A: el CostoUF que arma Certificar (CostoInversionEnFactorAjusteARetirar * porcentaje) se hereda", [sol(AC, IdPortafolio=501, CostoUF=0.123456789)])
    cert("certificar-mlt-18q-hereda", "FM-CER / FM-REI-50: MLT S con regimen 18Q y rescate 18Q: hereda el CostoUF de Certificar", [sol(AC, IndicadorMLT="S", IdRegimenTributario="18Q", IdSolicitudAporteRescateRelacionado=9002)])
    cert("certificar-fecha-emision-fecha-certificacion-del-rescate", "FM-CER (C:2418-2427) / FM-REI-48: FechaEmision que elige Certificar (liquidacion o certificacion del rescate, hoy 2026-03-06 viernes): fechas del aporte", [sol(AC, FechaEmision="2026-03-06")])
    cert("certificar-simulacion-siempre-n", "FM-CER (C:2543): Certificar siempre manda p_SimularOperacion N y canal AGE; con S la pasada no escribe (contrato de la cabecera)", [sol(AC)], p_SimularOperacion="S")
    cert("certificar-usa-reglas-de-negocio-n", "FM-CER (C:2544): p_UsaReglasNegocio de Certificar viene de su entrada (aqui N): la pasada de AR lo reenvia a Tesoreria y Spot (medio TRA)", [sol(AC, IdMedioDePago="TRA")], p_UsaReglasNegocio="N")
    # ---------------------------------------------------------------- lots of 2.000 (43, D-F2-19): the first lot is rejected one by one so the outputs stay small
    def grande(n_rechazados, ultimo, **extra):
        a = [sol(APORTE, IdInterno=i, IdRegimenTributario="NOAP") for i in range(1, n_rechazados + 1)]
        a.append(ultimo)
        return a

    g = [sol(APORTE, IdInterno=i) for i in range(1, 2002)]
    vista_conteo = """CREATE VIEW "CSR.ConteoSolicitudes" AS SELECT COUNT(*) AS "Filas", MAX("IdSolicitudAporteRescate") AS "UltimoId", MIN("IdSolicitudAporteRescate") AS "PrimerId" FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" WHERE "IdTipoMovimientoFondoMutuo" = 'APOREINV';"""
    c("lote-2001-aportes-dos-lotes", "D-CSR-01 FM-REI-43/62/64/65/C63, D-F2-19: 2.001 aportes validos: dos lotes (2.000 y 1): dos pedidos de secuencias (2.000 y 1), dos pasadas completas de lecturas y paridades, dos SaldoFlujoCaja, dos AgregarLog, "
      "el UPDATE de FechaFinalReinversion en cada lote; 2.001 filas insertadas (vista de conteo; los ids son consecutivos por lote)", g, colaboradores=bc(g), seed=[vista_conteo],
      tablas=L.TABLAS_GRANDES)
    a1 = [sol(APORTE, IdInterno=1, IdSerieFondo="FMX01U")] + [sol(APORTE, IdInterno=i, IdRegimenTributario="NOAP") for i in range(2, 2001)] + [sol(APORTE, IdInterno=2001)]
    c("lote-2001-corte-por-error-de-spot", "D-CSR-10 FM-REI-43/63/C57/C63, D-F2-12/D-F2-19: un aporte con Spot en el primer lote y error de Spot: `return true`: el segundo lote NO se procesa (sin mensaje, sin secuencias, sin lecturas, sin INSERT)", a1,
      colaboradores=L.colab(bc(a1), {"ProcesarIngresoOperacionSpot": {"defecto": {"P_OUT_MENSAJES": [{"id": 0, "tipo": "ERROR", "codigo": "UI_ERROR_SPOT", "descripcion": "sin liquidez"}],
                                                                              "P_OUT_IDOPERACIONSPOT": [op(1)]}}}), seed=DIFERIDO + SPOT_S + [vista_conteo], tablas=L.TABLAS_GRANDES)
    # ---------------------------------------------------------------- a mixed lot (determinism of the parallel streams)
    mixto = []
    for i in range(1, 31):
        k = dict(IdInterno=i, IdPortafolio=500 + (i % 6), IdMedioDePago="TRA" if i % 5 == 0 else "CIN", Monto=100000 + i * 1000, MontoOrigen=100000 + i * 1000)
        if i % 7 == 0:
            k["IdRegimenTributario"] = "NOAP"
        if i % 4 == 0:
            k["IdSerieFondo"] = "FMN01U"
            k["IdMoneda"] = "USD"
            k["Monto"] = 1000 + i
            k["MontoOrigen"] = 1000 + i
            k["IdSerieFondoOrigen"] = "FMX01U"
            k["IdEmisorOrigen"] = "EXT1"
        mixto.append(sol(APORTE, **k))
    c("lote-mixto-30-aportes", "FM-REI-43/55/56/61/64/65 (determinismo): 30 aportes de 6 portafolios, medios CIN/TRA, regimen que no permite aporte en algunos, series CLP y USD: orden de mensajes, resultados, secuencias e INSERT estables",
      mixto, colaboradores=bc(mixto), seed=DIFERIDO)
    # ---------------------------------------------------------------- order of the reads and writes with a non CIN medium (gate order, FM-REI-59/61)
    tra = [sol(APORTE, IdMedioDePago="TRA")]
    c("patrimonio-minimo-medio-tra", "FM-REI-59/61: medio TRA con PatrimonioMinimo > 0: la custodia se consulta ANTES de Tesoreria (lecturas, luego escrituras); sin SaldoFlujoCaja (no hay CIN)", tra,
      colaboradores=bc(tra), seed=[L.upd(L.SERIE, {"PatrimonioMinimo": 200000}, '"IdSerieFondo" = \'FM002A\'')])
    # ---------------------------------------------------------------- remaining branches of the reads, the collaborators' answers and the dates
    a1 = [sol(APORTE)]
    CLIENTE = '"BSCL.SocioNegocio.BD::CLI.Cliente"'
    c("cliente-sin-ejecutivo", "FM-REI-52/54: el cliente sin ejecutivo (IdPersonaEjecutivo sin empleado): IdEmpleadoEjecutivo 0 en la solicitud y nulo para el validador", a1, colaboradores=bc(a1),
      seed=[L.upd(CLIENTE, {"IdPersonaEjecutivo": 0}, '"IdCliente" = 100')])
    sab = [sol(APORTE, FechaEmision="2026-03-07")]
    c("fechaemision-sabado", "FM-REI-48: FechaEmision un sabado (dia inhabil): las fechas del calculo se corren al dia habil siguiente", sab, colaboradores=bc(sab))
    fer = [sol(APORTE, FechaEmision="2026-03-04")]
    c("fechaemision-vispera-de-feriado", "FM-REI-48: FechaEmision el miercoles 2026-03-04 con el feriado del 2026-03-05 (jueves): la certificacion y la liquidacion saltan el feriado", fer, colaboradores=bc(fer))
    pm = [L.upd(L.SERIE, {"PatrimonioMinimo": 200000}, '"IdSerieFondo" = \'FM002A\'')]
    c("patrimonio-minimo-flujo-de-otro-regimen-no-coincide", "FM-REI-59: la custodia es del instrumento pero de OTRO regimen (esFlujoCustodiaParaSolicitudProceso exige el regimen de la solicitud): no coincide, monto 0: rechazo por patrimonio minimo",
      a1, colaboradores=L.colab(bc(a1), L.custodia((901, "18Q", 5000))), seed=pm)
    tra1 = [sol(APORTE, IdMedioDePago="TRA")]
    c("tesoreria-rech-sin-campo-npe", "FM-REI-61/67: un resultado RECH con Campo nulo: NullPointerException (rs.Campo.equalsIgnoreCase) tras la llamada a Tesoreria", tra1,
      colaboradores=L.colab(bc(tra1), {"ProcesarIngresoEgresoTesoreria": {"defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_MENSAJES": [], "P_OUT_INGRESOEGRESOGENERADO": [],
                                                                                    "P_OUT_RESULTADOPROCESO": [{"Id": 1, "Campo": None, "Resultado": "RECH"}]}}}))
    spot1 = [sol(APORTE, IdSerieFondo="FMX01U")]
    c("spot-operacion-sin-idinterno", "FM-REI-63/67: una operacion de Spot sin IdInterno: NumberFormatException (Long.parseLong(null))", spot1,
      colaboradores=L.colab(bc(spot1), {"ProcesarIngresoOperacionSpot": {"defecto": {"P_OUT_MENSAJES": [], "P_OUT_IDOPERACIONSPOT": [{"IdInterno": None, "Estado": "PEN", "IdOperacionSpot": 5}]}}}), seed=DIFERIDO + SPOT_S)
    c("spot-operacion-repetida-gana-la-ultima", "D-CSR-09 FM-REI-63: dos respuestas de Spot para el mismo IdInterno: cada una se aplica (break solo del bucle interno): queda el IdOperacionSpot de la ultima", spot1,
      colaboradores=L.colab(bc(spot1), {"ProcesarIngresoOperacionSpot": {"defecto": {"P_OUT_MENSAJES": [], "P_OUT_IDOPERACIONSPOT": [op(1, "PEN", 31), op(1, "PEN", 32)]}}}), seed=DIFERIDO + SPOT_S)
    c("spot-dos-mensajes-error", "D-CSR-09 FM-REI-63, D-F2-12: dos mensajes ERROR de Spot: contador +2, dos mensajes 'SPT : ...' (id 0), siempre se rechaza la primera insertable; return true", spot1,
      colaboradores=L.colab(bc(spot1), {"ProcesarIngresoOperacionSpot": {"defecto": {"P_OUT_MENSAJES": [{"id": 0, "tipo": "ERROR", "codigo": "E1", "descripcion": "uno"}, {"id": 0, "tipo": "ERROR", "codigo": "E2", "descripcion": "dos"}],
                                                                                "P_OUT_IDOPERACIONSPOT": [op(1)]}}}), seed=DIFERIDO + SPOT_S)
    tres_noap = [sol(APORTE, IdInterno=i, IdRegimenTributario="NOAP") for i in (1, 2, 3)]
    c("tres-rechazos-iguales-un-solo-mensaje", "D-CSR-01 FM-REI-55, D-REQ-1, D-NOR-7: tres aportes con el mismo error de requisitos: el cierre informa UN solo mensaje (id del primero) y el AR rechaza solo la primera solicitud con ese id: las otras dos siguen pendientes (y se reportan como resultado)",
      tres_noap, colaboradores=bc(tres_noap))
    # ---------------------------------------------------------------- cases added after the mutation run of the reference implementation (survivors M13, M24, M44)
    usd_clp = [sol(APORTE, IdMoneda="USD", Monto=1000, MontoOrigen=1000)]
    c("spot-parametro-minuscula-usd-a-clp", "FM-REI-51/63, D-CSR-08: parametro 's' minuscula con un aporte USD a una serie en pesos (la segunda rama de la decision usa tambien equals('S')): no hay Spot", usd_clp,
      colaboradores=bc(usd_clp), seed=DIFERIDO + L.param("APORTE_GENERA_COMPRA_SPOT_DOLAR", "s"))
    cin_dif = [sol(APORTE, IdSerieFondo="FMN01U", IdSolicitudAporteRescateRelacionado=0)]
    c("saldo-usa-el-costo-en-moneda-local", "FM-REI-58/60: el saldo se compara con el COSTO en moneda local (CostoMonedaLocal 100000) y no con el Monto (111.11 USD): aporte CLP a serie USD diferida (sin Spot), related 0, saldo 1000 USD en la fecha: rechazo",
      cin_dif, colaboradores=L.colab(bc(cin_dif), L.saldo(1000, moneda="USD", fecha="2026-03-02")), seed=DIFERIDO)
    curso = [sol(APORTE, IdSerieFondoOrigen="FMX01U", IdEmisorOrigen="EXT1", IdSerieFondo="FMN03U", **USD_)]
    c("tipo-valutasnacusd-con-dias-de-curse", "FM-REI-47/48, D-SQL-1: VALUTASNACUSD usa los dias de curse de la serie destino (FMN03U: 2) y no 0 como SWITCH / INTERNACIONAL", curso, colaboradores=bc(curso))
