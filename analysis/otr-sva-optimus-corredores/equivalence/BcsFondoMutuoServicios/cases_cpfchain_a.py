"""FM-CHAIN cases: CPF (ConsultarCustodiaPortafolioFondoMutuo) over the REAL FFMM (ConsultarCustodiaPortafolioFFMM), both legacy classes unmodified, on one H2 (cases_cpfchain_lib.py). They cross
RULES_FONDOMUTUO_F3_C.md FM-CPFF-21 / FM-CPFF-22 (what CPF asks FFMM, and what the real FFMM answers that the double of the CPF front never did):
 22.1 the solicitudes in transit come back INSIDE the custody (quantity 0, IdBoveda ''), so the saldo has custody != null and its IdBoveda is '' (not 'COR'),
 22.2 PPPCustodia 0 with a guarantee and null PPP (quantity 0) that CPF turns into 0,
 22.3 Relacion and IdBoveda are always '' (CPF asks N / N),
 22.4 'the last custody of a CPF key wins' when two FFMM groups differ only in Nemotecnico / IdEmisor (HashMap order of FFMM),
 22.5 the date: CPF always passes a real date (the default '0001-01-01' never reaches FFMM),
 22.6 errors of FFMM go through CPF unchanged (it does not catch them), 22.8 the JSON / scale of the answer (in the NEW chain the answer travels as JSON through the port; in the legacy it does not).
Inputs only: expected.json is recorded from the legacy by record_cpfchain.sh.
"""
from cases_cpfchain_lib import *  # noqa: F401,F403
from cases_cpf_a import solm  # noqa: E402

CERRADA = dict(EGRFechaLiquidacion=D("2026-02-20"), EGRFechaOperacion=D("2026-02-18"))   # closed before the system date
RESP = TS("2026-03-01 00:00:00")                                                           # backup date of the historical cases (the system date is 2026-03-02)


def P(inst, **kw):
    emi, nemo = INSTRUMENTOS[inst]
    d = dict(IdInstrumento=inst, Nemotecnico=nemo, IdEmisor=emi)
    d.update(kw)
    return d


def H(i, **kw):
    kw.setdefault("FechaRespaldo", RESP)
    return his(i, **kw)


def HG(origen, **kw):
    kw.setdefault("FechaRespaldo", RESP)
    return hgar(origen, **kw)


def generar():
    solo_custodia()
    fechas()
    en_transito()
    desempate_y_orden()
    filtros_y_errores()


