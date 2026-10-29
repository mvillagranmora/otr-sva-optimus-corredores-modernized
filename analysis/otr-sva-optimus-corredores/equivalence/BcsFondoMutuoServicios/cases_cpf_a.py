"""FMM-CPF cases (ConsultarCustodiaPortafolioFondoMutuo + CalcularMontoNumeroCuota + ObtenerDiaSiguiente REAL; the custody calculation, CalcularParidad, EntregarPreciosTasas and
ConsultarEmpresa are scripted doubles): cards FM-CPF-01..14 (RULES_FONDOMUTUO_F3_A.md 8.2), defects D-F3A-03..08 and D-F3A-27 (section 9), D-NOR-9, matrix CPF-C01..C11
(section 11). Inputs only: expected.json is recorded from the legacy by record_cpf.sh.

Universe (seed/base-cpf.sql on top of seed/base-fmm.sql): clients 100 / 101 / 102; portfolios 500 (client 100, APV matrix 0, NOAPV matrix 8 by code PF500, ADC N), 501 (client 100,
APV matrix 7: product 3 'Producto APV tres', ADC S), 502 (client 101, NOAPV matrix 9, ADC S), 503 (client 101, APV matrix NULL); series FM001A (instrument 900, fund 10, CLP, AGF1,
4 decimals of quota, family FAM1), FM002B (901, AGF2, 2 decimals, family FAM2, restricts rescue), FM003U (902, USD fund, EXT1 foreign, 2 decimals), FM004C (903),
FM005D (904, AGF2), FM006E (905). Only FM001A has a regime (GEN); regimes GEN, BF, 18Q (RescateConMandato S), 18T (AceptaMandatoEnReinversion S), 57BIS. Clock and FechaSistema 2026-03-02,
system currency CLP (0 decimals, rounding 'R'), price 1500.1234 for every IdRegistro unless a case says otherwise, CalcularParidad answers 1 per quota for the IdInterno 1..8
of CPF's own call. Default solicitud: APO, ING, by units ('C') 10, FM001A, client 100, portfolio 500, regime GEN.
"""
from cases_cpf_lib import *  # noqa: F401,F403

INST = {s: d["inst"] for s, d in SERIES.items()}
SERIES_ID = list(SERIES)


def cus(serie="FM001A", **kw):
    """A custody row of the series (instrument and nemotecnico of the series)."""
    emisor = {"FM002B": "AGF2", "FM003U": "EXT1", "FM005D": "AGF2"}.get(serie, "AGF1")
    return custodia(instrumento=INST[serie], nemo=serie, IdEmisor=emisor, **kw)


def solm(i, monto, **ov):
    """A solicitud by AMOUNT with 0 quotas: the legacy does NOT value it (cuotas <= 0), so it makes NO collaborator call and the racy IdInterno assignment of CPF (parallelStream().forEach, D-F3A-04)
    never reaches an argument: the case is deterministic whatever the number of rows. The amount tells the rows apart in the output (CantidadMontoAporte / CantidadMontoRescate)."""
    d = dict(CalculoPorMontoCuota="M", Monto=monto, NumeroCuotas=0)
    d.update(ov)
    return sol(i, **d)


def generar():
    contrato()
    fecha_consulta()
    configuracion()
    solicitudes_en_transito()
    valorizacion_de_solicitudes()
    rescates_totales()
    agrupacion_y_custodia()
    salida_preliminar()
    mlt()
    valorizacion_custodia_garantia_transito()
    valor_cuota_moneda_sistema()
    orden_y_concurrencia()
    casos_adicionales()


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def contrato():
    # ---- FM-CPF-01 / CPF-C01: contract (defaults of the pojo) and the entry
    caso("custodia-sola", "FM-CPF-01 CPF-C01: solo custodia (100 cuotas FM001A, cliente 100, portafolio 500, GEN), sin solicitudes: una fila; los defaults del pojo (cliente 0, portafolio 0, "
         "instrumento 0, fecha 0001-01-01, familia '', BuscaMLT 'N', producto y emisor nulos) viajan a la custodia; la salida solo trae P_OUT_TBL (sin contador: D-F3A-27 => ROLLBACK del anfitrion)",
         entrada(), colaboradores=custodias(cus()))
    caso("sin-nada", "FM-CPF-01: sin solicitudes ni custodia => P_OUT_TBL vacio; CalcularMontoNumeroCuota se ejecuta 3 veces con 0 series (lee sus parametros) y no llama a colaboradores; "
         "ObtenerDiaSiguiente y ConsultarEmpresa SI se ejecutan (D-F3A-07)", entrada())
    caso("entrada-solo-empresa", "FM-CPF-01: solo p_IdEmpresa: usuario y modulo nulos viajan nulos a la custodia (no se validan requisitos basicos)", {"p_IdEmpresa": 1},
         colaboradores=custodias(cus()))
    caso("claves-desconocidas", "FM-CPF-01: claves que no son del pojo se ignoran", entrada(p_Otro=1, p_IdPortafolioX="a"), colaboradores=custodias(cus()))
    caso("empresa-ausente", "FM-CPF-01 D-NOR-9: entrada {} => p_IdEmpresa nulo: ConfiguracionSistema falla con NullPointerException (el kit lo cambia por ParametroNoEncontradoException: DESVIO-KIT-1)",
         {}, colaboradores=custodias(cus()))
    caso("empresa-nula", "FM-CPF-01 D-NOR-9: p_IdEmpresa nulo explicito => NullPointerException (DESVIO-KIT-1)", entrada(p_IdEmpresa=None), colaboradores=custodias(cus()))
    caso("empresa-sin-configuracion", "FM-CPF-01 D-NOR-9: empresa 99 sin configuracion => NullPointerException (DESVIO-KIT-1)", entrada(p_IdEmpresa=99), colaboradores=custodias(cus()))
    caso("empresa-como-texto", "FM-CPF-01: p_IdEmpresa '1' (texto) => Gson lo convierte a 1: responde como siempre", entrada(p_IdEmpresa="1"), colaboradores=custodias(cus()))
    caso("empresa-no-numerica", "FM-CPF-01: p_IdEmpresa 'abc' => JsonSyntaxException (HTTP 500)", entrada(p_IdEmpresa="abc"), colaboradores=custodias(cus()))
    caso("cliente-decimal", "FM-CPF-01: p_IdCliente 1.5 => JsonSyntaxException (no es un Long)", entrada(p_IdCliente=1.5))
    caso("cliente-nulo-explicito", "FM-CPF-01: p_IdCliente nulo explicito (el pojo lo deja nulo): el SQL compara con NULL (':IdCliente = 0' no es verdadero) y la custodia recibe nulo",
         entrada(p_IdCliente=None), [sol(1)], colaboradores=custodias(cus()))
    caso("cliente-negativo", "FM-CPF-01/03: p_IdCliente -1 (no es 0): filtra por ese cliente y no hay solicitudes", entrada(p_IdCliente=-1), [sol(1)])
    caso("instrumento-inexistente", "FM-CPF-01/03: p_IdInstrumento 999 (sin series): no hay solicitudes; la custodia (otra pieza) recibe el filtro", entrada(p_IdInstrumento=999), [sol(1)])
    caso("familia-nula-explicita", "FM-CPF-01/03: p_IdFamiliaFondo nulo explicito (el pojo la deja nula): ':IdFamiliaFondo = '' ' no es verdadero con NULL y no hay filtro valido: sin solicitudes; la custodia recibe nulo",
         entrada(p_IdFamiliaFondo=None), [solm(1, 1100)], colaboradores=custodias(cus()))
    caso("emisor-vacio", "FM-CPF-01/03: p_IdEmisor '' (no nulo) SI filtra (emisor igual a ''): sin solicitudes", entrada(p_IdEmisor=""), [solm(1, 1100)])
    caso("filtros-nulos-explicitos", "FM-CPF-01: p_IdProductoAgrupador, p_IdEmisor nulos explicitos = sin filtro (valor por defecto del legacy)", entrada(p_IdProductoAgrupador=None, p_IdEmisor=None),
         [sol(1)])


