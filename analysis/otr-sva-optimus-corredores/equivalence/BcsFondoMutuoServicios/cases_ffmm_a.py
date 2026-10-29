"""FM-CPFF cases (ConsultarCustodiaPortafolioFFMM REAL on H2; ConsultarEmpresa is the only scripted double): cards FM-CPFF-01..22 (RULES_FONDOMUTUO_F3_C.md 8), defects D-F3C-01..15 (section 9),
matrix C01..C22 (section 11; C23 = through CPF is the chain front, C24 = the old service is FfmmReglasTest) and the limits (letters after the number, or C25 and up).
Inputs only: expected.json is recorded from the legacy by record_ffmm.sh.

Universe (seed/base-ffmm.sql on top of seed/base-fmm.sql): see cases_ffmm_lib.py. In short: FechaSistema 2026-03-10; company 1 = person 1 / client 99 (the corredora); clients 100 / 101 (related
person) / 102; portfolios 500 .. 505; instruments 900 .. 905 (series FM001A .. FM006E, families FAM1 / FAM2 / FAM3, issuers AGF1 / AGF2 / EXT1). Default custody row: 100 quotas of FM001A, client
100, portfolio 500, regime GEN, vault COR, price 9800, purchase value 980000 (instrument currency) and 990000 (rate), in force since 2026-01-12, no egress. Default guarantee: 10 quotas of FM001A.
"""
from cases_ffmm_lib import *  # noqa: F401,F403

CERRADA = dict(EGRFechaLiquidacion=D("2026-02-27"), EGRFechaOperacion=D("2026-02-25"))   # a custody row closed before the query date


def P(inst, **kw):
    """Instrument columns (id, nemotecnico, issuer) of a custody / guarantee row."""
    emi, nemo = INSTRUMENTOS[inst]
    d = dict(IdInstrumento=inst, Nemotecnico=nemo, IdEmisor=emi)
    d.update(kw)
    return d


def nuevo_cliente(idc, persona, con_socio=True):
    s = [cliente(idc, persona, "4444444%d-1" % (idc % 10), "Cliente Ficticio %d" % idc)]
    if con_socio:
        s += [socio(1000 + idc, persona, idc), ins(SOCV, IdSocioDeNegocio=1000 + idc, IdPersona=persona, IdCliente=idc, IdEmpresa=1)]
    return s


def generar():
    contrato()
    fechas_y_errores()
    vigente_y_garantias()
    agrupaciones()
    fuentes_y_tipos_de_fecha()
    historico()
    filtros()
    solicitudes_en_transito()
    joins_y_fan_out()
    numeros()
    entradas_json()
    orden()


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def contrato():
    # ---- FM-CPFF-01 / C01 contract: only p_IdEmpresa and the date; every other field keeps the pojo default
    caso("01", "solo-empresa-y-fecha", "FM-CPFF-01 FM-CPFF-03 FM-CPFF-06 C01: JSON con solo p_IdEmpresa y p_FechaConsulta: usuario y modulo nulos (no se leen), cliente 0, portafolio 0, instrumento 0, "
         "agrupaciones N, tipo LIQ: una fila con la bodega y la relacion vacias", {"p_IdEmpresa": 1, "p_FechaConsulta": "2026-03-10"}, seed=[cartera(1)])
    caso("01b", "todos-los-campos-explicitos", "FM-CPFF-01 FM-CPFF-06: los 14 campos explicitos con sus valores por defecto equivalen al JSON minimo",
         {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_IdCliente": 0, "p_IdPortafolio": 0, "p_IdProductoAgrupador": None, "p_IdEmisor": None, "p_IdInstrumento": 0, "p_IdBoveda": "",
          "p_AgrupaPorBoveda": "N", "p_AgrupaPorRelacion": "N", "p_FechaConsulta": "2026-03-10", "p_TipoFechaConsulta": "LIQ", "p_IdFamiliaFondo": ""}, seed=[cartera(1)])
    caso("01c", "claves-desconocidas", "FM-CPFF-01: las claves que no son del pojo se ignoran (Gson)", entrada(p_Otro=1, p_IdPortafolioX="a"), seed=[cartera(1)])
    caso("25", "sin-datos", "FM-CPFF-02 FM-CPFF-03: sin custodia ni garantia ni solicitudes => {\"p_out_tbl\":[]} (sin contador de errores: D-F3A-27)", entrada())
    caso("04", "usuario-y-modulo-no-se-validan", "FM-CPFF-04 D-F3C-03: p_IdUsuario 999999 (inexistente) y p_IdModulo 'XXX' no se leen: no hay RevisarRequisitosBasicos; mismo resultado",
         entrada(p_IdUsuario=999999, p_IdModulo="XXX"), seed=[cartera(1)])
    caso("13", "boveda-como-filtro-se-ignora", "FM-CPFF-04 FM-CPFF-06 C13 D-F3C-03: p_IdBoveda 'DCV' (documentado como filtro) no llega al SQL: la posicion de la bodega COR sale igual",
         entrada(p_IdBoveda="DCV"), seed=[cartera(1)])


def fechas_y_errores():
    # ---- FM-CPFF-07 / C02: the date by default
    caso("02", "fecha-por-defecto-llamada-directa", "FM-CPFF-07 C02 D-F3C-04: sin p_FechaConsulta el pojo trae '0001-01-01' y llega CRUDO al SQL (no se reemplaza por PLA.FechaSistema): BETWEEN falso, la "
         "custodia vigente y la solicitud no salen: p_out_tbl vacio", entrada(p_FechaConsulta="ABSENT"), seed=[cartera(1), solicitud(1, IdSerieFondo="FM002B")])
    caso("02b", "fecha-cero-explicita", "FM-CPFF-07 D-F3C-04: '0001-01-01' explicito: igual que el defecto (fuente vigente por la comparacion de texto, vacio)", entrada(p_FechaConsulta="0001-01-01"),
         seed=[cartera(1)])
    caso("02c", "fecha-cero-con-adq", "FM-CPFF-07 D-F3C-04 D-F3C-05: '0001-01-01' con ADQ: vacio tambien", entrada(p_FechaConsulta="0001-01-01", p_TipoFechaConsulta="ADQ"), seed=[cartera(1)])
    caso("02d", "fecha-cero-con-posicion-desde-el-origen", "FM-CPFF-07 D-F3C-04: la fecha por defecto '0001-01-01' SI devuelve una posicion (y una garantia) cuyo ingreso por liquidacion es '0001-01-01' (vigente desde el "
         "origen: el BETWEEN con el texto crudo da '0001-01-01' <= '0001-01-01' < '2300-12-31'); la posicion normal (ingreso 2026-01-12) no", entrada(p_FechaConsulta="ABSENT"),
         seed=[cartera(1, INGFechaLiquidacion=D("0001-01-01"), INGFechaOperacion=D("0001-01-01")), garantia(origen=1, INGFechaLiquidacion=D("0001-01-01"), INGCantidad=N("5")), cartera(2, **P(901))])
    caso("02e", "fecha-cero-usa-la-fuente-vigente", "FM-CPFF-07 D-F3C-04: con la fecha '0001-01-01' la fuente es la VIGENTE aunque exista un respaldo del dia '0001-01-01' (el historico no se consulta): sale la cartera vigente "
         "y no la del respaldo", entrada(p_FechaConsulta="ABSENT"),
         seed=[his(1, FechaRespaldo=TS("0001-01-01 00:00:00"), INGFechaLiquidacion=D("0001-01-01")), cartera(2, INGFechaLiquidacion=D("0001-01-01"), **P(901))])
    # ---- FM-CPFF-05 / C03: the date is parsed BEFORE any query
    caso("03", "fecha-nula", "FM-CPFF-05 C03: p_FechaConsulta null explicito (Gson lo deja nulo): Fecha.toDate(null) => NullPointerException despues de ConsultarEmpresa y PLA.FechaSistema", entrada(p_FechaConsulta=None),
         seed=[cartera(1)])
    caso("03b", "fecha-mal-formada", "FM-CPFF-05 C03: '10/03/2026' => DateTimeParseException", entrada(p_FechaConsulta="10/03/2026"), seed=[cartera(1)])
    caso("03c", "fecha-vacia", "FM-CPFF-05: '' => DateTimeParseException", entrada(p_FechaConsulta=""), seed=[cartera(1)])
    caso("03d", "fecha-inexistente", "FM-CPFF-05 D-F3C-16: '2026-02-30' NO falla: el formateador yyyy-MM-dd (resolutor SMART) lo lleva al 2026-02-28 (anterior al sistema: fuente HISTORICA) y el texto crudo viaja al SQL: vacio", entrada(p_FechaConsulta="2026-02-30"), seed=[cartera(1)])
    caso("03e", "fecha-sin-ceros", "FM-CPFF-05: '2026-3-10' (no cero a la izquierda) => DateTimeParseException", entrada(p_FechaConsulta="2026-3-10"), seed=[cartera(1)])
    caso("03f", "fecha-con-hora", "FM-CPFF-05 FM-CPFF-07: '2026-03-10 00:00:00' (19 caracteres): se parsea con el formato fecha-hora y el TEXTO crudo (con hora) llega al SQL", entrada(p_FechaConsulta="2026-03-10 00:00:00"),
         seed=[cartera(1)])
    # ---- FM-CPFF-05 / C04: company and PLA.FechaSistema
    caso("04b", "empresa-inexistente", "FM-CPFF-05 C04: ConsultarEmpresa sin fila => Exception('Empresa no existe') y ninguna otra consulta", entrada(p_IdEmpresa=99999), seed=[cartera(1)],
         colaboradores={"ConsultarEmpresa": {"respuestas": [{"error": "Empresa no existe"}]}})
    caso("04c", "sin-fecha-de-sistema", "FM-CPFF-05 C04: sin PLA.FechaSistema => Exception de ConfiguracionSistema (mensaje del parametro faltante)", entrada(), seed=[config("PLA.FechaSistema", None), cartera(1)])
    caso("04d", "empresa-nula", "FM-CPFF-05 D-NOR-9 DESVIO-KIT-1: p_IdEmpresa null explicito: ConsultarEmpresa se llama con null (el doble responde) y ConfiguracionSistema falla con NullPointerException "
         "(el kit lo cambia por ParametroNoEncontradoException)", entrada(p_IdEmpresa=None), seed=[cartera(1)])
    caso("04e", "empresa-ausente", "FM-CPFF-05 D-NOR-9 DESVIO-KIT-1: sin p_IdEmpresa (nulo): igual que el nulo explicito", entrada(p_IdEmpresa="ABSENT"), seed=[cartera(1)])
    caso("04f", "empresa-sin-configuracion", "FM-CPFF-05 DESVIO-KIT-1: empresa 77 sin parametros de configuracion: NullPointerException del legacy (el kit: ParametroNoEncontradoException)",
         entrada(p_IdEmpresa=77), seed=[cartera(1)])
    caso("04g", "fecha-de-sistema-invalida", "FM-CPFF-05: PLA.FechaSistema 'hoy' => DateTimeParseException (la fecha de entrada se parsea primero, la de sistema despues)", entrada(),
         seed=[config("PLA.FechaSistema", "hoy"), cartera(1)])
    caso("04h", "fecha-de-sistema-con-hora", "FM-CPFF-05 FM-CPFF-07: PLA.FechaSistema '2026-03-10 00:00:00' (19 caracteres) se parsea igual: fecha de entrada = sistema => vigente", entrada(),
         seed=[config("PLA.FechaSistema", "2026-03-10 00:00:00"), cartera(1)])
    caso("04i", "empresa-sin-persona", "FM-CPFF-11 FM-CPFF-05: la empresa sin IdPersona (nulo): IdPersonaCuentaPropia nulo => setNull => sin personas relacionadas; con N no se nota", entrada(), seed=[cartera(1)],
         colaboradores={"ConsultarEmpresa": {"defecto": {"IdEmpresa": 1, "IdCliente": 99}}})