def solo_custodia():
    caso("custodia-sola", "FM-CPFF-21 FM-CPFF-22.3: una posicion de 100 cuotas (cliente 100, portafolio 500): CPF pide a la FFMM REAL con N / N / LIQ y la fecha del sistema; la fila de CPF trae IdBoveda '' (no 'COR') "
         "y Relacion ''", entrada(), seed=[cartera(1)])
    caso("custodia-mas-garantia", "FM-CPFF-22.2 D-F3C-01 D-F3A-03: custodia 100 + garantia 10: la FFMM da PPP 0 (monto reiniciado); CPF copia PPPCustodia 0 y CantidadGarantia 10; MontoGarantia 0 (D-F3A-03)", entrada(),
         seed=[cartera(1), garantia(origen=1)])
    caso("solo-garantia", "FM-CPFF-22.2 D-F3C-08: solo garantia de 25 (cartera origen cerrada): custodia 0, garantia 25, RegimenTributario nulo en la FFMM (CPF lo copia)", entrada(),
         seed=[cartera(1, **CERRADA), garantia(origen=1, INGCantidad=N("25"))])
    caso("dos-filas-mismo-grupo", "FM-CPFF-13: 60 + 40 cuotas del mismo grupo: CPF recibe una fila de 100 con PPP 9800", entrada(),
         seed=[cartera(1, INGCantidad=N("60"), INGMontoCompraActualMonedaInstrumento=N("600000"), PrecioCompra=N("10000"), INGMontoATasaCompraActual=N("600000")),
               cartera(2, INGCantidad=N("40"), INGMontoCompraActualMonedaInstrumento=N("380000"), PrecioCompra=N("9500"), INGMontoATasaCompraActual=N("390000"))])
    caso("boveda-dcv-sale-vacia", "FM-CPFF-22.3: la posicion esta en la bodega DCV; CPF pide p_AgrupaPorBoveda N: la FFMM responde IdBoveda '' y CPF lo copia (la bodega real nunca llega a CPF)", entrada(),
         seed=[cartera(1, IdBoveda="DCV")])
    caso("dos-bodegas-se-suman", "FM-CPFF-22.3: bodegas DCV (60) y COR (40): un solo grupo de 100 con IdBoveda ''", entrada(),
         seed=[cartera(1, IdBoveda="DCV", INGCantidad=N("60")), cartera(2, IdBoveda="COR", INGCantidad=N("40"))])
    caso("cliente-corredora-relacion-vacia", "FM-CPFF-22.3: posicion del cliente 99 (la corredora): con p_AgrupaPorRelacion N la FFMM no marca CPRO: Relacion '' en CPF", entrada(),
         seed=[cartera(1, IdCliente=99, IdPortafolio=504)])
    caso("dos-instrumentos", "FM-CPFF-21: dos instrumentos (900 y 901) del mismo cliente y portafolio: dos saldos en CPF (orden del HashMap de CPF)", entrada(), seed=[cartera(1, **P(900)), cartera(2, **P(901))])
    seis = [cartera(i + 1, **P(900 + i)) for i in range(6)]
    caso("seis-saldos", "FM-CPFF-16 FM-CPFF-22.4: seis saldos de un instrumento cada uno: el orden de CPF es el de SU HashMap (clave cliente_portafolio_instrumento_regimen); la FFMM entrega en el orden del suyo", entrada(),
         seed=seis, colaboradores=paridad(final=1, n=16))
    catorce = []
    i = 0
    for inst in range(900, 906):
        for port in (500, 501):
            i += 1
            catorce.append(cartera(i, IdPortafolio=port, **P(inst)))
    for inst in (900, 901):
        i += 1
        catorce.append(cartera(i, IdCliente=101, IdPortafolio=502, **P(inst)))
    caso("catorce-saldos-redimensiona", "FM-CPFF-16 FM-CPFF-22.4: 14 saldos: los dos HashMap (FFMM y CPF) se redimensionan; el orden final es el de CPF", entrada(), seed=catorce, colaboradores=paridad(final=1, n=40))
    caso("posicion-cantidad-cero", "FM-CPFF-22.2 FM-CPFF-15: posicion con INGCantidad 0: la FFMM da PPP nulo y CPF lo vuelve 0 (nulo -> 0)", entrada(), seed=[cartera(1, INGCantidad=N("0"))])
    caso("posicion-negativa", "FM-CPFF-13 FM-CPFF-15: custodia negativa (-10): CPF valoriza la cantidad negativa", entrada(),
         seed=[cartera(1, INGCantidad=N("-10"), INGMontoCompraActualMonedaInstrumento=N("-98000"), INGMontoATasaCompraActual=N("-99000"))])
    caso("regimen-bf-con-descripcion", "FM-CPFF-08 FM-CPFF-22.9: regimen BF: RegimenTributario 'Beneficio Fiscal' viene de la FFMM y CPF lo copia", entrada(), seed=[cartera(1, IdRegimenFondoMutuo="BF")])
    caso("regimen-nulo", "FM-CPFF-12 FM-CPFF-22.9: IdRegimenFondoMutuo NULL en la cartera: la clave de CPF lleva 'null'", entrada(), seed=[cartera(1, IdRegimenFondoMutuo=None)])
    caso("portafolio-apv-producto", "FM-CPFF-08 FM-CPFF-22.9: portafolio 501 (APV, producto 3, ADC S, matriz 7): Producto, EsADC e IdMatrizAgrupador de la FFMM", entrada(), seed=[cartera(1, IdPortafolio=501)])
    caso("portafolio-noapv-adc", "FM-CPFF-08: cliente 101 portafolio 502 (matriz NOAPV 9, ADC S)", entrada(), seed=[cartera(1, IdCliente=101, IdPortafolio=502)])
    caso("fondo-en-dolares", "FM-CPFF-22.8: instrumento 902 (fondo extranjero en USD): CPF valoriza con el tipo de cambio del fondo; la FFMM lo trae como cualquier otro", entrada(),
         seed=[cartera(1, **P(902))])
    caso("fan-out-dos-matrices-duplica", "FM-CPFF-12 D-F3C-09: dos matrices NOAPV del mismo codigo duplican la fila de la FFMM: CPF ve 200 cuotas", entrada(),
         seed=[ins(F.MNOAPV, IdMatrizAgrupadorNOAPV=18, CodigoPortafolio="PF500", EsADC="S"), cartera(1)])
    caso("fan-out-dos-series-duplica", "FM-CPFF-06 D-F3C-09: dos series del mismo instrumento duplican la custodia en la FFMM (200) pero CPF lo detecta en SelectSeries ('key field is not unique')", entrada(),
         seed=[F.clonar_serie("FM099Z", "10", fondo=False, serie={"IdInstrumento": 900}), cartera(1)])
    caso("sin-datos", "FM-CPFF-21: sin solicitudes ni custodia: la FFMM responde {\"p_out_tbl\":[]} y CPF {P_OUT_TBL: []}", entrada())
    caso("custodia-del-cliente-sin-socio", "FM-CPFF-10: la garantia de un cliente sin socio de negocio se omite en la FFMM; la custodia (LEFT JOIN) sale con IdSocioDeNegocio nulo", entrada(p_IdCliente=103),
         seed=[F.cliente(103, 7103, "44444443-1", "Cliente sin socio"), cartera(1, IdCliente=103, IdPortafolio=0), garantia(origen=1, IdCliente=103, IdPortafolio=0)])


