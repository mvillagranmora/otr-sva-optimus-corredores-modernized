"""FMM-DET cases (ConsultarDetalleSolicitud + its delegate ConsultarSolicitudAporteRescateUI + CalcularMontoNumeroCuota, all REAL): FM-DET-01 contract and validation,
FM-DET-02 delegation and valuation of ING/PEN, FM-DET-03 APV retention, FM-DET-04 output mapping. Ids: RULES_FONDOMUTUO_F3_A.md 8.6, D-F3A-24/25 (section 9),
matrix DET-C01..C04 (section 11). Inputs only: expected.json is recorded from the legacy by record_det.sh.

Universe (seed/base-det.sql on top of seed/base-fmm.sql): client 100/101/200; portfolios 500 (general, matrix NOAPV 8), 501 (APV, code APV-B, matrix APV 7: product 3, ADC S),
502 (APV, code APV-A), 503 (client 101), 504 (client 200, APV). Series FM001A (CLP, 4 decimals of quota, quota value 1500.1234 in FMM.ValorCuota but the valuation reads the
price of the EntregarPreciosTasas double), FM002U (USD fund, rates 950 / 940). Clock 2026-03-02. Default solicitud: APO, ING, by amount 150000 CLP, FM001A, portfolio 500.
"""
from cases_det_lib import *  # noqa: F401,F403
from cases3a_lib import clonar_serie, moneda_formato  # noqa: E402
from cases3b_rows import fila as _fila, fecha, RawSql  # noqa: E402

ING = RawSql("TIMESTAMP '2026-03-02 09:30:15'")
PRECIO = 1500.1234


def sol(i, **ov):
    """A FMM.SolicitudAporteRescate row with EVERY column (type defaults) and the DET defaults; `ov` overrides."""
    v = dict(IdSolicitudAporteRescate=i, IdTipoCanalIngreso="OPT", IdCliente=100, IdPortafolio=500, TipoSolicitud="APO", IdSerieFondo="FM001A",
             IdTipoMovimientoFondoMutuo="APONORM", CalculoPorMontoCuota="M", FechaEmision=fecha("2026-03-02"), FechaAceptacion=fecha("2026-03-02"),
             FechaValorCuota=fecha("2026-03-03"), FechaCertificacion=fecha("2026-03-04"), FechaLiquidacion=fecha("2026-03-05"), Monto=150000, NumeroCuotas=0,
             ValorCuota=0, IdMoneda="CLP", Estado="ING", EstadoLiquidacion="", IdMedioDePago="TRF", Glosa="glosa", IdEmpresa=1, IdUsuario=7, IdModulo="FMM",
             OrigenOperacion="M", SolicitudBatch="N", IdRegimenTributario="GEN", IdMonedaSistema="CLP", IdMonedaPago="CLP", IdEmpleadoEjecutivo=20,
             IndicadorMLT="N", Overnight="N", Switch="N", FechaIngresoRegistro=ING)
    v.update(ov)
    return _fila(SOLICITUD, **v)


def precio_de(i, precio=PRECIO):
    return precios((i, precio))


def valorizada(i, precio=PRECIO, monto_uf=3.85, valor_uf=39000, extra=None):
    """Scripted doubles for a CLP-fund solicitud `i` valued by CalcularMontoNumeroCuota: its price and the three system-currency / UF answers."""
    return conjunto(precios((i, precio)), paridad(t2=uf(i, monto_uf, valor_uf, sistema=None) + (extra or [])))


APV = dict(IdPortafolio=501, IdRegimenTributario="APV-B", IdTipoMovimientoFondoMutuo="RESNORM", TipoSolicitud="RES", IdMedioDePago="TRF")