def vigente_y_garantias():
    # ---- C05 / C06: custody
    caso("05", "custodia-vigente-liq-una-posicion", "FM-CPFF-08 FM-CPFF-13 FM-CPFF-15 C05 (ejemplo A): una posicion de 100 cuotas (valor 980000, monto 990000, precio 9800): una fila, "
         "INGPrecioPromedioCompra 9800, MontoPrecioPromedio 980000, PPPCustodia 9800, TotalPosicion 100", entrada(), seed=[cartera(1)])
    caso("05b", "decimales", "FM-CPFF-13 FM-CPFF-15: cantidades con 8 decimales: 33.33333333 cuotas, valor 1000000.50, monto 1000001.25, precio 30000.12345678", entrada(),
         seed=[cartera(1, INGCantidad=N("33.33333333"), INGMontoCompraActualMonedaInstrumento=N("1000000.50"), INGMontoATasaCompraActual=N("1000001.25"), PrecioCompra=N("30000.12345678"))])
    caso("06", "dos-filas-mismo-grupo", "FM-CPFF-13 FM-CPFF-15 C06 D-F3C-11: dos filas del mismo grupo (60 y 40 cuotas, precios 10000 y 9500): Cantidad 100, ValorCompra 980000, MontoCompra 990000, "
         "MontoPrecioPromedio 60*10000+40*9500 = 980000", entrada(),
         seed=[cartera(1, INGCantidad=N("60"), INGMontoCompraActualMonedaInstrumento=N("600000"), PrecioCompra=N("10000"), INGMontoATasaCompraActual=N("600000")),
               cartera(2, INGCantidad=N("40"), INGMontoCompraActualMonedaInstrumento=N("380000"), PrecioCompra=N("9500"), INGMontoATasaCompraActual=N("390000"))])
    caso("06b", "dos-filas-bodegas-distintas-se-suman", "FM-CPFF-12 FM-CPFF-13: dos filas con bodegas DCV y COR y p_AgrupaPorBoveda N: un solo grupo, IdBoveda ''", entrada(),
         seed=[cartera(1, IdBoveda="DCV", INGCantidad=N("60")), cartera(2, IdBoveda="COR", INGCantidad=N("40"))])
    caso("06c", "mismo-cliente-dos-fondos-dos-grupos", "FM-CPFF-12: el mismo cliente y portafolio con dos instrumentos (900 y 901): dos grupos (la clave empieza por el instrumento)", entrada(),
         seed=[cartera(1), cartera(2, **P(901))])
    # ---- C07 / C08 / C19: guarantee
    caso("07", "custodia-100-mas-garantia-10", "FM-CPFF-14 FM-CPFF-15 C07 D-F3C-01 D-F3C-02 (ejemplo B): custodia 100 + garantia 10: CantidadPrecioPromedio 110, MontoPrecioPromedio 0 (se reinicia por el monto "
         "de compra nulo de la garantia), PPPCustodia 0, TotalPosicion 110", entrada(), seed=[cartera(1), garantia(origen=1)])
    caso("07b", "garantia-negativa", "FM-CPFF-14 FM-CPFF-15: garantia negativa (-4) con custodia 100: CantidadPrecioPromedio 96, PPP 0, TotalPosicion 96", entrada(),
         seed=[cartera(1), garantia(origen=1, INGCantidad=N("-4"))])
    caso("07c", "garantia-de-otro-instrumento", "FM-CPFF-14: garantia del instrumento 901 y custodia del 900: dos grupos; el de garantia sin custodia (CantidadCustodia 0)", entrada(),
         seed=[cartera(1), cartera(2, **P(901), **CERRADA), garantia(origen=2, **P(901))])
    caso("07d", "garantia-estado-anulado", "FM-CPFF-10: garantia ANU no sale; garantia en estado 'LIQ' (distinto de ANU) si", entrada(),
         seed=[cartera(1), garantia(origen=1, IdEstado="ANU", INGCantidad=N("7")), garantia(origen=1, IdEstado="LIQ", INGCantidad=N("3"))])
    caso("07e", "garantia-vigencia", "FM-CPFF-10 QD-F3C-06: garantia que empieza en 2026-03-11 (no sale), que termina el 2026-03-10 (excluida: egreso exclusivo), que termina el 2026-03-11 (sale) y sin egreso (sale)",
         entrada(), seed=[cartera(1), garantia(origen=1, INGFechaLiquidacion=D("2026-03-11"), INGCantidad=N("1")), garantia(origen=1, EGRFechaLiquidacion=D("2026-03-10"), INGCantidad=N("2")),
                          garantia(origen=1, EGRFechaLiquidacion=D("2026-03-11"), INGCantidad=N("4")), garantia(origen=1, INGCantidad=N("8"))])
    caso("07f", "garantia-clasificacion-y-tipo", "FM-CPFF-10: garantia con IdTipoInstrumento 'ACC' o clasificacion 'RV' no sale", entrada(),
         seed=[cartera(1), garantia(origen=1, IdTipoInstrumento="ACC", INGCantidad=N("1")), garantia(origen=1, IdClasificacionTipoInstrumento="RV", INGCantidad=N("2"))])
    caso("07g", "garantia-sin-socio-de-negocio", "FM-CPFF-10: garantia de un cliente SIN socio de negocio (INNER JOIN SOC): se omite; la custodia del mismo cliente (LEFT JOIN) sale con IdSocioDeNegocio nulo",
         entrada(p_IdCliente=103), seed=[nuevo_cliente(103, 7103, con_socio=False), cartera(1, IdCliente=103, IdPortafolio=0), garantia(origen=1, IdCliente=103, IdPortafolio=0)])
    caso("07h", "garantia-empresa-distinta", "FM-CPFF-10: garantia de la empresa 2 no sale", entrada(), seed=[cartera(1), garantia(origen=1, IdEmpresa=2)])
    caso("07i", "garantia-cartera-origen-inexistente", "FM-CPFF-10: garantia cuya cartera origen no existe (INNER JOIN): se omite", entrada(), seed=[cartera(1), garantia(origen=99)])
    caso("07j", "garantia-emisor-de-la-cartera-origen", "FM-CPFF-06 FM-CPFF-10: el filtro p_IdEmisor de una garantia usa el emisor de la CARTERA ORIGEN (AGF1) y no el de la garantia (AGF2)",
         entrada(p_IdEmisor="AGF1"), seed=[cartera(1, **CERRADA), garantia(origen=1, IdEmisor="AGF2", INGCantidad=N("6"))])
    caso("07k", "garantia-bodega-y-regimen-de-la-cartera-origen", "FM-CPFF-10 FM-CPFF-12: la bodega y el regimen de la garantia son los de la cartera origen (DCV / BF); con p_AgrupaPorBoveda S la garantia de "
         "DCV y la custodia de COR son dos grupos", entrada(p_AgrupaPorBoveda="S"),
         seed=[cartera(1, IdBoveda="DCV", IdRegimenFondoMutuo="BF", **CERRADA), cartera(2, IdBoveda="COR"), garantia(origen=1, INGCantidad=N("5"))])
    caso("07l", "garantia-regimen-distinto-grupo-aparte", "FM-CPFF-14 D-F3C-08: custodia regimen GEN y garantia de una cartera BF: DOS grupos; el de solo garantia con RegimenTributario nulo y el de custodia con "
         "'Regimen General'", entrada(), seed=[cartera(1), cartera(2, IdRegimenFondoMutuo="BF", **CERRADA), garantia(origen=2, INGCantidad=N("5"))])
    caso("07m", "solo-garantia-de-portafolio-apv", "FM-CPFF-14 D-F3C-08: grupo de SOLO garantia en el portafolio APV 501: el grupo toma Producto 'Producto APV tres', EsADC 'S' e IdMatrizAgrupador 7 de la garantia (no de la custodia)",
         entrada(), seed=[cartera(1, IdPortafolio=501, **CERRADA), garantia(origen=1, IdPortafolio=501, INGCantidad=N("8"))])
    caso("08", "solo-garantia-25", "FM-CPFF-14 FM-CPFF-15 C08 D-F3C-08 (ejemplo C): solo garantia de 25 cuotas (la cartera origen esta cerrada): CantidadCustodia 0, RegimenTributario nulo (la garantia no lo tiene), "
         "PPPCustodia 0", entrada(), seed=[cartera(1, **CERRADA), garantia(origen=1, INGCantidad=N("25"))])
    caso("08b", "garantia-con-varias-filas-se-suma", "FM-CPFF-14: tres garantias del mismo grupo (10, 15 y 5) se suman en 30", entrada(),
         seed=[cartera(1), garantia(origen=1, INGCantidad=N("10")), garantia(origen=1, INGCantidad=N("15")), garantia(origen=1, INGCantidad=N("5"))])
    caso("08c", "garantia-con-cantidad-cero", "FM-CPFF-15: solo garantia de 0 cuotas: CantidadPrecioPromedio 0 => PPPCustodia nulo (no 0)", entrada(),
         seed=[cartera(1, **CERRADA), garantia(origen=1, INGCantidad=N("0"))])
    caso("19", "garantia-historica-con-egrmonto", "FM-CPFF-10 C19 D-F3C-01: garantia historica (respaldo 2026-03-09) con EGRMonto 777000 de la cartera origen: el monto NO se suma (PPP 0) aunque el SQL lo trae",
         entrada(p_FechaConsulta="2026-03-09"), seed=[his(1, EGRMonto=N("777000")), hgar(origen=1)])
    caso("19b", "garantia-historica-solo-garantia", "FM-CPFF-10 FM-CPFF-14: garantia historica cuya cartera origen (respaldo) esta cerrada: solo grupo de garantia", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, **CERRADA), hgar(origen=1, INGCantidad=N("9"))])
    caso("19f", "garantia-historica-de-otro-respaldo-no-sale", "FM-CPFF-10: la garantia historica exige FechaRespaldo = consulta: una garantia (con su cartera origen) del respaldo del 2026-03-08 no sale en la consulta del 2026-03-09",
         entrada(p_FechaConsulta="2026-03-09"), seed=[his(1), his(2, **P(901), FechaRespaldo=TS("2026-03-08 00:00:00")), hgar(origen=2, **P(901), FechaRespaldo=TS("2026-03-08 00:00:00"), INGCantidad=N("6"))])
    caso("19c", "garantia-historica-respaldo-distinto", "FM-CPFF-10: la garantia (respaldo 2026-03-09) y su cartera origen (respaldo 2026-03-08) tienen FechaRespaldo distinta (el JOIN exige la misma): la garantia se omite; sale solo la custodia del 901", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, FechaRespaldo=TS("2026-03-08 00:00:00")), his(2, **P(901)), hgar(origen=1)])
    caso("19d", "garantia-historica-estado-y-vigencia", "FM-CPFF-10: la garantia historica ANU no sale, la de egreso el 2026-03-09 (= consulta) tampoco, la de egreso el 2026-03-10 si", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1), hgar(origen=1, IdEstado="ANU", INGCantidad=N("1")), hgar(origen=1, EGRFechaLiquidacion=D("2026-03-09"), INGCantidad=N("2")), hgar(origen=1, EGRFechaLiquidacion=D("2026-03-10"), INGCantidad=N("4"))])
    caso("19g", "garantia-historica-estado-liq-entra", "FM-CPFF-10: la garantia historica exige IdEstado <> 'ANU' (no = 'VIG'): una garantia en estado LIQ del respaldo SI sale (3), la ANU no", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1), hgar(origen=1, IdEstado="LIQ", INGCantidad=N("3")), hgar(origen=1, IdEstado="ANU", INGCantidad=N("9"))])
    caso("19e", "garantia-historica-cantidad-nula", "FM-CPFF-10: garantia historica con INGCantidad NULL: IFNULL la vuelve 0", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, **CERRADA), hgar(origen=1, INGCantidad=None)])
    # ---- the quantities
    caso("14c", "custodia-cantidad-cero", "FM-CPFF-15: posicion con INGCantidad 0: los tres ING... valen 0 sin dividir, CantidadPrecioPromedio 0 => PPPCustodia nulo", entrada(), seed=[cartera(1, INGCantidad=N("0"))])
    caso("14d", "custodia-cantidad-cero-con-garantia", "FM-CPFF-15: custodia de 0 cuotas y garantia de 5: CantidadPrecioPromedio 5, MontoPrecioPromedio 0 (garantia), PPP 0", entrada(),
         seed=[cartera(1, INGCantidad=N("0")), garantia(origen=1, INGCantidad=N("5"))])
    caso("14e", "custodia-negativa", "FM-CPFF-13 FM-CPFF-15: custodia negativa (-10): ING... = valor / cantidad con signo; PPP = monto / cantidad", entrada(),
         seed=[cartera(1, INGCantidad=N("-10"), INGMontoCompraActualMonedaInstrumento=N("-98000"), INGMontoATasaCompraActual=N("-99000"))])
    caso("14f", "precio-compra-cero", "FM-CPFF-13: PrecioCompra 0 => MontoPrecioPromedio 0 => PPP 0 (con cantidad > 0)", entrada(), seed=[cartera(1, PrecioCompra=N("0"))])