def fechas():
    caso("fecha-por-defecto-cpf-la-reemplaza", "FM-CPFF-21 FM-CPFF-22.5 C02: CPF recibe '0001-01-01' y pasa a la FFMM la FECHA DEL SISTEMA (2026-03-02): la FFMM responde (a diferencia de la llamada directa con '0001-01-01', que da vacio)",
         entrada(p_FechaConsulta="0001-01-01"), seed=[cartera(1)])
    caso("fecha-nula-cpf-la-reemplaza", "FM-CPFF-22.5: p_FechaConsulta nula explicita => la fecha del sistema tambien en la FFMM", entrada(p_FechaConsulta=None), seed=[cartera(1)])
    caso("fecha-igual-al-sistema", "FM-CPFF-07 FM-CPFF-22.5: fecha explicita igual al sistema: vigente (>=)", entrada(p_FechaConsulta="2026-03-02"), seed=[cartera(1), H(2, **P(901))])
    caso("fecha-futura", "FM-CPFF-07 FM-CPFF-22.5: fecha futura 2026-04-01: vigente con esa fecha (la posicion con egreso el 2026-03-20 no sale)", entrada(p_FechaConsulta="2026-04-01"),
         seed=[cartera(1, **P(900)), cartera(2, **P(901), EGRFechaLiquidacion=D("2026-03-20"))])
    caso("fecha-historica", "FM-CPFF-09 FM-CPFF-22.5 D-F3C-07: fecha 2026-03-01 (anterior al sistema): FFMM HISTORICA (respaldo): Producto nulo y RegimenTributario '' en CPF aunque el portafolio sea APV",
         entrada(p_FechaConsulta="2026-03-01"), seed=[cartera(1, IdPortafolio=500), H(2, IdPortafolio=501)])
    caso("fecha-historica-tres-meses-antes", "FM-CPFF-07 FM-CPFF-22.5: fecha 2025-12-15 (varios meses antes del sistema): FFMM HISTORICA (fija 'no es anterior' frente a compareTo != -1 de LocalDate)", entrada(p_FechaConsulta="2025-12-15"),
         seed=[cartera(1, INGFechaLiquidacion=D("2024-01-01")), H(2, **P(901), INGFechaLiquidacion=D("2024-01-01"), FechaRespaldo=TS("2025-12-15 00:00:00"))])
    caso("fecha-historica-ultima-version", "FM-CPFF-09: respaldo con dos versiones del folio 77 (IdCartera 5 y 9): CPF recibe solo la de IdCartera mayor", entrada(p_FechaConsulta="2026-03-01"),
         seed=[H(5, FolioCartera=77), H(9, FolioCartera=77, INGCantidad=N("40"))])
    caso("fecha-historica-garantia", "FM-CPFF-10 D-F3C-01: custodia y garantia historicas: PPP 0", entrada(p_FechaConsulta="2026-03-01"), seed=[H(1, EGRMonto=N("500000")), HG(1)])
    caso("fecha-historica-sin-respaldo", "FM-CPFF-09: fecha 2026-02-01 sin respaldo: la FFMM responde vacio; CPF solo con sus solicitudes (ninguna)", entrada(p_FechaConsulta="2026-02-01"), seed=[cartera(1)])
    caso("fecha-historica-con-solicitud", "FM-CPFF-22.1 D-F3C-06: fecha historica y la solicitud en transito: la rama 2 historica de la FFMM exige ':IdEmisor = :IdEmisor' (CPF manda emisor nulo): la rama desaparece; el saldo "
         "de la solicitud NO tiene custodia y CPF le pone IdBoveda 'COR' (en vigente el saldo tiene custodia y sale '')", entrada(p_FechaConsulta="2026-03-01"), [sol(1, FechaAceptacion=F.fecha("2026-02-27"))])
    caso("fecha-historica-con-emisor", "FM-CPFF-22.1 D-F3C-06: fecha historica con p_IdEmisor 'AGF1': la rama 2 historica SI sale en la FFMM (el IdEmisor viaja a CPF para sus solicitudes): saldo con custodia e IdBoveda ''",
         entrada(p_FechaConsulta="2026-03-01", p_IdEmisor="AGF1"), [sol(1, FechaAceptacion=F.fecha("2026-02-27"))])
    caso("fecha-con-hora", "FM-CPFF-22.5 FM-CPFF-05: p_FechaConsulta '2026-02-27 10:00:00' (CPF no la valida): llega cruda a la FFMM (se parsea con el formato fecha-hora) y al SQL", entrada(p_FechaConsulta="2026-02-27 10:00:00"),
         seed=[cartera(1)])
    caso("fecha-texto-libre", "FM-CPFF-22.5 FM-CPFF-05: p_FechaConsulta 'hoy': la FFMM falla con DateTimeParseException y CPF no la captura", entrada(p_FechaConsulta="hoy"), seed=[cartera(1)])
    caso("fecha-vacia", "FM-CPFF-22.5 FM-CPFF-05: p_FechaConsulta '' no es la fecha por defecto: llega a la FFMM: DateTimeParseException", entrada(p_FechaConsulta=""), seed=[cartera(1)])
    caso("egreso-igual-a-la-fecha", "FM-CPFF-08 QD-F3C-06: la posicion con egreso el 2026-03-02 (= fecha del sistema) esta excluida; la que egresa el 03-03 sale", entrada(),
         seed=[cartera(1, **P(900), EGRFechaLiquidacion=D("2026-03-02")), cartera(2, **P(901), EGRFechaLiquidacion=D("2026-03-03"))])
    caso("sistema-distinto", "FM-CPFF-07: PLA.FechaSistema 2026-03-15: la fecha por defecto cambia y la posicion que ingresa el 03-10 sale", entrada(), seed=[config("PLA.FechaSistema", "2026-03-15"), cartera(1, INGFechaLiquidacion=D("2026-03-10"))])