def generar():
    entrada_y_validacion()
    delegacion_y_valorizacion()
    retencion()
    mapeo_de_salida()


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def entrada_y_validacion():
    # ---- FM-DET-01 / DET-C01, C04
    caso("id-nulo", "FM-DET-01 DET-C01: p_SolicitudesAporteRescate nulo explicito => contador 1, NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE (id 0), resultado vacio; hace rollback; "
         "no valida requisitos basicos ni perfil", entrada(None), [sol(1)])
    caso("id-ausente", "FM-DET-01: sin la clave p_SolicitudesAporteRescate (el pojo la deja nula) => igual que nulo", entrada("ABSENT"), [sol(1)])
    caso("id-cero", "FM-DET-01 DET-C01: id 0 => contador 1 y el mismo mensaje", entrada(0), [sol(1)])
    caso("id-cero-sin-diccionario", "FM-DET-01: sin la entrada del diccionario el texto del mensaje es '<codigo> sin traduccion' (kit)", entrada(0),
         ['DELETE FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = \'NO_SE_INGRESARON_SOLICITUDES_APORTE_RESCATE\';'])
    caso("id-cero-empresa-nula", "FM-DET-01: id 0 con p_IdEmpresa nulo => el mensaje se arma con la empresa nula (se observa si lanza o responde)", entrada(0, p_IdEmpresa=None))
    caso("id-texto-numerico", "FM-DET-01: id '5' como texto => Gson lo coacciona a Long 5: devuelve la solicitud 5", entrada("5"), [sol(5, Estado="CER"), sol(6, Estado="CER")])
    caso("id-no-numerico", "FM-DET-01: id 'abc' => JsonSyntaxException (HTTP 500)", entrada("abc"), [sol(1)])
    caso("id-decimal", "FM-DET-01: id 1.5 => JsonSyntaxException (no es un Long)", entrada(1.5), [sol(1)])
    caso("id-negativo", "FM-DET-01: id -1 no es 0 ni nulo: consulta y no encuentra => resultado vacio, contador 0, sin mensaje", entrada(-1), [sol(1)])
    caso("id-inexistente", "FM-DET-01/04 DET-C04: id 999 que no existe => P_OUT_RESULTADO vacio, contador 0, sin mensaje (la valorizacion corre con 0 series)", entrada(999), [sol(1)])
    caso("sin-requisitos-basicos", "FM-DET-01: usuario 999 y modulo 'XXX' inexistentes NO se validan (no hay RevisarRequisitosBasicos): responde igual", entrada(1, p_IdUsuario=999, p_IdModulo="XXX"),
         [sol(1, Estado="CER")])
    caso("usuario-y-modulo-nulos", "FM-DET-01: p_IdUsuario y p_IdModulo nulos se ignoran (solo se copian a la delegada, que no los lee)", entrada(1, p_IdUsuario=None, p_IdModulo=None),
         [sol(1, Estado="CER")])
    caso("empresa-nula-con-id", "FM-DET-02 D-NOR-9: p_IdEmpresa nulo con un id valido => la delegada consulta con IdEmpresa nulo (sin filas) y la valorizacion lee la configuracion de 'null': NullPointerException",
         entrada(1, p_IdEmpresa=None), [sol(1, Estado="CER")])
    caso("empresa-sin-configuracion", "FM-DET-02: empresa 99 (sin configuracion) => sin filas, pero CalcularMontoNumeroCuota lee UsaDolarObservado de la empresa 99: NullPointerException",
         entrada(1, p_IdEmpresa=99), [sol(1, Estado="CER")])
    caso("sin-parametro-usa-dolar-observado", "FM-DET-02: falta FONDOMUTUO/UsaDolarObservado (lo lee CalcularMontoNumeroCuota SIEMPRE, aunque no haya series que valorizar) => Exception 'Parametro ... no encontrado'",
         entrada(1), [sol(1, Estado="CER")], seed=['DELETE FROM %s WHERE "NombreTipoParametro" = \'UsaDolarObservado\';' % PARAM])
    caso("sin-moneda-sistema", "FM-DET-02: falta PLATAFORMA/PLA.MonedaSistema => Exception 'Parametro ... no encontrado' (aun con una solicitud sin valorizar)", entrada(1),
         [sol(1, Estado="CER")], seed=config("PLA.MonedaSistema", None))


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def delegacion_y_valorizacion():
    tabla = dict(Monto=123456, NumeroCuotas=7, ValorCuota="1400.5")
    # ---- FM-DET-02: only ING and PEN are valued; the others are returned as the view gives them
    for est in ("CER", "LIQ", "ANU", "PRO", "ing", "IN ", ""):
        caso("estado-%s-sin-valorizar" % (est.replace(" ", "-espacio") or "vacio"), "FM-DET-02 DET-C02: Estado %r no es ING ni PEN: NO se valoriza (Monto 123456, cuotas 7, "
             "valor cuota 1400.5 de la tabla)" % est, entrada(1), [sol(1, Estado=est, **tabla)], colaboradores=valorizada(1))
    for est in ("ING", "PEN"):
        caso("estado-%s-valorizada" % est.lower(), "FM-DET-02 DET-C02: Estado %s: se valoriza con CalcularMontoNumeroCuota (precio 1500.1234): Monto, ValorCuota y NumeroCuotas se "
             "sobrescriben (la tabla trae Monto 123456, cuotas 7)" % est, entrada(1), [sol(1, Estado=est, **tabla)], colaboradores=valorizada(1))
    caso("ing-por-monto", "FM-DET-02: ING por monto ('M') 150000 CLP: cuotas = 150000 / 1500.1234 con 4 decimales (HALF_DOWN), Monto con el formato de CLP", entrada(1), [sol(1)], colaboradores=valorizada(1))
    caso("ing-por-cuotas", "FM-DET-02: ING por cuotas ('C') 10,5 cuotas: Monto = cuotas * precio con el formato de CLP; NumeroCuotas con los decimales de la serie",
         entrada(1), [sol(1, CalculoPorMontoCuota="C", NumeroCuotas="10.5", Monto=0, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM")], colaboradores=valorizada(1))
    caso("ing-rescate-total", "FM-DET-02: ING 'T' (rescate total) con cuotas 25,5: ni 'M' ni 'C' exacto: entra por el else de CalcularMontoNumeroCuota (monto por cuota)", entrada(1),
         [sol(1, CalculoPorMontoCuota="T", NumeroCuotas="25.5", Monto=0, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM")], colaboradores=valorizada(1))
    caso("ing-por-porcentaje", "FM-DET-02: ING 'P' (porcentaje) con monto 0 y cuotas 0 => rama de division del monto entre el valor cuota", entrada(1),
         [sol(1, CalculoPorMontoCuota="P", NumeroCuotas=0, Monto=500000)], colaboradores=valorizada(1))
    caso("ing-precio-cero", "FM-DET-02: el precio de EntregarPreciosTasas es 0 => ValorCuota 0: se observa el resultado (error del colaborador o excepcion aritmetica)", entrada(1),
         [sol(1)], colaboradores=valorizada(1, precio=0))
    caso("ing-sin-precio-del-colaborador", "FM-DET-02: EntregarPreciosTasas no devuelve precios (lista vacia) => hashVCTP vacio: ERROR_VALORCUOTA sin IdInterno; la solicitud queda sin valorizar",
         entrada(1), [sol(1, **tabla)], colaboradores=conjunto(precios(), paridad()))
    caso("ing-precio-de-otro-registro", "FM-DET-02: el precio viene con IdRegistro 99 (no el de la solicitud 1): no se encuentra el precio de la solicitud", entrada(1),
         [sol(1, **tabla)], colaboradores=conjunto(precios((99, PRECIO)), paridad()))
    caso("ing-serie-inexistente-pisa-con-nulos", "FM-DET-02 D-F3A-24: serie 'XXXX' que no existe => CalcularMontoNumeroCuota responde ERROR_VALORCUOTA CON IdInterno y la delegada lo toma por "
         "valido: Monto, ValorCuota y NumeroCuotas quedan NULOS (se pisan los de la vista)", entrada(1), [sol(1, IdSerieFondo="XXXX", **tabla)], colaboradores=valorizada(1))
    caso("ing-moneda-distinta-diferido", "FM-DET-02: IdMoneda 'USD' con fondo en CLP => diferido: CalcularParidad(1) devuelve el monto destino (142.500.000) y FactorCambioLocal 950",
         entrada(1), [sol(1, IdMoneda="USD", Monto=150000)],
         colaboradores=conjunto(precios((1, PRECIO)), paridad(t1=[{"IdInterno": 1, "FactorCambioLocal": 950, "MontoDestino": 142500000}], t2=uf(1, 3.85, 39000, sistema=142500000))))
    caso("ing-sin-respuesta-de-paridad", "FM-DET-02: diferido pero CalcularParidad no responde con el IdInterno => la paridad queda nula (monto destino 0)", entrada(1),
         [sol(1, IdMoneda="USD", Monto=150000)], colaboradores=conjunto(precios((1, PRECIO)), paridad()))
    caso("ing-moneda-eur-por-paridad", "FM-DET-02: IdMoneda 'EUR' con fondo en CLP (ni dolar ni moneda del fondo): el monto destino sale de CalcularParidad(1) "
         "(FactorCambioLocal 1010, MontoDestino 151.500.000) y no del tipo de cambio de la serie", entrada(1), [sol(1, IdMoneda="EUR", Monto=150000)], seed=[moneda_formato("EUR", 2)],
         colaboradores=conjunto(precios((1, PRECIO)), paridad(t1=[{"IdInterno": 1, "FactorCambioLocal": 1010, "MontoDestino": 151500000}], t2=uf(1, 3.85, 39000, sistema=151500000))))
    caso("ing-moneda-eur-sin-paridad", "FM-DET-02: igual con CalcularParidad(1) sin respuesta para el IdInterno: la paridad es nula, el monto destino 0 y las cuotas 0", entrada(1),
         [sol(1, IdMoneda="EUR", Monto=150000)], seed=[moneda_formato("EUR", 2)], colaboradores=conjunto(precios((1, PRECIO)), paridad()))
    caso("ing-fondo-en-dolares", "FM-DET-02: fondo en USD (FM002U, 2 decimales de cuota) y solicitud en CLP: usa el tipo de cambio observado de aporte 950 de la serie (UsaDolarObservado N)",
         entrada(1), [sol(1, IdSerieFondo="FM002U", IdMoneda="CLP", Monto=1500000)], seed=clonar_serie("FM002U", "20", fondo={"IdMoneda": "USD"}, serie={"IdInstrumento": 901, "DecimalesCuota": 2,
                                                                                                                              "DecimalesValorCuota": 2}, tipo_cambio=(950, 940)),
         colaboradores=conjunto(precios((1, 31.5, 901)), paridad(t2=uf(1, 40, 39000, sistema=1500000))))
    caso("ing-fondo-en-dolares-rescate", "FM-DET-02: igual con un RESCATE: usa el tipo de cambio de rescate 940", entrada(1),
         [sol(1, IdSerieFondo="FM002U", IdMoneda="CLP", Monto=1500000, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM")],
         seed=clonar_serie("FM002U", "20", fondo={"IdMoneda": "USD"}, serie={"IdInstrumento": 901, "DecimalesCuota": 2, "DecimalesValorCuota": 2}, tipo_cambio=(950, 940)),
         colaboradores=conjunto(precios((1, 31.5, 901)), paridad(t2=uf(1, 40, 39000, sistema=1500000))))
    caso("ing-usa-dolar-observado-s", "FM-DET-02: UsaDolarObservado 'S' => no se usa el tipo de cambio de la serie: va por la paridad", entrada(1),
         [sol(1, IdMoneda="USD", Monto=150000)], seed=['UPDATE %s SET "ValorParametro" = \'S\' WHERE "NombreTipoParametro" = \'UsaDolarObservado\';' % PARAM],
         colaboradores=conjunto(precios((1, PRECIO)), paridad(t1=[{"IdInterno": 1, "FactorCambioLocal": 951, "MontoDestino": 142650000}], t2=uf(1, 3.85, 39000, sistema=142650000))))
    caso("ing-sin-serie-en-valor-cuota", "FM-DET-02: la serie existe pero el precio no esta en la tabla de la serie (se usa el del colaborador): no se lee FMM.ValorCuota", entrada(1),
         [sol(1)], seed=['DELETE FROM %s;' % VALORCUOTA], colaboradores=valorizada(1))
    caso("dos-solicitudes-solo-la-pedida", "FM-DET-02 FM-DET-04: hay 3 solicitudes en la tabla, se pide la 2: sale solo esa (la valorizacion usa solo las ING/PEN de la consulta)", entrada(2),
         [sol(1, Estado="CER"), sol(2), sol(3, IdCliente=101, IdPortafolio=503)], colaboradores=valorizada(2))
    caso("otra-empresa-no-sale", "FM-DET-02: la solicitud pedida pertenece a la empresa 2: la consulta filtra por IdEmpresa=1 y no la encuentra", entrada(1),
         [sol(1, IdEmpresa=2, Estado="CER")])
    caso("valorizacion-se-ejecuta-aunque-no-haya-ing", "FM-DET-02 D-F3A-24: con solo solicitudes no valorizables (CER) CalcularMontoNumeroCuota se EJECUTA igual (lee sus parametros: los casos sin-parametro-* fallan) pero "
         "sin series no llega a EntregarPreciosTasas ni a CalcularParidad: 0 llamadas a colaboradores en el dump", entrada(1), [sol(1, Estado="CER")], colaboradores=valorizada(1))
    caso("valorizacion-llama-a-los-colaboradores", "FM-DET-02 D-F3A-24: una ING valorizada llama a EntregarPreciosTasas una vez y a CalcularParidad (IdTrabajo 2) una vez (2 llamadas en el dump), con los argumentos "
         "armados por la delegada (fecha valor cuota 2026-03-03, UF, CLP)", entrada(1), [sol(1)], colaboradores=valorizada(1))
    caso("ing-monto-moneda-sistema-aporte", "FM-DET-02: ING aporte SIN Spot (IdOperacionSpot 0): MontoMonedaSistema (777 en la tabla) se sobrescribe con el calculado (150000), junto con CostoMonedaLocal, "
         "ParidadFechaCertificacion, CostoUF (3,85) y ValorUFFechaInversion (39000)", entrada(1), [sol(1, MontoMonedaSistema=777)], colaboradores=valorizada(1))
    caso("ing-monto-moneda-sistema-aporte-con-spot", "FM-DET-02: ING aporte CON Spot (IdOperacionSpot 300): MontoMonedaSistema NO se sobrescribe (queda 777); lo demas si", entrada(1),
         [sol(1, MontoMonedaSistema=777, IdOperacionSpot=300)], colaboradores=valorizada(1))
    caso("ing-monto-moneda-sistema-rescate", "FM-DET-02: ING rescate: MontoMonedaSistema se sobrescribe (aunque tenga Spot)", entrada(1),
         [sol(1, MontoMonedaSistema=777, IdOperacionSpot=300, TipoSolicitud="RES", IdTipoMovimientoFondoMutuo="RESNORM", CalculoPorMontoCuota="C", NumeroCuotas=10, Monto=0)],
         colaboradores=valorizada(1))
    caso("ing-con-id-de-solicitud-grande", "FM-DET-02: id 123456789012 (mas de 32 bits) y su IdInterno*4+k de la paridad: se conserva el Long", entrada(123456789012),
         [sol(123456789012)], colaboradores=valorizada(123456789012))


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def retencion():
    pct = lambda v: config("APV.PorcentajeRetencion", v)  # noqa: E731
    base = dict(APV, Estado="CER", Monto=1000000)
    # ---- FM-DET-03 DET-C03: Monto * porcentaje / 100, only RESNORM + APV-AN/APV-B + no traspaso + medio != CIN
    caso("retencion-apv-b-15", "FM-DET-03 DET-C03: rescate normal APV-B, Monto 1.000.000, porcentaje 15 => MontoRetencion 150000 (formato CLP)", entrada(1), [sol(1, **base)], seed=pct("15"))
    caso("retencion-apv-an", "FM-DET-03: regimen APV-AN tambien retiene", entrada(1), [sol(1, **dict(base, IdRegimenTributario="APV-AN"))], seed=pct("15"))
    caso("retencion-apv-a-no", "FM-DET-03: regimen APV-A NO retiene (MontoRetencion no se escribe)", entrada(1), [sol(1, **dict(base, IdRegimenTributario="APV-A"))], seed=pct("15"))
    caso("retencion-regimen-vacio-usa-codigo-portafolio", "FM-DET-03: IdRegimenTributario '' en un portafolio APV usa el CodigoPortafolio ('APV-B') como regimen APV: retiene", entrada(1),
         [sol(1, **dict(base, IdRegimenTributario=""))], seed=pct("15"))
    caso("retencion-portafolio-apv-a-sin-regimen", "FM-DET-03: regimen '' y portafolio APV-A => regimen APV 'APV-A': NO retiene", entrada(1),
         [sol(1, **dict(base, IdPortafolio=502, IdRegimenTributario=""))], seed=pct("15"))
    caso("retencion-portafolio-no-apv", "FM-DET-03: portafolio no APV (500) con regimen 'APV-B' en la solicitud: IdRegimenTributarioAPV queda '' => NO retiene", entrada(1),
         [sol(1, **dict(base, IdPortafolio=500))], seed=pct("15"))
    caso("retencion-medio-cuenta-inversion", "FM-DET-03: IdMedioDePago 'CIN' (cuenta de inversion) NO retiene", entrada(1), [sol(1, **dict(base, IdMedioDePago="CIN"))], seed=pct("15"))
    caso("retencion-medio-vacio", "FM-DET-03: IdMedioDePago '' (distinto de CIN) retiene", entrada(1), [sol(1, **dict(base, IdMedioDePago=""))], seed=pct("15"))
    caso("retencion-medio-minuscula", "FM-DET-03: 'cin' en minusculas no es CIN (equals): retiene", entrada(1), [sol(1, **dict(base, IdMedioDePago="cin"))], seed=pct("15"))
    caso("retencion-con-traspaso", "FM-DET-03: la solicitud tiene una orden de traspaso (IdTraspaso no nulo) => NO retiene", entrada(1), [sol(1, **base)],
         seed=pct("15") + ['INSERT INTO "BSCL.APV.BD::TRA.OrdenTraspaso" ("IdSolicitudAporteRescate", "IdTraspaso") VALUES (1, 700);'])
    caso("retencion-tipo-movimiento-restrasp", "FM-DET-03: IdTipoMovimientoFondoMutuo RESTRASP (no RESNORM) NO retiene", entrada(1),
         [sol(1, **dict(base, IdTipoMovimientoFondoMutuo="RESTRASP"))], seed=pct("15"))
    caso("retencion-tipo-movimiento-minuscula", "FM-DET-03: 'resnorm' en minusculas no es RESNORM: NO retiene", entrada(1),
         [sol(1, **dict(base, IdTipoMovimientoFondoMutuo="resnorm"))], seed=pct("15"))
    caso("retencion-aporte-no", "FM-DET-03: un aporte (APONORM) APV-B no retiene", entrada(1), [sol(1, **dict(base, IdTipoMovimientoFondoMutuo="APONORM", TipoSolicitud="APO"))], seed=pct("15"))
    # parameter
    caso("retencion-sin-parametro", "FM-DET-03: falta APV.PorcentajeRetencion => el try/catch lo deja en 0: MontoRetencion 0 (formateado)", entrada(1), [sol(1, **base)])
    caso("retencion-porcentaje-cero", "FM-DET-03: porcentaje 0 => MontoRetencion 0", entrada(1), [sol(1, **base)], seed=pct("0"))
    caso("retencion-porcentaje-cien", "FM-DET-03: porcentaje 100 => MontoRetencion = Monto", entrada(1), [sol(1, **base)], seed=pct("100"))
    caso("retencion-porcentaje-decimal-es-cero", "FM-DET-03: porcentaje '12.5' => getParametroConfiguracionLong lanza NumberFormatException y el catch lo deja en 0 (no 12,5)", entrada(1),
         [sol(1, **base)], seed=pct("12.5"))
    caso("retencion-porcentaje-no-numerico", "FM-DET-03: porcentaje 'abc' => 0", entrada(1), [sol(1, **base)], seed=pct("abc"))
    caso("retencion-porcentaje-negativo", "FM-DET-03: porcentaje -5 => retencion negativa (no se valida)", entrada(1), [sol(1, **base)], seed=pct("-5"))
    caso("retencion-porcentaje-mayor-que-cien", "FM-DET-03: porcentaje 250 => retencion mayor que el monto (no se valida)", entrada(1), [sol(1, **base)], seed=pct("250"))
    # amount and format
    caso("retencion-redondeo-clp", "FM-DET-03: Monto 1.000.055 y porcentaje 15 => 150008,25 formateado con 0 decimales de CLP", entrada(1), [sol(1, **dict(base, Monto=1000055))], seed=pct("15"))
    caso("retencion-redondeo-clp-mitad", "FM-DET-03: Monto 1.000.010 y porcentaje 15 => 150001,5 (redondeo de MonedaFormato en la mitad)", entrada(1), [sol(1, **dict(base, Monto=1000010))], seed=pct("15"))
    caso("retencion-moneda-usd", "FM-DET-03: solicitud en USD (2 decimales): Monto 1000.55 y 15 => 150.0825 formateado a 2 decimales", entrada(1), [sol(1, **dict(base, IdMoneda="USD", Monto="1000.55"))],
         seed=pct("15"))
    caso("retencion-moneda-sin-formato", "FM-DET-03: IdMoneda 'EUR' sin fila en PLA.MonedaFormato => el formateo falla y se deja el valor SIN formato (catch)", entrada(1),
         [sol(1, **dict(base, IdMoneda="EUR", Monto="1000.55"))], seed=pct("15"))
    caso("retencion-monto-cero", "FM-DET-03: Monto 0 => retencion 0", entrada(1), [sol(1, **dict(base, Monto=0))], seed=pct("15"))
    # with valuation: ING (Monto from the valuation) and the null Monto trap
    caso("retencion-sobre-monto-valorizado", "FM-DET-02/03: ING APV-B por cuotas 100: se valoriza primero (Monto = 100 * 1500.1234 = 150012) y la retencion se calcula sobre ese Monto", entrada(1),
         [sol(1, **dict(APV, Estado="ING", CalculoPorMontoCuota="C", NumeroCuotas=100, Monto=0))], seed=pct("15"), colaboradores=valorizada(1))
    caso("retencion-monto-nulo-por-error-de-valorizacion", "FM-DET-02/03 D-F3A-24: ING APV-B con serie inexistente: la valorizacion deja Monto NULO y la retencion lanza NullPointerException (solicitud.Monto.multiply)",
         entrada(1), [sol(1, **dict(APV, Estado="ING", IdSerieFondo="XXXX"))], seed=pct("15"), colaboradores=valorizada(1))
    caso("retencion-dos-solicitudes-por-traspaso", "FM-DET-03: UNA solicitud con DOS ordenes de traspaso (la vista la repite): una fila con IdTraspaso... ninguna retiene (todas tienen traspaso)", entrada(1),
         [sol(1, **base)], seed=pct("15") + ['INSERT INTO "BSCL.APV.BD::TRA.OrdenTraspaso" ("IdSolicitudAporteRescate", "IdTraspaso") VALUES (1, 700);',
                                              'INSERT INTO "BSCL.APV.BD::TRA.OrdenTraspaso" ("IdSolicitudAporteRescate", "IdTraspaso") VALUES (1, 701);'])


# ---------------------------------------------------------------------------------------------------------------------------------------------------
def mapeo_de_salida():
    # ---- FM-DET-04: copy of the fields, in the order of the output pojo, with the default Gson (nulls omitted, java.sql.Date as 'MMM d, yyyy', Timestamp by the JVM format)
    caso("campos-completos", "FM-DET-04 D-F3A-25: una solicitud CER con todos los campos de la salida: ids, fechas (Date 'MMM d, yyyy', Timestamp con el formato de Gson), nombres de estado, fondo, "
         "serie, cliente; nada se transforma", entrada(1),
         [sol(1, Estado="CER", IdOperacionSpot=0, FechaIngresoRegistro=RawSql("TIMESTAMP '2026-03-02 21:45:07'"), FechaLiquidacion=fecha("2026-03-05"))])
    caso("campos-nulos-se-omiten", "FM-DET-04 D-F3A-25: new Gson() plano: los campos nulos NO se escriben (sin Spot, sin omnibus, sin agrupador)", entrada(1), [sol(1, Estado="CER")])
    caso("spot-completo", "FM-DET-04: solicitud con IdOperacionSpot 300 y su operacion Spot (origen 300, estado CNF, confirmacion PRO, tipo CPUSD, liquidacion 2026-03-06): NombreEstadoSpot, "
        "NombreEstadoConfirmacionSpot, FechaLiquidacionSpot, IdTipoOperacionSpot y NombreTipoOperacionSpot", entrada(1), [sol(1, Estado="CER", IdOperacionSpot=300)],
         seed=['INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot" ("IdOperacionSpotOrigen", "Estado", "IdOperacionSpot", "EstadoConfirmacion", "IdTipoOperacion", "FechaLiquidacion", '
               '"MontoMonedaSecundaria") VALUES (300, \'CNF\', 301, \'PRO\', \'CPUSD\', DATE \'2026-03-06\', 123456);'])
    caso("spot-estado-mod-no-se-une", "FM-DET-04: la operacion Spot en estado MOD no se une (Spot.Estado != 'MOD'): nombres vacios, IdTipoOperacionSpot ''", entrada(1),
         [sol(1, Estado="CER", IdOperacionSpot=300)],
         seed=['INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot" ("IdOperacionSpotOrigen", "Estado", "IdOperacionSpot", "EstadoConfirmacion", "IdTipoOperacion", "FechaLiquidacion", '
               '"MontoMonedaSecundaria") VALUES (300, \'MOD\', 301, \'PRO\', \'CPUSD\', DATE \'2026-03-06\', 1);'])
    caso("spot-sin-operacion", "FM-DET-04: IdOperacionSpot 300 sin fila en SPT.OperacionSpot => NombreEstadoSpot '' y NombreEstadoConfirmacionSpot '' (COALESCE), tipo ''", entrada(1),
         [sol(1, Estado="CER", IdOperacionSpot=300)])
    caso("spot-dos-operaciones-duplican", "FM-DET-04: DOS operaciones Spot con el mismo origen (dato sucio) duplican la solicitud: dos elementos en P_OUT_RESULTADO", entrada(1),
         [sol(1, Estado="CER", IdOperacionSpot=300)],
         seed=['INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot" ("IdOperacionSpotOrigen", "Estado", "IdOperacionSpot", "EstadoConfirmacion", "IdTipoOperacion", "FechaLiquidacion", '
               '"MontoMonedaSecundaria") VALUES (300, \'CNF\', 301, \'PRO\', \'CPUSD\', DATE \'2026-03-06\', 1);',
               'INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot" ("IdOperacionSpotOrigen", "Estado", "IdOperacionSpot", "EstadoConfirmacion", "IdTipoOperacion", "FechaLiquidacion", '
               '"MontoMonedaSecundaria") VALUES (300, \'ACT\', 302, \'PRO\', \'VTUSD\', DATE \'2026-03-07\', 2);'])
    caso("duplicada-por-join-ing-falla", "FM-DET-04 D-NOR-9: una ING duplicada por el JOIN (dos operaciones Spot) => Collectors.toMap con clave repetida: IllegalStateException al valorizar",
         entrada(1), [sol(1, Estado="ING", IdOperacionSpot=300)], colaboradores=valorizada(1),
         seed=['INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot" ("IdOperacionSpotOrigen", "Estado", "IdOperacionSpot", "EstadoConfirmacion", "IdTipoOperacion", "FechaLiquidacion", '
               '"MontoMonedaSecundaria") VALUES (300, \'CNF\', 301, \'PRO\', \'CPUSD\', DATE \'2026-03-06\', 1);',
               'INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot" ("IdOperacionSpotOrigen", "Estado", "IdOperacionSpot", "EstadoConfirmacion", "IdTipoOperacion", "FechaLiquidacion", '
               '"MontoMonedaSecundaria") VALUES (300, \'ACT\', 302, \'PRO\', \'VTUSD\', DATE \'2026-03-07\', 2);'])
    caso("omnibus-y-agrupador", "FM-DET-04: FolioOmnibus 9001 => IdOmnibus 77 (OmnibusDetalle); IdAgrupador 55 de la solicitud (reinversion 1N)", entrada(1),
         [sol(1, Estado="CER", FolioOmnibus=9001, IdAgrupador=55)])
    caso("omnibus-inexistente", "FM-DET-04: FolioOmnibus sin fila en OmnibusDetalle => IdOmnibus nulo (se omite)", entrada(1), [sol(1, Estado="CER", FolioOmnibus=1234)])
    caso("cliente-sin-fila-en-la-vista", "FM-DET-04: cliente 777 (portafolio 505 sin cliente en la vista) => DocumentoIdentidad y nombres nulos (LEFT JOIN)", entrada(1),
         [sol(1, Estado="CER", IdPortafolio=505, IdCliente=777)], seed=['INSERT INTO "BSCL.Portafolio.BD::POR.Portafolio" ("IdPortafolio", "CodigoPortafolio", "IdCliente", "IdEstado", "Proposito", '
                                                                      '"DescripcionPortafolio", "IdMatrizAgrupadorAPV") VALUES (505, \'PF505\', 777, \'ACT\', \'PROPGEN\', \'Portafolio sin cliente\', 0);'])
    caso("portafolio-inexistente", "FM-DET-04: IdPortafolio 999 sin fila (LEFT JOIN): DescripcionPortafolio, cliente y matriz nulos; la salida lo omite", entrada(1),
         [sol(1, Estado="CER", IdPortafolio=999)])
    caso("serie-sin-fondo", "FM-DET-04: serie cuyo fondo no existe (IdFondo 98): NombreFondo / NombreFondoReal nulos y EsDiferido por NULL", entrada(1),
         [sol(1, Estado="CER", IdSerieFondo="FM006X")], seed=clonar_serie("FM006X", "98", fondo=False, serie={"IdInstrumento": 904}))
    caso("tipo-movimiento-sin-fila", "FM-DET-04: IdTipoMovimientoFondoMutuo 'ZZZ' sin fila: DescripcionTipoMovimiento nulo", entrada(1), [sol(1, Estado="CER", IdTipoMovimientoFondoMutuo="ZZZ")])
    caso("estado-sin-fila", "FM-DET-04: Estado 'XYZ' sin fila en PLA.Estado: NombreEstado nulo (no se valoriza: no es ING/PEN)", entrada(1), [sol(1, Estado="XYZ")])
    caso("calculo-por-monto-cuota-ui", "FM-DET-04: CalculoMontoCuotaUI (CUOTA / MONTO / TOTAL / '') y CalculoPorMontoCuotaUI (UI_NUMERO_CUOTAS / UI_MONTO / UI_TOTAL / UI_PORCENTAJE) segun "
         "CalculoPorMontoCuota 'C' (el valor de la tabla)", entrada(1), [sol(1, Estado="CER", CalculoPorMontoCuota="C")])
    caso("calculo-por-porcentaje-ui", "FM-DET-04: CalculoPorMontoCuota 'P' => CalculoMontoCuotaUI '' y CalculoPorMontoCuotaUI 'UI_PORCENTAJE'", entrada(1), [sol(1, Estado="CER", CalculoPorMontoCuota="P")])
    caso("calculo-por-total-ui", "FM-DET-04: 'T' => 'TOTAL' y 'UI_TOTAL'", entrada(1), [sol(1, Estado="CER", CalculoPorMontoCuota="T")])
    caso("calculo-por-monto-ui", "FM-DET-04: 'M' => 'MONTO' y 'UI_MONTO'", entrada(1), [sol(1, Estado="CER", CalculoPorMontoCuota="M")])
    caso("regimen-tributario-apv-segun-proposito", "FM-DET-04: IdRegimenTributario y IdRegimenTributarioAPV: el campo que sale en el detalle es IdRegimenTributario (el APV solo se usa para la retencion)", entrada(1),
         [sol(1, Estado="CER", IdPortafolio=501, IdRegimenTributario="APV-B")])
    caso("fechas-de-la-solicitud", "FM-DET-04: fechas Date (emision, certificacion, liquidacion, aceptacion, anulacion) y Timestamp (aceptacion registro) con el formato por defecto de Gson", entrada(1),
         [sol(1, Estado="CER", FechaEmision=fecha("2026-01-05"), FechaCertificacion=fecha("2026-01-07"), FechaLiquidacion=fecha("2026-01-08"), FechaAceptacion=fecha("2026-01-05"),
              FechaAnulacion=fecha("2026-01-09"), FechaAceptacionRegistro=RawSql("TIMESTAMP '2026-01-05 10:11:12'"))])
    caso("fecha-por-defecto-0001", "FM-DET-04: las fechas sin dato (DATE 0001-01-01 de la tabla) salen como 'Jan 1, 1' (formato Gson de java.sql.Date en el ano 1)", entrada(1), [sol(1, Estado="CER")])
    caso("monto-decimales-de-la-tabla", "FM-DET-04: Monto y ValorCuota con 8 decimales de la tabla (no valorizada) salen con su escala", entrada(1),
         [sol(1, Estado="CER", Monto="123456.12345678", ValorCuota="1400.5", NumeroCuotas="88.12345678")])
    caso("determinismo-solicitud-completa", "FM-DET-04: la misma solicitud ING valorizada con todos los joins: la salida es identica en las 20 corridas (parallelStream en el mapeo y en la valorizacion)",
         entrada(10), [sol(10, IdOperacionSpot=300, FolioOmnibus=9001, IdAgrupador=55), sol(11, Estado="CER")], colaboradores=valorizada(10),
         seed=['INSERT INTO "BSCL.Mesa.BD::SPT.OperacionSpot" ("IdOperacionSpotOrigen", "Estado", "IdOperacionSpot", "EstadoConfirmacion", "IdTipoOperacion", "FechaLiquidacion", '
               '"MontoMonedaSecundaria") VALUES (300, \'CNF\', 301, \'PRO\', \'CPUSD\', DATE \'2026-03-06\', 1);'])
