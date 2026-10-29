"""FMM-CVC cases, part A: contract, basic requirements, empty list, ISIN mode (FM-CVC-01..03) and the per-row state machine (FM-CVC-04).
Every case names the cards / defects it pins in "ref". Seeds use the fake universe of seed/base-cvc.sql: serie FM001A (CLP, 4 decimals, ISIN CL0000000001), FM001B (CLP, 2,
ISIN CL0000000002), FM001C (CLP, 6, the SAME ISIN as FM001B), FM002A (USD, 6, ISIN US0000000003), FM003A (CLP, 0), FM004A (CLP, 8, no ISIN), FM005A (company 2).
System date of company 1: 2026-03-02."""
import cases_cvc_lib as L
from cases_cvc_lib import ABSENT, Num, caso, entrada, vc, sin

HOY = "2026-03-02"
AYER = "2026-03-01"


def generar():
    # ------------------------------------------------------------------ FM-CVC-01 contract
    caso("real-una-serie-una-fecha", ["FM-CVC-01", "FM-CVC-08", "FM-CVC-09", "FM-CVC-10", "FM-CVC-11"], entrada([vc("FM001A", HOY, 1500.5)]))
    caso("contrato-con-isin-y-registro-fuera-de-modo-isin", ["FM-CVC-01", "FM-CVC-06"],
         entrada([vc("FM001A", HOY, 1500.5, isin="CL0000000001", calculo="2026-02-01", registro=7)]))
    caso("reglas-de-negocio-no-se-lee", ["FM-CVC-01"], entrada([vc("FM001A", HOY, 1500.5)], p_UsaReglasNegocio="S"))
    caso("reglas-de-negocio-ausente", ["FM-CVC-01"], entrada([vc("FM001A", HOY, 1500.5)], p_UsaReglasNegocio=ABSENT))
    caso("campos-desconocidos-se-ignoran", ["FM-CVC-01"], entrada([dict(vc("FM001A", HOY, 1500.5), OTRO="x")], p_Basura=[1, 2]))
    caso("pais-por-defecto-menos-uno", ["FM-CVC-01", "FM-CVC-07"], entrada([vc("FM001A", HOY, 105)]), seed=[L.historico(1, "FM001A", AYER, 100)])
    caso("pais-explicito-56-feriado-no-importa", ["FM-CVC-07"], entrada([vc("FM001A", "2026-03-01", 105)], p_IdPais=56),
         seed=[L.historico(1, "FM001A", "2026-02-28", 100)])
    caso("pais-nulo-explicito", ["FM-CVC-07"], entrada([vc("FM001A", HOY, 105)], p_IdPais=None), seed=[L.historico(1, "FM001A", AYER, 100)])
    caso("empresa-como-texto-se-convierte", ["FM-CVC-01"], entrada([vc("FM001A", HOY, 1500.5)], p_IdEmpresa="1", p_IdUsuario="7"))
    caso("empresa-no-numerica-lanza", ["FM-CVC-01"], entrada([vc("FM001A", HOY, 1500.5)], p_IdEmpresa="abc"))

    # ------------------------------------------------------------------ FM-CVC-02 basic requirements and empty list
    caso("usuario-inexistente", ["FM-CVC-02"], entrada([vc("FM001A", HOY, 1500.5)], p_IdUsuario=999))
    caso("usuario-y-modulo-invalidos-cuenta-errores", ["FM-CVC-02", "D-REQ-11"], entrada([vc("FM001A", HOY, 1500.5)], p_IdUsuario=999, p_IdModulo="XXX"))
    caso("usuario-no-vigente", ["FM-CVC-02"], entrada([vc("FM001A", HOY, 1500.5)], p_IdUsuario=9))
    caso("empresa-inexistente", ["FM-CVC-02"], entrada([vc("FM001A", HOY, 1500.5)], p_IdEmpresa=99))
    caso("empresa-nula", ["FM-CVC-02"], entrada([vc("FM001A", HOY, 1500.5)], p_IdEmpresa=None))
    caso("usuario-nulo-y-modulo-nulo", ["FM-CVC-02"], entrada([vc("FM001A", HOY, 1500.5)], p_IdUsuario=None, p_IdModulo=None))
    caso("lista-vacia-real", ["FM-CVC-02", "FM-CVC-08"], entrada([]))
    caso("lista-vacia-simulacion", ["FM-CVC-02"], entrada([], sim="S"))
    caso("lista-ausente-toma-el-defecto-vacio", ["FM-CVC-02"], entrada(ABSENT, p_ValorCuota=ABSENT))
    caso("requisitos-con-errores-y-lista-vacia-pisa-el-contador", ["FM-CVC-02", "D-REQ-11"], entrada([], p_IdUsuario=999, p_IdModulo="XXX"))
    caso("entrada-vacia-objeto", ["FM-CVC-02"], {})
    caso("lista-nula-explicita-npe", ["FM-CVC-02"], entrada(None))
    caso("requisitos-con-errores-no-escribe-ni-calcula", ["FM-CVC-02", "FM-CVC-08"], entrada([vc("FM001A", HOY, 1500.5), vc("FM001B", HOY, 10)], p_IdUsuario=999))
    caso("elemento-nulo-en-la-lista-npe", ["FM-CVC-04"], entrada([None]))

    # ------------------------------------------------------------------ FM-CVC-03 ISIN mode (p_AllFunds = S, case insensitive)
    isin = lambda i, v=1500.5, **k: vc(i, HOY, v, calculo=HOY, **k)  # noqa: E731
    caso("isin-conocido-real", ["FM-CVC-03", "D-F3A-21", "FM-CVC-06"], entrada([isin("CL0000000001")], todos="S"))
    caso("isin-minuscula-s-tambien-es-modo-isin", ["FM-CVC-03"], entrada([isin("CL0000000001")], todos="s"))
    caso("isin-conocido-simulacion", ["FM-CVC-03", "FM-CVC-08"], entrada([isin("CL0000000001")], sim="S", todos="S"))
    caso("isin-repetido-en-la-entrada-lanza", ["FM-CVC-03", "D-F3A-21", "D-NOR-9"], entrada([isin("CL0000000001"), isin("CL0000000001", 1600)], todos="S"))
    caso("isin-sin-serie-se-descarta-sin-mensaje", ["FM-CVC-03", "D-F3A-21"], entrada([isin("XX9999999999")], todos="S"))
    caso("isin-sin-serie-con-otro-conocido", ["FM-CVC-03", "D-F3A-21"], entrada([isin("XX9999999999"), isin("CL0000000001")], todos="S"))
    caso("isin-en-varias-series-genera-una-fila-por-serie", ["FM-CVC-03", "D-F3A-21", "FM-CVC-09", "FM-CVC-10"], entrada([isin("CL0000000002", 250.25)], todos="S"))
    caso("isin-en-lugar-del-id-de-serie-se-descarta", ["FM-CVC-03", "D-F3A-21"], entrada([isin("FM001A")], todos="S"))
    caso("isin-de-otra-empresa-serie-no-existe", ["FM-CVC-03", "FM-CVC-04", "D-F3A-21"], entrada([isin("CL0000000005")], todos="S"))
    caso("isin-moneda-distinta-no-se-valida", ["FM-CVC-03", "FM-CVC-04", "D-F3A-20"], entrada([isin("US0000000003", 12.5, moneda="CLP")], todos="S"))
    caso("isin-fechacalculo-propia-de-la-fila", ["FM-CVC-03", "FM-CVC-06", "FM-CVC-09", "FM-CVC-11"], entrada([vc("CL0000000001", HOY, 1500.5, calculo="2026-02-20")], todos="S"))
    caso("isin-fechacalculo-con-hora", ["FM-CVC-03", "FM-CVC-06"], entrada([vc("CL0000000001", HOY, 1500.5, calculo="2026-02-20 08:15:00")], todos="S"))
    caso("isin-fechacalculo-vacia-lanza", ["FM-CVC-03", "FM-CVC-06"], entrada([vc("CL0000000001", HOY, 1500.5)], todos="S"))
    caso("isin-fechacalculo-nula-npe", ["FM-CVC-03", "FM-CVC-06"], entrada([vc("CL0000000001", HOY, 1500.5, calculo=None)], todos="S"))
    caso("isin-fechacalculo-mal-formada-lanza", ["FM-CVC-03", "FM-CVC-06"], entrada([vc("CL0000000001", HOY, 1500.5, calculo="20-02-2026")], todos="S"))
    caso("isin-mensajevalidacion-error-fechacalculo-no-se-lee", ["FM-CVC-03", "FM-CVC-04", "FM-CVC-06"], entrada([vc("CL0000000001", HOY, 1500.5, msg="ERROR", calculo="basura")], todos="S"))
    caso("isin-lista-sin-ninguna-serie-no-escribe", ["FM-CVC-03", "D-F3A-21", "FM-CVC-08"], entrada([isin("XX9999999999"), isin("YY9999999999")], todos="S"))
    caso("isin-dos-isin-dos-series", ["FM-CVC-03", "FM-CVC-09", "FM-CVC-10"], entrada([isin("CL0000000001", 1500.5), isin("US0000000003", 12.5, moneda="USD")], todos="S"))
    caso("isin-vacio-coincide-con-todas-las-series-sin-isin", ["FM-CVC-03", "D-F3A-21"], entrada([isin("")], todos="S"))
    caso("isin-isin-del-pojo-se-pisa-con-el-de-la-serie", ["FM-CVC-03"], entrada([isin("CL0000000001", isin="NO-SE-USA")], todos="S"))

    # ------------------------------------------------------------------ FM-CVC-04 state of each row (the first that applies wins)
    caso("formato-invalido-mensajevalidacion-error", ["FM-CVC-04", "FM-CVC-12"], entrada([vc("FM001A", HOY, 1500.5, msg="ERROR")]))
    caso("formato-invalido-mensajevalidacion-nulo", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1500.5, msg=None)]))
    caso("formato-invalido-mensajevalidacion-ausente", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1500.5, msg=ABSENT)]))
    caso("formato-invalido-ok-en-minuscula", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1500.5, msg="ok")]))
    caso("duplicado-misma-serie-y-fecha-marca-las-dos", ["FM-CVC-04", "FM-CVC-13", "FM-CVC-12"], entrada([vc("FM001A", HOY, 1500.5), vc("FM001A", HOY, 1600)]))
    caso("duplicado-tres-apariciones", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1), vc("FM001A", HOY, 2), vc("FM001A", HOY, 3)]))
    caso("duplicado-fecha-con-hora-es-la-misma-fecha", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1500.5), vc("FM001A", HOY + " 00:00:00", 1600)]))
    caso("duplicado-no-aplica-a-otra-fecha-ni-otra-serie", ["FM-CVC-04", "FM-CVC-10"],
         entrada([vc("FM001A", HOY, 1500.5), vc("FM001A", AYER, 1499), vc("FM001B", HOY, 77.77)]))
    caso("duplicado-gana-a-serie-inexistente", ["FM-CVC-04"], entrada([vc("NOEXISTE", HOY, 1), vc("NOEXISTE", HOY, 2)]))
    caso("formato-invalido-gana-a-duplicado", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1, msg="ERROR"), vc("FM001A", HOY, 2)]))
    caso("duplicado-una-fila-ok-y-otra-error-de-formato-marca-la-ok", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1), vc("FM001A", HOY, 2, msg="X")]))
    caso("serie-inexistente", ["FM-CVC-04", "FM-CVC-12"], entrada([vc("NOEXISTE", HOY, 1500.5)]))
    caso("serie-en-minuscula-no-existe", ["FM-CVC-04"], entrada([vc("fm001a", HOY, 1500.5)]))
    caso("serie-de-otra-empresa-no-existe", ["FM-CVC-04"], entrada([vc("FM005A", HOY, 1500.5)]))
    caso("serie-vacia-no-existe", ["FM-CVC-04"], entrada([vc("", HOY, 1500.5)]))
    caso("serie-nula-npe", ["FM-CVC-04", "D-NOR-9"], entrada([vc(None, HOY, 1500.5)]))
    caso("serie-ausente-npe", ["FM-CVC-04"], entrada([sin(vc("FM001A", HOY, 1500.5), "IDSERIEFONDO")]))
    caso("valor-cero-invalido", ["FM-CVC-04", "D-F3A-23"], entrada([vc("FM001A", HOY, 0)]))
    caso("valor-cero-con-escala-invalido", ["FM-CVC-04", "D-F3A-23"], entrada([vc("FM001A", HOY, Num("0.00000000"))]))
    caso("valor-negativo-pasa", ["FM-CVC-04", "D-F3A-23", "FM-CVC-07"], entrada([vc("FM001A", HOY, -5.25)]), seed=[L.historico(1, "FM001A", AYER, 100)])
    caso("valor-nulo-npe", ["FM-CVC-04", "D-F3A-23"], entrada([vc("FM001A", HOY, None)]))
    caso("valor-ausente-npe", ["FM-CVC-04", "D-F3A-23"], entrada([sin(vc("FM001A", HOY, 1), "VALORCUOTA")]))
    caso("valor-nulo-en-fila-con-error-de-formato-igual-npe", ["FM-CVC-04", "D-F3A-23"], entrada([vc("FM001A", HOY, None, msg="ERROR")]))
    caso("valor-como-texto-numerico-se-convierte", ["FM-CVC-04"], entrada([vc("FM001A", HOY, "1500.5")]))
    caso("valor-no-numerico-lanza", ["FM-CVC-04"], entrada([vc("FM001A", HOY, "abc")]))
    caso("moneda-incorrecta-con-todos-N", ["FM-CVC-04", "D-F3A-20"], entrada([vc("FM001A", HOY, 1500.5, moneda="USD")], todos="N"))
    caso("moneda-no-se-valida-con-todos-nulo", ["FM-CVC-04", "D-F3A-20"], entrada([vc("FM001A", HOY, 1500.5, moneda="USD")], todos=None))
    caso("moneda-no-se-valida-con-todos-ausente", ["FM-CVC-04", "D-F3A-20"], entrada([vc("FM001A", HOY, 1500.5, moneda="USD")], todos=ABSENT))
    caso("moneda-no-se-valida-con-todos-n-minuscula", ["FM-CVC-04", "D-F3A-20"], entrada([vc("FM001A", HOY, 1500.5, moneda="USD")], todos="n"))
    caso("moneda-no-se-valida-con-todos-otro-texto", ["FM-CVC-04", "D-F3A-20"], entrada([vc("FM001A", HOY, 1500.5, moneda="USD")], todos="X"))
    caso("moneda-no-se-valida-con-todos-vacio", ["FM-CVC-04", "D-F3A-20"], entrada([vc("FM001A", HOY, 1500.5, moneda="USD")], todos=""))
    caso("moneda-correcta-con-todos-N", ["FM-CVC-04"], entrada([vc("FM002A", HOY, 12.5, moneda="USD")], todos="N"))
    caso("moneda-nula-con-todos-N-npe", ["FM-CVC-04", "D-F3A-20"], entrada([vc("FM001A", HOY, 1500.5, moneda=None)], todos="N"))
    caso("moneda-distinta-en-minuscula-con-todos-N", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1500.5, moneda="clp")], todos="N"))
    caso("valor-cero-gana-a-moneda-incorrecta", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 0, moneda="USD")]))
    caso("moneda-incorrecta-gana-a-fecha-futura", ["FM-CVC-04"], entrada([vc("FM001A", "2026-03-03", 5, moneda="USD")]))
    caso("fecha-futura", ["FM-CVC-04", "FM-CVC-12"], entrada([vc("FM001A", "2026-03-03", 1500.5)]))
    caso("fecha-igual-a-la-del-sistema-pasa", ["FM-CVC-04"], entrada([vc("FM001A", HOY, 1500.5)]), seed=[])
    caso("fecha-un-anio-atras-pasa", ["FM-CVC-04", "FM-CVC-10"], entrada([vc("FM001A", "2025-03-02", 1500.5)]))
    caso("fecha-con-hora-de-19-caracteres", ["FM-CVC-04", "FM-CVC-06"], entrada([vc("FM001A", "2026-03-02 00:00:00", 1500.5)]))
    caso("fecha-con-hora-futura-del-mismo-dia", ["FM-CVC-04"], entrada([vc("FM001A", "2026-03-02 23:59:59", 1500.5)]))
    caso("fecha-vacia-lanza", ["FM-CVC-04"], entrada([vc("FM001A", "", 1500.5)]))
    caso("fecha-nula-npe", ["FM-CVC-04"], entrada([vc("FM001A", None, 1500.5)]))
    caso("fecha-con-milisegundos-lanza", ["FM-CVC-04"], entrada([vc("FM001A", "2026-03-02 00:00:00.000", 1500.5)]))
    caso("fecha-dia-mes-anio-lanza", ["FM-CVC-04"], entrada([vc("FM001A", "02-03-2026", 1500.5)]))
    caso("fecha-invalida-en-fila-con-error-de-formato-igual-lanza", ["FM-CVC-04"], entrada([vc("FM001A", "2026-13-45", 1, msg="ERROR")]))
    caso("existe-valor-en-la-fecha-no-se-detecta", ["FM-CVC-04", "D-F3A-16", "FM-CVC-13"], entrada([vc("FM001A", HOY, 1500.5)]), seed=[L.vigente("FM001A", HOY, 1400)])
    caso("lote-todo-ok-cinco-series", ["FM-CVC-04", "FM-CVC-09", "FM-CVC-10", "FM-CVC-12"],
         entrada([vc("FM001A", HOY, 1500.5), vc("FM001B", HOY, 77.77), vc("FM001C", HOY, 88.888888), vc("FM003A", HOY, 99), vc("FM004A", HOY, 1.12345678)]))
    caso("lote-mixto-ok-y-error-contador-uno", ["FM-CVC-04", "FM-CVC-12", "FM-CVC-14", "D-F3A-22"],
         entrada([vc("FM001A", HOY, 1500.5), vc("NOEXISTE", HOY, 1)]))
    caso("lote-todo-error", ["FM-CVC-04", "FM-CVC-12", "FM-CVC-14"], entrada([vc("NOEXISTE", HOY, 1), vc("FM001A", "2026-03-09", 2), vc("FM001B", HOY, 0)]))