def agrupaciones():
    # ---- C11: vault
    caso("11", "agrupa-por-boveda-s", "FM-CPFF-12 C11: p_AgrupaPorBoveda S con las bodegas DCV (60) y COR (40): DOS grupos con IdBoveda DCV y COR", entrada(p_AgrupaPorBoveda="S"),
         seed=[cartera(1, IdBoveda="DCV", INGCantidad=N("60")), cartera(2, IdBoveda="COR", INGCantidad=N("40"))])
    caso("11b", "agrupa-por-boveda-n", "FM-CPFF-12 C11: las mismas filas con p_AgrupaPorBoveda N: UN grupo de 100 con IdBoveda ''", entrada(p_AgrupaPorBoveda="N"),
         seed=[cartera(1, IdBoveda="DCV", INGCantidad=N("60")), cartera(2, IdBoveda="COR", INGCantidad=N("40"))])
    caso("11c", "agrupa-por-boveda-minuscula", "FM-CPFF-12: 's' minuscula NO agrupa (comparacion exacta con 'S')", entrada(p_AgrupaPorBoveda="s"),
         seed=[cartera(1, IdBoveda="DCV", INGCantidad=N("60")), cartera(2, IdBoveda="COR", INGCantidad=N("40"))])
    caso("11f", "agrupa-por-boveda-minuscula-con-garantia", "FM-CPFF-12: 's' minuscula tampoco agrupa la GARANTIA por bodega: la garantia de la bodega DCV y la custodia de COR son UN grupo (con S serian dos: caso 07k)",
         entrada(p_AgrupaPorBoveda="s"), seed=[cartera(1, IdBoveda="DCV", **CERRADA), cartera(2, IdBoveda="COR"), garantia(origen=1, INGCantidad=N("5"))])
    caso("11d", "agrupa-por-boveda-nula", "FM-CPFF-12: p_AgrupaPorBoveda null explicito: no agrupa (SI.equals(null) falso)", entrada(p_AgrupaPorBoveda=None),
         seed=[cartera(1, IdBoveda="DCV", INGCantidad=N("60")), cartera(2, IdBoveda="COR", INGCantidad=N("40"))])
    caso("11e", "agrupa-por-boveda-con-solicitud", "FM-CPFF-08 FM-CPFF-12: con S la fila de la solicitud en transito (IdBoveda '') es su propio grupo con IdBoveda ''", entrada(p_AgrupaPorBoveda="S"),
         seed=[cartera(1, IdBoveda="DCV"), solicitud(1, IdSerieFondo="FM002B")])
    # ---- C12: relation
    rel_seed = [cartera(1, IdCliente=99, IdPortafolio=504, INGCantidad=N("10")), cartera(2, IdCliente=101, IdPortafolio=502, INGCantidad=N("20")), cartera(3, IdCliente=100, INGCantidad=N("30")),
                garantia(origen=3, INGCantidad=N("3"))]
    caso("12", "agrupa-por-relacion-s", "FM-CPFF-11 FM-CPFF-12 C12: p_AgrupaPorRelacion S: cliente 99 (= IdCliente de la empresa) CPRO, cliente 101 (persona 7101 con relacion activa) REL, cliente 100 TER; "
         "la garantia del cliente 100 es TER", entrada(p_AgrupaPorRelacion="S"), seed=rel_seed)
    caso("12b", "agrupa-por-relacion-n", "FM-CPFF-11 FM-CPFF-12 C12: las mismas filas con N: la relacion es '' y no entra en la clave (se consulta REL.ConsultarRelacionPersona igual)", entrada(), seed=rel_seed)
    caso("12c", "agrupa-por-relacion-minuscula", "FM-CPFF-11: 's' minuscula => no agrupa por relacion", entrada(p_AgrupaPorRelacion="s"), seed=rel_seed)
    caso("12d", "relacion-inactiva-es-tercero", "FM-CPFF-11: la persona 7102 tiene relacion INACTIVA: TER; la 7101 (activa) REL", entrada(p_AgrupaPorRelacion="S"),
         seed=[cartera(1, IdCliente=102, IdPortafolio=505), cartera(2, IdCliente=101, IdPortafolio=502)])
    caso("12e", "relacion-dos-clientes-dos-grupos", "FM-CPFF-12: con S, el mismo instrumento para un cliente REL (101) y otro TER (102): dos grupos", entrada(p_AgrupaPorRelacion="S"),
         seed=[cartera(1, IdCliente=101, IdPortafolio=502), cartera(2, IdCliente=102, IdPortafolio=505)])
    caso("12f", "relacion-y-boveda-juntas", "FM-CPFF-12: S y S: la clave termina en _<relacion>_<bodega>", entrada(p_AgrupaPorRelacion="S", p_AgrupaPorBoveda="S"),
         seed=[cartera(1, IdCliente=99, IdPortafolio=504, IdBoveda="DCV"), cartera(2, IdCliente=99, IdPortafolio=504, IdBoveda="COR")])
    caso("12g", "personas-destino-persona-nula-npe", "FM-CPFF-11 D-NOR-9 D-F3C-12 D-F3C-18: una fila de REL.ConsultarRelacionPersona activa con IdPersonaDestino NULL: List2Hash NO falla (la clave nula entra en el HashMap; la tarjeta FM-CPFF-11 lo suponia NPE); "
         "se consulta siempre, aunque no se agrupe por relacion", entrada(), seed=[cartera(1), ins(REL, IdPersonaOrigen=1, IdPersonaDestino=None, IdEstado="ACT")])
    caso("12h", "personas-destino-repetida-distinct", "FM-CPFF-11: la misma persona destino dos veces en la vista: SELECT DISTINCT la deja una (sin 'key field is not unique')", entrada(p_AgrupaPorRelacion="S"),
         seed=[cartera(1, IdCliente=101, IdPortafolio=502), relacion(1, 7101, "ACT")])
    caso("12i", "persona-del-cliente-nula-es-tercero", "FM-CPFF-11: el cliente sin IdPersona en CLI.ConsultarCliente (nulo): IdPersona nulo => TER (hPersonaDestino.get(null) es nulo)", entrada(p_AgrupaPorRelacion="S"),
         seed=[ins(CLI, IdCliente=104, IdPersona=None, DocumentoIdentidad="55555555-5", RazonSocial="Cliente sin persona", IdEmpresa=1), cartera(1, IdCliente=104, IdPortafolio=0)])
    caso("12j", "empresa-con-otro-cliente", "FM-CPFF-11: la empresa del doble con IdCliente 100 (no 99): el cliente 100 es CPRO", entrada(p_AgrupaPorRelacion="S"),
         seed=[cartera(1, IdCliente=100), cartera(2, IdCliente=101, IdPortafolio=502)], colaboradores=empresa(persona=1, cliente_id=100))
    caso("12k", "empresa-con-otra-persona", "FM-CPFF-11: la empresa del doble con IdPersona 7100: las relaciones se buscan con ESA persona (no hay): el cliente 101 es TER", entrada(p_AgrupaPorRelacion="S"),
         seed=[cartera(1, IdCliente=101, IdPortafolio=502)], colaboradores=empresa(persona=7100, cliente_id=99))