def en_transito():
    caso("solicitud-sin-cartera-22-1", "FM-CPFF-22.1 FM-CPFF-08: aporte ING de 10 cuotas sin cartera de la serie: la FFMM devuelve una fila de custodia con cantidad 0 e IdBoveda ''; el saldo de CPF TIENE custodia: IdBoveda '' "
         "(no 'COR'), PPPCustodia 0 (nulo -> 0)", entrada(), [sol(1)])
    caso("solicitud-con-cartera-vigente", "FM-CPFF-22.1: la serie tiene cartera vigente del cliente: la FFMM NO devuelve la fila de solicitud (NOT IN); CPF suma la solicitud a la custodia real y la bodega es ''", entrada(),
         [sol(1)], seed=[cartera(1)])
    caso("rescate-en-transito-con-custodia", "FM-CPFF-22.1: custodia de 100 y un rescate en transito de 30 cuotas: CPF calcula CantidadTransSolicitudesRescate con la custodia real de la FFMM", entrada(),
         [sol(1, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM", NumeroCuotas=30)], seed=[cartera(1)])
    caso("aporte-y-rescate-sin-cartera", "FM-CPFF-22.1: aporte de 10 y rescate de 4 de la misma serie sin cartera: la FFMM da UNA fila por solicitud (misma clave: se suman en un grupo, cantidad 0)", entrada(),
         [sol(1), sol(2, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM", NumeroCuotas=4)])
    caso("dos-solicitudes-misma-serie", "FM-CPFF-22.1 FM-CPFF-13: dos aportes de la misma serie: dos filas en la FFMM sumadas (ValorCompra / MontoCompra) y las dos en CPF", entrada(),
         [sol(1, NumeroCuotas=10, ValorCuota=1000, Monto=10000), sol(2, NumeroCuotas=5, ValorCuota=1100, Monto=5500)])
    caso("solicitud-por-monto", "FM-CPFF-22.1: solicitud por monto (0 cuotas, no se valoriza): la FFMM igual devuelve la fila de cantidad 0", entrada(), [solm(1, 1100)])
    caso("solicitud-pen", "FM-CPFF-22.1: Estado PEN entra en las dos", entrada(), [sol(1, Estado="PEN")])
    caso("solicitud-estado-ejecutada", "FM-CPFF-22.1: Estado EJE no entra en ninguna", entrada(), [sol(1, Estado="EJE")])
    caso("solicitud-aceptada-despues", "FM-CPFF-22.1 FM-CPFF-08: solicitud con FechaAceptacion 2026-03-05 (> fecha de consulta 2026-03-02): CPF la incluye (su SQL no filtra por fecha) pero la FFMM NO: el saldo no tiene custodia: IdBoveda 'COR'",
         entrada(), [sol(1, FechaAceptacion=F.fecha("2026-03-05"))])
    caso("solicitud-de-otro-cliente-con-filtro", "FM-CPFF-22.1: p_IdCliente 101 y una solicitud del cliente 101 con cartera del cliente 100: solo el filtro del cliente", entrada(p_IdCliente=101),
         [sol(1, IdCliente=101, IdPortafolio=502, IdSerieFondo="FM002B")], seed=[cartera(1)])
    caso("filtro-instrumento-solicitud-ajena", "FM-CPFF-22.1 D-F3C-06: p_IdInstrumento 901: CPF solo trae la solicitud de FM002B, pero la FFMM trae TAMBIEN la solicitud de FM001A (la rama 2 no aplica el instrumento): "
         "aparece un saldo de FM001A con cantidad 0 que CPF no habria pedido", entrada(p_IdInstrumento=901), [sol(1), sol(2, IdSerieFondo="FM002B")])
    caso("filtro-emisor-solicitud-ajena", "FM-CPFF-22.1 D-F3C-06: p_IdEmisor AGF2: la rama 2 vigente de la FFMM no aplica el emisor: la solicitud de FM001A (AGF1) aparece como saldo", entrada(p_IdEmisor="AGF2"),
         [sol(1), sol(2, IdSerieFondo="FM002B")])
    caso("filtro-familia-solicitud-ajena", "FM-CPFF-22.1 D-F3C-06: p_IdFamiliaFondo FAM2: la rama 2 de la FFMM no aplica la familia", entrada(p_IdFamiliaFondo="FAM2"), [sol(1), sol(2, IdSerieFondo="FM002B")])
    caso("solicitud-serie-inexistente", "FM-CPFF-22.1: solicitud de la serie 'NOEXISTE': la FFMM la devuelve con instrumento nulo; CPF la omite por no tener serie", entrada(), [sol(1, IdSerieFondo="NOEXISTE")])
    caso("solicitud-regimen-bf", "FM-CPFF-22.1 FM-CPFF-08: solicitud con regimen BF: la FFMM trae 'Beneficio Fiscal'; CPF la copia", entrada(), [sol(1, IdRegimenTributario="BF")])
    caso("transito-y-garantia", "FM-CPFF-22.1 FM-CPFF-14: solicitud en transito y garantia de la misma serie (cartera origen cerrada): un grupo de la FFMM con custodia 0 y garantia 12", entrada(), [sol(1)],
         seed=[cartera(1, **CERRADA), garantia(origen=1, INGCantidad=N("12"))])
    caso("solicitud-cartera-anulada", "FM-CPFF-22.1 FM-CPFF-08: cartera ANULADA vigente en la fecha: la FFMM no trae la custodia y excluye la solicitud (NOT IN); CPF si ve la solicitud", entrada(), [sol(1)],
         seed=[cartera(1, IdEstado="ANU")])


def desempate_y_orden():
    caso("desempate-nemotecnico", "FM-CPFF-22.4 D-F3C-13: dos grupos de la FFMM del mismo instrumento / cliente / portafolio / regimen con Nemotecnico FM001A (100) y FM001X (40): la clave de CPF es la misma: GANA LA ULTIMA fila "
         "en el orden de iteracion del HashMap de la FFMM", entrada(), seed=[cartera(1), cartera(2, Nemotecnico="FM001X", INGCantidad=N("40"))])
    caso("desempate-nemotecnico-invertido", "FM-CPFF-22.4 D-F3C-13: los mismos dos grupos insertados al reves: el ganador lo decide el hash de la clave, no la insercion", entrada(),
         seed=[cartera(1, Nemotecnico="FM001X", INGCantidad=N("40")), cartera(2)])
    caso("desempate-emisor", "FM-CPFF-22.4 D-F3C-13: dos grupos del mismo instrumento con IdEmisor AGF1 y AGF2", entrada(), seed=[cartera(1), cartera(2, IdEmisor="AGF2", INGCantidad=N("25"))])
    caso("desempate-tres-grupos", "FM-CPFF-22.4 D-F3C-13: tres grupos con la misma clave de CPF (Nemotecnico FM001A / FM001X / FM001Y, emisores AGF1 / AGF2 / AGF1)", entrada(),
         seed=[cartera(1), cartera(2, Nemotecnico="FM001X", IdEmisor="AGF2", INGCantidad=N("25")), cartera(3, Nemotecnico="FM001Y", INGCantidad=N("7"))])
    caso("desempate-garantia-y-custodia", "FM-CPFF-22.4 D-F3C-08: custodia de FM001A y una garantia de FM001X (cartera origen cerrada): dos grupos de la FFMM con la misma clave de CPF; el de solo garantia tiene "
         "RegimenTributario nulo", entrada(), seed=[cartera(1), cartera(2, Nemotecnico="FM001X", **CERRADA), garantia(origen=2, Nemotecnico="FM001X", INGCantidad=N("5"))])
    caso("desempate-con-solicitud", "FM-CPFF-22.4 FM-CPFF-22.1: dos grupos de custodia de FM001A / FM001X del cliente y una solicitud de FM002B (otro instrumento: sin empate)", entrada(),
         [sol(1, IdSerieFondo="FM002B")], seed=[cartera(1), cartera(2, Nemotecnico="FM001X", INGCantidad=N("3"))])


def filtros_y_errores():
    caso("filtro-familia", "FM-CPFF-06: p_IdFamiliaFondo FAM2: solo los instrumentos 901 y 904", entrada(p_IdFamiliaFondo="FAM2"), seed=[cartera(1, **P(900)), cartera(2, **P(901)), cartera(3, **P(904))])
    caso("filtro-emisor", "FM-CPFF-06: p_IdEmisor AGF2: la FFMM filtra por CAR.IdEmisor (el de la cartera)", entrada(p_IdEmisor="AGF2"), seed=[cartera(1, **P(900)), cartera(2, **P(901))])
    caso("filtro-producto", "FM-CPFF-06: p_IdProductoAgrupador 3: solo el portafolio APV 501", entrada(p_IdProductoAgrupador=3), seed=[cartera(1, IdPortafolio=500), cartera(2, IdPortafolio=501)])
    caso("filtro-cliente", "FM-CPFF-06: p_IdCliente 101", entrada(p_IdCliente=101), seed=[cartera(1), cartera(2, IdCliente=101, IdPortafolio=502)])
    caso("filtro-portafolio", "FM-CPFF-06: p_IdPortafolio 501 (custodia y garantia)", entrada(p_IdPortafolio=501), seed=[cartera(1, IdPortafolio=500), cartera(2, IdPortafolio=501), garantia(origen=2, IdPortafolio=501)])
    caso("filtro-instrumento", "FM-CPFF-06: p_IdInstrumento 901", entrada(p_IdInstrumento=901), seed=[cartera(1, **P(900)), cartera(2, **P(901))])
    caso("empresa-inexistente-en-ffmm", "FM-CPFF-22.6 FM-CPFF-05: ConsultarEmpresa sin fila (la FFMM la llama PRIMERO): Exception('Empresa no existe') sube por CPF sin captura", entrada(p_IdEmpresa=1), seed=[cartera(1)],
         colaboradores={"ConsultarEmpresa": {"respuestas": [{"error": "Empresa no existe"}]}})
    caso("valor-compra-nulo-npe", "FM-CPFF-22.6 FM-CPFF-13 D-F3C-11: ValorCompra NULL en el respaldo (HIS permite nulos): NullPointerException en la FFMM que atraviesa CPF", entrada(p_FechaConsulta="2026-03-01"),
         seed=[H(1, INGMontoCompraActualMonedaInstrumento=None)])
    caso("persona-destino-nula", "FM-CPFF-11 D-F3C-18: REL.ConsultarRelacionPersona con IdPersonaDestino NULL: no falla (la FFMM la consulta aunque CPF no pida relacion)", entrada(), seed=[cartera(1), ins(REL, IdPersonaOrigen=1, IdPersonaDestino=None, IdEstado="ACT")])
    caso("empresa-sin-persona", "FM-CPFF-11: la empresa sin IdPersona: la FFMM consulta las personas relacionadas con NULL", entrada(), seed=[cartera(1)], colaboradores={"ConsultarEmpresa": {"defecto": {"IdEmpresa": 1, "IdCliente": 99}}})
    caso("estado-vigente-solo-vig", "FM-CPFF-08: LIQ y ANU no son custodia vigente", entrada(), seed=[cartera(1, **P(900)), cartera(2, **P(901), IdEstado="LIQ"), cartera(3, **P(903), IdEstado="ANU")])
    caso("empresa-ajena-no-sale", "FM-CPFF-08: cartera de la empresa 2 no sale", entrada(), seed=[cartera(1), cartera(2, IdEmpresa=2, **P(901))])
    caso("instrumento-sin-serie", "FM-CPFF-06 FM-CPFF-08: instrumento 906 sin serie: la FFMM lo descarta (Serie.IdFamiliaFondo nulo)", entrada(),
         seed=[ins(INST, IdInstrumento=906, IdTipoInstrumento="FM"), cartera(1, IdInstrumento=906, Nemotecnico="FM007F"), cartera(2, **P(901))])
    caso("sin-parametro-fecha-sistema", "FM-CPFF-05: sin PLA.FechaSistema: CPF falla primero (lo lee antes que nada)", entrada(), seed=[config("PLA.FechaSistema", None), cartera(1)])
    # ---- appended after the first canary run (numbers of the earlier cases do not move): what the first chain canary left unpinned
    caso("ppp-no-exacto", "FM-CPFF-15 FM-CPFF-22.2: dos filas (1 cuota a 100 y 2 cuotas a 101): MontoPrecioPromedio 302, PPP = 302 / 3 con DECIMAL128 (34 digitos): CPF copia el PPP sin recortarlo", entrada(),
         seed=[cartera(1, INGCantidad=N("1"), INGMontoCompraActualMonedaInstrumento=N("100"), INGMontoATasaCompraActual=N("100"), PrecioCompra=N("100")),
               cartera(2, INGCantidad=N("2"), INGMontoCompraActualMonedaInstrumento=N("202"), INGMontoATasaCompraActual=N("202"), PrecioCompra=N("101"))])
    caso("solicitud-con-cartera-de-otro-cliente", "FM-CPFF-22.1: el NOT IN de la rama de solicitudes mira el cliente de la solicitud: la cartera de FM001A del cliente 101 NO excluye la solicitud del cliente 100 (sin filtro de cliente): "
         "dos saldos, el del cliente 101 con custodia y el del 100 con la solicitud y bodega ''", entrada(), [sol(1)], seed=[cartera(1, IdCliente=101, IdPortafolio=502)])
    caso("garantia-estado-liq-entra", "FM-CPFF-10: la garantia exige IdEstado <> 'ANU' (no = 'VIG'): una garantia LIQ entra (4) y una ANU no (7)", entrada(),
         seed=[cartera(1), garantia(origen=1, IdEstado="LIQ", INGCantidad=N("4")), garantia(origen=1, IdEstado="ANU", INGCantidad=N("7"))])
    caso("garantia-egreso-igual-a-la-fecha", "FM-CPFF-10 QD-F3C-06: la garantia con egreso el 2026-03-02 (= fecha del sistema) esta excluida; la que egresa el 03-03 entra", entrada(),
         seed=[cartera(1), garantia(origen=1, EGRFechaLiquidacion=D("2026-03-02"), INGCantidad=N("9")), garantia(origen=1, EGRFechaLiquidacion=D("2026-03-03"), INGCantidad=N("2"))])
