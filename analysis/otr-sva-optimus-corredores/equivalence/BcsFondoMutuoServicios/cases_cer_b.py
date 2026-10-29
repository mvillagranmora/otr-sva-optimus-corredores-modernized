"""FMM-CER cases of front F-B (FM-CER-15..20 and 23): indicators, recalculation of quotas and amounts (the REAL CalcularMontoNumeroCuota), partition and order, GEN_OCSNEG
(OperaSaldoNegativo), effective certification date and the format of the amounts.

F-B has no output of its own: its effect is seen in what the `completo` variant of the case writes (the UPDATE to CRT, formatted) and in the arguments of the first collaborator that
F-C / F-D call (the `parada` variant). See cases_cer_lib / API_ASSUMED.md."""
import cases_cer_lib as L
import cases_cer_caso as C
from cases_cer_lib import aporte, rescate, solo_tabla, config, param, ins, Num  # noqa: F401
from cases_cer_caso import caso, entrada


def ap(id_, pf=500, **c):
    """an aporte that F-C will send to SaldoFlujoCaja (MueveCta S, manual origin)"""
    return aporte(id_, IdPortafolio=pf, CodigoPortafolio="PF%d" % pf, MueveCta="S", **c)


def pr(i, *a, **k):
    return L.precio(i, *a, **k)