def fuentes_y_tipos_de_fecha():
    # ---- C14 / C15 / C16: LIQ / ADQ / other
    caso("14", "adq-posicion-abierta-y-cerrada", "FM-CPFF-08 C14 D-F3C-05: ADQ: la posicion ABIERTA (egreso '0001-01-01') NO sale (el limite superior vale '0001-01-01'); la cerrada el 2026-03-20 (despues de la fecha) "
         "SI; la cerrada el 2026-03-01 (antes) no", entrada(p_TipoFechaConsulta="ADQ"),
         seed=[cartera(1, **P(900)), cartera(2, **P(901), EGRFechaOperacion=D("2026-03-20"), EGRFechaLiquidacion=D("2026-03-22")), cartera(3, **P(903), EGRFechaOperacion=D("2026-03-01"), EGRFechaLiquidacion=D("2026-03-03"))])
    caso("14b", "adq-usa-fecha-de-operacion", "FM-CPFF-08: ADQ usa INGFechaOperacion: operada el 2026-03-12 (despues de la consulta) no sale aunque este liquidada antes; con LIQ la misma sale",
         entrada(p_TipoFechaConsulta="ADQ"), seed=[cartera(1, INGFechaOperacion=D("2026-03-12"), INGFechaLiquidacion=D("2026-01-12"), EGRFechaOperacion=D("2026-03-20"), EGRFechaLiquidacion=D("2026-03-22"))])
    caso("14g", "liq-misma-posicion-que-adq", "FM-CPFF-08: la posicion de 14b con LIQ (por defecto) SI sale", entrada(),
         seed=[cartera(1, INGFechaOperacion=D("2026-03-12"), INGFechaLiquidacion=D("2026-01-12"), EGRFechaOperacion=D("2026-03-20"), EGRFechaLiquidacion=D("2026-03-22"))])
    caso("15", "tipo-fecha-xxx-solo-solicitudes", "FM-CPFF-08 C15: p_TipoFechaConsulta 'XXX': la rama de la custodia no devuelve nada; la solicitud en transito SI sale aunque haya cartera de la misma serie "
         "(el NOT IN tampoco encuentra cartera vigente)", entrada(p_TipoFechaConsulta="XXX"), seed=[cartera(1), solicitud(1)])
    caso("15b", "tipo-fecha-minuscula", "FM-CPFF-08: 'liq' minuscula = otro valor: igual que XXX", entrada(p_TipoFechaConsulta="liq"), seed=[cartera(1), solicitud(1, IdSerieFondo="FM002B")])
    caso("15c", "tipo-fecha-nulo", "FM-CPFF-08: p_TipoFechaConsulta null explicito: la comparacion es desconocida: la custodia no sale y la solicitud (NOT IN sin filas) si", entrada(p_TipoFechaConsulta=None),
         seed=[cartera(1), solicitud(1, IdSerieFondo="FM002B")])
    caso("16", "egreso-igual-a-la-consulta-excluida", "FM-CPFF-08 C16 QD-F3C-06: egreso por liquidacion el 2026-03-10 (= fecha de consulta) => excluida; el 2026-03-11 incluida; el 2026-03-09 excluida; "
         "ingreso el 2026-03-10 incluido; ingreso el 2026-03-11 excluido", entrada(),
         seed=[cartera(1, **P(900), EGRFechaLiquidacion=D("2026-03-10")), cartera(2, **P(901), EGRFechaLiquidacion=D("2026-03-11")), cartera(3, **P(903), EGRFechaLiquidacion=D("2026-03-09")),
               cartera(4, **P(904), INGFechaLiquidacion=D("2026-03-10")), cartera(5, **P(905), INGFechaLiquidacion=D("2026-03-11"))])
    caso("16b", "fecha-futura-usa-vigente", "FM-CPFF-07: fecha de consulta 2026-04-01 (futura): fuente VIGENTE con esa fecha: la posicion con egreso el 2026-03-20 ya no sale, la abierta si",
         entrada(p_FechaConsulta="2026-04-01"), seed=[cartera(1, **P(900)), cartera(2, **P(901), EGRFechaLiquidacion=D("2026-03-20"))])
    caso("16c", "fecha-igual-al-sistema-es-vigente", "FM-CPFF-07: consulta = PLA.FechaSistema => vigente (>=); HIS.Cartera con respaldo de ese dia se ignora", entrada(p_FechaConsulta="2026-03-10"),
         seed=[cartera(1), his(2, **P(901), FechaRespaldo=TS("2026-03-10 00:00:00"))])
    caso("16d", "un-dia-antes-del-sistema-es-historico", "FM-CPFF-07: consulta 2026-03-09 (= sistema - 1) => HISTORICO; CUS.Cartera se ignora", entrada(p_FechaConsulta="2026-03-09"),
         seed=[cartera(1), his(2, **P(901))])
    caso("16e", "fecha-historica-sin-respaldo", "FM-CPFF-09: consulta 2026-02-01 sin filas de respaldo de ese dia: vacio", entrada(p_FechaConsulta="2026-02-01"), seed=[cartera(1), his(2)])
    caso("16f", "sistema-posterior-todo-historico", "FM-CPFF-07: con PLA.FechaSistema 2026-12-31 la fecha 2026-03-10 es historica", entrada(),
         seed=[config("PLA.FechaSistema", "2026-12-31"), cartera(1), his(2, FechaRespaldo=TS("2026-03-10 00:00:00"))])
    caso("16h", "historico-tres-dias-antes", "FM-CPFF-07 FM-CPFF-09: consulta 2026-03-07 (3 dias antes del sistema): HISTORICO. Fija que la comparacion es 'no es anterior' y no 'compareTo != -1' de LocalDate (que da -3)",
         entrada(p_FechaConsulta="2026-03-07"), seed=[cartera(1), his(2, **P(901), FechaRespaldo=TS("2026-03-07 00:00:00"))])
    caso("16i", "historico-dos-meses-antes", "FM-CPFF-07 FM-CPFF-09: consulta 2026-01-05 (2 meses antes): HISTORICO (LocalDate.compareTo daria -2)", entrada(p_FechaConsulta="2026-01-05"),
         seed=[cartera(1, INGFechaLiquidacion=D("2024-01-01")), his(2, **P(901), INGFechaLiquidacion=D("2024-01-01"), FechaRespaldo=TS("2026-01-05 00:00:00"))])
    caso("16j", "historico-un-anio-antes", "FM-CPFF-07 FM-CPFF-09: consulta 2025-03-10 (1 anio antes): HISTORICO", entrada(p_FechaConsulta="2025-03-10"),
         seed=[cartera(1, INGFechaLiquidacion=D("2024-01-01")), his(2, **P(901), INGFechaLiquidacion=D("2024-01-01"), FechaRespaldo=TS("2025-03-10 00:00:00"))])
    caso("16k", "vigente-tres-dias-despues", "FM-CPFF-07: consulta 2026-03-13 (3 dias despues): VIGENTE; el respaldo de ese dia se ignora", entrada(p_FechaConsulta="2026-03-13"),
         seed=[cartera(1), his(2, **P(901), FechaRespaldo=TS("2026-03-13 00:00:00"))])
    caso("16g", "fecha-cero-con-sistema-cero", "FM-CPFF-07 D-F3C-04: PLA.FechaSistema '0001-01-01' y consulta '0001-01-01': la comparacion de texto da vigente; vacio", entrada(p_FechaConsulta="0001-01-01"),
         seed=[config("PLA.FechaSistema", "0001-01-01"), cartera(1)])


