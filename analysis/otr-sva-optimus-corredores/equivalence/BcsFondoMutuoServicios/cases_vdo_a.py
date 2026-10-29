"""FMM-VDO cases (ConsultarValorDolarFondoMutuo). Ids: FM-VDO-01..04 (cards, RULES_FONDOMUTUO_F3_A.md 8.7), D-F3A-14/15/25 and D-NOR-9 (section 9), matrix
VDO-C01..C04 (section 11). Inputs only: expected.json is recorded from the legacy by record_vdo.sh."""
from cases_vdo_lib import *  # noqa: F401,F403

D1 = "2026-03-10"
D2 = "2026-03-11"
D3 = "2026-03-12"


def sin(ent, *claves):
    d = dict(ent)
    for c in claves:
        d.pop(c, None)
    return d


def generar():
    # ---- FM-VDO-01 contract and parameters (VDO-C04)
    caso("lista-vacia-rama-tabla", "FM-VDO-01 VDO-C04: p_Series [] con UsaDolarObservado N => {P_OUT_ERRORESCONTADOR 0, P_OUT_RESULTADO []}: COMMIT, sin consultar la tabla ni la paridad",
         entrada([]))
    caso("lista-vacia-rama-observado", "FM-VDO-01 VDO-C04: p_Series [] con UsaDolarObservado S => igual: no se llama a CalcularParidad", entrada([]), seed=parametro_dolar("S"))
    caso("lista-ausente-es-vacia", "FM-VDO-01: la clave p_Series ausente => el constructor deja [] (como la lista vacia)", sin(entrada([]), "p_Series"))
    caso("lista-nula-explicita-npe", "FM-VDO-01 D-F3A-25: p_Series null explicito => size() sobre nulo: NullPointerException (HTTP 500), tras leer los dos parametros", entrada(None))
    caso("parametro-observado-ausente-aun-con-lista-vacia", "FM-VDO-01 VDO-C04: el parametro FONDOMUTUO/UsaDolarObservado se lee SIEMPRE y es obligatorio: sin el, lista vacia igual lanza",
         entrada([]), seed=parametro_dolar(None))
    caso("moneda-sistema-ausente-aun-con-lista-vacia", "FM-VDO-01 VDO-C04: PLATAFORMA/PLA.MonedaSistema se lee siempre y es obligatorio", entrada([]), seed=config_plataforma("PLA.MonedaSistema", None))
    caso("empresa-sin-configuracion", "FM-VDO-01 VDO-C04: empresa 2 sin registros de configuracion => el legacy lanza al leer (lo fija el oraculo)", entrada([sr("V004", "APO")], empresa=2))
    caso("empresa-ausente", "FM-VDO-01 VDO-C04: p_IdEmpresa ausente (nulo; es Long) => la lectura del parametro con empresa nula", sin(entrada([sr("V001")]), "p_IdEmpresa"))
    caso("usuario-y-modulo-no-se-usan", "FM-VDO-01/04: usuario inexistente y modulo basura no se validan: mismo resultado que con datos buenos", entrada([sr("V001", "APO")], usuario=987654, modulo="XXX"))
    caso("campos-desconocidos-se-ignoran", "FM-VDO-01: claves desconocidas (tambien dentro de cada serie) se ignoran con el Gson plano", entrada([sr("V001", "APO", Otro=1)], Extra=1))
    caso("empresa-como-texto-json-syntax", "FM-VDO-01: p_IdEmpresa 'abc' => JsonSyntaxException (NumberFormatException)", entrada([sr("V001")], empresa="abc"))
    caso("series-como-texto-json-syntax", "FM-VDO-01: p_Series como texto => JsonSyntaxException", entrada("abc"))
    caso("elemento-nulo-en-la-lista-npe", "FM-VDO-01/03: un elemento null dentro de p_Series => NullPointerException al leer serie.IdSerieFondo", entrada([None]))

    # ---- FM-VDO-02 branch UsaDolarObservado S (VDO-C01, VDO-C02)
    obs = parametro_dolar("S")
    caso("observado-una-fecha-tres-series", "FM-VDO-02 VDO-C01: 3 series con la misma Fecha 2026-03-10 => UNA entrada a CalcularParidad (p_IdTrabajo 1, USD -> CLP, MontoOrigen 1, IdInterno 0) y las 3 reciben 940.25",
         entrada([sr("V001", "APO", D1), sr("V002", "RES", D1), sr("V003", "APO", D1)]), seed=obs, colaboradores=paridad((0, 940.25)))
    caso("observado-dos-fechas-clave-no-unica", "FM-VDO-02 VDO-C02 D-F3A-14 D-NOR-9: 2 fechas distintas => 2 entradas con IdInterno 0 (la variable i no se incrementa); las 2 salidas llevan IdInterno 0 y List2Hash lanza 'key field is not unique'",
         entrada([sr("V001", "APO", D1), sr("V002", "APO", D2)]), seed=obs, colaboradores=paridad((0, 940.25), (0, 941.5)))
    caso("observado-tres-fechas-clave-no-unica", "FM-VDO-02 VDO-C02 D-F3A-14: 3 fechas distintas, la misma excepcion", entrada([sr("V001", "APO", D1), sr("V002", "APO", D2), sr("V003", "APO", D3)]),
         seed=obs, colaboradores=paridad((0, 940.25), (0, 941.5), (0, 942.0)))
    caso("observado-dos-fechas-doble-devuelve-una-salida", "FM-VDO-02 D-F3A-14: 2 fechas y un colaborador que devuelve UNA sola salida (IdInterno 0): no hay clave repetida y las dos series reciben ese valor",
         entrada([sr("V001", "APO", D1), sr("V002", "RES", D2)]), seed=obs, colaboradores=paridad((0, 940.25)))
    caso("observado-dos-fechas-una-sola-entrada-por-fecha", "FM-VDO-02: la deduplicacion es por Fecha (primera aparicion): V001 y V002 comparten D1, V003 trae D2 => 2 entradas y excepcion con dos salidas de id 0",
         entrada([sr("V001", "APO", D1), sr("V002", "RES", D1), sr("V003", "APO", D2)]), seed=obs, colaboradores=paridad((0, 940.25), (0, 941.5)))
    caso("observado-tipo-solicitud-se-ignora", "FM-VDO-02: en la rama observada IdTipoSolicitud no cuenta: APO y RES de la misma fecha reciben el mismo valor y se devuelven con su tipo",
         entrada([sr("V001", "APO", D1), sr("V001", "RES", D1)]), seed=obs, colaboradores=paridad((0, 940.25)))
    caso("observado-colaborador-devuelve-vacio", "FM-VDO-02: sin salidas del colaborador => ValorParidad 0 en todas", entrada([sr("V001", "APO", D1)]), seed=obs, colaboradores=paridad())
    caso("observado-colaborador-id-distinto-de-cero", "FM-VDO-02 D-F3A-14: el colaborador devuelve IdInterno 1 (el legacy busca el 0) => ValorParidad 0 aunque haya salida",
         entrada([sr("V001", "APO", D1)]), seed=obs, colaboradores=paridad((1, 940.25)))
    caso("observado-colaborador-falla", "FM-VDO-02: el colaborador lanza una excepcion => se propaga tal cual (HTTP 500, mensaje del colaborador)", entrada([sr("V001", "APO", D1)]), seed=obs,
         colaboradores={"CalcularParidad": {"defecto": {"p_out_CalculosSalida": []}, "respuestas": [{"error": "Paridad no disponible para la fecha"}]}})
    caso("observado-colaborador-monto-nulo", "FM-VDO-02: salida con MontoDestino nulo => ValorParidad nulo (la clave se omite en el JSON plano)", entrada([sr("V001", "APO", D1)]), seed=obs,
         colaboradores={"CalcularParidad": {"defecto": {"p_out_CalculosSalida": [{"IdTrabajo": 1, "IdInterno": 0, "MontoDestino": None}]}}})
    caso("observado-colaborador-contador-de-errores-se-ignora", "FM-VDO-02: el colaborador informa errores pero VDO no los lee: usa igual la salida", entrada([sr("V001", "APO", D1)]), seed=obs,
         colaboradores={"CalcularParidad": {"defecto": {"p_out_CalculosSalida": [{"IdTrabajo": 1, "IdInterno": 0, "MontoDestino": 933.3}], "P_OUT_ERRORESCONTADOR": 3}}})
    caso("observado-fecha-nula", "FM-VDO-02: dos series con Fecha null explicito => una sola entrada con Fecha nula (HashMap acepta nulo); resultado con Fecha omitida",
         entrada([sr("V001", "APO", None), sr("V002", "APO", None)]), seed=obs, colaboradores=paridad((0, 940.25)))
    caso("observado-fecha-vacia-y-texto-libre", "FM-VDO-02: la Fecha no se valida ('' y 'manana' son claves distintas) => dos entradas, dos salidas de id 0, excepcion",
         entrada([sr("V001", "APO", ""), sr("V002", "APO", "manana")]), seed=obs, colaboradores=paridad((0, 1.0), (0, 2.0)))
    caso("observado-moneda-sistema-usd", "FM-VDO-02: la moneda destino sale de PLA.MonedaSistema (aqui USD): IdMonedaDestino USD en la entrada del colaborador", entrada([sr("V001", "APO", D1)]),
         seed=obs + config_plataforma("PLA.MonedaSistema", "USD"), colaboradores=paridad((0, 1.0)))
    caso("observado-valor-con-escala", "FM-VDO-02: ValorParidad toma el MontoDestino del colaborador sin redondear (940.123456789)", entrada([sr("V001", "APO", D1)]), seed=obs, colaboradores=paridad((0, 940.123456789)))
    caso("observado-minuscula-no-es-s", "FM-VDO-02 D-F3A-15: UsaDolarObservado 's' en minuscula no es 'S' (equals) => rama de tabla, el colaborador no se llama", entrada([sr("V001", "APO", D1)]),
         seed=parametro_dolar("s"), colaboradores=paridad((0, 940.25)))
    caso("observado-con-espacio-no-es-s", "FM-VDO-02: ' S' con espacio => rama de tabla", entrada([sr("V001", "RES", D1)]), seed=parametro_dolar(" S"), colaboradores=paridad((0, 940.25)))
    caso("observado-vacio-es-rama-tabla", "FM-VDO-02: '' => rama de tabla", entrada([sr("V001", "RES", D1)]), seed=parametro_dolar(""))
    caso("observado-misma-serie-dos-tipos", "FM-VDO-02: serie repetida con la misma fecha => una entrada", entrada([sr("V001", "APO", D1), sr("V001", "APO", D1)]), seed=obs, colaboradores=paridad((0, 940.25)))
    caso("observado-treinta-series-una-fecha", "FM-VDO-02 VDO-C01: 30 series con la misma fecha: una sola entrada y 30 resultados en el orden de entrada (determinismo de 20 corridas)",
         entrada([sr("V%03d" % (900 + i), "APO" if i % 2 else "RES", D1) for i in range(30)]), seed=obs, colaboradores=paridad((0, 940.25)))

    # ---- FM-VDO-03 branch of the table (VDO-C03)
    caso("tabla-aporte", "FM-VDO-03 VDO-C03: APO => ValorDolarAporte de V001 (930.10 con escala 8: 930.10000000)", entrada([sr("V001", "APO")]))
    caso("tabla-rescate", "FM-VDO-03 VDO-C03: RES => ValorDolarRescate de V001 (925.40)", entrada([sr("V001", "RES")]))
    caso("tabla-tipo-vacio-es-rescate", "FM-VDO-03 D-F3A-15: tipo '' => dolar de rescate", entrada([sr("V001", "")]))
    caso("tabla-tipo-nulo-es-rescate", "FM-VDO-03 D-F3A-15: tipo null => dolar de rescate (\"APO\".equals(null) falso)", entrada([sr("V001", None)]))
    caso("tabla-tipo-minuscula-es-rescate", "FM-VDO-03 D-F3A-15: 'apo' => rescate (equals exacto)", entrada([sr("V001", "apo")]))
    caso("tabla-tipo-desconocido-es-rescate", "FM-VDO-03 D-F3A-15: 'XYZ' => rescate", entrada([sr("V001", "XYZ")]))
    caso("tabla-tipo-con-espacio-es-rescate", "FM-VDO-03 D-F3A-15: ' APO' => rescate", entrada([sr("V001", " APO")]))
    caso("tabla-todos-los-tipos-en-una-peticion", "FM-VDO-03 VDO-C03: V001 con APO, RES, '', null, 'apo' y V001 repetida: el resultado conserva el orden y la repeticion de la entrada",
         entrada([sr("V001", "APO"), sr("V001", "RES"), sr("V001", ""), sr("V001", None), sr("V001", "apo"), sr("V001", "APO")]))
    caso("tabla-fecha-se-ignora", "FM-VDO-03 D-F3A-15: la Fecha no cuenta: fechas distintas, nulas o basura dan el mismo valor y se devuelven tal cual",
         entrada([sr("V001", "APO", "1999-01-01"), sr("V001", "APO", None), sr("V001", "APO", "no es fecha")]))
    caso("tabla-serie-desconocida-es-cero", "FM-VDO-03 VDO-C03: serie inexistente => ValorParidad 0 (sin error)", entrada([sr("NOEXISTE", "APO"), sr("V001", "APO")]))
    caso("tabla-serie-nula", "FM-VDO-03: IdSerieFondo null explicito => el IN recibe un nulo, la serie no existe => 0", entrada([sr(None, "APO")]))
    caso("tabla-serie-sin-fila-de-tipo-de-cambio", "FM-VDO-03: V003 existe pero no tiene fila en ValorTipoCambio: el LEFT JOIN + COALESCE da 0 en aporte y en rescate", entrada([sr("V003", "APO"), sr("V003", "RES")]))
    caso("tabla-valores-cero", "FM-VDO-03: V002 con tipos de cambio 0 => 0 (con escala 8: 0E-8 en el texto de Gson)", entrada([sr("V002", "APO"), sr("V002", "RES")]))
    caso("tabla-escala-ocho", "FM-VDO-03: V005 1000.12345678 / 999.99999999 sin redondeo", entrada([sr("V005", "APO"), sr("V005", "RES")]))
    caso("tabla-serie-de-otra-empresa", "FM-VDO-03: V004 es de la empresa 2: pedida con empresa 1 no sale del SELECT => 0", entrada([sr("V004", "APO")]))
    caso("tabla-empresa-2-sin-configuracion", "FM-VDO-03: pedida con empresa 2: sin configuracion del modulo para la empresa 2 el legacy no llega a la consulta", entrada([sr("V004", "APO")], empresa=2))
    caso("tabla-empresa-2-con-configuracion", "FM-VDO-03: empresa 2 con sus dos parametros: V004 APO 910.5 y RES 905.5",
         entrada([sr("V004", "APO"), sr("V004", "RES")], empresa=2),
         seed=[insert(PARAM, {"IdEmpresa": 2, "NombreTipoParametro": "UsaDolarObservado", "ValorParametro": "N"}),
               insert('"BSCL.Plataforma.BD::PLA.Configuracion"', {"IdEmpresa": 2, "CodigoParametro": "PLA.MonedaSistema", "Valor": "CLP"}),
               insert('"BSCL.Plataforma.BD::PLA.Configuracion"', {"IdEmpresa": 2, "CodigoParametro": "PLA.LocalizacionSistema", "Valor": "es"})])
    caso("tabla-base-fm001a", "FM-VDO-03: la serie de la base (FM001A) con 900 / 890", entrada([sr("FM001A", "APO"), sr("FM001A", "RES")]))
    caso("tabla-dos-filas-de-tipo-de-cambio-list2hash", "FM-VDO-03 D-NOR-9 SINTETICO: la clave de ValorTipoCambio es IdSerieFondo en HANA; con dos filas (clave suelta en H2) List2Hash lanza 'key field is not unique'",
         entrada([sr("V001", "APO")]), seed=['ALTER TABLE %s DROP PRIMARY KEY;' % TIPOCAMBIO, insert(TIPOCAMBIO, {"IdSerieFondo": "V001", "IdEmpresa": 1, "ValorDolarAporte": 1.0, "ValorDolarRescate": 2.0})])
    caso("tabla-sin-tipo-solicitud-ni-fecha", "FM-VDO-03: series sin las claves IdTipoSolicitud ni Fecha (nulas): rescate y salida sin esas claves", entrada([{"IdSerieFondo": "V001"}]))
    caso("tabla-treinta-series-en-in", "FM-VDO-03 VDO-C03: 30 series (algunas inexistentes) en el IN: orden de entrada y ceros para las desconocidas",
         entrada([sr("V001" if i % 3 == 0 else "V005" if i % 3 == 1 else "NOX%02d" % i, "APO" if i % 2 else "RES") for i in range(30)]))
    caso("tabla-serie-con-comilla", "FM-VDO-03: texto con comilla y espacios en IdSerieFondo (parametro enlazado, no concatenado) => 0", entrada([sr("V001' OR '1'='1", "APO")]))

    # ---- FM-VDO-04 what VDO does not do
    caso("sin-validaciones-ni-perfil", "FM-VDO-04 VDO-C04: usuario 0, modulo vacio y series inexistentes: sin requisitos basicos ni perfil, sin mensajes; el contador sale 0 y se hace COMMIT",
         entrada([sr("NOEXISTE", "APO")], usuario=0, modulo=""))
    caso("tablas-no-cambian", "FM-VDO-04: solo lectura: las tablas del volcado quedan intactas y no hay tablas locales ni commits autonomos", entrada([sr("V001", "APO"), sr("V005", "RES")]))