def generar():
    # ------------------------------------------------------------------ FM-CER-15 portfolio indicators
    caso("indicadores-sobregiro-S-saca-el-portafolio-del-flujo", ["FM-CER-15", "FM-CER-21", "D-NOR-16"], entrada([3001, 3002, 3003]),
         seed=[ap(3001, 500), ap(3002, 501), ap(3003, 502), L.indicador_portafolio(500, "S"), L.indicador_portafolio(501, "N")], modo="continua")
    caso("indicador-distinto-de-N-tambien-saca-el-portafolio", ["FM-CER-15", "FM-CER-21"], entrada([3001, 3002]),
         seed=[ap(3001, 500), ap(3002, 501), L.indicador_portafolio(500, "s"), L.indicador_portafolio(501, "X")], modo="continua")
    caso("indicadores-duplicados-en-la-vista-lanzan", ["FM-CER-15", "D-NOR-9"], entrada([3001]), seed=[ap(3001, 500), L.indicador_portafolio(500, "S"), L.indicador_portafolio(500, "N")])
    caso("indicadores-de-portafolio-ajeno-no-influyen", ["FM-CER-15"], entrada([3001]), seed=[ap(3001, 500), L.indicador_portafolio(777, "S")], modo="continua")
    caso("indicadores-solo-de-las-solicitudes-vigentes", ["FM-CER-15"], entrada([3001, 3002]),
         seed=[ap(3001, 500), ap(3002, 501, estado="CRT"), L.indicador_portafolio(501, "S"), L.indicador_portafolio(501, "N")], modo="continua")

    # ------------------------------------------------------------------ FM-CER-16 / 17 recalculation of quotas and amounts
    caso("calculo-por-monto-rescate-y-aporte", ["FM-CER-16", "FM-CER-17"], entrada([3001, 3002]), seed=[rescate(3001), aporte(3002, CostoMonedaLocal=2500000, Monto=2500000)], modo="continua")
    caso("calculo-por-cuotas", ["FM-CER-16", "FM-CER-17"], entrada([3001]), seed=[rescate(3001, CalculoPorMontoCuota="C", NumeroCuotas=Num("250.5"), CostoMonedaLocal=0, Monto=0)], modo="continua")
    caso("calculo-por-total", ["FM-CER-16", "FM-CER-17"], entrada([3001]), seed=[rescate(3001, CalculoPorMontoCuota="T", NumeroCuotas=Num("100.25"), CostoMonedaLocal=0, Monto=0)], modo="continua")
    caso("calculo-con-cuotas-y-monto-previos-distintos", ["FM-CER-16", "FM-CER-17"], entrada([3001]),
         seed=[rescate(3001, CalculoPorMontoCuota="M", NumeroCuotas=Num("999.9999"), CostoMonedaLocal=300000, Monto=1, ValorCuota=7, CostoUF=3, ParidadFechaCertificacion=5, MontoPago=2, MontoPagoLocal=3,
                       MontoMonedaSistema=4, ValorUFFechaInversion=6)], modo="continua")
    caso("calculo-serie-con-dos-decimales-de-valor-cuota", ["FM-CER-16"], entrada([3001]), seed=[rescate(3001, IdSerieFondo="FM001B", CostoMonedaLocal=777777)],
         colaboradores=L.precios(pr(3001, 1200.5, nemo="FM001B")), modo="continua")
    caso("calculo-fondo-usd-inversion-clp-diferido", ["FM-CER-16", "FM-CER-17", "FM-CER-23"], entrada([3001, 3002]),
         seed=[aporte(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=5000000, Monto=0),
               rescate(3002, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=4000000, Monto=0)], modo="continua")
    caso("calculo-fondo-clp-inversion-usd", ["FM-CER-16", "FM-CER-17", "FM-CER-23"], entrada([3001]),
         seed=[aporte(3001, IdMonedaFondo="CLP", IdMoneda="USD", IdMonedaPago="CLP", CostoMonedaLocal=2000, Monto=0)], modo="continua")
    caso("calculo-fondo-eur-inversion-clp-por-paridad-y-formato-truncado", ["FM-CER-16", "FM-CER-23"], entrada([3001]),
         seed=[rescate(3001, IdMonedaFondo="EUR", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=1234567, Monto=0, MontoComision=Num("12.34567"), MontoComisionLocal=Num("98.765"))],
         colaboradores=L.precios(pr(3001, 1500.1234, moneda="EUR")), modo="continua")
    caso("calculo-usa-dolar-observado-S", ["FM-CER-16"], entrada([3001]),
         seed=[aporte(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=5000000, Monto=0), param("UsaDolarObservado", "S")],
         modo="continua")
    caso("calculo-precio-cero-por-monto-lanza", ["FM-CER-16", "D-CUO-1"], entrada([3001]), seed=[rescate(3001)], colaboradores=L.precios(pr(3001, 0)))
    caso("calculo-precio-cero-por-cuotas-rechaza", ["FM-CER-16"], entrada([3001, 3002]),
         seed=[rescate(3001, CalculoPorMontoCuota="C", NumeroCuotas=100, CostoMonedaLocal=0), rescate(3002)], colaboradores=L.precios(pr(3001, 0), pr(3002)), modo="continua")
    caso("calculo-todas-las-series-sin-registro-rechaza", ["FM-CER-16"], entrada([3001, 3002]),
         seed=[rescate(3001, IdSerieFondo="FM999Z"), rescate(3002, IdSerieFondo="FM998Z")], colaboradores=L.precios(pr(3001, nemo="FM999Z"), pr(3002, nemo="FM998Z")), modo="continua")
    caso("calculo-una-serie-sin-registro-y-otra-valida-lanza", ["FM-CER-16", "D-CUO-1"], entrada([3001, 3002]),
         seed=[rescate(3001, IdSerieFondo="FM999Z"), rescate(3002)], colaboradores=L.precios(pr(3001, nemo="FM999Z"), pr(3002)))
    caso("calculo-sin-resultado-para-la-solicitud-rechaza-a-todas", ["FM-CER-16", "FM-CER-14"], entrada([3001, 3002]), seed=[rescate(3001), aporte(3002)],
         colaboradores={"EntregarPreciosTasas": {"respuestas": [{"cuando": {"0.p_IdUsuario": None}, "respuesta": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": []}}],
                                                 "defecto": {"P_OUT_ERRORESCONTADOR": 0, "P_OUT_PRECIOSTASAS": [pr(3001), pr(3002)]}}}, modo="continua")
    caso("calculo-un-valido-y-un-fallido-en-el-mismo-lote", ["FM-CER-16", "FM-CER-69"], entrada([3001, 3002, 3003]),
         seed=[rescate(3001), rescate(3002, CalculoPorMontoCuota="C", NumeroCuotas=10, CostoMonedaLocal=0), aporte(3003)], colaboradores=L.precios(pr(3001), pr(3002, 0), pr(3003)), modo="continua")
    caso("calculo-reinversion-con-cuotas-cero", ["FM-CER-16", "D-CUO-2"], entrada([3001]),
         seed=[rescate(3001, IdTipoMovimientoFondoMutuo="RESREINV", CalculoPorMontoCuota="C", NumeroCuotas=0, CostoMonedaLocal=500000)], modo="continua")

    # TieneComisionNNRelacionada = S: the amounts of the database are KEPT (FM-CER-17)
    caso("comision-nn-relacionada-conserva-los-montos-de-la-base", ["FM-CER-17", "D-F3B-12"], entrada([3001]),
         seed=[rescate(3001, Monto=Num("123456.789"), NumeroCuotas=Num("55.5555"), ValorCuota=Num("2222.2222"), CostoMonedaLocal=Num("123400.4"), MontoPago=Num("100000.5"), MontoPagoLocal=Num("100000.6"),
                       MontoMonedaSistema=Num("123456.789"), ParidadFechaCertificacion=Num("1.5"), CostoUF=Num("3.25"), ValorUFFechaInversion=Num("38000.1")),
               solo_tabla(3002, "RES", "ING", IdTipoMovimientoFondoMutuo="RESCNN", IdSolicitudAporteRescateRelacionado=3001)], modo="continua")
    caso("comision-nn-de-otra-solicitud-no-cuenta", ["FM-CER-17"], entrada([3001]),
         seed=[rescate(3001), solo_tabla(3002, "RES", "ING", IdTipoMovimientoFondoMutuo="RESCNN", IdSolicitudAporteRescateRelacionado=3005),
               solo_tabla(3003, "RES", "ING", IdTipoMovimientoFondoMutuo="RESNORM", IdSolicitudAporteRescateRelacionado=3001)], modo="continua")
    caso("comision-nn-dos-hijas-duplica-la-fila", ["FM-CER-17", "D-F3B-12", "D-NOR-9"], entrada([3001]),
         seed=[rescate(3001), solo_tabla(3002, "RES", "ING", IdTipoMovimientoFondoMutuo="RESCNN", IdSolicitudAporteRescateRelacionado=3001),
               solo_tabla(3003, "RES", "ING", IdTipoMovimientoFondoMutuo="RESCNN", IdSolicitudAporteRescateRelacionado=3001)])
    caso("comision-nn-con-monto-nulo-en-la-vista", ["FM-CER-17", "FM-CER-23", "Q-W0B-10"], entrada([3001]),
         seed=[rescate(3001, Monto=None, NumeroCuotas=Num("10"), CostoMonedaLocal=15000), solo_tabla(3002, "RES", "ING", IdTipoMovimientoFondoMutuo="RESCNN", IdSolicitudAporteRescateRelacionado=3001)], modo="continua")
    caso("aporte-reinversion-conserva-costo-uf-y-valor-uf", ["FM-CER-17", "D-F3B-10"], entrada([3001]),
         seed=[aporte(3001, IdTipoMovimientoFondoMutuo="APOREINV", CostoUF=Num("12.5"), ValorUFFechaInversion=Num("39000.25"))], modo="continua")
    caso("aporte-normal-toma-costo-uf-del-calculo", ["FM-CER-17"], entrada([3001]), seed=[aporte(3001, CostoUF=Num("12.5"), ValorUFFechaInversion=Num("39000.25"))], modo="continua")
    caso("aporte-con-spot-conserva-monto-moneda-sistema", ["FM-CER-17"], entrada([3001, 3002]),
         seed=[aporte(3001, IdOperacionSpot=888, MontoMonedaSistema=Num("777000.5")), aporte(3002, IdOperacionSpot=0, MontoMonedaSistema=Num("777000.5"))], modo="continua")
    caso("rescate-con-spot-no-conserva-monto-moneda-sistema", ["FM-CER-17"], entrada([3001]), seed=[rescate(3001, IdOperacionSpot=888, MontoMonedaSistema=Num("777000.5"))], modo="continua")

    # ------------------------------------------------------------------ FM-CER-18 effective certification date
    caso("fecha-efectiva-sistema-si-la-entrada-es-anterior", ["FM-CER-18", "FM-CER-21", "D-FEC-6"], entrada([3001], fecha="2026-02-27"),
         seed=[ap(3001, FechaCertificacion="2026-02-27")], colaboradores=L.precios(pr(3001)), modo="continua")
    caso("fecha-efectiva-entrada-si-es-posterior", ["FM-CER-18", "FM-CER-21"], entrada([3001], fecha="2026-03-05"),
         seed=[ap(3001, FechaCertificacion="2026-03-05")], colaboradores=L.precios(pr(3001)), modo="continua")
    caso("fecha-efectiva-igual-a-la-de-sistema", ["FM-CER-18", "FM-CER-21"], entrada([3001]), seed=[ap(3001)], modo="continua")
    caso("fecha-efectiva-lote-con-fecha-de-sistema-cambiada", ["FM-CER-18", "FM-CER-21"], entrada([3001], fecha="2026-03-05"),
         seed=[ap(3001, FechaCertificacion="2026-03-05"), config("PLA.FechaSistema", "2026-03-10")], colaboradores={**L.precios(pr(3001)), **L.saldo_flujo({"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-10", "Saldo": 999999999})}, modo="continua")

    # ------------------------------------------------------------------ FM-CER-19 partition and order
    caso("particion-aportes-y-rescates-desordenados", ["FM-CER-19"], entrada([3005, 3002, 3004, 3001, 3003]),
         seed=[rescate(3005), aporte(3002), rescate(3004), aporte(3001), rescate(3003)], modo="continua")
    caso("particion-ids-en-orden-descendente-de-entrada", ["FM-CER-19"], entrada([3009, 3008, 3007, 3006, 3005, 3004]),
         seed=[rescate(3009), rescate(3008), aporte(3007), aporte(3006), rescate(3005), aporte(3004)], modo="continua")
    caso("particion-solo-aportes", ["FM-CER-19"], entrada([3003, 3001, 3002]), seed=[aporte(3003), aporte(3001), aporte(3002)], modo="continua")
    caso("particion-solo-rescates", ["FM-CER-19"], entrada([3003, 3001, 3002]), seed=[rescate(3003), rescate(3001), rescate(3002)], modo="continua")
    caso("particion-tipo-ajeno-no-entra-en-ninguna-lista", ["FM-CER-19", "D-F3B-16"], entrada([3003, 3001, 3002]), seed=[rescate(3003), L.sol(3001, "XXX"), aporte(3002)], modo="continua")
    caso("particion-solo-tipo-ajeno", ["FM-CER-19", "D-F3B-16"], entrada([3001]), seed=[L.sol(3001, "XXX")], modo="continua")
    caso("particion-ids-de-muchos-digitos-y-comparador", ["FM-CER-19"], entrada([2147483650, 2147483648, 2147483649, 10]),
         seed=[rescate(2147483650), rescate(2147483648), rescate(2147483649), rescate(10)], modo="continua")
    caso("particion-doce-solicitudes-mixtas-y-rechazadas", ["FM-CER-19", "FM-CER-14", "FM-CER-69"], entrada([3012, 3003, 3011, 3001, 3010, 3002, 3009, 3004, 3008, 3005, 3007, 3006]),
         seed=[rescate(3012), aporte(3003), rescate(3011, estado="CRT"), aporte(3001), rescate(3010), aporte(3002, FechaCertificacion="2026-03-03"), aporte(3009), rescate(3004), rescate(3008),
               aporte(3005, IdTipoMovimientoFondoMutuo="APOOMNI", IdOmnibus=None), rescate(3007), L.sol(3006, "XXX")], modo="continua")
    caso("particion-una-sola-solicitud-aporte", ["FM-CER-19"], entrada([3001]), seed=[aporte(3001)], modo="continua")
    caso("particion-una-solicitud-es-rechazada-por-calculo-y-no-entra", ["FM-CER-16", "FM-CER-19"], entrada([3001, 3002, 3003]),
         seed=[aporte(3001), rescate(3002), aporte(3003, CalculoPorMontoCuota="C", NumeroCuotas=10, CostoMonedaLocal=0)], colaboradores=L.precios(pr(3001), pr(3002), pr(3003, 0)), modo="continua")

    # ------------------------------------------------------------------ FM-CER-20 OperaSaldoNegativo (GEN_OCSNEG, CELLINCECT-763)
    saldo0 = L.saldo_flujo(*[{"IdPortafolio": pf, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 0} for pf in (500, 501, 502, 503, 504, 505, 506)])
    caso("opera-saldo-negativo-S-N-ausente-minuscula-y-otro", ["FM-CER-20", "FM-CER-21"], entrada([3001, 3002, 3003, 3004, 3005]),
         seed=[ap(3001, 500), ap(3002, 501), ap(3003, 502), ap(3004, 503), ap(3005, 504), L.opera_saldo_negativo(500, "S"), L.opera_saldo_negativo(501, "N"), L.opera_saldo_negativo(503, "n"),
               L.opera_saldo_negativo(504, "X")], colaboradores=saldo0, modo="continua")
    caso("opera-saldo-negativo-gana-el-primer-indicador-del-portafolio", ["FM-CER-20", "D-F3B-11"], entrada([3001, 3002]),
         seed=[ap(3001, 505), ap(3002, 506), L.opera_saldo_negativo(505, "S"), L.opera_saldo_negativo(505, "N"), L.opera_saldo_negativo(506, "N"), L.opera_saldo_negativo(506, "S")],
         colaboradores=saldo0, modo="continua")
    caso("opera-saldo-negativo-aporte-pac", ["FM-CER-20", "FM-CER-22"], entrada([3001, 3002]),
         seed=[ap(3001, 500, IdTipoCanalIngreso="PAC"), ap(3002, 501, IdTipoCanalIngreso="PAC"), L.opera_saldo_negativo(500, "S"), L.opera_saldo_negativo(501, "N")], colaboradores=saldo0, modo="continua")
    caso("opera-saldo-negativo-se-consulta-aunque-no-haya-aportes", ["FM-CER-20"], entrada([3001]), seed=[rescate(3001), L.opera_saldo_negativo(500, "S")], modo="continua")
    caso("opera-saldo-negativo-saldo-suficiente-no-rechaza", ["FM-CER-20", "FM-CER-21"], entrada([3001, 3002]), seed=[ap(3001, 500), ap(3002, 500), L.opera_saldo_negativo(500, "N")],
         colaboradores=L.saldo_flujo({"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 5000000}), modo="continua")
    caso("opera-saldo-negativo-dos-aportes-del-mismo-portafolio-rechazan-a-ambos", ["FM-CER-20", "FM-CER-21"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=300000), ap(3002, 500, CostoMonedaLocal=300000), L.opera_saldo_negativo(500, "N")],
         colaboradores=L.saldo_flujo({"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 500000}), modo="continua")

    caso("pares-portafolio-moneda-omitidos-por-listas-independientes", ["FM-CER-21", "FM-CER-20", "D-NOR-16"], entrada([3001, 3002, 3003, 3004]),
         seed=[ap(3001, 500, IdMoneda="CLP"), ap(3002, 501, IdMoneda="USD", CostoMonedaLocal=2000, Monto=0), ap(3003, 500, IdMoneda="USD", CostoMonedaLocal=2000, Monto=0),
               ap(3004, 501, IdMoneda="CLP")],
         colaboradores=L.saldo_flujo({"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 5000000}, {"IdPortafolio": 501, "IdMoneda": "USD", "FechaSaldo": "2026-03-02", "Saldo": 5000000}),
         modo="continua")
    caso("saldo-varias-filas-del-mismo-par-gana-la-ultima", ["FM-CER-21"], entrada([3001]), seed=[ap(3001, 500)],
         colaboradores=L.saldo_flujo({"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 0}, {"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-02", "Saldo": 9000000}),
         modo="continua")
    caso("saldo-de-otra-fecha-no-cuenta", ["FM-CER-18", "FM-CER-21"], entrada([3001]), seed=[ap(3001, 500)],
         colaboradores=L.saldo_flujo({"IdPortafolio": 500, "IdMoneda": "CLP", "FechaSaldo": "2026-03-03", "Saldo": 9000000}), modo="continua")
    caso("pac-y-no-pac-del-mismo-portafolio-dos-llamadas-al-flujo", ["FM-CER-21", "FM-CER-22"], entrada([3001, 3002]),
         seed=[ap(3001, 500), ap(3002, 500, IdTipoCanalIngreso="PAC")], modo="continua")
    caso("calculo-por-monto-con-numero-de-cuotas-nulo-en-la-vista", ["FM-CER-16"], entrada([3001]), seed=[rescate(3001, NumeroCuotas=None)], modo="continua")
    caso("calculo-por-cuotas-con-numero-de-cuotas-nulo-lanza", ["FM-CER-16", "D-CUO-1"], entrada([3001]), seed=[rescate(3001, CalculoPorMontoCuota="C", NumeroCuotas=None)])
    ids30 = [4000 + ((i * 7) % 30) for i in range(30)]
    caso("lote-de-treinta-solicitudes-con-ids-cruzados", ["FM-CER-19", "FM-CER-14", "FM-CER-69"], entrada(ids30),
         seed=[(aporte if (i % 3 == 0) else rescate)(4000 + i, estado=("CRT" if i % 11 == 5 else "ING"), IdPortafolio=500 + (i % 4), CodigoPortafolio="PF%d" % (500 + (i % 4)),
                                                  CostoMonedaLocal=100000 + i * 1000, Monto=100000 + i * 1000) for i in range(30)], modo="continua")

    caso("lote-de-ciento-veinte-solicitudes", ["FM-CER-19", "FM-CER-04", "D-F3B-32"], entrada([5000 + ((i * 37) % 120) for i in range(120)]),
         seed=[(aporte if (i % 2 == 0) else rescate)(5000 + i, IdPortafolio=500 + (i % 6), CodigoPortafolio="PF%d" % (500 + (i % 6)), CostoMonedaLocal=50000 + i * 777, Monto=50000 + i * 777)
               for i in range(120)], modo="continua")
    caso("simulacion-S-solo-fija-la-entrada-y-el-contador-final", ["FM-CER-01", "FM-CER-68", "D-F3B-01"], entrada([3001, 3002], sim="S"), seed=[rescate(3001), aporte(3002)], modo="continua")
    caso("simulacion-vacia-del-job-no-fuerza-contador", ["FM-CER-01", "FM-CER-68"], entrada([3001, 3002], sim=""), seed=[rescate(3001), aporte(3002)], modo="continua")

    # ------------------------------------------------------------------ FM-CER-23 format of the amounts
    caso("formato-comisiones-clp-sin-decimales", ["FM-CER-23"], entrada([3001, 3002]),
         seed=[rescate(3001, MontoComision=Num("1234.5678"), MontoComisionLocal=Num("98.765"), MontoLiquido=Num("55.5555"), MontoLiquidoLocal=Num("99.9999")),
               aporte(3002, MontoComision=Num("0.5"), MontoComisionLocal=Num("1.5"))], modo="continua")
    caso("formato-fondo-usd-dos-decimales-y-moneda-de-inversion-clp", ["FM-CER-23"], entrada([3001]),
         seed=[aporte(3001, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=1234567, Monto=0,
                      MontoComision=Num("12.3456"), MontoComisionLocal=Num("11111.5"))], modo="continua")
    caso("formato-moneda-sistema-propia", ["FM-CER-23"], entrada([3001]),
         seed=[rescate(3001, IdMonedaFondo="USD", IdMoneda="USD", IdMonedaPago="USD", IdSerieFondo="FM002A", IdInstrumento=903, CostoMonedaLocal=5000, Monto=0)], modo="continua")
    caso("formato-con-monto-comision-nn-conserva-y-redondea-los-de-la-base", ["FM-CER-17", "FM-CER-23"], entrada([3001]),
         seed=[rescate(3001, IdMonedaFondo="USD", IdMoneda="CLP", IdSerieFondo="FM002A", IdInstrumento=903, Monto=Num("1234.5678"), CostoMonedaLocal=Num("5000.5"), MontoPago=Num("1234.5678"),
                       MontoPagoLocal=Num("5000.5"), MontoMonedaSistema=Num("5000.4999")),
               solo_tabla(3002, "RES", "ING", IdTipoMovimientoFondoMutuo="RESCNN", IdSolicitudAporteRescateRelacionado=3001)], modo="continua")
    caso("formato-moneda-sin-fila-de-formato", ["FM-CER-23", "FM-CER-71"], entrada([3001]), seed=[rescate(3001, IdMonedaFondo="JPY", IdMoneda="CLP")], colaboradores=L.precios(pr(3001, moneda="JPY")))