def historico():
    # ---- C17 / C18: historical custody
    caso("17", "historico-ultima-version-por-folio", "FM-CPFF-09 C17: respaldo 2026-03-09: el FolioCartera 77 tiene 2 versiones (IdCartera 5: 100 cuotas, 9: 40): solo la de IdCartera MAYOR; el folio 78 una version",
         entrada(p_FechaConsulta="2026-03-09"), seed=[his(5, FolioCartera=77), his(9, FolioCartera=77, INGCantidad=N("40")), his(6, FolioCartera=78, **P(901), INGCantidad=N("7"))])
    caso("17b", "historico-empate-de-idcartera", "FM-CPFF-09 Q-F3C-7: dos filas con el MISMO IdCartera y FolioCartera (RANK empata): ambas salen y se suman", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(5, FolioCartera=77, INGCantidad=N("10")), his(5, FolioCartera=77, INGCantidad=N("20"))])
    caso("17c", "historico-estado", "FM-CPFF-09 FM-CPFF-20: historico exige IdEstado <> 'ANU': ANU no sale, LIQ y VIG si (la vigente exigiria VIG)", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, **P(900), IdEstado="ANU"), his(2, **P(901), IdEstado="LIQ"), his(3, **P(903), IdEstado="VIG")])
    caso("17d", "historico-estado-nulo", "FM-CPFF-09: IdEstado NULL en el respaldo: NULL <> 'ANU' desconocido: no sale", entrada(p_FechaConsulta="2026-03-09"), seed=[his(1, IdEstado=None)])
    caso("17e", "historico-respaldo-de-otro-dia", "FM-CPFF-09: respaldos del 2026-03-08 y 2026-03-09: solo el del dia de la consulta", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, FechaRespaldo=TS("2026-03-08 00:00:00")), his(2, **P(901))])
    caso("17f", "historico-respaldo-con-hora", "FM-CPFF-09: respaldo el 2026-03-09 con hora 10:00:00: la igualdad TIMESTAMP = texto de fecha no lo encuentra", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, FechaRespaldo=TS("2026-03-09 10:00:00"))])
    caso("17g", "historico-sin-socio-se-omite", "FM-CPFF-09: el historico usa INNER JOIN con SOC.SocioDeNegocio: el cliente sin socio de negocio se omite", entrada(p_FechaConsulta="2026-03-09"),
         seed=[nuevo_cliente(103, 7103, con_socio=False), his(1, IdCliente=103, IdPortafolio=0), his(2, **P(901))])
    caso("17h", "historico-tipo-y-clasificacion", "FM-CPFF-09: el historico filtra IdTipoInstrumento 'FFM' e IdClasificacion 'FM' en la cartera",
         entrada(p_FechaConsulta="2026-03-09"), seed=[his(1, IdTipoInstrumento="ACC"), his(2, IdClasificacionTipoInstrumento="RV"), his(3, **P(901))])
    caso("17i", "historico-adq", "FM-CPFF-09 D-F3C-05: historico con ADQ: la abierta no sale, la cerrada despues si", entrada(p_FechaConsulta="2026-03-09", p_TipoFechaConsulta="ADQ"),
         seed=[his(1), his(2, **P(901), EGRFechaOperacion=D("2026-03-20"), EGRFechaLiquidacion=D("2026-03-22"))])
    caso("17j", "historico-tipo-xxx", "FM-CPFF-09: historico con tipo XXX: la custodia no sale", entrada(p_FechaConsulta="2026-03-09", p_TipoFechaConsulta="XXX"), seed=[his(1)])
    caso("17k", "historico-regimen-nulo", "FM-CPFF-09: respaldo con IdRegimenFondoMutuo NULL: la clave usa el texto 'null' y la salida trae IdRegimenFondoMutuo nulo", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, IdRegimenFondoMutuo=None)])
    caso("17l", "historico-egreso-nulo", "FM-CPFF-09: respaldo con EGRFechaLiquidacion NULL (HIS permite nulos): IFNULL lo trata como abierta: sale", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, EGRFechaLiquidacion=None)])
    caso("17m", "historico-ingreso-nulo", "FM-CPFF-09: respaldo con INGFechaLiquidacion NULL: el BETWEEN es desconocido: no sale", entrada(p_FechaConsulta="2026-03-09"), seed=[his(1, INGFechaLiquidacion=None)])
    caso("17n", "historico-cantidad-nula", "FM-CPFF-09: respaldo con INGCantidad NULL: IFNULL la vuelve 0: cantidad 0, PPP nulo", entrada(p_FechaConsulta="2026-03-09"), seed=[his(1, INGCantidad=None)])
    caso("17o", "historico-precio-nulo", "FM-CPFF-09: respaldo con PrecioCompra NULL: COALESCE a 0: MontoPrecioPromedio 0, PPP 0", entrada(p_FechaConsulta="2026-03-09"), seed=[his(1, PrecioCompra=None)])
    caso("17p", "historico-bodega-nula-con-agrupa", "FM-CPFF-12: respaldo con IdBoveda NULL y p_AgrupaPorBoveda S: la clave usa 'null' y IdBoveda de salida es nulo", entrada(p_FechaConsulta="2026-03-09", p_AgrupaPorBoveda="S"),
         seed=[his(1, IdBoveda=None)])
    caso("17q", "historico-folio-nulo", "FM-CPFF-09: respaldo con FolioCartera NULL: RANK() agrupa los nulos en una sola particion: solo la de IdCartera mayor", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, FolioCartera=None, INGCantidad=N("10")), his(2, FolioCartera=None, INGCantidad=N("20"))])
    # C18: Producto null / RegimenTributario ''
    caso("18", "historico-producto-nulo-regimen-vacio", "FM-CPFF-09 C18 D-F3C-07: el portafolio 501 es APV con producto 'Producto APV tres' y la serie tiene regimen GEN: en HISTORICO Producto sale NULO y "
         "RegimenTributario '' (el SELECT externo no las trae); en vigente (caso 18b) si", entrada(p_FechaConsulta="2026-03-09"), seed=[his(1, IdPortafolio=501)])
    caso("18b", "vigente-producto-y-regimen", "FM-CPFF-08 FM-CPFF-20 D-F3C-07: las mismas filas en vigente: Producto 'Producto APV tres' y RegimenTributario 'Regimen General'", entrada(), seed=[cartera(1, IdPortafolio=501)])
    caso("18c", "historico-solicitud-emisor-nulo-desaparece", "FM-CPFF-09 C18 D-F3C-06: historico con p_IdEmisor nulo (lo habitual): la tautologia ':IdEmisor = :IdEmisor' es desconocida: la rama de solicitudes "
         "desaparece completa; con la cartera historica presente sale solo ella", entrada(p_FechaConsulta="2026-03-09"), seed=[his(1), solicitud(1, IdSerieFondo="FM002B")])
    caso("18d", "historico-solicitud-emisor-informado-aparece", "FM-CPFF-09 C18 D-F3C-06: con p_IdEmisor 'ZZZ' (informado, ninguna cartera lo tiene) la rama de solicitudes SI aparece (la rama 2 no filtra por "
         "emisor): salen las solicitudes y no la cartera", entrada(p_FechaConsulta="2026-03-09", p_IdEmisor="ZZZ"), seed=[his(1), solicitud(1, IdSerieFondo="FM002B")])
    caso("18e", "historico-solicitud-instrumento-informado", "FM-CPFF-09 D-F3C-06: con p_IdInstrumento 901 la rama de solicitudes de OTRO instrumento aparece tambien (no lo aplica), y la tautologia se cumple "
         "(901 = 901)", entrada(p_FechaConsulta="2026-03-09", p_IdEmisor="AGF1", p_IdInstrumento=901), seed=[his(1, **P(901)), solicitud(1, IdSerieFondo="FM001A")])
    caso("18f", "historico-solicitud-sin-regimen-descripcion", "FM-CPFF-09 D-F3C-07: la rama de solicitudes historica no hace JOIN con FMM.RegimenFondoMutuo: RegimenTributario '' aunque exista (BF)",
         entrada(p_FechaConsulta="2026-03-09", p_IdEmisor="AGF1"), seed=[solicitud(1, IdRegimenTributario="BF")])
    caso("18g", "historico-solicitud-excluida-por-cartera-vigente", "FM-CPFF-09: el NOT IN de la rama de solicitudes historica consulta CUS.Cartera (VIGENTE, no el respaldo): una cartera vigente de FM001A del mismo cliente "
         "excluye la solicitud", entrada(p_FechaConsulta="2026-03-09", p_IdEmisor="AGF1"), seed=[cartera(1), solicitud(1)])
    # C21: null purchase value
    caso("21", "valor-compra-nulo-npe", "FM-CPFF-13 C21 D-F3C-11: ValorCompra (INGMontoCompraActualMonedaInstrumento) NULL en una fila historica: NullPointerException en la suma (el SQL no lo protege)",
         entrada(p_FechaConsulta="2026-03-09"), seed=[his(1, INGMontoCompraActualMonedaInstrumento=None)])
    caso("21b", "monto-compra-nulo-npe", "FM-CPFF-13 D-F3C-11: MontoCompra (INGMontoATasaCompraActual) NULL: NullPointerException en la suma", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, INGMontoATasaCompraActual=None)])
    caso("21c", "valor-compra-nulo-en-segunda-fila", "FM-CPFF-13 D-F3C-11: la segunda fila del grupo trae ValorCompra nulo: NPE tambien (aunque la primera era valida)", entrada(p_FechaConsulta="2026-03-09"),
         seed=[his(1, FolioCartera=1), his(2, FolioCartera=2, INGMontoCompraActualMonedaInstrumento=None)])