def fecha_consulta():
    # ---- FM-CPF-02 / CPF-C08
    caso("fecha-defecto", "FM-CPF-02 CPF-C08: p_FechaConsulta '0001-01-01' => se usa PLA.FechaSistema (2026-03-02) en la custodia, los precios y la paridad", entrada(p_FechaConsulta="0001-01-01"),
         colaboradores=custodias(cus()))
    caso("fecha-nula", "FM-CPF-02 CPF-C08: p_FechaConsulta nula explicita => tambien la fecha del sistema", entrada(p_FechaConsulta=None), colaboradores=custodias(cus()))
    caso("fecha-explicita", "FM-CPF-02 CPF-C08: p_FechaConsulta 2026-02-27 se usa tal cual (custodia, precios y paridad con esa fecha)", entrada(p_FechaConsulta="2026-02-27"),
         colaboradores=custodias(cus()))
    caso("fecha-con-hora", "FM-CPF-02: p_FechaConsulta '2026-02-27 10:00:00' (con hora, sin validar formato) se usa tal cual: se observa lo que hace la valorizacion con ella", entrada(p_FechaConsulta="2026-02-27 10:00:00"),
         colaboradores=custodias(cus()))
    caso("fecha-vacia", "FM-CPF-02: p_FechaConsulta '' no es la fecha por defecto: se usa tal cual (la valorizacion decide)", entrada(p_FechaConsulta=""), colaboradores=custodias(cus()))
    caso("fecha-texto-libre", "FM-CPF-02: p_FechaConsulta 'hoy' (sin validar formato) llega a la custodia y a la valorizacion", entrada(p_FechaConsulta="hoy"), colaboradores=custodias(cus()))
    caso("fecha-explicita-sin-valorizar", "FM-CPF-02: fecha explicita y nada que valorizar: solo se observa lo que llega a la custodia", entrada(p_FechaConsulta="2026-01-15"))
    caso("fecha-sistema-distinta", "FM-CPF-02: PLA.FechaSistema 2026-01-15: la fecha por defecto cambia con el parametro", entrada(), colaboradores=custodias(cus()),
         seed=config("PLA.FechaSistema", "2026-01-15"))
    caso("fecha-explicita-sin-fecha-sistema", "FM-CPF-02: aun con fecha explicita el legacy lee PLA.FechaSistema primero: sin el parametro => Exception 'Parametro ... no encontrado'",
         entrada(p_FechaConsulta="2026-02-27"), seed=config("PLA.FechaSistema", None))


