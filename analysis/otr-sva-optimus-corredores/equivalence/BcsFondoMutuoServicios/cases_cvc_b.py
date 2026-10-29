"""FMM-CVC cases, part B: the value that is written (FM-CVC-05, 06), the previous-day value and variation (FM-CVC-07), simulation (FM-CVC-08), the historico and vigente
tables (FM-CVC-09, 10), the audit log (FM-CVC-11), messages and counter (FM-CVC-12), repetition (FM-CVC-13), transaction and partial failure (FM-CVC-14), configuration."""
import cases_cvc_lib as L
from cases_cvc_lib import ABSENT, Num, caso, entrada, vc, sin

HOY = "2026-03-02"
AYER = "2026-03-01"


def generar():
    # ------------------------------------------------------------------ FM-CVC-05 the value written is the INPUT value with all its scale (D-F3A-17)
    caso("decimales-de-la-serie-4-valor-5-decimales-no-se-trunca", ["FM-CVC-05", "D-F3A-17"], entrada([vc("FM001A", HOY, Num("1693.85619"))]))
    caso("decimales-de-la-serie-0-valor-con-decimales", ["FM-CVC-05", "D-F3A-17"], entrada([vc("FM003A", HOY, Num("1693.9"))]))
    caso("decimales-de-la-serie-8-valor-con-nueve", ["FM-CVC-05", "D-F3A-17"], entrada([vc("FM004A", HOY, Num("1.123456789"))]))
    caso("decimales-de-la-serie-usd-6", ["FM-CVC-05"], entrada([vc("FM002A", HOY, Num("12.3456789"), moneda="USD")]))
    caso("escala-mayor-a-16-half-down-del-binder", ["FM-CVC-05", "D-F3A-23"], entrada([vc("FM001A", HOY, Num("0.123456784999999950"))]))
    caso("escala-mayor-a-16-sin-empate-sube", ["FM-CVC-05", "D-F3A-23"], entrada([vc("FM001A", HOY, Num("0.123456784999999951"))]))
    caso("escala-del-motor-8-redondea-el-noveno-decimal", ["FM-CVC-05", "D-F3A-23"], entrada([vc("FM001A", HOY, Num("10.123456785"))]))
    caso("valor-con-ceros-a-la-derecha-conserva-la-escala-en-salida-y-log", ["FM-CVC-05", "FM-CVC-11"], entrada([vc("FM001A", HOY, Num("1500.50"))]))
    caso("valor-con-exponente-conserva-el-texto-en-el-log", ["FM-CVC-05", "FM-CVC-11"], entrada([vc("FM001A", HOY, Num("1.5E+2"))]))
    caso("valor-entero-grande-dentro-de-la-columna", ["FM-CVC-05"], entrada([vc("FM001A", HOY, Num("123456789012345678.5"))]))
    caso("valor-demasiado-grande-fallo-de-escritura", ["FM-CVC-05", "FM-CVC-14", "D-F3A-22"], entrada([vc("FM001A", HOY, Num("12345678901234567890.5"))]))
    caso("valor-muy-pequeno-redondea-a-cero-en-la-columna", ["FM-CVC-05", "D-F3A-23"], entrada([vc("FM001A", HOY, Num("0.000000001"))]))

    # ------------------------------------------------------------------ FM-CVC-06 the other fields of the result
    caso("fecha-vigencia-de-fila-con-error-es-0001-01-01", ["FM-CVC-06"], entrada([vc("FM001A", HOY, 1, msg="ERROR"), vc("FM001B", HOY, 2)]))
    caso("emisor-vacio-para-fila-con-error", ["FM-CVC-06"], entrada([vc("NOEXISTE", HOY, 1), vc("FM002A", HOY, 2, moneda="USD")]))
    caso("fecha-calculo-es-la-fecha-del-sistema-no-la-de-cierre", ["FM-CVC-06"], entrada([vc("FM001A", AYER, 1500.5)]))
    caso("observacion-nula-fallo-de-escritura", ["FM-CVC-06", "FM-CVC-14", "D-F3A-22"], entrada([vc("FM001A", HOY, 1500.5, obs=None)]), seed=[L.historico(1, "FM001A", HOY, 100)])
    caso("observacion-ausente-fallo-de-escritura", ["FM-CVC-06", "FM-CVC-14"], entrada([sin(vc("FM001A", HOY, 1500.5), "OBSERVACION")]))
    caso("observacion-vacia-se-escribe-vacia", ["FM-CVC-06"], entrada([vc("FM001A", HOY, 1500.5, obs="")]))
    caso("observacion-de-101-caracteres-fallo-de-escritura", ["FM-CVC-06", "FM-CVC-14"], entrada([vc("FM001A", HOY, 1500.5, obs="x" * 101)]))
    caso("moneda-nula-sin-validacion-fallo-de-escritura", ["FM-CVC-06", "FM-CVC-14", "D-F3A-20"], entrada([vc("FM001A", HOY, 1500.5, moneda=None)], todos=None))
    caso("moneda-de-4-caracteres-sin-validacion-fallo-de-escritura", ["FM-CVC-06", "FM-CVC-14", "D-F3A-20"], entrada([vc("FM001A", HOY, 1500.5, moneda="CLPX")], todos=None))
    caso("moneda-de-la-entrada-no-la-del-fondo-se-escribe", ["FM-CVC-09", "FM-CVC-10", "D-F3A-20"], entrada([vc("FM001A", HOY, 1500.5, moneda="USD")], todos=None))

    # ------------------------------------------------------------------ FM-CVC-07 previous-day value and variation (informative only)
    caso("variacion-105-contra-100-es-5", ["FM-CVC-07"], entrada([vc("FM001A", HOY, 105)]), seed=[L.historico(1, "FM001A", AYER, 100)])
    caso("variacion-1693-8561-contra-1690-es-0-200", ["FM-CVC-07"], entrada([vc("FM001A", HOY, Num("1693.8561"))]), seed=[L.historico(1, "FM001A", AYER, 1690)])
    caso("variacion-es-valor-absoluto-si-baja", ["FM-CVC-07"], entrada([vc("FM001A", HOY, 95)]), seed=[L.historico(1, "FM001A", AYER, 100)])
    caso("variacion-sin-umbral-se-escribe-un-salto-de-mil-por-ciento", ["FM-CVC-07", "D-F3A-23"], entrada([vc("FM001A", HOY, 1100)]), seed=[L.historico(1, "FM001A", AYER, 100)])
    caso("variacion-anterior-cero-es-cero", ["FM-CVC-07"], entrada([vc("FM001A", HOY, 105)]), seed=[L.historico(1, "FM001A", AYER, 0)])
    caso("variacion-sin-valor-anterior-es-cero", ["FM-CVC-07"], entrada([vc("FM001A", HOY, 105)]))
    caso("anterior-solo-cuenta-la-ultima-carga-diaria", ["FM-CVC-07", "FM-CVC-09"], entrada([vc("FM001A", HOY, 105)]),
         seed=[L.historico(1, "FM001A", AYER, 90, ultima="N"), L.historico(2, "FM001A", AYER, 100, ultima="S")])
    caso("anterior-con-solo-cargas-no-ultimas-es-cero", ["FM-CVC-07"], entrada([vc("FM001A", HOY, 105)]), seed=[L.historico(1, "FM001A", AYER, 90, ultima="N")])
    caso("anterior-de-otro-dia-no-cuenta", ["FM-CVC-07"], entrada([vc("FM001A", HOY, 105)]), seed=[L.historico(1, "FM001A", "2026-02-27", 100)])
    caso("anterior-es-el-dia-calendario-anterior-aunque-sea-feriado", ["FM-CVC-07"], entrada([vc("FM001A", "2026-03-01", 105)], p_IdPais=56),
         seed=[L.historico(1, "FM001A", "2026-02-28", 100)])
    caso("anterior-cruza-mes-y-anio", ["FM-CVC-07"], entrada([vc("FM001A", "2026-01-01", 105)]), seed=[L.historico(1, "FM001A", "2025-12-31", 100)])
    caso("anterior-historico-de-dos-vigentes-misma-fecha-lanza", ["FM-CVC-07", "D-NOR-9"], entrada([vc("FM001A", HOY, 105)]),
         seed=[L.historico(1, "FM001A", AYER, 100), L.historico(2, "FM001A", AYER, 101)])
    caso("anterior-con-fecha-de-entrada-con-hora", ["FM-CVC-07"], entrada([vc("FM001A", "2026-03-02 00:00:00", 105)]), seed=[L.historico(1, "FM001A", AYER, 100)])
    caso("dia-anterior-reutilizado-todas-las-filas-usan-la-ultima-fecha", ["FM-CVC-07", "D-F3A-18"],
         entrada([vc("FM001A", HOY, 105), vc("FM001B", AYER, 50)]),
         seed=[L.historico(1, "FM001A", AYER, 100), L.historico(2, "FM001B", "2026-02-28", 40)])
    caso("dia-anterior-reutilizado-la-ultima-fila-manda-aunque-sea-la-mas-antigua", ["FM-CVC-07", "D-F3A-18"],
         entrada([vc("FM001B", AYER, 50), vc("FM001A", HOY, 105)]),
         seed=[L.historico(1, "FM001A", AYER, 100), L.historico(2, "FM001B", "2026-02-28", 40)])
    caso("dia-anterior-misma-serie-dos-fechas-el-anterior-es-de-la-ultima", ["FM-CVC-07", "D-F3A-18", "FM-CVC-10"],
         entrada([vc("FM001A", HOY, 105), vc("FM001A", "2026-02-27", 95)]),
         seed=[L.historico(1, "FM001A", AYER, 100), L.historico(2, "FM001A", "2026-02-26", 90)])
    caso("variacion-de-filas-con-error-es-cero", ["FM-CVC-07"], entrada([vc("FM001A", HOY, 105), vc("NOEXISTE", HOY, 7)]), seed=[L.historico(1, "FM001A", AYER, 100)])

    # ------------------------------------------------------------------ FM-CVC-08 only the EXACT text "N" writes
    tres = [vc("FM001A", HOY, 1500.5), vc("FM001B", HOY, 77.77), vc("FM001C", HOY, 88.88)]
    caso("simulacion-S-tres-filas-no-escribe", ["FM-CVC-08"], entrada(tres, sim="S"))
    caso("simulacion-ausente-no-escribe", ["FM-CVC-08"], entrada(tres, sim=ABSENT))
    caso("simulacion-nula-no-escribe", ["FM-CVC-08"], entrada(tres, sim=None))
    caso("simulacion-n-minuscula-no-escribe", ["FM-CVC-08"], entrada(tres, sim="n"))
    caso("simulacion-otro-texto-no-escribe", ["FM-CVC-08"], entrada(tres, sim="X"))
    caso("simulacion-vacia-no-escribe", ["FM-CVC-08"], entrada(tres, sim=""))
    caso("simulacion-S-con-filas-con-error-mismos-mensajes-que-real", ["FM-CVC-08", "FM-CVC-12"], entrada([vc("FM001A", HOY, 1), vc("NOEXISTE", HOY, 2)], sim="S"))
    caso("simulacion-S-calcula-variacion-y-no-escribe", ["FM-CVC-08", "FM-CVC-07"], entrada([vc("FM001A", HOY, 105)], sim="S"), seed=[L.historico(1, "FM001A", AYER, 100)])
    caso("real-N-tres-filas-escribe-en-orden", ["FM-CVC-08", "FM-CVC-09", "FM-CVC-10", "FM-CVC-11"], entrada(tres))

    # ------------------------------------------------------------------ FM-CVC-09 historico
    caso("historico-la-carga-previa-de-la-misma-serie-y-fecha-pasa-a-N", ["FM-CVC-09"], entrada([vc("FM001A", HOY, 1500.5)]), seed=[L.historico(1, "FM001A", HOY, 99)])
    caso("historico-otras-fechas-de-la-serie-quedan-S", ["FM-CVC-09"], entrada([vc("FM001A", HOY, 1500.5)]),
         seed=[L.historico(1, "FM001A", AYER, 99), L.historico(2, "FM001A", HOY, 98)])
    caso("historico-otra-empresa-misma-serie-y-fecha-queda-S", ["FM-CVC-09"], entrada([vc("FM001A", HOY, 1500.5)]), seed=[L.historico(1, "FM001A", HOY, 99, empresa=2)])
    caso("historico-otra-serie-misma-fecha-queda-S", ["FM-CVC-09"], entrada([vc("FM001A", HOY, 1500.5)]), seed=[L.historico(1, "FM001B", HOY, 99)])
    caso("historico-carga-ya-no-ultima-no-se-toca", ["FM-CVC-09"], entrada([vc("FM001A", HOY, 1500.5)]), seed=[L.historico(1, "FM001A", HOY, 90, ultima="N"), L.historico(2, "FM001A", HOY, 99)])
    caso("historico-dos-fechas-una-serie", ["FM-CVC-09", "FM-CVC-10"], entrada([vc("FM001A", AYER, 1499), vc("FM001A", HOY, 1500.5)]))
    caso("historico-guarda-fecha-carga-y-moneda-de-la-entrada", ["FM-CVC-09"], entrada([vc("FM001B", HOY, 77.77, moneda="USD", obs="otra moneda")], todos=None))
    caso("historico-ids-consecutivos-en-orden-de-entrada", ["FM-CVC-09"], entrada([vc("FM001C", HOY, 3), vc("FM001A", HOY, 1), vc("FM001B", HOY, 2)]))
    caso("historico-no-escribe-las-filas-con-error", ["FM-CVC-09", "FM-CVC-14"], entrada([vc("FM001A", HOY, 1), vc("FM001B", HOY, 0), vc("FM001C", HOY, 3)]))

    # ------------------------------------------------------------------ FM-CVC-10 vigente (FMM.ValorCuota)
    caso("vigente-dos-fechas-de-la-serie-queda-la-mas-reciente", ["FM-CVC-10"], entrada([vc("FM001A", AYER, 1499), vc("FM001A", HOY, 1500.5)]))
    caso("vigente-dos-fechas-orden-invertido-queda-la-mas-reciente", ["FM-CVC-10"], entrada([vc("FM001A", HOY, 1500.5), vc("FM001A", AYER, 1499)]))
    caso("vigente-tres-fechas-una-serie", ["FM-CVC-10"], entrada([vc("FM001A", "2026-02-20", 1), vc("FM001A", HOY, 3), vc("FM001A", "2026-02-25", 2)]))
    caso("vigente-dos-series-dos-fechas-cada-una", ["FM-CVC-10"],
         entrada([vc("FM001A", AYER, 1499), vc("FM001B", HOY, 77), vc("FM001A", HOY, 1500.5), vc("FM001B", AYER, 76)]))
    caso("vigente-fecha-atrasada-pisa-la-vigente", ["FM-CVC-10", "D-F3A-19"], entrada([vc("FM001A", "2026-02-27", 90)]), seed=[L.vigente("FM001A", HOY, 100)])
    caso("vigente-fecha-posterior-reemplaza", ["FM-CVC-10"], entrada([vc("FM001A", HOY, 110)]), seed=[L.vigente("FM001A", AYER, 100)])
    caso("vigente-lote-con-fecha-atrasada-y-reciente-queda-la-reciente-del-lote", ["FM-CVC-10", "D-F3A-19"],
         entrada([vc("FM001A", "2026-02-20", 90), vc("FM001A", AYER, 95)]), seed=[L.vigente("FM001A", HOY, 100)])
    caso("vigente-el-delete-por-serie-reemplaza-solo-la-fila-de-esa-serie", ["FM-CVC-10"], entrada([vc("FM001A", HOY, 110)]),
         seed=[L.vigente("FM001A", AYER, 100), L.vigente("FM001B", AYER, 5)])
    caso("vigente-otra-serie-no-se-toca", ["FM-CVC-10"], entrada([vc("FM001A", HOY, 110)]), seed=[L.vigente("FM001B", AYER, 5)])
    caso("vigente-fila-con-error-no-reemplaza", ["FM-CVC-10"], entrada([vc("FM001A", HOY, 0)]), seed=[L.vigente("FM001A", AYER, 100)])
    caso("vigente-usa-la-empresa-de-la-peticion-no-la-de-la-serie", ["FM-CVC-10"], entrada([vc("FM001A", HOY, 110)]))
    caso("vigente-dos-isin-en-modo-isin-por-serie", ["FM-CVC-10", "FM-CVC-03"],
         entrada([vc("CL0000000002", HOY, 5, calculo=HOY), vc("CL0000000001", HOY, 6, calculo=HOY)], todos="S"), seed=[L.vigente("FM001C", AYER, 1)])

    # ------------------------------------------------------------------ FM-CVC-11 audit log (AgregarLog: HTTP in the new module; its failure is swallowed)
    caso("log-una-fila-con-sus-campos-fijos", ["FM-CVC-11"], entrada([vc("FM001A", HOY, 1500.5)]))
    caso("log-solo-de-las-filas-correctas", ["FM-CVC-11", "FM-CVC-14"], entrada([vc("FM001A", HOY, 1), vc("NOEXISTE", HOY, 2), vc("FM001B", HOY, 3)]))
    caso("log-con-usuario-distinto", ["FM-CVC-11"], entrada([vc("FM001A", HOY, 1500.5)]), seed=[L.ins("BSCL.Plataforma.BD::ACC.Usuario", IdUsuario=70, IdEmpresa=1, IdEstado="ACT", TipoEntidad="EMP", IdEntidad=70)],
         )
    caso("log-ninguna-fila-correcta-no-llama-al-log", ["FM-CVC-11"], entrada([vc("NOEXISTE", HOY, 2)]))
    caso("log-en-simulacion-no-se-llama", ["FM-CVC-11", "FM-CVC-08"], entrada([vc("FM001A", HOY, 1500.5)], sim="S"))
    caso("log-falla-se-traga-y-la-carga-sigue", ["FM-CVC-11", "D-F3A-22", "D-NOR-11"], entrada([vc("FM001A", HOY, 1500.5)]), colaboradores=L.log_cae())
    caso("log-falla-con-lote-mixto-sigue-el-contador-de-errores", ["FM-CVC-11", "FM-CVC-14", "D-F3A-22"], entrada([vc("FM001A", HOY, 1500.5), vc("NOEXISTE", HOY, 1)]),
         colaboradores=L.log_cae("timeout del router"))
    caso("log-modo-isin-subregistro-es-la-serie-real", ["FM-CVC-11", "FM-CVC-03"], entrada([vc("CL0000000002", HOY, 5, calculo="2026-02-20")], todos="S"))
    caso("log-fecha-del-log-es-la-fecha-de-calculo", ["FM-CVC-11", "FM-CVC-06"], entrada([vc("CL0000000001", HOY, 5, calculo="2026-02-20")], todos="S"))

    # ------------------------------------------------------------------ FM-CVC-12 messages and counter
    caso("dos-errores-contador-dos-mensajes-con-y-sin-traduccion", ["FM-CVC-12", "D-REQ-11"], entrada([vc("NOEXISTE", HOY, 1), vc("FM001A", HOY, 0), vc("FM001B", "2026-03-09", 3)]))
    caso("tres-errores-de-tres-codigos-sin-traduccion", ["FM-CVC-12"], entrada([vc("FM001B", "2026-03-09", 3), vc("FM001A", HOY, 1, moneda="USD"), vc("FM001C", HOY, 2, msg="ERROR")]))
    caso("mensajes-usan-la-empresa-del-resultado", ["FM-CVC-12"], entrada([vc("NOEXISTE", HOY, 1)], p_IdEmpresa=1))
    caso("todo-correcto-contador-cero-sin-mensajes", ["FM-CVC-12"], entrada([vc("FM001A", HOY, 1500.5)]))
    caso("errores-repetidos-de-una-misma-fila-duplicada-cuentan-dos", ["FM-CVC-12", "FM-CVC-04"], entrada([vc("FM001A", HOY, 1), vc("FM001A", HOY, 2), vc("FM001B", HOY, 3)]))

    # ------------------------------------------------------------------ FM-CVC-13 repetition and idempotency (two requests in the SAME session)
    base = entrada([vc("FM001A", HOY, 1500.5)])
    caso("repetir-la-misma-carga-el-historico-crece-una-fila", ["FM-CVC-13", "D-F3A-16"], base, previas=[base])
    caso("repetir-tres-veces-la-misma-carga", ["FM-CVC-13", "D-F3A-16"], base, previas=[base, base])
    caso("repetir-con-otro-valor-la-segunda-queda-vigente", ["FM-CVC-13", "FM-CVC-09"], entrada([vc("FM001A", HOY, 1600)]), previas=[base])
    caso("repetir-en-simulacion-no-escribe-nunca", ["FM-CVC-13", "FM-CVC-08"], entrada([vc("FM001A", HOY, 1500.5)], sim="S"), previas=[entrada([vc("FM001A", HOY, 1500.5)], sim="S")])
    caso("repetir-tras-una-carga-con-error-la-primera-se-revirtio", ["FM-CVC-13", "FM-CVC-14", "D-F3A-22"], base,
         previas=[entrada([vc("FM001A", HOY, 1500.5), vc("NOEXISTE", HOY, 1)])])
    caso("dos-cargas-de-fechas-sucesivas-la-segunda-ve-el-anterior-de-la-primera", ["FM-CVC-13", "FM-CVC-07"], entrada([vc("FM001A", HOY, 105)]),
         previas=[entrada([vc("FM001A", AYER, 100)])])

    # ------------------------------------------------------------------ FM-CVC-14 transaction: no commit / rollback of its own, partial failure
    mixto = entrada([vc("FM001A", HOY, 1500.5), vc("FM001B", HOY, 77.77), vc("NOEXISTE", HOY, 1)])
    caso("lote-mixto-escribe-antes-de-contar-y-el-anfitrion-revierte-todo", ["FM-CVC-14", "D-F3A-22"], mixto, seed=[L.vigente("FM001A", AYER, 100), L.historico(1, "FM001A", HOY, 99)])
    caso("lote-mixto-con-formato-invalido", ["FM-CVC-14", "D-F3A-22"], entrada([vc("FM001A", HOY, 1500.5), vc("FM001B", HOY, 1, msg="ERROR")]))
    caso("lote-sin-errores-el-anfitrion-confirma", ["FM-CVC-14"], entrada([vc("FM001A", HOY, 1500.5), vc("FM001B", HOY, 77.77)]), seed=[L.historico(1, "FM001A", HOY, 99)])
    caso("fallo-en-el-paso-1-actualizar-ultima-carga", ["FM-CVC-14", "D-F3A-22", "D-NOR-11"], entrada([vc("FM001A", HOY, 1500.5)]),
         seed=[L.historico(1, "FM001A", HOY, 99), L.falla_en(L.VCH, "\"EsUltimaCargaDiaria\" = 'S'")])
    caso("fallo-en-el-paso-2-insertar-historico", ["FM-CVC-14", "D-F3A-22", "D-NOR-11"], entrada([vc("FM001A", HOY, 1500.5), vc("FM001B", HOY, 77.77)]),
         seed=[L.historico(1, "FM001A", HOY, 99), L.vigente("FM001A", AYER, 100), L.falla_en(L.VCH, "\"ValorCuota\" <> 77.77")])
    caso("fallo-en-el-paso-4-insertar-vigente", ["FM-CVC-14", "D-F3A-22", "D-NOR-11"], entrada([vc("FM001A", HOY, 1500.5), vc("FM001B", HOY, 77.77)]),
         seed=[L.historico(1, "FM001A", HOY, 99), L.vigente("FM001A", AYER, 100), L.vigente("FM001B", AYER, 5), L.falla_en(L.VC, "\"ValorCuota\" <> 77.77")])
    caso("fallo-en-el-paso-2-con-error-de-fila-tambien-lanza", ["FM-CVC-14", "D-F3A-22"], entrada([vc("FM001A", HOY, 1500.5), vc("NOEXISTE", HOY, 1)]),
         seed=[L.falla_en(L.VCH, "\"ValorCuota\" <> 1500.5")])
    caso("fallo-de-escritura-en-simulacion-no-ocurre", ["FM-CVC-14", "FM-CVC-08"], entrada([vc("FM001A", HOY, 1500.5)], sim="S"), seed=[L.falla_en(L.VCH, "\"ValorCuota\" <> 1500.5")])
    caso("log-y-fallo-de-escritura-el-log-no-se-llama", ["FM-CVC-14", "FM-CVC-11"], entrada([vc("FM001A", HOY, 1500.5)]), seed=[L.falla_en(L.VC, "\"ValorCuota\" <> 1500.5")])

    # ------------------------------------------------------------------ configuration of the company
    caso("empresa-sin-configuracion", ["FM-CVC-04", "DESVIO-KIT-1"], entrada([vc("FM001A", HOY, 1500.5)], p_IdEmpresa=2, p_IdUsuario=8),
         seed=L.config("PLA.LocalizacionSistema", None, empresa=2))
    caso("empresa-con-modulo-pero-sin-fecha-de-sistema-excepcion-de-parametro", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1500.5)], p_IdEmpresa=2, p_IdUsuario=8))
    caso("fecha-sistema-ausente-excepcion-de-parametro", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1500.5)]), seed=L.config("PLA.FechaSistema", None))
    caso("fecha-sistema-con-hora", ["FM-CVC-04", "FM-CVC-06"], entrada([vc("FM001A", HOY, 1500.5)]), seed=L.config("PLA.FechaSistema", "2026-03-02 23:00:00"))
    caso("fecha-sistema-mal-formada-lanza", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1500.5)]), seed=L.config("PLA.FechaSistema", "02/03/2026"))
    caso("fecha-sistema-posterior-permite-fecha-futura-de-base", ["FM-CVC-04"], entrada([vc("FM001A", "2026-03-03", 1500.5)]), seed=L.config("PLA.FechaSistema", "2026-03-05"))

    # ------------------------------------------------------------------ hypothetical: a case-INSENSITIVE ISIN comparison (HANA is not; kills an otherwise unreachable branch)
    caso("isin-con-comparacion-insensible-a-mayusculas-npe-del-mapa", ["FM-CVC-03", "D-F3A-21", "D-NOR-9"],
         entrada([vc("cl0000000001", HOY, 1500.5, calculo=HOY)], todos="S"),
         seed=['ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ALTER COLUMN "ISIN" SET DATA TYPE VARCHAR_IGNORECASE(40);'])