def filtros():
    # ---- C20: product / family; FM-CPFF-06 filters
    caso("20", "producto-agrupador-informado", "FM-CPFF-06 C20: p_IdProductoAgrupador 3: solo el portafolio APV 501 (producto 3); los demas portafolios (sin producto) no salen", entrada(p_IdProductoAgrupador=3),
         seed=[cartera(1, IdPortafolio=500), cartera(2, IdPortafolio=501)])
    caso("20b", "producto-agrupador-cero", "FM-CPFF-06 C20: p_IdProductoAgrupador 0: un portafolio sin producto equivale a producto 0 (MAP): salen los portafolios 500 y 502, no el 501", entrada(p_IdProductoAgrupador=0),
         seed=[cartera(1, IdPortafolio=500), cartera(2, IdPortafolio=501), cartera(3, IdCliente=101, IdPortafolio=502)])
    caso("20c", "producto-agrupador-nulo", "FM-CPFF-06 C20: p_IdProductoAgrupador nulo = sin filtro: salen todos", entrada(p_IdProductoAgrupador=None),
         seed=[cartera(1, IdPortafolio=500), cartera(2, IdPortafolio=501)])
    caso("20d", "producto-agrupador-inexistente", "FM-CPFF-06: p_IdProductoAgrupador 99: ninguno", entrada(p_IdProductoAgrupador=99), seed=[cartera(1, IdPortafolio=500), cartera(2, IdPortafolio=501)])
    caso("20e", "familia-informada", "FM-CPFF-06 C20: p_IdFamiliaFondo FAM2: solo los instrumentos 901 y 904 (series de la familia)", entrada(p_IdFamiliaFondo="FAM2"),
         seed=[cartera(1, **P(900)), cartera(2, **P(901)), cartera(3, **P(904)), cartera(4, **P(905))])
    caso("20f", "familia-vacia-sin-filtro", "FM-CPFF-06 C20: p_IdFamiliaFondo '' = sin filtro", entrada(p_IdFamiliaFondo=""), seed=[cartera(1, **P(900)), cartera(2, **P(901))])
    caso("20g", "familia-nula-sin-filtro", "FM-CPFF-06 C20: p_IdFamiliaFondo null explicito = sin filtro (IFNULL)", entrada(p_IdFamiliaFondo=None), seed=[cartera(1, **P(900)), cartera(2, **P(901))])
    caso("20h", "familia-inexistente", "FM-CPFF-06: p_IdFamiliaFondo 'ZZZ': ninguno de la custodia; las solicitudes en transito SI salen (la rama 2 no la aplica: D-F3C-06)", entrada(p_IdFamiliaFondo="ZZZ"),
         seed=[cartera(1), solicitud(1, IdSerieFondo="FM002B")])
    caso("20i", "cliente-filtra", "FM-CPFF-06: p_IdCliente 100: solo el cliente 100", entrada(p_IdCliente=100), seed=[cartera(1), cartera(2, IdCliente=101, IdPortafolio=502)])
    caso("20j", "cliente-nulo-sin-filtro", "FM-CPFF-06 FM-CPFF-01: p_IdCliente null explicito: sin filtro (el CASE usa la columna)", entrada(p_IdCliente=None), seed=[cartera(1), cartera(2, IdCliente=101, IdPortafolio=502)])
    caso("20k", "cliente-negativo-sin-filtro", "FM-CPFF-06: p_IdCliente -5: '> 0' falso: sin filtro", entrada(p_IdCliente=-5), seed=[cartera(1), cartera(2, IdCliente=101, IdPortafolio=502)])
    caso("20l", "portafolio-filtra", "FM-CPFF-06: p_IdPortafolio 501: solo ese portafolio (y la garantia de ese portafolio)", entrada(p_IdPortafolio=501),
         seed=[cartera(1, IdPortafolio=500), cartera(2, IdPortafolio=501), garantia(origen=2, IdPortafolio=501), garantia(origen=1, IdPortafolio=500)])
    caso("20m", "instrumento-filtra", "FM-CPFF-06: p_IdInstrumento 901: solo ese instrumento", entrada(p_IdInstrumento=901), seed=[cartera(1, **P(900)), cartera(2, **P(901)), garantia(origen=1, **P(900))])
    caso("20n", "instrumento-nulo-sin-filtro", "FM-CPFF-06: p_IdInstrumento null explicito: IFNULL(:x,0) > 0 falso: sin filtro", entrada(p_IdInstrumento=None), seed=[cartera(1, **P(900)), cartera(2, **P(901))])
    caso("20o", "instrumento-negativo-sin-filtro", "FM-CPFF-06: p_IdInstrumento -1: sin filtro", entrada(p_IdInstrumento=-1), seed=[cartera(1, **P(900)), cartera(2, **P(901))])
    caso("20p", "emisor-filtra", "FM-CPFF-06: p_IdEmisor AGF2: solo la cartera cuyo IdEmisor es AGF2 (la custodia filtra por CAR.IdEmisor, no por el emisor del fondo)", entrada(p_IdEmisor="AGF2"),
         seed=[cartera(1, **P(900)), cartera(2, **P(901)), cartera(3, **P(903, IdEmisor="AGF2"))])
    caso("20q", "emisor-vacio-no-es-sin-filtro", "FM-CPFF-06: p_IdEmisor '' (vacio, no nulo) FILTRA: ninguna cartera tiene emisor vacio", entrada(p_IdEmisor=""), seed=[cartera(1)])
    caso("20r", "estado-vigente-solo-vig", "FM-CPFF-08 FM-CPFF-20: la custodia VIGENTE exige IdEstado VIG: ANU y LIQ no salen", entrada(),
         seed=[cartera(1, **P(900)), cartera(2, **P(901), IdEstado="ANU"), cartera(3, **P(903), IdEstado="LIQ")])
    caso("20s", "clasificacion-y-tipo-vigente", "FM-CPFF-08: la vigente filtra la clasificacion FM y el tipo FFM (join): tipo 'ACC' o clasificacion 'RV' no salen", entrada(),
         seed=[cartera(1, **P(900)), cartera(2, **P(901), IdTipoInstrumento="ACC"), cartera(3, **P(903), IdClasificacionTipoInstrumento="RV")])
    caso("20t", "empresa-distinta-no-sale", "FM-CPFF-08: cartera de la empresa 2: no sale", entrada(), seed=[cartera(1), cartera(2, IdEmpresa=2, **P(901))])


