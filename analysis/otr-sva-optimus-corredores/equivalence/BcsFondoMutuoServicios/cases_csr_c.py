"""FMM-CSR (C): the validations of the AR (cards FM-REI-53..57, 59, 60; defects D-F2-13, D-F2-18, D-F2-21, D-REQ-6/7/9)."""
import cases_csr_lib as L
from cases_csr_lib import APORTE, sol

F11 = '"IdFondo" = \'11\''
S2 = '"IdSerieFondo" = \'FM002A\''
CIN = '"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"'
CLIEST = '"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteEstado"'


def generar():
    c = L.caso
    bc = L.base_colab

    def una(slug, ref, seed=None, extra=None, **kw):
        a = [sol(APORTE, **kw.pop("campos", {}))]
        col = bc(a)
        if extra:
            col = L.colab(col, extra)
        return c(slug, ref, a, colaboradores=col, seed=seed, **kw)

    # ---- maximum amount (D-F2-18: the third condition uses MontoTotalInversion, 0 by default)
    una("montomax-con-default-no-rechaza", "FM-REI-53/C67, D-F2-18: MontoMaximo 1000 < Monto 100000 pero MontoTotalInversion 0 (default): EsMontoMaximo S: pasa", seed=[L.upd(L.SERIE, {"MontoMaximo": 1000}, S2)])
    una("montomax-con-montototal-rechaza", "FM-REI-53/C67: MontoMaximo 1000 < Monto y < MontoTotalInversion 500000: EsMontoMaximo N: rechazo", seed=[L.upd(L.SERIE, {"MontoMaximo": 1000}, S2)],
        campos=dict(MontoTotalInversion=500000))
    una("montomax-cero-no-valida", "FM-REI-53: MontoMaximo 0 no valida aunque MontoTotalInversion sea grande", campos=dict(MontoTotalInversion=500000))
    una("montomax-monto-bajo-el-maximo", "FM-REI-53: Monto bajo el maximo: pasa", seed=[L.upd(L.SERIE, {"MontoMaximo": 150000}, S2)], campos=dict(MontoTotalInversion=500000))
    # ---- minimum amount (the same third condition with MontoMinimo > MontoTotalInversion)
    una("montomin-violado-con-default-rechaza", "FM-REI-53/C68, D-F2-18: MontoMinimo 200000 > Monto 100000 y > MontoTotalInversion 0: EsMontoMinimo N: rechazo", seed=[L.upd(L.SERIE, {"MontoMinimo": 200000}, S2)])
    una("montomin-con-montototal-mayor-pasa", "FM-REI-53/C68: MontoMinimo 200000 pero MontoTotalInversion 300000: la condicion MontoMinimo > MontoTotalInversion no se cumple: pasa",
        seed=[L.upd(L.SERIE, {"MontoMinimo": 200000}, S2)], campos=dict(MontoTotalInversion=300000))
    una("montomin-bf-exento", "FM-REI-53/C68: portafolio BF (502): EsMontoMinimo se fuerza a S", seed=[L.upd(L.SERIE, {"MontoMinimo": 200000}, S2)], campos=dict(IdPortafolio=502))
    una("montomin-otro-portafolio-no-exento", "FM-REI-53: solo BF esta exento del minimo: el portafolio 506 (apv-a, no BF) con el minimo violado rechaza",
        seed=[L.upd(L.SERIE, {"MontoMinimo": 200000}, S2)], campos=dict(IdPortafolio=506))
    una("montomin-regimen-no-valida", "FM-REI-53: el regimen no valida monto minimo (ValidaMontoMinimoInstrumento N): no hay chequeo de minimo en la solicitud",
        seed=[L.upd(L.SERIE, {"MontoMinimo": 200000}, S2), L.upd(L.REGIMEN, {"ValidaMontoMinimoInstrumento": "N"}, "\"IdRegimenFondoMutuo\" = 'GEN'")])
    # ---- deferred / regime / proposito / CIN
    una("diferido-n-moneda-distinta-rechaza", "D-CSR-11 FM-REI-53: aporte en CLP a una serie en USD con Diferido N (parametro Spot N): EsDiferido N: rechazo", campos=dict(IdSerieFondo="FMN01U"))
    una("diferido-s-moneda-distinta-pasa", "FM-REI-53/51: aporte CLP a serie USD con Diferido S (parametro Spot N): pasa; IdMonedaPago = moneda de la serie; sin Spot",
        seed=[L.upd(L.SERIE, {"Diferido": "S"}, '"IdSerieFondo" = \'FMN01U\'')], campos=dict(IdSerieFondo="FMN01U"))
    una("regimen-sin-fila-para-la-serie", "FM-REI-53/52: regimen ZZZ sin RegimenSerieDeFondo: EsRegimenValido N y sin Tributacion ni TipoCartera", campos=dict(IdRegimenTributario="ZZZ"))
    una("regimen-no-permite-aporte", "FM-REI-53: regimen NOAP (PermiteAporte N): rechazo", campos=dict(IdRegimenTributario="NOAP"))
    for rid, slug in (("APVA", "apv"), ("A108", "art108"), ("A107", "art107"), ("R57B", "57bis"), ("18Q", "defecto")):
        una("tributacion-" + slug, "FM-REI-52: GrupoRegimen del regimen %s -> Tributacion de la solicitud (ModeloRetiroAPV / MayorMenorValor / Beneficio107LIR / Beneficio57BIS / defecto)" % rid,
            campos=dict(IdRegimenTributario=rid, IdSolicitudAporteRescateRelacionado=9002))
    una("portafolio-sin-proposito-regimen", "FM-REI-53: el regimen no tiene configuracion de proposito para el portafolio: EsPortafolioRegimenValido N: rechazo",
        seed=["DELETE FROM %s WHERE \"IdRegimenFondoMutuo\" = 'GEN';" % L.PROPOSITO])
    una("cin-sin-indicador-npe", "FM-REI-53/67/C69: medio CIN y el portafolio sin indicador GEN_OCCINV (507): NullPointerException (indicadorPortafolio.ValorIndicador)", campos=dict(IdPortafolio=507))
    una("cin-indicador-n-rechaza", "FM-REI-53: medio CIN e indicador GEN_OCCINV N: EsCuentaInversionValido N: rechazo", seed=[L.upd(CIN, {"ValorIndicador": "N"}, '"IdPortafolio" = 500')])
    una("medio-no-cin-sin-indicador-no-falla", "FM-REI-53/61: medio TRA con el portafolio sin indicador: no se mira el indicador (cortocircuito): sigue a Tesoreria", campos=dict(IdPortafolio=507, IdMedioDePago="TRA"))
    una("overnight-liquidez-cerrada-advierte", "FM-REI-53/C70, D-F2-21: Overnight S al validador: con liquidez cerrada (comercial) advierte FECHA_LIQUIDEZ_CERRADO",
        seed=[L.upd(L.FONDO, {"EstadoLiquidezComercial": "C"}, F11)])
    una("fondo-bloqueado-para-aporte", "FM-REI-53: Fondo.BloqueoAporte S (EstadoBloqueo del validador): rechazo", seed=[L.upd(L.FONDO, {"BloqueoAporte": "S"}, F11)])
    una("serie-inactiva", "FM-REI-53: serie con IdEstado distinto de ACT (IdEstadoSerie): rechazo", seed=[L.upd(L.SERIE, {"IdEstado": "INA"}, S2)])
    una("portafolio-inactivo", "FM-REI-53/54: portafolio inactivo (portVal.IdEstadoPortafolio): rechazo", seed=[L.upd(L.PORTAFOLIO, {"IdEstado": "INA"}, '"IdPortafolio" = 500')])
    una("cliente-inactivo", "FM-REI-53/54: cliente inactivo (portVal.IdEstadoCliente): rechazo", seed=[L.upd(CLIEST, {"IdEstado": "INA"}, '"IdCliente" = 100')])
    una("cliente-no-es-el-del-portafolio", "D-CSR-05 FM-REI-53/54: IdCliente 777 distinto del cliente del portafolio: el validador NO rechaza (portVal.IdClienteReal viene del portafolio): pasa", campos=dict(IdCliente=777))
    # ---- patrimonio emisor (54, 57): candidate series with PatrimonioAltos S; the accumulation of the lot is added
    pat = [L.upd(L.SERIE, {"PatrimonioAltos": "S", "MontoPatrimonioEmisor": 500000}, S2)]
    una("patrimonio-emisor-insuficiente", "FM-REI-54/57/C72, D-REQ-7: PatrimonioAltos S, patrimonio del emisor 500000: 0 + acumulado del lote (100000) < 500000: UI_PATRIMONIO_MINIMO_EMISOR_INSUFICIENTE: rechazo", seed=pat)
    una("patrimonio-emisor-suficiente-con-monto-patrimonio", "FM-REI-54/57: MontoPatrimonio 450000 de la entrada + 100000 acumulado >= 500000: pasa", seed=pat, campos=dict(MontoPatrimonio=450000))
    dos = [sol(APORTE, IdInterno=1, MontoPatrimonio=350000), sol(APORTE, IdInterno=2, MontoPatrimonio=0)]
    c("patrimonio-emisor-acumulado-del-lote", "FM-REI-54/57/C72: dos aportes del mismo cliente y emisor: cada candidato suma el acumulado de TODO el lote (200000): el primero (350000+200000) pasa, el segundo (0+200000) falla",
      dos, colaboradores=bc(dos), seed=pat)
    una("patrimonio-emisor-serie-sin-patrimonio-altos", "FM-REI-54: PatrimonioAltos N: ni se acumula ni se valida aunque el emisor exija 500000", seed=[L.upd(L.SERIE, {"MontoPatrimonioEmisor": 500000}, S2)])
    # ---- ValidaMontoInversion (56): the amount is the MontoDestino of the parity of trabajo 1
    una("monto-inversion-minimo-total", "FM-REI-56/C71, D-REQ-6: MontoMinimoTotalInversion alto: ValidaMontoInversion rechaza con el MontoDestino de la paridad", seed=L.param("MontoMinimoTotalInversion", "999999"))
    una("monto-inversion-maximo-total", "FM-REI-56: MontoMaximoTotalInversion bajo: rechazo", seed=L.param("MontoMaximoTotalInversion", "100"))
    una("monto-inversion-aportes-altos", "FM-REI-56: serie con AportesAltos S: ValidaMontoInversion usa los limites de aportes altos (MontoMinimoAportesAltos alto)", seed=[L.upd(L.SERIE, {"AportesAltos": "S"}, S2)] + L.param("MontoMinimoAportesAltos", "999999"))
    dos2 = [sol(APORTE, IdInterno=1, IdMoneda="CLP"), sol(APORTE, IdInterno=2, IdMoneda="CLP", IdRegimenTributario="NOAP")]
    c("monto-inversion-incluye-rechazadas", "FM-REI-55/56/C71: el segundo aporte ya rechazado por requisitos (NOAP) tambien pasa por ValidaMontoInversion (todas las solicitudes del lote)", dos2, colaboradores=bc(dos2),
      seed=L.param("MontoMinimoTotalInversion", "999999"))
    # ---- balance of the investment account (58, 60, D-F2-13)
    una("saldo-insuficiente-relacionado-cero", "FM-REI-60/C74: relacionado 0, sobregiro ausente (N), saldo 10 < costo 100000: UI_SALDO_INSUFICIENTE_FLUJO_CAJA", campos=dict(IdSolicitudAporteRescateRelacionado=0),
        extra=L.saldo(10, fecha="2026-03-03"))
    una("saldo-suficiente-relacionado-cero", "FM-REI-60: saldo 100000 (= costo) en la FechaCertificacion: pasa (total > saldo es falso)", campos=dict(IdSolicitudAporteRescateRelacionado=0), extra=L.saldo(100000, fecha="2026-03-03"))
    una("saldo-fecha-distinta-es-cero", "FM-REI-60/C74: el saldo existe pero en otra fecha que FechaCertificacion: se toma 0: rechazo", campos=dict(IdSolicitudAporteRescateRelacionado=0), extra=L.saldo(99999999, fecha="2026-03-09"))
    una("saldo-sobregiro-s-no-valida", "FM-REI-60/C74: OperaLineaSobregiro S: no se valida el saldo", campos=dict(IdSolicitudAporteRescateRelacionado=0), extra=L.saldo(10, fecha="2026-03-03"),
        seed=[L.ins(L.INDFM, {"IdPortafolio": 500, "OperaLineaSobregiro": "S"})])
    una("saldo-sobregiro-n-explicito-valida", "FM-REI-60: OperaLineaSobregiro N explicito: valida: rechazo", campos=dict(IdSolicitudAporteRescateRelacionado=0), extra=L.saldo(10, fecha="2026-03-03"),
        seed=[L.ins(L.INDFM, {"IdPortafolio": 500, "OperaLineaSobregiro": "N"})])
    una("saldo-relacionado-distinto-de-cero-no-valida", "FM-REI-60/C73, D-F2-13: relacionado 9001: no se valida el saldo (pero SaldoFlujoCaja se llama igual)", extra=L.saldo(10, fecha="2026-03-03"))
    dos3 = [sol(APORTE, IdInterno=1, IdSolicitudAporteRescateRelacionado=0, Monto=60000, MontoOrigen=60000), sol(APORTE, IdInterno=2, IdSolicitudAporteRescateRelacionado=0, Monto=60000, MontoOrigen=60000)]
    c("saldo-acumula-por-portafolio-y-moneda", "FM-REI-58/60: dos aportes CIN del mismo portafolio y moneda: el total acumulado (120000) se compara con el saldo (100000): ambos rechazados; un solo par en SaldoFlujoCaja",
      dos3, colaboradores=L.colab(bc(dos3), L.saldo(100000, fecha="2026-03-03")))
    usd = dict(IdMoneda="USD", Monto=1000, MontoOrigen=1000, IdSerieFondoOrigen="FMX02U", IdEmisorOrigen="EXT1", IdSerieFondo="FMX01U")
    mixto = [sol(APORTE, IdInterno=1), sol(APORTE, IdInterno=2, IdPortafolio=501, **usd), sol(APORTE, IdInterno=3, **usd)]
    c("saldo-pares-portafolio-moneda-omitidos", "D-CSR-12 FM-REI-58, D-NOR-16 analogo: aportes (500, CLP), (501, USD) y (500, USD): el tercer par se OMITE porque su portafolio y su moneda ya estan en las listas independientes: solo se consultan dos pares", mixto,
      colaboradores=bc(mixto))
    c("saldo-no-se-llama-si-todos-son-no-cin", "FM-REI-58: sin aportes CIN pendientes no se llama SaldoFlujoCaja", [sol(APORTE, IdMedioDePago="TRA")], colaboradores=bc([APORTE]))
    # ---- custody and minimum equity of the fund (59)
    pm = [L.upd(L.SERIE, {"PatrimonioMinimo": 200000}, S2)]
    una("patrimonio-minimo-con-custodia-suficiente", "FM-REI-59/C75: PatrimonioMinimo 200000: Monto 100000 + custodia 1000 cuotas x valor cuota 1200.5 >= minimo: pasa; se consulta la custodia", seed=pm)
    una("patrimonio-minimo-sin-custodia-rechaza", "FM-REI-59/C75, D-REQ-9: sin custodia del portafolio y Monto 100000 < 200000: UI_PATRIMONIO_MINIMO_INSUFICIENTE (sin parametros)", seed=pm,
        extra=L.custodia())
    una("patrimonio-minimo-primer-flujo-gana", "FM-REI-59/C75, D-F2-8: dos filas de custodia del instrumento: se toma la PRIMERA (break) aunque la segunda alcanzaria", seed=pm,
        extra=L.custodia((901, "GEN", 0.01), (901, "GEN", 5000)))
    una("patrimonio-minimo-cero-no-consulta-custodia", "FM-REI-59: PatrimonioMinimo 0: no se llama ConsultarFlujoCustodia")
    dosp = [sol(APORTE, IdInterno=1), sol(APORTE, IdInterno=2, IdPortafolio=501)]
    c("custodia-una-cartera-por-portafolio", "FM-REI-59: dos portafolios con PatrimonioMinimo: una cartera por portafolio y un instrumento por IdInstrumento (con el regimen del primero)", dosp, colaboradores=bc(dosp), seed=pm)