def configuracion():
    # ---- parameters CPF and its collaborators read (every one mandatory)
    caso("sin-parametro-ws-solicitudes", "FM-CPF-03 D-NOR-9: falta FONDOMUTUO/WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS => Exception 'Parametro ... no encontrado'",
         entrada(), seed=fparam("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", None))
    caso("sin-moneda-sistema", "FM-CPF-08 D-NOR-9: falta PLATAFORMA/PLA.MonedaSistema => Exception 'Parametro ... no encontrado' (la lee CalcularMontoNumeroCuota primero)", entrada(),
         colaboradores=custodias(cus()), seed=config("PLA.MonedaSistema", None))
    caso("sin-usa-dolar-observado", "FM-CPF-04 D-NOR-9: falta FONDOMUTUO/UsaDolarObservado => Exception (CalcularMontoNumeroCuota lo lee SIEMPRE, aunque no haya series)", entrada(),
         seed=fparam("UsaDolarObservado", None))
    caso("sin-pais-sistema", "FM-CPF-04 D-NOR-9: falta PLATAFORMA/PLA.PaisSistema => Exception al valorizar (solo con series que valorizar)", entrada(), [sol(1)],
         seed=config("PLA.PaisSistema", None))
    caso("sin-calcula-valor-cuota-dia-siguiente", "FM-CPF-13 D-NOR-9: falta PLATAFORMA/PLA.CalculaValorCuotaDiaSiguiente => Exception 'Parametro ... no encontrado'", entrada(),
         colaboradores=custodias(cus()), seed=config("PLA.CalculaValorCuotaDiaSiguiente", None))


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def solicitudes_en_transito():
    # ---- FM-CPF-03 / CPF-C02: universe and filters of the solicitudes in transit
    caso("transito-sin-custodia", "FM-CPF-03/04/07/08 CPF-C02: aporte ING por cuotas (10) sin custodia: la fila sale con los datos del cliente/portafolio de la solicitud (IdBoveda 'COR', Relacion ''), "
         "valorizada (IdInterno 2 en la peticion de precios), MontoTransSolicitudesAporte = monto calculado", entrada(), [sol(1)])
    caso("estados-ing-y-pen", "FM-CPF-03: solo Estado ING y PEN entran (CER, ANU, LIQ no); cada estado en una serie distinta para no mezclarlas", entrada(),
         [solm(1, 1100, Estado="ING", IdSerieFondo="FM001A"), solm(2, 1200, Estado="PEN", IdSerieFondo="FM002B"), solm(3, 1300, Estado="CER", IdSerieFondo="FM003U"),
          solm(4, 1400, Estado="ANU", IdSerieFondo="FM004C"), solm(5, 1500, Estado="LIQ", IdSerieFondo="FM005D"), solm(6, 1600, Estado="ing", IdSerieFondo="FM006E")])
    caso("tipos-apo-res-otros-no", "FM-CPF-03: solo TipoSolicitud APO y RES (TRA y vacio no)", entrada(),
         [solm(1, 1100, IdSerieFondo="FM001A"), solm(2, 1200, IdSerieFondo="FM002B", TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM"),
          solm(3, 1300, TipoSolicitud="TRA", IdSerieFondo="FM003U"), solm(4, 1400, TipoSolicitud="", IdSerieFondo="FM004C")])
    caso("empresa-ajena-no-sale", "FM-CPF-03: la solicitud de la empresa 2 no sale (IdEmpresa = :IdEmpresa)", entrada(), [sol(1), sol(2, IdEmpresa=2, IdSerieFondo="FM002B")])
    caso("filtro-cliente", "FM-CPF-03: p_IdCliente 101 (solo el cliente 101; el 0 es 'sin filtro')", entrada(p_IdCliente=101),
         [sol(1), sol(2, IdCliente=101, IdPortafolio=502, IdSerieFondo="FM002B")], colaboradores=custodias(cus(cliente=101, portafolio=502, serie="FM002B")))
    caso("filtro-portafolio", "FM-CPF-03: p_IdPortafolio 501 (portafolio APV del cliente 100)", entrada(p_IdPortafolio=501),
         [sol(1), sol(2, IdPortafolio=501, IdSerieFondo="FM002B")])
    caso("filtro-instrumento", "FM-CPF-03: p_IdInstrumento 901 (serie FM002B)", entrada(p_IdInstrumento=901), [sol(1), sol(2, IdSerieFondo="FM002B")])
    caso("filtro-producto-agrupador", "FM-CPF-03: p_IdProductoAgrupador 3: solo el portafolio 501 (matriz APV 7, producto 3)", entrada(p_IdProductoAgrupador=3),
         [sol(1), sol(2, IdPortafolio=501, IdSerieFondo="FM002B")])
    caso("filtro-producto-agrupador-cero", "FM-CPF-03: p_IdProductoAgrupador 0: MAP(producto, NULL, 0, producto) = 0 => los portafolios SIN producto (500) y no el 501", entrada(p_IdProductoAgrupador=0),
         [sol(1), sol(2, IdPortafolio=501, IdSerieFondo="FM002B")])
    caso("filtro-emisor", "FM-CPF-03: p_IdEmisor 'AGF2' (fondo de FM002B)", entrada(p_IdEmisor="AGF2"), [sol(1), sol(2, IdSerieFondo="FM002B")])
    caso("filtro-familia", "FM-CPF-03: p_IdFamiliaFondo 'FAM2' (serie FM002B); '' es sin filtro", entrada(p_IdFamiliaFondo="FAM2"), [sol(1), sol(2, IdSerieFondo="FM002B")])
    caso("filtros-combinados-sin-filas", "FM-CPF-03: cliente 101 y emisor AGF1 sin solicitudes que cumplan => vacio", entrada(p_IdCliente=101, p_IdEmisor="AGF1"),
         [sol(1, IdCliente=101, IdPortafolio=502, IdSerieFondo="FM002B")])
    # automatic income filter (CPF-C10)
    auto = [solm(1, 1100, IdSerieFondo="FM001A", IngresoAutomatico="S"), solm(2, 1200, IdSerieFondo="FM002B", IngresoAutomatico="N"),
            solm(3, 1300, IdSerieFondo="FM003U", IngresoAutomatico="S", IdRegimenTributario="BF"), solm(4, 1400, IdSerieFondo="FM005D", IngresoAutomatico="")]
    caso("automaticas-sin-filtro", "FM-CPF-03 CPF-C10: parametro 'N': entran todas (automaticas o no)", entrada(), auto)
    caso("automaticas-con-filtro", "FM-CPF-03 CPF-C10 QD-F3A-02: parametro 'S': solo IngresoAutomatico 'N' o regimen 'BF' (la automatica con BF entra; la vacia no)", entrada(), auto,
         seed=fparam("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", "S"))
    caso("automaticas-filtro-minuscula", "FM-CPF-03: parametro 's' no es 'S' (equals): sin filtro", entrada(), auto, seed=fparam("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", "s"))
    caso("automaticas-filtro-valor-raro", "FM-CPF-03: parametro 'SI' tampoco: sin filtro", entrada(), auto, seed=fparam("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", "SI"))
    # spot origin (the aporte by amount takes the Spot secondary amount)
    spot = dict(CalculoPorMontoCuota="M", Monto=200000, NumeroCuotas=5, MontoMonedaSistema=199000)
    caso("aporte-con-spot-origen", "FM-CPF-03/04: aporte por monto con cuotas 5 y operacion Spot origen 300 (no MOD): CantidadMontoAporte = Spot.MontoMonedaSecundaria (777,5), no el MontoMonedaSistema 199000",
         entrada(), [sol(1, IdOperacionSpot=300, **spot)], seed=[ins("BSCL.Mesa.BD::SPT.OperacionSpot", IdOperacionSpotOrigen=300, Estado="CNF", MontoMonedaSecundaria=777.5)])
    caso("aporte-con-spot-modificado", "FM-CPF-03: la operacion Spot esta en estado MOD: se ignora (join con Estado != 'MOD') y se usa MontoMonedaSistema 199000", entrada(),
         [sol(1, IdOperacionSpot=300, **spot)], seed=[ins("BSCL.Mesa.BD::SPT.OperacionSpot", IdOperacionSpotOrigen=300, Estado="MOD", MontoMonedaSecundaria=777.5)])
    caso("aporte-sin-spot", "FM-CPF-03: sin operacion Spot (IdOperacionSpot 0): MontoMonedaSistema 199000", entrada(), [sol(1, **spot)])
    caso("portafolio-inexistente", "FM-CPF-03: portafolio 999 sin fila en POR.Portafolio: los datos del portafolio y del socio salen nulos (LEFT JOIN), matriz 0 y ADC 'N'", entrada(),
         [sol(1, IdPortafolio=999)])
    caso("portafolio-apv", "FM-CPF-03/08: portafolio 501 APV (matriz 7, producto 3 'Producto APV tres', ADC 'S'): Producto, IdMatrizAgrupador y EsADC de la solicitud", entrada(),
         [sol(1, IdPortafolio=501)])
    caso("portafolio-noapv-adc", "FM-CPF-03/08: portafolio 502 (cliente 101, matriz NOAPV 9, ADC 'S')", entrada(), [sol(1, IdCliente=101, IdPortafolio=502)])
    caso("portafolio-sin-matriz", "FM-CPF-03/08: portafolio 503 (matriz APV nula): matriz 0, ADC 'N', producto nulo", entrada(), [sol(1, IdCliente=101, IdPortafolio=503)])
    caso("regimen-sin-descripcion", "FM-CPF-03/08: regimen de la solicitud 'XYZ' sin fila en FMM.RegimenFondoMutuo: RegimenTributario '' (COALESCE)", entrada(), [sol(1, IdRegimenTributario="XYZ")])
    caso("garantia-en-transito", "FM-CPF-03/04: tipo de movimiento INGGTA (garantia) no se excluye del transito: la peticion de precios lleva IdUso 'GARANTIA'", entrada(),
         [sol(1, IdTipoMovimientoFondoMutuo="INGGTA")])
    caso("sin-solicitudes-que-cumplan", "FM-CPF-03: solo solicitudes CER: universo vacio", entrada(), [sol(1, Estado="CER")])


def valorizacion_de_solicitudes():
    # ---- FM-CPF-04: valuation of the solicitudes in transit
    caso("aporte-por-monto-sin-cuotas", "FM-CPF-04 CPF-C02: aporte por monto ('M') con 0 cuotas: no se valoriza (cuotas <= 0): CantidadAporte 0 y CantidadMontoAporte = Monto del SQL (150000), sin llamar a "
         "colaboradores", entrada(), [sol(1, CalculoPorMontoCuota="M", Monto=150000, NumeroCuotas=0)])
    caso("aporte-por-monto-con-cuotas", "FM-CPF-04: aporte por monto con cuotas 5: se valoriza; CantidadAporte = cuotas calculadas (Monto / precio) y CantidadMontoAporte = MontoMonedaSistema de la fila (199000)",
         entrada(), [sol(1, CalculoPorMontoCuota="M", Monto=150000, NumeroCuotas=5, MontoMonedaSistema=199000)])
    caso("aporte-por-cuotas", "FM-CPF-04: aporte por cuotas ('C') 10: CantidadMontoAporte = MontoMonedaSistema calculado (10 x 1500,1234 = 15001)", entrada(), [sol(1)])
    caso("rescate-por-cuotas", "FM-CPF-04: rescate por cuotas 8,5: CantidadRescate = 8,5 y CantidadMontoRescate = MontoMonedaSistema calculado (12751)", entrada(), [rescate(1, NumeroCuotas=8.5)])
    caso("rescate-por-monto-con-cuotas", "FM-CPF-04: rescate por monto ('M') con cuotas 3: CantidadMontoRescate = MontoMonedaSistema calculado (a diferencia del aporte)", entrada(),
         [rescate(1, CalculoPorMontoCuota="M", Monto=90000, NumeroCuotas=3)])
    caso("aporte-y-rescate-series-distintas", "FM-CPF-04: un aporte (FM001A, monto 1100) y un rescate (FM002B, monto 1200) por monto sin cuotas, en series distintas (dos saldos, nada que valorizar): "
         "CantidadMontoAporte 1100 y CantidadMontoRescate 1200 tal como los trae el SQL", entrada(),
         [solm(1, 1100), solm(2, 1200, IdSerieFondo="FM002B", TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM")])
    caso("cuotas-negativas-no-valorizan", "FM-CPF-04 D-F3A-05: aporte con cuotas -5 (suma negativa): no se valoriza y queda como lo trae el SQL", entrada(), [sol(1, NumeroCuotas=-5, Monto=100)])
    caso("aporte-y-rescate-misma-fila-de-saldo", "FM-CPF-04/07: aporte (monto 1100) y rescate (monto 2200) por monto sin cuotas de la misma serie, cliente, portafolio y regimen (dos filas del SQL por distinto "
         "IdTipoMovimientoFondoMutuo) se combinan en un saldo sin valorizar", entrada(), [solm(1, 1100), solm(2, 2200, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM")])
    caso("valorizacion-sin-precio", "FM-CPF-04: EntregarPreciosTasas no devuelve precios: CalcularMontoNumeroCuota responde ERROR_VALORCUOTA sin IdInterno; el aporte queda sin valorizar", entrada(), [sol(1)],
         colaboradores=precios(precio=None))
    caso("valorizacion-sin-precio-dos-series", "FM-CPF-04 D-NOR-9: sin precios y DOS series: dos resultados con IdInterno nulo => toMap con clave repetida: IllegalStateException", entrada(),
         [sol(1), sol(2, IdSerieFondo="FM002B")], colaboradores=precios(precio=None))
    caso("serie-desconocida-valorizada", "FM-CPF-04: la solicitud es de la serie 'XXXX' (no existe): CalcularMontoNumeroCuota responde ERROR_VALORCUOTA CON IdInterno (cuotas y monto nulos) y CPF lo toma "
         "por valido; la fila no sale (instrumento nulo)", entrada(), [sol(1, IdSerieFondo="XXXX")])
    caso("aporte-fondo-en-dolares", "FM-CPF-04: aporte de 10 cuotas de FM003U (fondo USD, 2 decimales): MontoMonedaSistema desde CalcularParidad (id*4+1 = 9: 7.200.000)", entrada(),
         [sol(1, IdSerieFondo="FM003U", IdMoneda="USD", IdMonedaSistema="CLP")], colaboradores=conjunto(precios(precio=8), paridad(final=900, cuota=uf(2, 3.85, 39000, sistema=7200000))))
    caso("rescate-fondo-en-dolares", "FM-CPF-04/12: rescate de 4 cuotas de FM003U: MontoMonedaSistema desde CalcularParidad (id -2: -9 = 2.880.000; al valorizarlo de nuevo como rescate en transito con el id del saldo -1: -5 = 2.881.000)", entrada(),
         [rescate(1, IdSerieFondo="FM003U", IdMoneda="USD", NumeroCuotas=4)],
         colaboradores=conjunto(precios(precio=8), paridad(final=900, cuota=uf(-2, 3.85, 39000, sistema=2880000) + uf(-1, 3.85, 39000, sistema=2881000))))
    caso("moneda-fondo-sin-formato", "FM-CPF-04: fondo USD sin formato de moneda USD (se borra): el formateo de CalcularMontoNumeroCuota falla y se observa la salida", entrada(),
         [sol(1, IdSerieFondo="FM003U", IdMoneda="USD")], seed=['DELETE FROM "BSCL.Plataforma.BD::PLA.MonedaFormato" WHERE "IdMoneda" = \'USD\';'],
         colaboradores=conjunto(precios(precio=8), paridad(final=900, cuota=uf(2, 3.85, 39000, sistema=7200000))))


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def rescates_totales():
    # ---- FM-CPF-05 / CPF-C04
    caso("rescate-total-con-custodia-y-aporte", "FM-CPF-05/09 CPF-C04: custodia 100 + aporte en transito 20 + solicitud 'T' del portafolio/instrumento: CantidadTransSolicitudesRescate = 100 + 20 = 120 "
         "y se valoriza con la regla de transito (MontoTransSolicitudesRescate)", entrada(), [sol(1, NumeroCuotas=20), sol(2, CalculoPorMontoCuota="T", TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM",
         NumeroCuotas=7)], colaboradores=custodias(cus()))
    caso("rescate-total-solo-custodia", "FM-CPF-05/09: custodia 100 y solicitud 'T' (nada en transito): CantidadTransSolicitudesRescate = 100", entrada(),
         [rescate(1, CalculoPorMontoCuota="T", NumeroCuotas=7)], colaboradores=custodias(cus()))
    caso("rescate-total-sin-saldo", "FM-CPF-05: la solicitud 'T' sola no crea saldo (solo marca): salida vacia", entrada(), [rescate(1, CalculoPorMontoCuota="T", NumeroCuotas=7)])
    caso("rescate-total-otro-portafolio", "FM-CPF-05: la 'T' es del portafolio 501 y la custodia del 500: la clave portafolio_instrumento no coincide: sin rescate total", entrada(),
         [rescate(1, CalculoPorMontoCuota="T", IdPortafolio=501)], colaboradores=custodias(cus()))
    caso("rescate-total-otro-instrumento", "FM-CPF-05: la 'T' es del instrumento 901 y la custodia del 900: sin rescate total", entrada(),
         [rescate(1, CalculoPorMontoCuota="T", IdSerieFondo="FM002B")], colaboradores=custodias(cus()))
    caso("rescate-total-aplica-a-todos-los-regimenes", "FM-CPF-05: la clave es solo portafolio_instrumento: marca los DOS regimenes (GEN y BF) de la custodia", entrada(),
         [rescate(1, CalculoPorMontoCuota="T")], colaboradores=custodias(cus(), cus(regimen="BF", cantidad=40, rt="Beneficio Fiscal")))
    caso("rescate-total-dos-solicitudes-t", "FM-CPF-05: dos solicitudes 'T' del mismo portafolio/instrumento (Contador 2): una sola clave", entrada(),
         [rescate(1, CalculoPorMontoCuota="T"), rescate(2, CalculoPorMontoCuota="T", IdCliente=100)], colaboradores=custodias(cus()))
    caso("rescate-total-estado-cerrado", "FM-CPF-05: la 'T' esta CER (no ING/PEN): no cuenta", entrada(), [rescate(1, CalculoPorMontoCuota="T", Estado="CER")], colaboradores=custodias(cus()))
    caso("rescate-total-apo-tambien-cuenta", "FM-CPF-05: el SQL de totales acepta APO y RES: un APO con 'T' tambien marca", entrada(), [sol(1, CalculoPorMontoCuota="T")], colaboradores=custodias(cus()))
    caso("rescate-total-filtro-automatico-sin-bf", "FM-CPF-05 QD-F3A-02: filtro de automaticas 'S': la 'T' automatica NO cuenta y el SQL de totales no exime a BF (la de BF tampoco)", entrada(),
         [rescate(1, CalculoPorMontoCuota="T", IngresoAutomatico="S"), rescate(2, CalculoPorMontoCuota="T", IngresoAutomatico="S", IdRegimenTributario="BF", IdSerieFondo="FM002B")],
         colaboradores=custodias(cus(), cus("FM002B", regimen="BF", rt="Beneficio Fiscal")), seed=fparam("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", "S"))
    caso("rescate-total-filtro-automatico-no-automatica", "FM-CPF-05: filtro 'S' y la 'T' NO automatica: cuenta", entrada(), [rescate(1, CalculoPorMontoCuota="T", IngresoAutomatico="N")],
         colaboradores=custodias(cus()), seed=fparam("WS_SOLICITUDES_MUESTRA_SOLO_SOLICITUDES_NO_AUTOMATICAS", "S"))
    caso("rescate-total-filtros-de-entrada", "FM-CPF-05: los filtros de la entrada tambien rigen los totales (p_IdCliente 101): la 'T' del cliente 100 no cuenta", entrada(p_IdCliente=101),
         [rescate(1, CalculoPorMontoCuota="T", IdCliente=100)], colaboradores=custodias(cus(cliente=101, portafolio=502, codigo="PF502", socio=1101)))


def agrupacion_y_custodia():
    # ---- FM-CPF-06 / 07 / CPF-C03
    caso("solicitud-y-custodia-misma-clave", "FM-CPF-06/07/08 CPF-C03: aporte en transito y custodia del mismo cliente/portafolio/instrumento/regimen: UN saldo con los datos de la custodia (IdBoveda 'DCV', "
         "Relacion 'REL1') y las cantidades en transito", entrada(), [sol(1, NumeroCuotas=20)], colaboradores=custodias(cus(boveda="DCV", relacion="REL1")))
    caso("merge-dos-solicitudes-mismo-saldo", "FM-CPF-07 CPF-C03 D-F3A-05: dos filas del SQL (aporte 'C' 10 cuotas y aporte 'M' con 6 cuotas) del mismo saldo: se suman SOLO cantidades y montos de aporte y "
         "rescate sobre la primera; MontoMonedaSistemaAporte de la segunda no se suma", entrada(),
         [sol(1), sol(2, CalculoPorMontoCuota="M", Monto=9000, NumeroCuotas=6, MontoMonedaSistema=8500)])
    caso("merge-sin-valorizar", "FM-CPF-07 D-F3A-05: dos filas del SQL del mismo saldo SIN cuotas (montos 1100 aporte y 2300 aporte de otro tipo de movimiento, rescate 700): se suman los cuatro campos "
         "(CantidadMontoAporte 3400, CantidadMontoRescate 700) sin llamar a nadie", entrada(),
         [solm(1, 1100), solm(2, 2300, IdTipoMovimientoFondoMutuo="APOTRASP"), solm(3, 700, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM")])
    caso("merge-tres-solicitudes", "FM-CPF-07 D-F3A-05: tres filas del SQL del mismo saldo (C, M, y rescate)", entrada(),
         [sol(1), sol(2, CalculoPorMontoCuota="M", Monto=9000, NumeroCuotas=6), rescate(3, NumeroCuotas=2)])
    caso("custodia-la-ultima-gana", "FM-CPF-07: dos custodias de la misma clave (bovedas COR y DCV): la ultima reemplaza a la primera", entrada(),
         colaboradores=custodias(cus(boveda="COR", cantidad=100), cus(boveda="DCV", cantidad=60, relacion="R2")))
    caso("custodia-y-transito-de-saldos-distintos", "FM-CPF-07: custodia de FM001A y transito de FM002B: dos saldos (uno sin solicitudes, otro sin custodia)", entrada(),
         [sol(1, IdSerieFondo="FM002B")], colaboradores=custodias(cus()))
    caso("regimenes-distintos-dos-saldos", "FM-CPF-07: misma serie en dos regimenes (GEN y BF): dos saldos", entrada(),
         [sol(1, IdRegimenTributario="BF")], colaboradores=custodias(cus(), cus(regimen="BF", cantidad=40, rt="Beneficio Fiscal")))
    caso("clientes-distintos-dos-saldos", "FM-CPF-07: mismo instrumento de dos clientes: dos saldos", entrada(),
         colaboradores=custodias(cus(), cus(cliente=101, portafolio=502, codigo="PF502", socio=1101, doc="22222222-2", razon="Cliente Ficticio Dos", matriz=9, adc="S", cantidad=30)))
    caso("custodia-instrumento-sin-serie", "FM-CPF-08 CPF-C09: la custodia es del instrumento 999 que no tiene serie: el saldo se omite sin mensaje; el otro sale", entrada(),
         colaboradores=custodias(cus(), custodia(instrumento=999, nemo="XX999")))
    caso("dos-series-un-instrumento", "FM-CPF-08 CPF-C09 D-NOR-9: dos series con el mismo IdInstrumento (900): List2Hash lanza 'key field is not unique'", entrada(),
         colaboradores=custodias(cus()), seed=clonar_serie("FM007F", "16", serie={"IdInstrumento": 900}))
    caso("custodia-tabla-nula", "FM-CPF-06: la custodia responde sin p_out_tbl (nulo): NullPointerException al recorrerla", entrada(), colaboradores={"ConsultarCustodiaPortafolioFFMM": {"defecto": {"p_out_tbl": None}}})
    caso("custodia-falla", "FM-CPF-06: el calculo de custodia lanza una excepcion: se propaga tal cual (Exception con su texto)", entrada(),
         colaboradores={"ConsultarCustodiaPortafolioFFMM": {"respuestas": [{"error": "falla simulada de la custodia"}]}})
    caso("custodia-cantidad-nula", "FM-CPF-06/11: la custodia trae CantidadCustodia nula: se copia nula y la valorizacion falla con NullPointerException", entrada(),
         colaboradores=custodias(cus(cantidad=None)))
    caso("custodia-cantidad-garantia-nula", "FM-CPF-06/12: CantidadGarantia nula: el filtro de garantia compara con nulo => NullPointerException", entrada(), colaboradores=custodias(cus(garantia=None)))
    caso("custodia-instrumento-nulo", "FM-CPF-07/08: IdInstrumento nulo en la custodia: la clave lleva 'null' y la lista de instrumentos de la consulta de series lleva un nulo: el saldo se omite", entrada(),
         colaboradores=custodias(cus(), custodia(instrumento=None, nemo="XX000")))
    caso("custodia-sin-regimen", "FM-CPF-07: IdRegimenFondoMutuo nulo en la custodia: la clave lleva 'null'", entrada(), colaboradores=custodias(cus(regimen=None)))
    caso("custodia-cliente-nulo", "FM-CPF-07: IdCliente nulo: la clave lleva 'null_...' y los datos salen nulos", entrada(), colaboradores=custodias(cus(cliente=None)))


def salida_preliminar():
    # ---- FM-CPF-08 / CPF-C01
    caso("campos-de-la-serie", "FM-CPF-08 D-F3A-06: FM002B (fondo AGF2, 2 decimales, RestringeRescate 'S', documento 76000002-2 = el predeterminado, no el otro): Nemotecnico = IdSerieFondo, "
         "NombreAdministradora = IdEmisor, IdMonedaPrincipal = IdMonedaFondo = IdMoneda = moneda del fondo", entrada(), colaboradores=custodias(cus("FM002B")))
    caso("fondo-en-dolares-extranjero", "FM-CPF-08: FM003U (fondo USD extranjero 'S', tipo FMX, emisor EXT1 sin documento): IdMoneda USD, DocumentoIdentidadAdministradora nulo", entrada(),
         colaboradores=conjunto(custodias(cus("FM003U")), precios(precio=8), paridad(final=900, cuota=uf(1, 3.85, 39000, sistema=7200000))))
    caso("emisor-sin-documento-predeterminado", "FM-CPF-08: emisor AGF3 cuyo unico documento no es el predeterminado: DocumentoIdentidadAdministradora nulo", entrada(),
         colaboradores=custodias(cus()), seed=[upd("BSCL.FondoMutuo.BD::FMM.Fondo", {"IdEmisor": "AGF3"}, '"IdFondo" = \'10\'')])
    caso("sin-custodia-datos-de-la-solicitud", "FM-CPF-08: sin custodia: IdBoveda 'COR', Relacion '', cantidades de custodia 0, PPPCustodia 0 y los INGPrecioPromedio 0 (valores por defecto)", entrada(),
         [sol(1)])
    caso("ppp-custodia-nulo", "FM-CPF-08: PPPCustodia nulo en la custodia: sale 0", entrada(), colaboradores=custodias(cus(ppp=None)))
    caso("ing-precios-nulos", "FM-CPF-08: los tres INGPrecioPromedio nulos de la custodia se copian nulos", entrada(), colaboradores=custodias(cus(ing_ppc=None, ing_monto=None, ing_moneda=None)))
    caso("serie-sin-fondo", "FM-CPF-08: serie cuyo fondo no existe (LEFT JOIN): moneda y emisor nulos; la valorizacion de la custodia falla con NullPointerException", entrada(),
         colaboradores=custodias(custodia(instrumento=906, nemo="FM007F")), seed=clonar_serie("FM007F", "99", fondo=False, serie={"IdInstrumento": 906}))
    caso("instrumento-sin-tipo", "FM-CPF-08: el instrumento 907 no esta en INS.Instrumento (LEFT JOIN): tipo y clasificacion nulos", entrada(),
         colaboradores=custodias(custodia(instrumento=907, nemo="FM008G")), seed=clonar_serie("FM008G", "17", serie={"IdInstrumento": 907}))
    caso("solicitud-sin-serie-instrumento-ajeno", "FM-CPF-08: la solicitud es de una serie de otra empresa (SerieFondo.IdEmpresa 2): el join por empresa deja el instrumento nulo y el saldo se omite",
         entrada(), [sol(1, IdSerieFondo="FM007F")], seed=clonar_serie("FM007F", "98", serie={"IdInstrumento": 908, "IdEmpresa": 2}))
    caso("custodia-datos-de-cliente", "FM-CPF-08: con custodia los datos de cliente, portafolio, producto, regimen, ADC y matriz salen de la custodia (no de la solicitud)", entrada(),
         [sol(1, IdPortafolio=501)], colaboradores=custodias(cus(portafolio=501, codigo="APV-B", producto="Producto APV tres", adc="S", matriz=7, socio=5555, doc="99999999-9", razon="Razon de la custodia")))


def mlt():
    # ---- FM-CPF-10 / CPF-C06 (only FM001A has a regime in the base: GEN, RescateConMandato 'N'; every other series has none until the case adds one)
    reg = lambda serie, r, estado="ACT": reg_serie(serie, r, estado)  # noqa: E731
    caso("mlt-no-buscado", "FM-CPF-10 CPF-C06: p_BuscaMLT 'N' (defecto): MLT 'N' aunque el regimen acepte mandato (18Q)", entrada(), colaboradores=custodias(cus("FM002B")), seed=[reg("FM002B", "18Q")])
    caso("mlt-s-rescate-con-mandato", "FM-CPF-10 CPF-C06: BuscaMLT 'S' y un regimen ACT con RescateConMandato 'S' (18Q): MLT 'S'", entrada(p_BuscaMLT="S"),
         colaboradores=custodias(cus("FM002B")), seed=[reg("FM002B", "18Q")])
    caso("mlt-s-acepta-mandato-reinversion", "FM-CPF-10: BuscaMLT 'S' y AceptaMandatoEnReinversion 'S' (18T): MLT 'S'", entrada(p_BuscaMLT="S"), colaboradores=custodias(cus("FM005D")),
         seed=[reg("FM005D", "18T")])
    caso("mlt-s-sin-mandato", "FM-CPF-10: BuscaMLT 'S' con un regimen sin mandato (57BIS): MLT 'N'", entrada(p_BuscaMLT="S"), colaboradores=custodias(cus("FM006E")), seed=[reg("FM006E", "57BIS")])
    caso("mlt-s-sin-regimenes", "FM-CPF-10 D-F3A-08: BuscaMLT 'S' y la serie FM004C sin ningun regimen: MLT queda NULO (ni 'S' ni 'N')", entrada(p_BuscaMLT="S"), colaboradores=custodias(cus("FM004C")))
    caso("mlt-s-solo-el-primer-regimen", "FM-CPF-10 D-F3A-08: FM001A con dos regimenes ACT, el primero (GEN, sin mandato) y el segundo (18Q con mandato): se toma el PRIMERO del SQL (sin ORDER BY): 'N'",
         entrada(p_BuscaMLT="S"), colaboradores=custodias(cus()), seed=[reg("FM001A", "18Q")])
    caso("mlt-s-primer-regimen-con-mandato", "FM-CPF-10 D-F3A-08: FM002B con dos regimenes ACT, el primero con mandato (18Q) y el segundo sin (57BIS): se toma el PRIMERO: 'S'",
         entrada(p_BuscaMLT="S"), colaboradores=custodias(cus("FM002B")), seed=[reg("FM002B", "18Q"), reg("FM002B", "57BIS")])
    caso("mlt-s-regimen-inactivo", "FM-CPF-10: el regimen con mandato (18Q) esta INA y no se considera; el unico ACT (57BIS) no tiene mandato: 'N'", entrada(p_BuscaMLT="S"),
         colaboradores=custodias(cus("FM002B")), seed=[reg("FM002B", "18Q", "INA"), reg("FM002B", "57BIS")])
    caso("mlt-s-solo-regimenes-inactivos", "FM-CPF-10: todos los regimenes de la serie estan INA: no hay registro y MLT queda nulo", entrada(p_BuscaMLT="S"),
         colaboradores=custodias(cus("FM002B")), seed=[reg("FM002B", "18Q", "INA")])
    caso("mlt-minuscula", "FM-CPF-10: BuscaMLT 's' no es 'S': MLT 'N' (equals)", entrada(p_BuscaMLT="s"), colaboradores=custodias(cus("FM002B")), seed=[reg("FM002B", "18Q")])
    caso("mlt-nulo", "FM-CPF-10: BuscaMLT nulo explicito: MLT 'N'", entrada(p_BuscaMLT=None), colaboradores=custodias(cus("FM002B")), seed=[reg("FM002B", "18Q")])
    caso("mlt-s-solo-ese-saldo", "FM-CPF-10: dos saldos, uno con mandato (FM002B+18Q) y otro sin (FM001A, GEN): MLT 'S' y 'N' respectivamente", entrada(p_BuscaMLT="S"),
         colaboradores=custodias(cus(), cus("FM002B")), seed=[reg("FM002B", "18Q")])
    caso("mlt-s-dos-saldos-misma-serie", "FM-CPF-10: dos saldos (regimenes GEN y BF) de la misma serie FM001A con BuscaMLT 'S': ambos toman el primer regimen de la SERIE (GEN) => 'N'",
         entrada(p_BuscaMLT="S"), colaboradores=custodias(cus(), cus(regimen="BF", cantidad=40, rt="Beneficio Fiscal")), seed=[reg("FM001A", "18Q")])


def valorizacion_custodia_garantia_transito():
    # ---- FM-CPF-11 / CPF-C01
    caso("custodia-valorizada", "FM-CPF-11 CPF-C01: 100 cuotas x 1500,1234: Monto 150012 (formato CLP), ValorCuota 1500,1234, FechaValorCuota 2026-03-02 (de la fuente del precio), MontoMonedaSistema "
         "recalculado al final", entrada(), colaboradores=custodias(cus()))
    caso("custodia-fecha-de-la-fuente", "FM-CPF-11: la fuente del precio trae fecha 2026-02-27: FechaValorCuota sale con esa fecha", entrada(),
         colaboradores=conjunto(custodias(cus()), precios(fuente="2026-02-27 00:00:00.000")))
    caso("custodia-sin-precio", "FM-CPF-11: sin precio: la custodia queda sin valorizar (FechaValorCuota '0001-01-01', Monto 0, ValorCuota 0); el resultado de error sin IdInterno cabe una vez en el mapa",
         entrada(), colaboradores=conjunto(custodias(cus()), precios(precio=None)))
    caso("custodia-dos-series-sin-precio", "FM-CPF-11 D-NOR-9: sin precio y dos series en custodia: dos resultados con IdInterno nulo => IllegalStateException", entrada(),
         colaboradores=conjunto(custodias(cus(), cus("FM002B")), precios(precio=None)))
    caso("custodia-cantidad-cero", "FM-CPF-11: CantidadCustodia 0: se valoriza igual (cero cuotas): rama de CalcularMontoNumeroCuota con cero cuotas", entrada(), colaboradores=custodias(cus(cantidad=0)))
    caso("custodia-cantidad-fraccionaria", "FM-CPF-11: 33,3333 cuotas x 1500,1234 = 50003,9... redondeado por el formato CLP", entrada(), colaboradores=custodias(cus(cantidad=33.3333)))
    caso("custodia-cantidad-negativa", "FM-CPF-11: CantidadCustodia -10 (no se valida): monto negativo", entrada(), colaboradores=custodias(cus(cantidad=-10)))
    caso("custodia-cantidad-grande", "FM-CPF-11: 99999999999,5 cuotas (11 enteros): el producto con DECIMAL128 y el formato CLP", entrada(), colaboradores=custodias(cus(cantidad=99999999999.5)))
    caso("custodia-cantidad-minima", "FM-CPF-11: 0,00000001 cuotas: el monto redondea a 0", entrada(), colaboradores=custodias(cus(cantidad=0.00000001)))
    caso("custodia-precio-cero", "FM-CPF-11: precio 0 (BigDecimal.ZERO): el calculo lo salta (continue): ValorCuota 0 y Monto 0", entrada(), colaboradores=conjunto(custodias(cus()), precios(precio=0)))
    caso("custodia-precio-cero-con-escala", "FM-CPF-11: precio 0.0 (escala 1, distinto de BigDecimal.ZERO): el calculo NO lo salta (equals con escala); se observa el resultado", entrada(),
         colaboradores=conjunto(custodias(cus()), precios(precio=0.0)))
    # ---- FM-CPF-12 / CPF-C05
    caso("garantia-distinta-de-cero", "FM-CPF-12 CPF-C05 D-F3A-03: CantidadGarantia 5: se pide a CalcularMontoNumeroCuota con IdInterno*-2 y se busca con IdInterno: MontoGarantia SIEMPRE 0; "
         "MontoGarantiaMonedaSistema = 5 x ValorCuotaMonedaSistema (formato)", entrada(), colaboradores=conjunto(custodias(cus(garantia=5)), paridad(final=1500.25)))
    caso("garantia-cero-no-se-valoriza", "FM-CPF-12: CantidadGarantia 0: no se pide valorizacion (la lista va vacia)", entrada(), colaboradores=custodias(cus(garantia=0)))
    caso("garantia-fraccionaria-y-decimal-cero", "FM-CPF-12: garantia 0,00000001 (distinta de cero por compareTo): se valoriza", entrada(), colaboradores=custodias(cus(garantia=0.00000001)))
    caso("garantia-negativa", "FM-CPF-12: CantidadGarantia -2 (distinta de cero: el filtro es != 0 y no > 0): se valoriza y MontoGarantiaMonedaSistema sale negativo", entrada(),
         colaboradores=conjunto(custodias(cus(garantia=-2)), paridad(final=1500.25)))
    caso("garantia-dos-saldos-solo-uno", "FM-CPF-12: dos custodias, solo una con garantia: solo esa se pide (ids negativos pares)", entrada(),
         colaboradores=custodias(cus(garantia=3), cus("FM002B", garantia=0)))
    caso("garantia-fondo-en-dolares", "FM-CPF-12 D-F3A-03: garantia de un fondo USD: la peticion lleva IdInterno -2 y la paridad (id*4-1...) tambien; el resultado no se usa (MontoGarantia 0)", entrada(),
         colaboradores=conjunto(custodias(cus("FM003U", garantia=2)), precios(precio=8), paridad(final=900, cuota=uf(1, 3.85, 39000, sistema=7200000) + uf(-2, 3.85, 39000, sistema=14400))))
    # transit rescate valued as aporte (D-F3A-05)
    caso("transito-rescate-valorizado", "FM-CPF-12 CPF-C05 D-F3A-05: rescate por cuotas 8,5 en transito: CantidadTransSolicitudesRescate 8,5 se valoriza con CalcularMontoNumeroCuota (id -1, tipo APO): "
         "MontoTransSolicitudesRescate = MontoMonedaSistema del resultado", entrada(), [rescate(1, NumeroCuotas=8.5)])
    caso("transito-rescate-con-custodia", "FM-CPF-12: rescate en transito + custodia: el rescate en transito se valoriza aparte de la custodia", entrada(), [rescate(1, NumeroCuotas=8.5)],
         colaboradores=custodias(cus()))
    caso("transito-rescate-sin-precio", "FM-CPF-12: sin precio: MontoTransSolicitudesRescate queda con lo que dejo la valorizacion de solicitudes (que tampoco valoro)", entrada(),
         [rescate(1, NumeroCuotas=8.5, CalculoPorMontoCuota="M", Monto=5000)], colaboradores=precios(precio=None))
    caso("transito-rescate-dos-saldos", "FM-CPF-12: dos saldos con rescate en transito: dos peticiones con ids -1 y -2 (orden del HashMap)", entrada(),
         [rescate(1, NumeroCuotas=8.5), rescate(2, NumeroCuotas=2, IdSerieFondo="FM002B")])


def valor_cuota_moneda_sistema():
    # ---- FM-CPF-13 / CPF-C07
    cal = fin_de_semana(56) + [feriado("2026-03-09", 56)]
    caso("dia-siguiente-s-viernes", "FM-CPF-13 CPF-C07 QD-F3A-04: PLA.CalculaValorCuotaDiaSiguiente 'S' y FechaValorCuota viernes 2026-03-06 (fines de semana inhabiles y lunes 03-09 feriado): "
         "la paridad se calcula con el dia habil siguiente (martes 2026-03-10)", entrada(), colaboradores=conjunto(custodias(cus()), precios(fuente="2026-03-06 00:00:00.000")),
         seed=config("PLA.CalculaValorCuotaDiaSiguiente", "S") + cal)
    caso("dia-siguiente-s-habil", "FM-CPF-13: 'S' y FechaValorCuota martes 2026-03-03: la paridad usa miercoles 2026-03-04", entrada(),
         colaboradores=conjunto(custodias(cus()), precios(fuente="2026-03-03 00:00:00.000")), seed=config("PLA.CalculaValorCuotaDiaSiguiente", "S") + cal)
    caso("dia-siguiente-n", "FM-CPF-13 CPF-C07: parametro 'N': la paridad usa la fecha de consulta (2026-03-02); ObtenerDiaSiguiente se ejecuta igual (ConsultarEmpresa se llama)", entrada(),
         colaboradores=conjunto(custodias(cus()), precios(fuente="2026-03-06 00:00:00.000")), seed=cal)
    caso("dia-siguiente-minuscula", "FM-CPF-13: parametro 's' no es 'S' (equals): fecha de consulta", entrada(), colaboradores=conjunto(custodias(cus()), precios(fuente="2026-03-06 00:00:00.000")),
         seed=config("PLA.CalculaValorCuotaDiaSiguiente", "s") + cal)
    caso("dia-siguiente-s-dos-fechas", "FM-CPF-13: 'S' con dos fechas de valor cuota distintas (2026-03-06 y 2026-03-03): una fecha siguiente por cada una (toMap por fecha)", entrada(),
         colaboradores=conjunto(custodias(cus(), cus("FM002B")), precios(fuente="2026-03-06 00:00:00.000", por_id={2: (1500.1234, "2026-03-03 00:00:00.000")}, k=8)),
         seed=config("PLA.CalculaValorCuotaDiaSiguiente", "S") + cal)
    caso("dia-siguiente-s-sin-valorizar", "FM-CPF-13: 'S' y custodia sin valorizar (FechaValorCuota '0001-01-01'): el dia siguiente de esa fecha por defecto", entrada(),
         colaboradores=conjunto(custodias(cus()), precios(precio=None)), seed=config("PLA.CalculaValorCuotaDiaSiguiente", "S") + cal)
    caso("dia-siguiente-s-pais-sin-feriados", "FM-CPF-13: 'S' sin calendario (ni fines de semana): el dia calendario siguiente", entrada(),
         colaboradores=conjunto(custodias(cus()), precios(fuente="2026-03-06 00:00:00.000")), seed=config("PLA.CalculaValorCuotaDiaSiguiente", "S"))
    caso("empresa-falla-aunque-no-use-dia-siguiente", "FM-CPF-13 D-F3A-07: con el parametro 'N' el legacy calcula igual los dias siguientes y consulta la empresa: si ConsultarEmpresa falla, falla todo",
         entrada(), colaboradores=conjunto(custodias(cus()), {"ConsultarEmpresa": {"respuestas": [{"error": "empresa no disponible"}]}}))
    caso("empresa-sin-pais", "FM-CPF-13: ConsultarEmpresa responde sin IdPais (nulo): el pais de las fechas es nulo y se observa el resultado", entrada(),
         colaboradores=conjunto(custodias(cus()), {"ConsultarEmpresa": {"defecto": {"IdEmpresa": 1, "Codigo": "BCS", "Nombre": "Corredora Ficticia", "IdEstado": "ACT", "IdSocioDeNegocio": 10}}}))
    # CalcularParidad answers
    caso("paridad-valor-cuota-moneda-sistema", "FM-CPF-13 CPF-C01: CalcularParidad responde MontoDestino 1500,25 por cuota: ValorCuotaMonedaSistema 1500,25 y MontoMonedaSistema = 10 x 1500,25 = 15002,5 "
         "con el formato CLP (0 decimales, 'R')", entrada(), colaboradores=conjunto(custodias(cus(cantidad=10)), paridad(final=1500.25)))
    caso("paridad-redondeo-mitad", "FM-CPF-13: 10 x 1500,35 = 15003,5: el formato redondea la mitad (se observa la regla de MonedaFormato)", entrada(),
         colaboradores=conjunto(custodias(cus(cantidad=10)), paridad(final=1500.35)))
    caso("paridad-sin-respuesta", "FM-CPF-13: CalcularParidad no responde: ValorCuotaMonedaSistema 0 (monto destino no valido) y montos 0", entrada(), colaboradores=conjunto(custodias(cus()), paridad(final=None)))
    caso("paridad-monto-destino-nulo", "FM-CPF-13: la respuesta de la paridad trae MontoDestino nulo: se trata como sin respuesta (0)", entrada(),
         colaboradores=conjunto(custodias(cus()), {"CalcularParidad": {"respuestas": [{"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": [{"IdTrabajo": 2, "IdInterno": 1,
                                                                                                                                                     "FactorOrigenDestino": 1}]}}],
                                                                       "defecto": {"p_out_CalculosSalida": []}}}))
    caso("paridad-falla", "FM-CPF-13: CalcularParidad (la de CPF) lanza una excepcion: se propaga; la primera llamada (CalcularMontoNumeroCuota, IdTrabajo 2) tambien usa el mismo doble", entrada(),
         colaboradores=conjunto(custodias(cus()), {"CalcularParidad": {"respuestas": [{"cuando": {"0.p_CalculosEntrada.0.IdMonedaDestino": "CLP", "0.p_CalculosEntrada.0.IdMonedaOrigen": "CLP"},
                                                                                       "error": "paridad no disponible"},
                                                                                      {"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": []}}],
                                                                       "defecto": {"p_out_CalculosSalida": []}}}))
    caso("paridad-id-repetido", "FM-CPF-13 D-NOR-9: la paridad de CPF responde dos filas con el mismo IdInterno: toMap lanza IllegalStateException (las de CalcularMontoNumeroCuota responden sin repetir)", entrada(),
         colaboradores=conjunto(custodias(cus()), {"CalcularParidad": {"respuestas": [
             {"cuando": {"0.p_CalculosEntrada.0.IdMonedaDestino": "CLP", "0.p_CalculosEntrada.0.IdMonedaOrigen": "CLP"}, "respuesta": {"p_out_CalculosSalida": [
                 {"IdTrabajo": 2, "IdInterno": 1, "MontoDestino": 1}, {"IdTrabajo": 2, "IdInterno": 1, "MontoDestino": 2}]}},
             {"cuando": {"0.p_IdTrabajo": 2}, "respuesta": {"p_out_CalculosSalida": []}}], "defecto": {"p_out_CalculosSalida": []}}}))
    caso("moneda-sistema-sin-formato", "FM-CPF-13: PLA.MonedaSistema 'EUR' sin formato de moneda: el formateo del final falla y se devuelve el producto sin formato (catch)", entrada(),
         colaboradores=conjunto(custodias(cus(cantidad=10)), paridad(final={1: 1500.25})), seed=config("PLA.MonedaSistema", "EUR"))
    caso("moneda-sistema-trunca", "FM-CPF-13: el formato de la moneda sistema (CLP) TRUNCA en vez de redondear (MontoTrucaRedondea 'T'): 10 x 1500,25 = 15002,5 => 15002 (y la valorizacion de CalcularMontoNumeroCuota tambien trunca)",
         entrada(), colaboradores=conjunto(custodias(cus(cantidad=10)), paridad(final=1500.25)),
         seed=["UPDATE \"BSCL.Plataforma.BD::PLA.MonedaFormato\" SET \"MontoTrucaRedondea\" = 'T' WHERE \"IdMoneda\" = 'CLP';"])
    caso("fondo-en-dolares-paridad", "FM-CPF-13: custodia de FM003U (USD) con paridad USD->CLP 900 por cuota: ValorCuotaMonedaSistema 900", entrada(),
         colaboradores=conjunto(custodias(cus("FM003U", cantidad=10)), precios(precio=8), paridad(final=900, cuota=uf(1, 3.85, 39000, sistema=72000))))
    caso("garantia-con-moneda-sistema", "FM-CPF-13: MontoGarantiaMonedaSistema = garantia x ValorCuotaMonedaSistema (formato): 4 x 1500,25 = 6001", entrada(),
         colaboradores=conjunto(custodias(cus(garantia=4)), paridad(final=1500.25)))


def orden_y_concurrencia():
    # ---- FM-CPF-14 / CPF-C11
    seis = [cus(s, cantidad=10 * (i + 1)) for i, s in enumerate(SERIES_ID)]
    caso("orden-seis-saldos", "FM-CPF-14 CPF-C11 D-F3A-04: seis saldos de seis instrumentos (cliente 100, portafolio 500, GEN): la salida sigue el orden de iteracion de un HashMap de claves "
         "'cliente_portafolio_instrumento_regimen' (estable para las mismas claves), y IdInterno interno 1..6 en ese orden", entrada(), colaboradores=conjunto(custodias(*seis), paridad(final=1, n=6)))
    seis_b = [cus(s, cantidad=5 * (i + 1), cliente=100 + (i % 3), portafolio=500 + (i % 3), regimen=("GEN", "BF", "18Q")[i % 3]) for i, s in enumerate(SERIES_ID)]
    caso("orden-claves-mixtas", "FM-CPF-14: seis saldos de claves mezcladas (clientes 100/101/102, portafolios 500..502, regimenes GEN/BF/18Q): otro orden del HashMap", entrada(),
         colaboradores=conjunto(custodias(*seis_b), paridad(final=1, n=6)))
    trece = [cus(s, cantidad=3 * (i + 1) + 7 * j, cliente=100 + j) for j in (0, 1) for i, s in enumerate(SERIES_ID)] + [cus("FM001A", cantidad=99, cliente=102, portafolio=503, codigo="PF503")]
    caso("orden-trece-saldos-redimensiona", "FM-CPF-14 D-F3A-04: 13 saldos (6 series x clientes 100 y 101, mas el cliente 102): el HashMap supera su umbral (12) y se redimensiona: otro orden de iteracion, y "
         "IdInterno interno 1..13 en ese orden", entrada(), colaboradores=conjunto(custodias(*trece), precios(k=14), paridad(final=1, n=13)))
    caso("orden-insercion-solicitudes-primero", "FM-CPF-14: los saldos de solicitudes se insertan antes que los de custodia: para la misma coleccion de claves el HashMap depende solo de las claves", entrada(),
         [solm(1, 1100, IdSerieFondo="FM005D"), solm(2, 1200, IdSerieFondo="FM006E")], colaboradores=custodias(cus(), cus("FM002B")))
    caso("solo-un-saldo-ids", "FM-CPF-14 D-F3A-04: un saldo con aporte y rescate en transito: IdInterno de solicitudes 2 y 3 (aporte y rescate: dos filas del SQL) y de custodia 1", entrada(),
         [sol(1, NumeroCuotas=10, CalculoPorMontoCuota="C"), rescate(2, NumeroCuotas=3, CalculoPorMontoCuota="M", Monto=500)], colaboradores=custodias(cus()))
    caso("dos-solicitudes-ids-solicitudes", "FM-CPF-14 D-F3A-04: dos saldos con transito: IdInterno 2 y 3 asignados en un parallelStream().forEach (el reparto puede variar: ver determinism-cpf.txt)",
         entrada(), [sol(1, IdSerieFondo="FM001A"), sol(2, IdSerieFondo="FM002B")])
    caso("cuatro-solicitudes-ids", "FM-CPF-14 D-F3A-04: cuatro saldos con transito: la asignacion de IdInterno 2..5 es concurrente (se espera inestable; se documenta)", entrada(),
         [sol(1, IdSerieFondo="FM001A"), sol(2, IdSerieFondo="FM002B"), sol(3, IdSerieFondo="FM005D"), sol(4, IdSerieFondo="FM006E")])


def casos_adicionales():
    """Added AFTER the first canary of the goldens (they pin what two mutations of the legacy left unnoticed); appended at the end so the numbers (and the goldens) of the others do not move."""
    precio_solo_del_id = {"EntregarPreciosTasas": {"respuestas": [{"cuando": {"0.p_DatosInstrumentos.0.IdRegistro": -2}, "respuesta": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": [
        {"IdRegistro": -2, "IdClasificacionTipoInstrumento": "FM", "Precio": 8, "IdInstrumento": 900, "FechaFuente": FUENTE}]}}],
        "defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": []}}}
    caso("transito-rescate-sin-precio-en-el-saldo", "FM-CPF-04/12: rescate de 4 cuotas de FM003U (USD): la primera valorizacion (id -2) tiene precio y deja CantidadMontoRescate = MontoMonedaSistema en pesos (2.880.000); "
         "la del saldo (id -1) y la de la custodia no tienen precio (ERROR sin IdInterno) => MontoTransSolicitudesRescate conserva el monto en pesos de la primera (no el monto en dolares)", entrada(),
         [rescate(1, IdSerieFondo="FM003U", IdMoneda="USD", NumeroCuotas=4)], colaboradores=conjunto(precio_solo_del_id, paridad(final=900, cuota=uf(-2, 3.85, 39000, sistema=2880000))))
    cal = fin_de_semana(56) + [feriado("2026-03-09", 56)]
    caso("dia-siguiente-s-fondo-en-dolares", "FM-CPF-13: con 'S' y un fondo en dolares la paridad lleva IdMonedaOrigen USD e IdMonedaDestino CLP (la moneda sistema) y la fecha del dia habil siguiente (viernes 03-06 => martes 03-10)",
         entrada(), colaboradores=conjunto(custodias(cus("FM003U", cantidad=10)), precios(precio=8, fuente="2026-03-06 00:00:00.000"), paridad(final=900, cuota=uf(1, 3.85, 39000, sistema=72000))),
         seed=config("PLA.CalculaValorCuotaDiaSiguiente", "S") + cal)
    caso("rescate-total-custodia-negativa", "FM-CPF-09/12: rescate total con custodia -10 (no se valida): CantidadTransSolicitudesRescate = -10 + 0 (distinta de cero: el filtro es != 0, no > 0) y SE valoriza como rescate en transito (-15001)",
         entrada(), [rescate(1, CalculoPorMontoCuota="T", NumeroCuotas=7)], colaboradores=custodias(cus(cantidad=-10)))