def solicitudes_en_transito():
    # ---- C09 / C10: requests in transit
    caso("09", "solicitud-sin-cartera-de-la-serie", "FM-CPFF-08 FM-CPFF-15 C09 (ejemplo D): solicitud de aporte ING aceptada el 2026-03-05 sin cartera de la serie: fila con cantidad 0, IdBoveda '', PPPCustodia NULO, "
         "regimen tributario 'Regimen General' (JOIN con RegimenFondoMutuo)", entrada(), seed=[solicitud(1)])
    caso("10", "solicitud-de-otro-instrumento-con-filtro", "FM-CPFF-06 FM-CPFF-08 C10 D-F3C-06: con p_IdInstrumento 901 la solicitud del instrumento 900 APARECE igual (la rama 2 vigente no aplica IdInstrumento) "
         "junto a la custodia del 901", entrada(p_IdInstrumento=901), seed=[cartera(1, **P(901)), solicitud(1, IdSerieFondo="FM001A")])
    caso("10b", "solicitud-ignora-emisor-y-familia", "FM-CPFF-08 D-F3C-06: la solicitud de la serie FM002B (AGF2, FAM2) aparece con p_IdEmisor AGF1 y p_IdFamiliaFondo FAM1 (la rama 2 vigente no los aplica)",
         entrada(p_IdEmisor="AGF1", p_IdFamiliaFondo="FAM1"), seed=[solicitud(1, IdSerieFondo="FM002B")])
    caso("10c", "solicitud-estado-pen", "FM-CPFF-08: Estado PEN entra; EJE y ANU no", entrada(),
         seed=[solicitud(1, Estado="PEN", IdSerieFondo="FM002B"), solicitud(2, Estado="EJE", IdSerieFondo="FM004C"), solicitud(3, Estado="ANU", IdSerieFondo="FM005D")])
    caso("10d", "solicitud-fecha-de-aceptacion", "FM-CPFF-08: FechaAceptacion <= consulta: el 2026-03-10 entra, el 2026-03-11 no", entrada(),
         seed=[solicitud(1, FechaAceptacion=fecha("2026-03-10"), IdSerieFondo="FM002B"), solicitud(2, FechaAceptacion=fecha("2026-03-11"), IdSerieFondo="FM004C")])
    caso("10e", "solicitud-rescate-igual-que-aporte", "FM-CPFF-08: no distingue aporte de rescate: un rescate en transito (RES) sale como fila de cantidad 0", entrada(),
         seed=[solicitud(1, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM", IdSerieFondo="FM002B")])
    caso("10f", "dos-solicitudes-misma-serie-se-suman", "FM-CPFF-13 FM-CPFF-08: dos solicitudes de la misma serie/cliente/portafolio/regimen: UN grupo; ValorCompra (1000 + 2500) y MontoCompra (50000 + 75000) se suman pero NO se "
         "observan: con cantidad 0 los tres ING... salen 0 sin dividir", entrada(),
         seed=[solicitud(1, ValorCuota=1000, Monto=50000), solicitud(2, ValorCuota=2500, Monto=75000)])
    caso("10g", "solicitud-cartera-vigente-de-la-serie-la-excluye", "FM-CPFF-08: cartera VIGENTE del mismo cliente con Nemotecnico = IdSerieFondo de la solicitud: la solicitud NO aparece (NOT IN)", entrada(),
         seed=[cartera(1), solicitud(1)])
    caso("10h", "solicitud-cartera-de-otro-cliente-no-la-excluye", "FM-CPFF-08: la cartera de la serie es del cliente 101: la solicitud del cliente 100 aparece junto a la custodia del 101", entrada(),
         seed=[cartera(1, IdCliente=101, IdPortafolio=502), solicitud(1)])
    caso("10i", "solicitud-cartera-cerrada-no-la-excluye", "FM-CPFF-08: cartera de la serie CERRADA antes de la fecha (egreso 2026-03-01): no esta vigente: la solicitud aparece y la custodia no",
         entrada(), seed=[cartera(1, EGRFechaLiquidacion=D("2026-03-01"), EGRFechaOperacion=D("2026-02-27")), solicitud(1)])
    caso("10j", "solicitud-cartera-de-otro-portafolio-la-excluye", "FM-CPFF-08: el NOT IN compara por cliente (no por portafolio): una cartera de la serie en el portafolio 501 excluye la solicitud del portafolio 500",
         entrada(), seed=[cartera(1, IdPortafolio=501), solicitud(1)])
    caso("10k", "solicitud-cartera-anulada-la-excluye", "FM-CPFF-08: el NOT IN no mira el estado: una cartera ANULADA vigente en la fecha excluye la solicitud (y la custodia tampoco sale)", entrada(),
         seed=[cartera(1, IdEstado="ANU"), solicitud(1)])
    caso("10l", "solicitud-filtra-cliente-y-portafolio", "FM-CPFF-06: la rama 2 SI aplica p_IdCliente y p_IdPortafolio: solo la solicitud del cliente 101 portafolio 502", entrada(p_IdCliente=101, p_IdPortafolio=502),
         seed=[solicitud(1), solicitud(2, IdCliente=101, IdPortafolio=502, IdSerieFondo="FM002B")])
    caso("10l2", "solicitud-filtra-solo-cliente", "FM-CPFF-06: p_IdCliente 101 con portafolio 0: la rama de solicitudes aplica el filtro de cliente por si solo (la solicitud del cliente 100 no sale)", entrada(p_IdCliente=101),
         seed=[solicitud(1), solicitud(2, IdCliente=101, IdPortafolio=502, IdSerieFondo="FM002B")])
    caso("10l3", "solicitud-filtra-solo-portafolio", "FM-CPFF-06: p_IdPortafolio 502 con cliente 0: la rama de solicitudes aplica el filtro de portafolio por si solo (la solicitud del portafolio 500 no sale)", entrada(p_IdPortafolio=502),
         seed=[solicitud(1), solicitud(2, IdCliente=101, IdPortafolio=502, IdSerieFondo="FM002B")])
    caso("10m", "solicitud-filtra-producto", "FM-CPFF-06: la rama 2 SI aplica p_IdProductoAgrupador: producto 3 => solo la del portafolio APV 501", entrada(p_IdProductoAgrupador=3),
         seed=[solicitud(1), solicitud(2, IdPortafolio=501, IdSerieFondo="FM002B")])
    caso("10n", "solicitud-portafolio-fuera-de-la-vista", "FM-CPFF-08: portafolio 999 que no esta en la vista ConsultarPortafolio: CodigoPortafolio nulo, matriz 0, EsADC N", entrada(),
         seed=[solicitud(1, IdPortafolio=999)])
    caso("10o", "solicitud-serie-inexistente", "FM-CPFF-08: IdSerieFondo 'NOEXISTE' sin fila de serie: IdInstrumento nulo, clasificacion nula, emisor nulo; la clave usa el texto 'null'", entrada(),
         seed=[solicitud(1, IdSerieFondo="NOEXISTE")])
    caso("10p", "solicitud-cliente-fuera-de-la-vista", "FM-CPFF-08: cliente 888 no esta en CLI.ConsultarCliente ni tiene socio: documento, razon social, IdSocioDeNegocio nulos", entrada(),
         seed=[solicitud(1, IdCliente=888)])
    caso("10q", "solicitud-con-regimen-bf", "FM-CPFF-08: solicitud con regimen BF: la descripcion 'Beneficio Fiscal' sale en RegimenTributario; IdRegimenFondoMutuo es el de la solicitud", entrada(),
         seed=[solicitud(1, IdRegimenTributario="BF")])
    caso("10r", "solicitud-de-la-empresa-2-no-sale", "FM-CPFF-08: solicitud de IdEmpresa 2: no sale", entrada(), seed=[solicitud(1, IdEmpresa=2)])
    caso("10s", "solicitud-y-garantia-mismo-grupo", "FM-CPFF-14 FM-CPFF-08: solicitud en transito de FM001A y garantia de FM001A (cartera origen cerrada, regimen GEN): la clave coincide: UN grupo "
         "con la garantia y los datos de la solicitud (cantidad custodia 0, PPP 0)", entrada(),
         seed=[cartera(1, EGRFechaLiquidacion=D("2026-03-01"), EGRFechaOperacion=D("2026-02-27")), solicitud(1), garantia(origen=1, INGCantidad=N("12"))])
    caso("10t", "solicitud-con-relacion-cpro", "FM-CPFF-11: la solicitud del cliente 99 (la corredora) con p_AgrupaPorRelacion S es CPRO; la del 101 REL", entrada(p_AgrupaPorRelacion="S"),
         seed=[solicitud(1, IdCliente=99, IdPortafolio=504), solicitud(2, IdCliente=101, IdPortafolio=502, IdSerieFondo="FM002B")])
    caso("10u", "solicitud-tipo-calculo-total-no-se-excluye", "FM-CPFF-08: CalculoPorMontoCuota 'T' (rescate total): no se excluye (CPF lo excluye, esta clase no)", entrada(),
         seed=[solicitud(1, CalculoPorMontoCuota="T", IdSerieFondo="FM002B")])


def joins_y_fan_out():
    caso("22b", "dos-series-mismo-instrumento-duplican", "FM-CPFF-06 D-F3C-09: dos series de la misma empresa para el instrumento 900 (el JOIN con SerieDeFondo duplica la fila): la custodia sale x2 (200 cuotas)", entrada(),
         seed=[clonar_serie("FM099Z", "10", fondo=False, serie={"IdInstrumento": 900}), cartera(1)])
    caso("22c", "instrumento-sin-serie-no-sale", "FM-CPFF-06: instrumento 906 en INS.Instrumento sin serie: Serie.IdFamiliaFondo NULO => la igualdad es desconocida: la fila se descarta (aun con p_IdFamiliaFondo '')",
         entrada(), seed=[ins(INST, IdInstrumento=906, IdTipoInstrumento="FFM"), cartera(1, IdInstrumento=906, Nemotecnico="FM007F"), cartera(2, **P(901))])
    caso("22d", "instrumento-fuera-de-ins-instrumento-no-sale", "FM-CPFF-08: la vigente parte de INS.Instrumento: una cartera del instrumento 907 que no esta alli no sale", entrada(),
         seed=[cartera(1, IdInstrumento=907, Nemotecnico="FM008G"), cartera(2, **P(901))])
    caso("22e", "dos-matrices-noapv-mismo-codigo-duplican", "FM-CPFF-12 D-F3C-09: dos filas de MatrizAgrupadorNOAPV con el codigo PF500: el JOIN duplica la fila: 200 cuotas", entrada(),
         seed=[ins(MNOAPV, IdMatrizAgrupadorNOAPV=18, CodigoPortafolio="PF500", EsADC="S"), cartera(1)])
    caso("22f", "dos-socios-misma-persona-duplican", "FM-CPFF-12 D-F3C-09: dos filas de SOC.SocioDeNegocio para la persona 7100: la custodia vigente (LEFT JOIN) se duplica: el primer descriptivo es el de la primera fila", entrada(),
         seed=[socio(2100, 7100, 100), cartera(1)])
    caso("22g", "dos-filas-de-cliente-misma-vista", "FM-CPFF-12 D-F3C-09: CLI.ConsultarCliente con el cliente 100 repetido (otra razon social): la fila se duplica y la primera razon social es la que queda", entrada(),
         seed=[cliente(100, 7100, "11111111-1", "Razon repetida"), cartera(1)])
    caso("22h", "codigo-de-portafolio-distinto-misma-clave", "FM-CPFF-12 D-F3C-09: la clave no incluye el CodigoPortafolio: el portafolio 500 repetido en POR.Portafolio con otro codigo duplica la fila; los "
         "descriptivos son los de la primera fila vista", entrada(), seed=[ins(PORT, IdPortafolio=500, CodigoPortafolio="PF500B", IdCliente=100, IdEstado="ACT", Proposito="PROPGEN", IdMatrizAgrupadorAPV=0), cartera(1)])
    caso("22i", "mismo-instrumento-nemotecnico-distinto-grupo", "FM-CPFF-12 FM-CPFF-22.4 D-F3C-13: dos filas del MISMO instrumento 900, cliente, portafolio y regimen con Nemotecnico distinto (FM001A y FM001X): "
         "DOS grupos (insumo del desempate 'la ultima gana' de CPF)", entrada(), seed=[cartera(1), cartera(2, Nemotecnico="FM001X")])
    caso("22j", "mismo-instrumento-emisor-distinto-grupo", "FM-CPFF-12 FM-CPFF-22.4: dos filas del mismo instrumento con IdEmisor distinto (AGF1 y AGF2): DOS grupos", entrada(), seed=[cartera(1), cartera(2, IdEmisor="AGF2")])
    caso("22k", "regimen-nulo-y-regimen-informado-dos-grupos", "FM-CPFF-12: IdRegimenFondoMutuo NULL y 'GEN' del mismo instrumento: dos grupos (clave con 'null' y 'GEN'); el nulo con RegimenTributario ''", entrada(),
         seed=[cartera(1), cartera(2, IdRegimenFondoMutuo=None)])
    caso("22l", "regimen-sin-descripcion", "FM-CPFF-08: regimen 'ZZZ' que no esta en RegimenFondoMutuo: RegimenTributario '' (COALESCE) e IdRegimenFondoMutuo 'ZZZ'", entrada(), seed=[cartera(1, IdRegimenFondoMutuo="ZZZ")])


def numeros():
    caso("43", "division-no-exacta-decimal128", "FM-CPFF-15 D-F3C-10: 3 cuotas con ValorCompra 1000 y MontoCompra 2000: INGPrecioPromedioCompra = 1000/3 con 34 digitos (DECIMAL128), y (X / Cantidad) * Cantidad "
         "NO vuelve exactamente a X (999.99999...)", entrada(), seed=[cartera(1, INGCantidad=N("3"), INGMontoCompraActualMonedaInstrumento=N("1000"), INGMontoATasaCompraActual=N("2000"), PrecioCompra=N("333.33333333"))])
    caso("43b", "division-no-exacta-con-garantia", "FM-CPFF-15 D-F3C-02: la misma posicion con garantia de 7: PPP = 0 / 10", entrada(),
         seed=[cartera(1, INGCantidad=N("3"), INGMontoCompraActualMonedaInstrumento=N("1000"), INGMontoATasaCompraActual=N("2000"), PrecioCompra=N("333.33333333")), garantia(origen=1, INGCantidad=N("7"))])
    caso("43c", "maximos-de-decimal-26-8", "FM-CPFF-13 D-F3C-11: 999999999999999999.99999999 cuotas (maximo DECIMAL(26,8)) por precio 999999999999999999.99999999: el producto supera 34 digitos y se redondea (DECIMAL128, "
         "HALF_EVEN)", entrada(), seed=[cartera(1, INGCantidad=N("999999999999999999.99999999"), PrecioCompra=N("999999999999999999.99999999"), INGMontoCompraActualMonedaInstrumento=N("999999999999999999.99999999"),
                                          INGMontoATasaCompraActual=N("999999999999999999.99999999"))])
    caso("43d", "cantidad-minima", "FM-CPFF-15: 0.00000001 cuotas con valor 0.00000001: INGPrecioPromedioCompra = 1", entrada(),
         seed=[cartera(1, INGCantidad=N("0.00000001"), INGMontoCompraActualMonedaInstrumento=N("0.00000001"), INGMontoATasaCompraActual=N("0.00000003"), PrecioCompra=N("1"))])
    caso("43e", "suma-de-cantidades-con-escala", "FM-CPFF-13: 0.1 + 0.2 + 0.3 cuotas en tres filas: la escala del resultado es la de la suma DECIMAL128 de DECIMAL(26,8)", entrada(),
         seed=[cartera(1, INGCantidad=N("0.1")), cartera(2, INGCantidad=N("0.2")), cartera(3, INGCantidad=N("0.3"))])
    caso("43f", "precio-promedio-con-garantia-y-custodia-fraccionaria", "FM-CPFF-15: custodia 2.5 con garantia 0.5: CantidadPrecioPromedio 3.0 (suma exacta sin MathContext)", entrada(),
         seed=[cartera(1, INGCantidad=N("2.5")), garantia(origen=1, INGCantidad=N("0.5"))])


def entradas_json():
    caso("51", "cliente-como-texto-numerico", "FM-CPFF-01 FM-CPFF-03: p_IdCliente '100' (texto): Gson lo convierte en 100", entrada(p_IdCliente="100"), seed=[cartera(1), cartera(2, IdCliente=101, IdPortafolio=502)])
    caso("51b", "cliente-no-numerico", "FM-CPFF-01: p_IdCliente 'abc' => JsonSyntaxException", entrada(p_IdCliente="abc"), seed=[cartera(1)])
    caso("51c", "cliente-decimal", "FM-CPFF-01: p_IdCliente 1.5 => JsonSyntaxException", entrada(p_IdCliente=1.5), seed=[cartera(1)])
    caso("51d", "empresa-como-texto", "FM-CPFF-01: p_IdEmpresa '1' (texto): Gson lo convierte en 1", entrada(p_IdEmpresa="1"), seed=[cartera(1)])
    caso("51e", "boveda-agrupa-numerico", "FM-CPFF-01: p_AgrupaPorBoveda 1 (numero): Gson lo convierte en el texto '1': no es 'S': no agrupa", entrada(p_AgrupaPorBoveda=1), seed=[cartera(1, IdBoveda="DCV"), cartera(2)])
    caso("51f", "fecha-como-numero", "FM-CPFF-01 FM-CPFF-05: p_FechaConsulta 20260310 (numero): Gson lo convierte en el texto '20260310' => DateTimeParseException", entrada(p_FechaConsulta=20260310), seed=[cartera(1)])
    caso("51g", "salida-con-nulos-escritos", "FM-CPFF-03: Utils.getGsonBuilder escribe los nulos: Producto null en la fila, RegimenTributario de una garantia sola null", entrada(),
         seed=[cartera(1, **CERRADA), garantia(origen=1)])


def orden():
    # ---- C22: order of the output (HashMap), 6 and 14 keys and the same keys inserted in another order
    seis = [cartera(i + 1, **P(900 + i)) for i in range(6)]
    caso("22", "orden-seis-claves", "FM-CPFF-16 C22 D-F3C-13: seis grupos (un instrumento cada uno): el orden de p_out_tbl es el de iteracion del HashMap<String,Instrumento> (20 corridas)", entrada(), seed=seis)
    caso("22m", "orden-seis-claves-insertadas-al-reves", "FM-CPFF-16 D-F3C-13: las mismas seis claves insertadas en orden inverso: el orden de salida depende del hash de la clave (no de la insercion) salvo en un mismo "
         "bucket", entrada(), seed=list(reversed(seis)))
    catorce = []
    i = 0
    for inst in range(900, 906):
        for port in (500, 501):
            i += 1
            catorce.append(cartera(i, IdPortafolio=port, **P(inst)))
    for inst in (900, 901):
        i += 1
        catorce.append(cartera(i, IdCliente=101, IdPortafolio=502, **P(inst)))
    caso("22n", "orden-catorce-claves-redimensiona", "FM-CPFF-16 C22 D-F3C-13: 14 grupos (el HashMap se redimensiona al pasar de 12): el orden es el de la tabla de 32 cubos", entrada(), seed=catorce)
    caso("22o", "orden-catorce-claves-mas-garantias", "FM-CPFF-16 D-F3C-13: 14 grupos de custodia y 2 grupos solo de garantia (insertados DESPUES de la custodia en hInstrumento)", entrada(),
         seed=catorce + [cartera(20, **P(900), IdBoveda="DCV", IdRegimenFondoMutuo="BF", **CERRADA), garantia(origen=20, **P(900), INGCantidad=N("3")),
                         cartera(21, **P(901), IdRegimenFondoMutuo="BF", **CERRADA), garantia(origen=21, **P(901), INGCantidad=N("4"))])
    veinte = []
    for k in range(1, 21):
        veinte.append(cartera(k, IdCliente=100, IdPortafolio=500 if k % 2 else 501, **P(900 + (k % 6)), IdRegimenFondoMutuo=["GEN", "BF", "18Q", "18T", "57BIS"][k % 5], INGCantidad=N(str(k))))
    caso("22p", "orden-veinte-claves-regimenes", "FM-CPFF-16 D-F3C-13: hasta 20 grupos con regimenes distintos (clave con el regimen): orden por hash", entrada(), seed=veinte)
    caso("22q", "orden-con-relacion-y-boveda", "FM-CPFF-16: con S y S las claves llevan _relacion_bodega: el orden cambia", entrada(p_AgrupaPorRelacion="S", p_AgrupaPorBoveda="S"),
         seed=[cartera(1, IdCliente=99, IdPortafolio=504, **P(900), IdBoveda="DCV"), cartera(2, IdCliente=101, IdPortafolio=502, **P(900), IdBoveda="COR"), cartera(3, IdCliente=100, **P(900), IdBoveda="DCV"),
               cartera(4, IdCliente=99, IdPortafolio=504, **P(901), IdBoveda="COR"), cartera(5, IdCliente=101, IdPortafolio=502, **P(901), IdBoveda="DCV"), cartera(6, IdCliente=100, **P(901), IdBoveda="COR")])
