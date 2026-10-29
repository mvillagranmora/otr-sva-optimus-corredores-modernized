"""FMM-CER T2 cases of front F-C (APORTES): FM-CER-21, 22 (saldo de la cuenta de inversion, canal no PAC / PAC), the format of the aportes (FM-CER-23 as seen after the saldo),
FM-CER-24..26 (costo UF de los aportes de reinversion), FM-CER-27, 28 (custodia de ingreso, cartera y rentabilidad real).

Legacy: `CertificarSolicitudAporteRescate.logica` C:575-579 (saldo + format), C:600-601 (reCalcularCostoUFAportes), C:602-620 (custodia, rentabilidad real). Every case is recorded with the
unmodified legacy (modo completo); a case whose `excepcion=True` is expected to throw inside F-C."""
import cases_cer_t2 as T
from cases_cer_t2 import caso, entrada, ap, aporte, rescate, origen, reinv, mov, err, resp_custodia, res_custodia, saldo_por_uso, fila_saldo, paridad_uf, L, Num, config, param, ins
from cases_cer_lib import solo_tabla  # noqa: F401

SIS = "2026-03-02"


def saldo_fijo(*filas):
    return L.saldo_flujo(*filas)


def generar():
    # ================================================================== FM-CER-21 saldo de la cuenta de inversion (canal distinto de PAC)
    caso("saldo-suficiente-no-pac", ["FM-CER-21", "matriz:C12"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 500000)))
    caso("saldo-insuficiente-rechaza", ["FM-CER-21", "matriz:C12"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=700000, Monto=700000), ap(3002, 501, CostoMonedaLocal=100000, Monto=100000), L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 500000), fila_saldo(501, "CLP", 500000)))
    caso("dos-aportes-del-mismo-par-rechazan-a-ambos", ["FM-CER-21", "matriz:C12"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=300000, Monto=300000), ap(3002, 500, CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 500000)))
    caso("dos-aportes-del-mismo-par-uno-basta-pasan-ambos", ["FM-CER-21", "matriz:C12"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=200000, Monto=200000), ap(3002, 500, CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 500000)))
    caso("saldo-igual-al-total-no-rechaza", ["FM-CER-21"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=500000, Monto=500000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 500000)))
    caso("opera-saldo-negativo-S-no-rechaza-aunque-no-alcance", ["FM-CER-20", "FM-CER-21", "matriz:C11"], entrada([3001]),
         seed=[ap(3001, 500, CostoMonedaLocal=900000, Monto=900000), L.opera_saldo_negativo(500, "S")], saldo=saldo_fijo(fila_saldo(500, "CLP", 100)))
    caso("opera-saldo-negativo-ausente-es-N-y-rechaza", ["FM-CER-20", "FM-CER-21", "matriz:C11"], entrada([3001]),
         seed=[ap(3001, 500, CostoMonedaLocal=900000, Monto=900000)], saldo=saldo_fijo(fila_saldo(500, "CLP", 100)))
    caso("opera-saldo-negativo-n-minuscula-rechaza-s-minuscula-no", ["FM-CER-20", "FM-CER-21", "matriz:C11"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=900000, Monto=900000), ap(3002, 501, CostoMonedaLocal=900000, Monto=900000), L.opera_saldo_negativo(500, "n"), L.opera_saldo_negativo(501, "s")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 100), fila_saldo(501, "CLP", 100)))
    caso("sobregiro-S-saca-el-portafolio-del-flujo", ["FM-CER-15", "FM-CER-21"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=900000, Monto=900000), ap(3002, 501, CostoMonedaLocal=900000, Monto=900000), L.indicador_portafolio(500, "S"), L.indicador_portafolio(501, "N")],
         saldo=saldo_fijo(fila_saldo(501, "CLP", 100)))
    caso("sin-mueve-cuenta-no-consulta-flujo", ["FM-CER-21"], entrada([3001]), seed=[aporte(3001, IdPortafolio=500, CodigoPortafolio="PF500", MueveCta="N", CostoMonedaLocal=900000, Monto=900000)])
    caso("origen-no-manual-no-consulta-flujo", ["FM-CER-21"], entrada([3001]), seed=[ap(3001, 500, OrigenOperacion="A", CostoMonedaLocal=900000, Monto=900000)])
    caso("pares-omitidos-por-listas-independientes-quedan-con-saldo-cero", ["FM-CER-21", "D-NOR-16", "matriz:C14"], entrada([3001, 3002, 3003]),
         seed=[ap(3001, 500, IdMoneda="CLP", CostoMonedaLocal=100000, Monto=100000), ap(3002, 501, IdMoneda="USD", CostoMonedaLocal=2000, Monto=0),
               ap(3003, 500, IdMoneda="USD", CostoMonedaLocal=2000, Monto=0), L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 5000000), fila_saldo(501, "USD", 5000000)))
    caso("par-omitido-con-fila-de-saldo-en-la-respuesta-pasa", ["FM-CER-21", "D-NOR-16", "matriz:C14"], entrada([3001, 3002, 3003]),
         seed=[ap(3001, 500, IdMoneda="CLP", CostoMonedaLocal=100000, Monto=100000), ap(3002, 501, IdMoneda="USD", CostoMonedaLocal=2000, Monto=0),
               ap(3003, 500, IdMoneda="USD", CostoMonedaLocal=2000, Monto=0), L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 5000000), fila_saldo(501, "USD", 5000000), fila_saldo(500, "USD", 5000000)))
    caso("fecha-de-certificacion-anterior-al-sistema-usa-la-fecha-de-sistema", ["FM-CER-21", "FM-CER-18", "matriz:C09"], entrada([3001], fecha="2026-02-27"),
         seed=[ap(3001, 500, FechaCertificacion="2026-02-27", CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 0, "2026-02-27"), fila_saldo(500, "CLP", 500000, SIS)))
    caso("fecha-de-certificacion-posterior-al-sistema-usa-su-propia-fecha", ["FM-CER-21", "FM-CER-18", "matriz:C09"], entrada([3001], fecha="2026-03-05"),
         seed=[ap(3001, 500, FechaCertificacion="2026-03-05", CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 0, SIS), fila_saldo(500, "CLP", 500000, "2026-03-05")))
    caso("varias-filas-del-mismo-par-gana-la-ultima", ["FM-CER-21"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 9000000), fila_saldo(500, "CLP", 100)))
    caso("fila-de-otra-fecha-no-cuenta", ["FM-CER-21", "FM-CER-18"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 9000000, "2026-03-03")))
    caso("respuesta-sin-filas-saldo-cero-rechaza", ["FM-CER-21"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo())
    caso("monedas-distintas-del-mismo-portafolio-totales-separados", ["FM-CER-21", "matriz:C12"], entrada([3001, 3002]),
         seed=[ap(3001, 500, IdMoneda="CLP", CostoMonedaLocal=400000, Monto=400000), ap(3002, 500, IdMoneda="USD", CostoMonedaLocal=900, Monto=0), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 500000), fila_saldo(500, "USD", 500)))
    caso("aporte-rechazado-por-F-A-no-entra-al-flujo", ["FM-CER-21", "FM-CER-06"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=400000, Monto=400000), ap(3002, 500, estado="CRT", CostoMonedaLocal=900000, Monto=900000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 500000)))
    caso("ids-invertidos-el-flujo-ve-los-aportes-por-id-ascendente", ["FM-CER-21", "FM-CER-19", "matriz:C44"], entrada([3003, 3001, 3002]),
         seed=[ap(3001, 502, CostoMonedaLocal=100000, Monto=100000), ap(3002, 501, CostoMonedaLocal=100000, Monto=100000), ap(3003, 500, CostoMonedaLocal=100000, Monto=100000),
               L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N"), L.opera_saldo_negativo(502, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 1000000), fila_saldo(501, "CLP", 1000000), fila_saldo(502, "CLP", 1000000)))
    caso("el-flujo-de-caja-falla-y-propaga", ["FM-CER-21", "matriz:C12"], entrada([3001]), seed=[ap(3001, 500)], colaboradores=L.falla("SaldoFlujoCaja", "flujo de caja no disponible"), excepcion=True)

    # ================================================================== FM-CER-22 canal PAC
    caso("pac-saldo-suficiente", ["FM-CER-22", "matriz:C13"], entrada([3001]), seed=[ap(3001, 500, IdTipoCanalIngreso="PAC", CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_por_uso(pac=[fila_saldo(500, "CLP", 500000)]))
    caso("pac-saldo-insuficiente-rechaza", ["FM-CER-22", "matriz:C13"], entrada([3001, 3002]),
         seed=[ap(3001, 500, IdTipoCanalIngreso="PAC", CostoMonedaLocal=300000, Monto=300000), ap(3002, 500, IdTipoCanalIngreso="PAC", CostoMonedaLocal=300000, Monto=300000),
               L.opera_saldo_negativo(500, "N")],
         saldo=saldo_por_uso(pac=[fila_saldo(500, "CLP", 500000)]))
    caso("pac-y-no-pac-del-mismo-par-no-se-suman-entre-si", ["FM-CER-21", "FM-CER-22", "D-F3B-11", "matriz:C13"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=300000, Monto=300000), ap(3002, 500, IdTipoCanalIngreso="PAC", CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_por_uso(nopac=[fila_saldo(500, "CLP", 500000)], pac=[fila_saldo(500, "CLP", 500000)]))
    caso("pac-ignora-el-saldo-de-la-llamada-no-pac", ["FM-CER-22", "matriz:C13"], entrada([3001]),
         seed=[ap(3001, 500, IdTipoCanalIngreso="PAC", CostoMonedaLocal=300000, Monto=300000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_por_uso(nopac=[fila_saldo(500, "CLP", 9000000)], pac=[fila_saldo(500, "CLP", 100)]))
    caso("pac-opera-saldo-negativo-S-no-rechaza", ["FM-CER-20", "FM-CER-22"], entrada([3001]),
         seed=[ap(3001, 500, IdTipoCanalIngreso="PAC", CostoMonedaLocal=900000, Monto=900000), L.opera_saldo_negativo(500, "S")], saldo=saldo_por_uso(pac=[fila_saldo(500, "CLP", 1)]))
    caso("pac-sobregiro-S-no-consulta", ["FM-CER-15", "FM-CER-22"], entrada([3001]),
         seed=[ap(3001, 500, IdTipoCanalIngreso="PAC", CostoMonedaLocal=900000, Monto=900000), L.indicador_portafolio(500, "S")])
    caso("pac-pares-omitidos-por-listas-independientes", ["FM-CER-22", "D-NOR-16", "D-F3B-11", "matriz:C14"], entrada([3001, 3002, 3003]),
         seed=[ap(3001, 500, IdTipoCanalIngreso="PAC", IdMoneda="CLP", CostoMonedaLocal=100000, Monto=100000),
               ap(3002, 501, IdTipoCanalIngreso="PAC", IdMoneda="USD", CostoMonedaLocal=2000, Monto=0),
               ap(3003, 500, IdTipoCanalIngreso="PAC", IdMoneda="USD", CostoMonedaLocal=2000, Monto=0), L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N")],
         saldo=saldo_por_uso(pac=[fila_saldo(500, "CLP", 5000000), fila_saldo(501, "USD", 5000000)]))
    caso("el-flujo-pac-falla-despues-del-no-pac", ["FM-CER-22", "D-F3B-11"], entrada([3001, 3002]), seed=[ap(3001, 500), ap(3002, 500, IdTipoCanalIngreso="PAC")],
         colaboradores={"SaldoFlujoCaja": {"respuestas": [{"cuando": {"0.P_USO": "CERTIFICACION_APORTE_FMM_PAC"}, "error": "flujo PAC no disponible"}],
                                           "defecto": {"P_OUT_FLUJOCAJA": [fila_saldo(500, "CLP", 999999999)]}}}, excepcion=True)

    # ================================================================== FM-CER-23 format of the aportes (right after the saldo)
    caso("formato-de-los-aportes-fondo-usd-tras-el-saldo", ["FM-CER-23", "FM-CER-27"], entrada([3001, 3002]),
         seed=[ap(3001, 500, IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", IdMoneda="CLP", IdMonedaPago="CLP", CostoMonedaLocal=1234567, Monto=0,
                  MontoComision=Num("12.3456"), MontoComisionLocal=Num("11111.5")),
               ap(3002, 501, CostoMonedaLocal=Num("100000.6"), Monto=Num("100000.6")), L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N")])
    caso("el-formato-alcanza-tambien-al-aporte-rechazado-por-saldo", ["FM-CER-23", "FM-CER-21"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=900000, Monto=900000), ap(3002, 501, CostoMonedaLocal=Num("1000.6"), Monto=Num("1000.6")), L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 1), fila_saldo(501, "CLP", 5000000)))

    # ================================================================== FM-CER-24..26 costo UF de los aportes de reinversion
    uf = lambda x: paridad_uf(x)
    # inherited from the rescate of origin: the regimes APV-A, BF, DC-DL, DC-AL, DC-R (equalsIgnoreCase)
    for r in ("APV-A", "BF", "DC-DL", "DC-AL", "DC-R", "bf", "dc-dl"):
        caso("reinversion-hereda-costo-uf-regimen-" + ("minuscula-" if r != r.upper() else "") + r.lower(), ["FM-CER-24", "FM-CER-25", "FM-CER-26", "D-F3B-10", "matriz:C16"], entrada([3002]),
             seed=[reinv(3002, 3001, IdRegimenTributario=r), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"), IdRegimenTributario=r)])
    caso("reinversion-mlt-S-y-ambos-18Q-hereda", ["FM-CER-24", "FM-CER-26", "D-F3B-10", "matriz:C16"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="18Q", IndicadorMLT="S"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"), IdRegimenTributario="18Q")])
    caso("reinversion-mlt-S-y-18q-minuscula-hereda", ["FM-CER-26"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="18q", IndicadorMLT="s"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"), IdRegimenTributario="18Q")])
    caso("reinversion-mlt-S-pero-el-rescate-no-es-18Q-usa-paridad", ["FM-CER-26", "D-F3B-10", "matriz:C16"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="18Q", IndicadorMLT="S"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"), IdRegimenTributario="GEN")],
         paridad=[uf(39500.25)])
    caso("reinversion-mlt-N-regimen-18Q-usa-paridad", ["FM-CER-26", "matriz:C16"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="18Q", IndicadorMLT="N"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"), IdRegimenTributario="18Q")],
         paridad=[uf(39500.25)])
    caso("reinversion-otro-regimen-usa-la-paridad-de-la-uf", ["FM-CER-24", "FM-CER-26", "D-F3B-10", "matriz:C16"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="GEN"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))], paridad=[uf(39500.25)])
    caso("reinversion-paridad-sin-salida-traga-el-error-y-no-cambia", ["FM-CER-24", "D-F3B-10", "D-NOR-11", "matriz:C16"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="GEN"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))],
         paridad=[{"cuando": {"0.p_IdTrabajo": 1, "0.p_CalculosEntrada.0.IdMonedaOrigen": "UF"}, "respuesta": {"p_out_CalculosSalida": []}}])
    caso("reinversion-paridad-sin-monto-local-usa-el-valor-uf-propio", ["FM-CER-26", "D-F3B-10", "D-F3B-40", "matriz:C16"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="GEN", ValorUFFechaInversion=Num("38000.5")), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))],
         paridad=[uf(None)], excepcion="posterior")
    caso("reinversion-paridad-error-de-colaborador-se-traga", ["FM-CER-24", "D-NOR-11", "matriz:C16"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="GEN"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))],
         paridad=[{"cuando": {"0.p_IdTrabajo": 1, "0.p_CalculosEntrada.0.IdMonedaOrigen": "UF"}, "error": "paridad no disponible"}])
    caso("reinversion-origen-sin-valor-uf-no-cambia", ["FM-CER-24"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=0)])
    caso("reinversion-origen-sin-costo-uf-no-cambia", ["FM-CER-24"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF"), origen(3001, MontoMonedaSistema=1200000, CostoUF=0, ValorUFFechaInversion=Num("39000.5"))])
    caso("reinversion-origen-inexistente-aborta-el-lote-de-aportes", ["FM-CER-24", "D-F3B-10", "D-NOR-11", "matriz:C16"], entrada([3002, 3004]),
         seed=[reinv(3002, 3999, IdRegimenTributario="BF"), reinv(3004, 3003, IdRegimenTributario="BF"),
               origen(3003, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))])
    caso("reinversion-un-origen-valido-y-otro-despues-se-procesan-en-orden", ["FM-CER-24", "D-F3B-10"], entrada([3002, 3004]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF"), reinv(3004, 3003, IdRegimenTributario="DC-R", MontoMonedaSistema=900000),
               origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5")),
               origen(3003, MontoMonedaSistema=1000000, CostoUF=25, ValorUFFechaInversion=Num("38800.5"))])
    caso("aporte-no-reinversion-se-salta-aunque-tenga-relacionado", ["FM-CER-24"], entrada([3002]),
         seed=[ap(3002, 500, IdSolicitudAporteRescateRelacionado=3001, IdRegimenTributario="BF"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))])
    caso("dos-reinversiones-otro-regimen-la-paridad-ve-la-entrada-repetida", ["FM-CER-24", "FM-CER-26", "D-F3B-10", "D-F3B-11", "matriz:C16"], entrada([3002, 3004]),
         seed=[reinv(3002, 3001, IdRegimenTributario="GEN", FechaValorCuota="2026-03-02"), reinv(3004, 3003, IdRegimenTributario="GEN", FechaValorCuota="2026-03-02"),
               origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5")), origen(3003, MontoMonedaSistema=1000000, CostoUF=25, ValorUFFechaInversion=Num("38800.5"))],
         paridad=[uf(39500.25)])
    caso("reinversion-rechazada-por-saldo-igual-se-recalcula", ["FM-CER-24", "FM-CER-21"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF", CostoMonedaLocal=900000, Monto=900000), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5")),
               L.opera_saldo_negativo(500, "N")], saldo=saldo_fijo(fila_saldo(500, "CLP", 1)))
    caso("reinversion-lee-el-rescate-de-origen-de-la-base-no-el-de-memoria", ["FM-CER-24", "FM-CER-25", "D-F3B-10", "matriz:C16"], entrada([3001, 3002]),
         seed=[rescate(3001, CostoUF=40, MontoMonedaSistema=1200000, ValorUFFechaInversion=Num("39000.5")), reinv(3002, 3001, IdRegimenTributario="BF")])
    # FM-CER-25: the value of the UF of the rescate, foreign currency branch (sum of the DISTINCT MontoMonedaSistema of the not rejected reinversion aportes)
    ext = dict(IdMoneda="USD", IdMonedaPago="CLP", IdSerieFondo="FM002A", IdInstrumento=903, IdMonedaFondo="USD", CostoMonedaLocal=5000, Monto=0)
    caso("reinversion-moneda-extranjera-suma-los-montos-distintos", ["FM-CER-25", "D-F3B-10", "matriz:C16"], entrada([3002, 3004]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF", **ext), reinv(3004, 3001, IdRegimenTributario="BF", **ext),
               origen(3001, IdMoneda="USD", IdMonedaFondo="USD", IdSerieFondo="FM002A", IdInstrumento=903, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))])
    caso("reinversion-moneda-extranjera-montos-distintos-se-suman", ["FM-CER-25", "D-F3B-10", "matriz:C16"], entrada([3002, 3004]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF", **ext), reinv(3004, 3001, IdRegimenTributario="BF", **dict(ext, CostoMonedaLocal=7000)),
               origen(3001, IdMoneda="USD", IdMonedaFondo="USD", IdSerieFondo="FM002A", IdInstrumento=903, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))])
    caso("reinversion-moneda-extranjera-origen-en-otra-moneda-usa-su-monto", ["FM-CER-25", "matriz:C16"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF", **ext),
               origen(3001, IdMoneda="CLP", MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))])
    caso("reinversion-moneda-de-sistema-igual-usa-el-monto-del-rescate", ["FM-CER-25", "matriz:C16"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF"), origen(3001, MontoMonedaSistema=1500000, CostoUF=50, ValorUFFechaInversion=Num("39000.5"))])
    caso("reinversion-moneda-extranjera-sin-el-aporte-rechazado-en-la-suma", ["FM-CER-25", "FM-CER-21", "D-F3B-10"], entrada([3002, 3004]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF", IdPortafolio=500, CodigoPortafolio="PF500", **ext), reinv(3004, 3001, IdRegimenTributario="BF", IdPortafolio=501, CodigoPortafolio="PF501", **dict(ext, CostoMonedaLocal=7000)),
               origen(3001, IdMoneda="USD", IdMonedaFondo="USD", IdSerieFondo="FM002A", IdInstrumento=903, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5")),
               L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N")],
         saldo=saldo_fijo(fila_saldo(500, "USD", 1), fila_saldo(501, "USD", 5000000)))

    # ================================================================== FM-CER-27 / 28 custodia de ingreso, cartera y rentabilidad real
    caso("custodia-de-ingreso-un-aporte", ["FM-CER-27", "FM-CER-28", "D-F3B-01", "matriz:C17"], entrada([3001]), seed=[ap(3001, 500, NumeroCuotas=0, CostoMonedaLocal=300000, Monto=300000)],
         custodia=dict(aportes=resp_custodia([mov(3001, IdCustodia=9101, MontoCompraFactorOriginal=Num("7.25"))])))
    caso("custodia-de-ingreso-varios-aportes-en-orden-de-id", ["FM-CER-27", "FM-CER-19", "matriz:C44"], entrada([3003, 3001, 3002]),
         seed=[ap(3001, 500), ap(3002, 501), ap(3003, 502)],
         custodia=dict(aportes=resp_custodia([mov(3001, IdCustodia=9101), mov(3002, IdCustodia=9102), mov(3003, IdCustodia=9103)])))
    caso("custodia-aporte-pac-y-otros-canales-van-juntos-con-canal-vacio", ["FM-CER-27"], entrada([3001, 3002, 3003]),
         seed=[ap(3001, 500, IdTipoCanalIngreso="PAC"), ap(3002, 501, IdTipoCanalIngreso="OPT"), ap(3003, 502, IdTipoCanalIngreso="AGE")])
    caso("custodia-aporte-reinversion-con-fecha-original", ["FM-CER-27", "matriz:C17"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF", FechaOriginalInversion="2025-11-15"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))])
    caso("custodia-aporte-reinversion-fecha-original-0001-01-01-se-lee-como-0001-01-03-en-h2", ["FM-CER-27", "D-FEC-6"], entrada([3002]),
         seed=[reinv(3002, 3001, IdRegimenTributario="BF", FechaOriginalInversion="0001-01-01"), origen(3001, MontoMonedaSistema=1200000, CostoUF=40, ValorUFFechaInversion=Num("39000.5"))])
    caso("custodia-aporte-no-reinversion-ignora-la-fecha-original", ["FM-CER-27"], entrada([3001]), seed=[ap(3001, 500, FechaOriginalInversion="2025-11-15")])
    caso("custodia-aporte-con-regimen-tributario-y-ejecutivo", ["FM-CER-27"], entrada([3001]), seed=[ap(3001, 500, IdRegimenTributario="APV-A", IdEmpleadoEjecutivo=33, TipoCarteraNormativa="TCN1")])
    caso("custodia-error-rechaza-al-aporte-por-su-id", ["FM-CER-27", "D-F3B-01", "matriz:C17"], entrada([3001, 3002]), seed=[ap(3001, 500), ap(3002, 501)],
         custodia=dict(aportes=resp_custodia([mov(3001), mov(3002)], [err(3002, "UI_ERROR_CUSTODIA_APORTE", "Error de custodia en el aporte")], contador=1)))
    caso("custodia-error-con-id-negativo-usa-el-valor-absoluto", ["FM-CER-27", "matriz:C17"], entrada([3001, 3002]), seed=[ap(3001, 500), ap(3002, 501)],
         custodia=dict(aportes=resp_custodia([mov(3001), mov(3002)], [err(-3001, "UI_ERROR_CUSTODIA_APORTE", "Error de custodia en el aporte")], contador=1)))
    caso("custodia-error-de-un-id-que-no-es-aporte-solo-queda-el-mensaje", ["FM-CER-27", "matriz:C17"], entrada([3001]), seed=[ap(3001, 500)],
         custodia=dict(aportes=resp_custodia([mov(3001)], [err(9999, "UI_ERROR_CUSTODIA_GENERAL", "Error general de custodia")], contador=1)))
    caso("custodia-mensaje-que-no-es-error-no-rechaza", ["FM-CER-27"], entrada([3001]), seed=[ap(3001, 500)],
         custodia=dict(aportes=resp_custodia([mov(3001)], [err(3001, "UI_ERROR_CUSTODIA_APORTE", "Error de custodia en el aporte", tipo="ADVERTENCIA")], contador=0)))
    caso("custodia-contador-sin-mensajes-sube-el-contador-final", ["FM-CER-27", "D-F3B-01"], entrada([3001]), seed=[ap(3001, 500)], custodia=dict(aportes=resp_custodia([mov(3001)], [], contador=2)))
    caso("custodia-dos-errores-del-mismo-id-rechaza-una-vez-y-agrega-ambos-mensajes", ["FM-CER-27"], entrada([3001]), seed=[ap(3001, 500)],
         custodia=dict(aportes=resp_custodia([mov(3001)], [err(3001, "UI_ERROR_CUSTODIA_APORTE", "Error de custodia en el aporte"), err(3001, "UI_ERROR_CUSTODIA_GENERAL", "Error general de custodia")], contador=2)))
    caso("todos-los-aportes-rechazados-por-saldo-no-llaman-a-custodia", ["FM-CER-27", "FM-CER-21"], entrada([3001]), seed=[ap(3001, 500, CostoMonedaLocal=900000, Monto=900000), L.opera_saldo_negativo(500, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 1)))
    caso("custodia-falla-y-propaga", ["FM-CER-27", "matriz:C17"], entrada([3001]), seed=[ap(3001, 500)], colaboradores=L.falla("ProcesarCustodiaFondoMutuo", "custodia no disponible"), excepcion=True)
    caso("custodia-con-mensaje-sin-id-lanza-NPE-al-tomar-el-valor-absoluto", ["FM-CER-27", "D-NOR-9"], entrada([3001]), seed=[ap(3001, 500)],
         custodia=dict(aportes=resp_custodia([mov(3001)], [{"tipo": "ERROR", "codigo": "UI_ERROR_CUSTODIA_GENERAL", "descripcion": "Error general de custodia"}], contador=1)), excepcion=True)
    # FM-CER-28: cartera y rentabilidad real
    caso("cartera-del-aporte-se-asigna-con-el-primer-movimiento-de-su-id", ["FM-CER-28", "matriz:C17"], entrada([3001]), seed=[ap(3001, 500)],
         custodia=dict(aportes=resp_custodia([mov(3001, IdCustodia=9201, MontoCompraFactorOriginal=Num("3.5")), mov(3001, IdCustodia=9202, MontoCompraFactorOriginal=Num("4.5"))])))
    caso("cartera-sin-movimiento-en-la-respuesta-no-se-asigna", ["FM-CER-28", "D-F3B-37"], entrada([3001, 3002]), seed=[ap(3001, 500), ap(3002, 501, RentabilidadReal=Num("1.25"), IdCartera=77)],
         custodia=dict(aportes=resp_custodia([mov(3001, IdCustodia=9201)])))
    caso("cartera-del-aporte-rechazado-por-custodia-no-se-asigna", ["FM-CER-28", "FM-CER-27"], entrada([3001, 3002]), seed=[ap(3001, 500), ap(3002, 501)],
         custodia=dict(aportes=resp_custodia([mov(3001, IdCustodia=9201), mov(3002, IdCustodia=9202)], [err(3002)], contador=1)))
    caso("cartera-movimiento-con-id-ajeno-no-se-asigna", ["FM-CER-28"], entrada([3001]), seed=[ap(3001, 500)], custodia=dict(aportes=resp_custodia([mov(7777, IdCustodia=9999)])))
    caso("aportes-y-rescate-mezclados-la-custodia-solo-ve-los-aportes", ["FM-CER-27", "FM-CER-28", "matriz:C19"], entrada([3001, 3002, 3003]),
         seed=[ap(3001, 500), rescate(3002), ap(3003, 501)], custodia=dict(aportes=resp_custodia([mov(3001, IdCustodia=9301), mov(3003, IdCustodia=9303)])))
    caso("lote-mixto-un-aporte-rechazado-por-saldo-el-otro-pasa", ["FM-CER-21", "FM-CER-27", "FM-CER-69", "matriz:C05"], entrada([3001, 3002]),
         seed=[ap(3001, 500, CostoMonedaLocal=900000, Monto=900000), ap(3002, 501, CostoMonedaLocal=100000, Monto=100000), L.opera_saldo_negativo(500, "N"), L.opera_saldo_negativo(501, "N")],
         saldo=saldo_fijo(fila_saldo(500, "CLP", 1), fila_saldo(501, "CLP", 5000000)))
