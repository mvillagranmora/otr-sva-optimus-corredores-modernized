"""FMM-FSA cases (ConsultarFechaSolicitudAporteRescate). Ids: FM-FSA-01..08 (cards, RULES_FONDOMUTUO_F3_A.md 8.1), D-F3A-01/02 and D-NOR-9 (section 9),
matrix FSA-C01..C09 (section 11). Inputs only: expected.json is recorded from the legacy by record_fsa.sh."""
from cases_fsa_lib import *  # noqa: F401,F403

M = "2026-03-02"      # Monday = the fixed clock
SAT = "2026-03-07"
HOL = "2026-03-05"    # holiday of country 56 (Thursday)


def generar():
    # ---- FM-FSA-01 / 04 / 06 (FSA-C01, C02): one request, both profiles, both types
    caso("apo-nacional-comercial", "FM-FSA-01/04/06 FSA-C01: 1 APO FSN01, perfil comercial: dias de aporte de la serie, estados A",
         entrada([sol(1, "FSN01", "APO")]))
    caso("res-nacional-backoffice", "FM-FSA-06 FSA-C02: 1 RES FSN01 perfil backoffice (usuario 8): dias de rescate; un solo elemento no usa la regla 4/2",
         entrada([sol(1, "FSN01", "RES")], usuario=8))
    caso("res-nacional-comercial", "FM-FSA-06: 1 RES FSN02 perfil comercial: dias de rescate de la serie (3/2/4/0)", entrada([sol(7, "FSN02", "RES")]))
    caso("apo-extranjero-backoffice", "FM-FSA-06: 1 APO extranjero (FSX01) perfil backoffice: estados C/C, dias de aporte 1/1/2/0",
         entrada([sol(1, "FSX01", "APO")], usuario=8))
    caso("res-extranjero-unica-dias-de-la-serie", "FM-FSA-05/06 D-F3A-01: 1 solo RES extranjero => fondosExtranjeros=N: dias de la serie (5/3/6), no 4/2/1/2",
         entrada([sol(1, "FSX01", "RES")]))
    caso("res-extranjero-unica-backoffice", "FM-FSA-06: 1 RES extranjero perfil backoffice (estado C: cambia la rama A/A del calculo de certificacion)",
         entrada([sol(1, "FSX01", "RES")], usuario=8))
    caso("tipo-no-apo-es-rescate", "FM-FSA-06: tipo nulo en FSA va por la rama de rescate; el colaborador lo rechaza (Tipo Solicitud no valida)",
         entrada([sol(1, "FSN01", None)]))
    caso("tipo-minuscula-apo-es-rescate", "FM-FSA-06: 'apo' en minuscula NO es APO (equals): rama de rescate en FSA; el colaborador rechaza el tipo",
         entrada([sol(1, "FSN01", "apo")]))
    caso("simular-y-reglas-se-ignoran", "FM-FSA-01: p_SimularOperacion=S y p_UsaReglasNegocio nulo no se leen: mismo resultado que el caso 01",
         entrada([sol(1, "FSN01", "APO")], p_SimularOperacion="S", p_UsaReglasNegocio=None))

    # ---- FM-FSA-05/06 D-F3A-01 (FSA-C03..C06): sets of more than one request
    caso("dos-res-extranjeras-mismo-emisor", "FM-FSA-05/06 FSA-C03: 2 RES extranjeras mismo emisor => ret 2, valuta cuota 1, cert 2 (no los de la serie)",
         entrada([sol(1, "FSX01", "RES", moneda="USD"), sol(2, "FSX02", "RES", moneda="USD")]))
    caso("dos-res-extranjeras-emisores-distintos", "FM-FSA-05/06 FSA-C04: 2 RES extranjeras con emisores EXT1/EXT2 => ret 4, valuta cuota 1, cert 2",
         entrada([sol(1, "FSX01", "RES", moneda="USD"), sol(2, "FSX03", "RES", moneda="USD")]))
    caso("tres-res-extranjeras-un-emisor-distinto", "FM-FSA-05/06: 3 RES extranjeras, el tercero de otro emisor => mismoEmisor=N para todas (4)",
         entrada([sol(1, "FSX01", "RES"), sol(2, "FSX02", "RES"), sol(3, "FSX03", "RES")]))
    caso("dos-res-una-nacional", "FM-FSA-05 FSA-C05: 2 RES, una serie nacional => fondosExtranjeros=N: dias de cada serie (no la regla 4/2)",
         entrada([sol(1, "FSX01", "RES", moneda="USD"), sol(2, "FSN01", "RES", moneda="USD")]))
    caso("dos-res-nacionales-emisores-distintos", "FM-FSA-05: 2 RES nacionales emisores AGF1/AGF2: dias de la serie de cada una",
         entrada([sol(1, "FSN01", "RES"), sol(2, "FSN02", "RES")]))
    caso("aporte-y-rescate-extranjeros-mezclados", "FM-FSA-05/06 FSA-C06 D-F3A-01: APO+RES en una lista de 2 (todas extranjeras mismo emisor): el APO "
         "usa dias de aporte, el RES la regla 2/1/2 (la lista no es una reinversion y se trata como tal)",
         entrada([sol(1, "FSX01", "APO", moneda="USD"), sol(2, "FSX02", "RES", moneda="USD")]))
    caso("dos-aportes-extranjeros", "FM-FSA-05/06: 2 APO extranjeros: el aporte no usa la regla 4/2: dias de aporte de la serie",
         entrada([sol(1, "FSX01", "APO", moneda="USD"), sol(2, "FSX02", "APO", moneda="USD")]))
    caso("dos-solicitudes-misma-serie", "FM-FSA-03/05: la misma serie repetida: el IN deduplica la lectura, la regla de lista de 2 se aplica igual",
         entrada([sol(1, "FSX01", "RES"), sol(2, "FSX01", "RES")]))
    caso("emisor-en-mayusculas-minusculas", "FM-FSA-05: la comparacion de emisor es equalsIgnoreCase (EXT1 vs EXT1 en dos series; FondoExtranjero 's' minuscula no baja)",
         entrada([sol(1, "FSX01", "RES"), sol(2, "FSX02", "RES")]),
         seed=[update(FONDO, {"IdEmisor": "ext1"}, "\"IdFondo\" = '52'"), update(FONDO, {"FondoExtranjero": "s"}, "\"IdFondo\" = '52'")])
    caso("fondo-extranjero-n-minuscula-baja", "FM-FSA-05: FondoExtranjero 'n' en minuscula (equalsIgnoreCase) baja fondosExtranjeros a N",
         entrada([sol(1, "FSX01", "RES"), sol(2, "FSX02", "RES")]), seed=[update(FONDO, {"FondoExtranjero": "n"}, "\"IdFondo\" = '52'")])
    caso("lista-de-cuatro-mezcla-extranjera-nacional-dolar", "FM-FSA-05/06/07: 4 solicitudes (2 extranjeras, nacional dolar, nacional) mezcladas: una nacional "
         "baja fondosExtranjeros; monedas distintas => Spot S",
         entrada([sol(1, "FSX01", "RES", moneda="USD"), sol(2, "FSD01", "RES", moneda="USD"), sol(3, "FSN01", "APO", moneda="CLP"), sol(4, "FSX02", "RES", moneda="USD")]))

    # ---- FM-FSA-07 (FSA-C07): RescateGeneraSpotCertificacion (FSX04 / FSX01+FSX03 make the Spot adjustment of the certification date visible)
    caso("spot-una-solicitud-usd-clp", "FM-FSA-07 FSA-C07: 1 solicitud IdMoneda USD / IdMonedaPago CLP => spot S (la certificacion se adelanta al dia habil anterior a la liquidacion)",
         entrada([sol(1, "FSX04", "RES", moneda="USD", pago="CLP")]))
    caso("spot-una-solicitud-usd-usd", "FM-FSA-07: 1 solicitud USD/USD => spot N (certificacion sin ajuste)", entrada([sol(1, "FSX04", "RES", moneda="USD", pago="USD")]))
    caso("spot-una-solicitud-pago-vacio", "FM-FSA-07: 1 solicitud USD / pago '' => spot N", entrada([sol(1, "FSX04", "RES", moneda="USD", pago="")]))
    caso("spot-una-solicitud-moneda-vacia", "FM-FSA-07: 1 solicitud moneda '' / pago CLP => spot N (tercera condicion)",
         entrada([sol(1, "FSX04", "RES", moneda="", pago="CLP")]))
    caso("spot-una-solicitud-sin-monedas", "FM-FSA-07: 1 solicitud sin IdMoneda ni IdMonedaPago (ambas '' por el constructor) => spot N", entrada([sol(1, "FSX04", "RES")]))
    caso("spot-aporte-tambien", "FM-FSA-07: el calculo vale tambien para un APO (USD/CLP): el flag va en S pese al nombre",
         entrada([sol(1, "FSX04", "APO", moneda="USD", pago="CLP")]))
    caso("spot-mayusculas-minusculas", "FM-FSA-07: 'usd' vs 'USD' (equalsIgnoreCase) => iguales => spot N", entrada([sol(1, "FSX04", "RES", moneda="usd", pago="USD")]))
    caso("spot-lista-monedas-distintas", "FM-FSA-07 FSA-C07: 2 solicitudes con IdMoneda USD y CLP => spot S en las dos (emisores distintos: la certificacion se ajusta)",
         entrada([sol(1, "FSX01", "RES", emision="2026-03-09", moneda="USD", pago="USD"), sol(2, "FSX03", "RES", emision="2026-03-09", moneda="CLP", pago="CLP")]))
    caso("spot-lista-misma-moneda-pago-distinto", "FM-FSA-07: 2 solicitudes con la misma IdMoneda: el pago se ignora en listas => spot N (sin ajuste)",
         entrada([sol(1, "FSX01", "RES", emision="2026-03-09", moneda="USD", pago="CLP"), sol(2, "FSX03", "RES", emision="2026-03-09", moneda="USD", pago="CLP")]))
    caso("spot-lista-segunda-moneda-nula", "FM-FSA-07: segunda solicitud con IdMoneda nula (equalsIgnoreCase(null)=false): diferentes => S, sin excepcion",
         entrada([sol(1, "FSX01", "RES", emision="2026-03-09", moneda="USD"), sol(2, "FSX03", "RES", emision="2026-03-09", moneda=None, IdMoneda=None)]))
    caso("spot-lista-primera-moneda-nula-npe", "FM-FSA-07 D-F3A-01: lista de 2 con la PRIMERA IdMoneda nula => idMoneda.equalsIgnoreCase lanza NullPointerException",
         entrada([sol(1, "FSX01", "RES", IdMoneda=None), sol(2, "FSX03", "RES", moneda="USD")]))
    caso("spot-una-solicitud-moneda-nula-npe", "FM-FSA-07 D-F3A-01: 1 solicitud con IdMoneda nula => NullPointerException",
         entrada([sol(1, "FSX04", "RES", pago="CLP", IdMoneda=None)]))
    caso("spot-una-solicitud-pago-nulo", "FM-FSA-07: 1 solicitud con IdMonedaPago nula: equalsIgnoreCase(null) false, '' no es nulo => spot S",
         entrada([sol(1, "FSX04", "RES", moneda="USD", IdMonedaPago=None)]))

    # ---- FM-FSA-02 (FSA-C08): validations, in order
    caso("sin-perfil", "FM-FSA-02 FSA-C08: usuario 9 sin perfil => contador 1, PERFIL_INCORRECTO, resultado vacio, sin leer series",
         entrada([sol(1, "FSN01", "APO"), sol(2, "FSN02", "RES")], usuario=9))
    caso("lista-vacia", "FM-FSA-02 FSA-C08: lista vacia => contador 1, NO_SE_INGRESARON_SOLICITUDES", entrada([]))
    caso("lista-vacia-mensaje-sin-diccionario", "FM-FSA-02: lista vacia sin la entrada del diccionario (texto del mensaje sale como lo resuelva el legacy)",
         entrada([]), seed=['DELETE FROM %s WHERE "CodDiccionario" = \'NO_SE_INGRESARON_SOLICITUDES\';' % DICC])
    caso("lista-nula-npe", "FM-FSA-02: p_SolicitudAporteRescate nula (JSON null) con perfil valido => NullPointerException en size()", entrada(None))
    caso("lista-ausente-queda-vacia", "FM-FSA-02: la clave p_SolicitudAporteRescate ausente: el constructor deja la lista vacia => NO_SE_INGRESARON_SOLICITUDES",
         {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_SimularOperacion": "N", "p_UsaReglasNegocio": "S"})
    caso("lista-nula-sin-perfil", "FM-FSA-02: lista nula pero sin perfil: el perfil se valida antes, no hay excepcion", entrada(None, usuario=9))
    caso("requisitos-usuario-no-existe", "FM-FSA-02(1): RevisarRequisitosBasicos con error (usuario 99 inexistente) => contador 1 y sus mensajes, sin leer el perfil",
         entrada([sol(1, "FSN01", "APO")], usuario=99))
    caso("requisitos-usuario-no-vigente", "FM-FSA-02(1): usuario 11 no vigente => USUARIO_NO_VIGENTE",
         entrada([sol(1, "FSN01", "APO")], usuario=11))
    caso("requisitos-modulo-no-existe", "FM-FSA-02(1): modulo XXX inexistente => MODULO_NO_EXISTE", entrada([sol(1, "FSN01", "APO")], p_IdModulo="XXX"))
    caso("requisitos-modulo-y-usuario-juntos", "FM-FSA-02(1): dos errores de requisitos (usuario y modulo) => se copian los dos mensajes, contador 1 (no 2)",
         entrada([sol(1, "FSN01", "APO")], usuario=99, p_IdModulo="XXX"))
    caso("requisitos-empresa-no-existe", "FM-FSA-02(1): empresa 77 inexistente => EMPRESA_NO_EXISTE (y usuario inexistente para esa empresa)",
         entrada([sol(1, "FSN01", "APO")], p_IdEmpresa=77))
    caso("requisitos-empresa-nula", "FM-FSA-02(1): p_IdEmpresa nula => EMPRESA_VACIA (mensaje de texto fijo)", entrada([sol(1, "FSN01", "APO")], p_IdEmpresa=None))
    caso("requisitos-usuario-nulo", "FM-FSA-02(1): p_IdUsuario nulo => USUARIO_VACIO", entrada([sol(1, "FSN01", "APO")], usuario=None))
    caso("requisitos-modulo-nulo", "FM-FSA-02(1): p_IdModulo nulo => MODULO_VACIO", entrada([sol(1, "FSN01", "APO")], p_IdModulo=None))
    caso("usuario-con-dos-perfiles-top-1", "FM-FSA-02 D-REQ-8: usuario 10 con perfil comercial y backoffice: el SELECT TOP 1 sin ORDER BY decide (orden de H2 = de insercion; "
         "el orden de HANA NO esta probado)", entrada([sol(1, "FSX01", "RES")], usuario=10))

    # ---- FM-FSA-03/04 (FSA-C09): series and country
    caso("serie-inexistente-una-solicitud", "FM-FSA-03/04 FSA-C09: 1 solicitud con serie inexistente: FSA arma la entrada con 4 campos, el colaborador falla (NPE)",
         entrada([sol(1, "NOEXISTE", "APO")]))
    caso("serie-inexistente-dos-solicitudes", "FM-FSA-03/05 FSA-C09 D-NOR-9: 2 solicitudes, la primera con serie inexistente => NPE en FSA",
         entrada([sol(1, "NOEXISTE", "RES"), sol(2, "FSN01", "RES")]))
    caso("serie-inexistente-segunda-de-dos", "FM-FSA-05: 2 solicitudes, la segunda con serie inexistente => NPE al recorrer la lista",
         entrada([sol(1, "FSN01", "RES"), sol(2, "NOEXISTE", "RES")]))
    caso("serie-nula", "FM-FSA-03: IdSerieFondo nula en una sola solicitud: el IN lleva nulo; sin configuracion; falla el colaborador",
         entrada([sol(1, None, "RES")]))
    caso("serie-con-moneda-sin-fila", "FM-FSA-03: la moneda del fondo no existe en PLA.Moneda (LEFT JOIN) => IdPaisMoneda nulo (se trata como 0 en el colaborador)",
         entrada([sol(1, "FSX01", "RES")]), seed=['DELETE FROM %s WHERE "IdMoneda" = \'USD\';' % MONEDA])
    caso("serie-dos-filas-toMap-IllegalState", "FM-FSA-03 D-NOR-9: dos filas para la misma serie (PLA.Moneda con IdMoneda repetido; SINTETICO: en HANA la clave de "
         "PLA.Moneda lo impediria) => IllegalStateException de Collectors.toMap",
         entrada([sol(1, "FSN01", "RES")]), seed=[insert(MONEDA, {"IdMoneda": "CLP", "IdPais": 56})])
    caso("fondo-nacional-dolar-rescate", "FM-FSA-03/06: serie FSD01 (pais del sistema y moneda adicional USD) => FondoNacionalDolar S (el colaborador repasa fechas)",
         entrada([sol(1, "FSD01", "RES", moneda="USD", pago="CLP")]))
    caso("fondo-nacional-dolar-aporte", "FM-FSA-03/06: aporte de serie nacional-dolar", entrada([sol(1, "FSD01", "APO")]))
    caso("moneda-adicional-ausente", "FM-FSA-03: sin PLA.MonedaAdicional el LEFT JOIN da nulo y FondoNacionalDolar queda N para FSD01",
         entrada([sol(1, "FSD01", "RES")]), seed=config_plataforma("PLA.MonedaAdicional", None))
    caso("pais-sistema-distinto", "FM-FSA-03: PLA.PaisSistema=840: FSX01 (pais 840) con moneda adicional USD => FondoNacionalDolar S",
         entrada([sol(1, "FSX01", "RES", moneda="USD")]), seed=config_plataforma("PLA.PaisSistema", "840"))
    caso("empresa-pais-840", "FM-FSA-04: ConsultarEmpresa (doble) devuelve IdPais 840: el pais de la empresa viaja al calculo de fechas",
         entrada([sol(1, "FSX01", "RES")]), colaboradores=empresa(IdPais=840))
    caso("empresa-sin-pais", "FM-FSA-04: ConsultarEmpresa devuelve IdPais nulo => p_IdPais nulo al colaborador",
         entrada([sol(1, "FSN01", "RES")]), colaboradores={"ConsultarEmpresa": {"defecto": {"IdEmpresa": 1, "Codigo": "BCS"}}})
    caso("empresa-falla", "FM-FSA-04: ConsultarEmpresa lanza una Exception => se propaga tal cual (HTTP 500)",
         entrada([sol(1, "FSN01", "RES")]), colaboradores={"ConsultarEmpresa": {"respuestas": [{"error": "empresa no disponible"}]}})
    caso("empresa-se-consulta-dos-veces", "FM-FSA-04/08: ConsultarEmpresa se llama desde FSA y desde el colaborador (conteo de llamadas fijado en el golden)",
         entrada([sol(1, "FSN01", "APO")]))

    # ---- FM-FSA-08 / dates (what the collaborator does with the entry): emission dates, calendar
    caso("emision-sabado", "FM-FSA-08: emision en sabado (2026-03-07): la fecha de digitacion pasa al siguiente dia habil",
         entrada([sol(1, "FSN01", "APO", emision=SAT)]))
    caso("emision-feriado", "FM-FSA-08: emision en feriado del pais 56 (2026-03-05): salto de feriado en las fechas",
         entrada([sol(1, "FSN01", "RES", emision=HOL)]))
    caso("emision-con-hora", "FM-FSA-08: FechaEmision con hora (19 caracteres) se pasa tal cual al colaborador", entrada([sol(1, "FSN01", "RES", emision="2026-03-05 10:20:30")]))
    caso("emision-vacia", "FM-FSA-08: FechaEmision vacia => el colaborador lanza (DateTimeParseException)", entrada([sol(1, "FSN01", "RES", emision="")]))
    caso("emision-nula", "FM-FSA-08: FechaEmision nula => excepcion del colaborador", entrada([sol(1, "FSN01", "RES", emision=None)]))
    caso("emision-formato-invalido", "FM-FSA-08: FechaEmision '02/03/2026' => excepcion del colaborador", entrada([sol(1, "FSN01", "RES", emision="02/03/2026")]))
    caso("res-nacional-viernes-valutas", "FM-FSA-06/08: RES FSN02 emitido el viernes 2026-03-06: valutas 2/4/3 cruzan el fin de semana",
         entrada([sol(1, "FSN02", "RES", emision="2026-03-06")]))
    caso("aporte-lunes-money-market-no", "FM-FSA-08: APO emitido un lunes (regla del lunes del colaborador, serie no money market)", entrada([sol(1, "FSN02", "APO")]))

    # ---- IdInterno (D-NOR-9) and ordering
    caso("idinterno-nulo", "FM-FSA-01 D-NOR-9: IdInterno nulo en una solicitud: se copia tal cual; el resultado lo define el colaborador",
         entrada([sol(None, "FSN01", "RES")]))
    caso("idinterno-nulo-dos-solicitudes", "D-NOR-9: IdInterno nulo en una lista de 2 (la regla de lista corre igual)", entrada([sol(None, "FSX01", "RES"), sol(2, "FSX02", "RES")]))
    caso("idinterno-cero", "FM-FSA-01: IdInterno 0 (el JSON de prueba del legacy lo usa)", entrada([sol(0, "FSN01", "RES")]))
    caso("idinterno-repetido-dos-solicitudes", "D-NOR-9: IdInterno repetido en dos solicitudes distintas: el colaborador indexa por IdInterno",
         entrada([sol(5, "FSX01", "RES"), sol(5, "FSX02", "RES")]))
    caso("idinterno-negativo", "FM-FSA-01: IdInterno negativo", entrada([sol(-3, "FSN01", "APO")]))
    caso("orden-de-salida-ids-desordenados", "FM-FSA-08: el orden de salida es el del colaborador: ids 30,10,20 de series distintas",
         entrada([sol(30, "FSN02", "RES"), sol(10, "FSN01", "APO"), sol(20, "FSX01", "RES")]))
    caso("diez-solicitudes-orden-y-determinismo", "FM-FSA-08 parallelStream: 10 solicitudes mezcladas (orden de salida estable en 20 corridas)",
         entrada([sol(i, ["FSN01", "FSN02", "FSX01", "FSX02", "FSD01"][i % 5], "APO" if i % 3 == 0 else "RES", emision=["2026-03-02", "2026-03-04", "2026-03-06"][i % 3],
                      moneda="USD" if i % 2 else "CLP") for i in range(1, 11)]))
    caso("veinticinco-solicitudes-determinismo", "FM-FSA-08 parallelStream: 25 solicitudes de la misma serie y tipo con ids ascendentes (mapa, lista y salida paralelos)",
         entrada([sol(i, "FSX01" if i % 2 else "FSX02", "RES", moneda="USD") for i in range(1, 26)]))
    caso("treinta-aportes-determinismo", "FM-FSA-08 parallelStream: 30 aportes con series de 5 emisores y 3 fechas",
         entrada([sol(100 + i, ["FSN01", "FSN02", "FSX01", "FSX02", "FSX03", "FSD01"][i % 6], "APO", emision=["2026-03-02", "2026-03-03", "2026-03-04"][i % 3]) for i in range(30)]))

    # ---- FM-FSA-06 EstadoFondo / configuration selectors
    caso("estado-fondo-selecciona-configuracion", "FM-FSA-06: filas de configuracion con EstadoFondo C distinto: el perfil backoffice (estado C) y el comercial (A) leen filas distintas",
         entrada([sol(1, "FSN01", "RES")], usuario=8),
         seed=[insert('"BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"', {"IdEmpresa": 1, "IdModalidad": "UNITARIO", "IdTipoFecha": "CERTIF", "TipoSolicitud": None,
               "EstadoFondo": "C", "FondoExtranjero": None, "Descripcion": "CERTIF-C", "CantidadDias": 4, "EsHabil": "S", "IdTipoFechaOrigen": "CURSE",
               "DebeSerHabilFechaOrigen": "N", "ValutaCurse": "N", "ValutaValorCuota": "N", "ValutaCertificacion": "N", "ValutaRetencion": "N", "CalendarioLocal": "N",
               "CalendarioEmisor": "S", "CalendarioFondo": "S", "CalendarioMoneda": "S", "IdEstado": "ACT"})])
    caso("estado-fondo-selecciona-configuracion-comercial", "FM-FSA-06: igual que el anterior con perfil comercial (estado A): la fila con estado C no se usa",
         entrada([sol(1, "FSN01", "RES")], usuario=7),
         seed=[insert('"BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"', {"IdEmpresa": 1, "IdModalidad": "UNITARIO", "IdTipoFecha": "CERTIF", "TipoSolicitud": None,
               "EstadoFondo": "C", "FondoExtranjero": None, "Descripcion": "CERTIF-C", "CantidadDias": 4, "EsHabil": "S", "IdTipoFechaOrigen": "CURSE",
               "DebeSerHabilFechaOrigen": "N", "ValutaCurse": "N", "ValutaValorCuota": "N", "ValutaCertificacion": "N", "ValutaRetencion": "N", "CalendarioLocal": "N",
               "CalendarioEmisor": "S", "CalendarioFondo": "S", "CalendarioMoneda": "S", "IdEstado": "ACT"})])
    # the state the fund reports for the type and the profile selects the configuration row (EstadoFondo) of CalcularFecha: pins EstadoAporte*/EstadoRescate* x comercial/backoffice
    fila = lambda estado, dias: insert('"BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"', {"IdEmpresa": 1, "IdModalidad": "UNITARIO", "IdTipoFecha": "CERTIF",
            "TipoSolicitud": None, "EstadoFondo": estado, "FondoExtranjero": None, "Descripcion": "CERTIF-" + estado, "CantidadDias": dias, "EsHabil": "S",
            "IdTipoFechaOrigen": "CURSE", "DebeSerHabilFechaOrigen": "N", "ValutaCurse": "N", "ValutaValorCuota": "N", "ValutaCertificacion": "N", "ValutaRetencion": "N",
            "CalendarioLocal": "N", "CalendarioEmisor": "S", "CalendarioFondo": "S", "CalendarioMoneda": "S", "IdEstado": "ACT"})
    caso("estado-rescate-comercial-R", "FM-FSA-06: RES comercial de FSN02 => EstadoFondo R (EstadoRescateComercial) => lee la fila CERTIF de estado R (5 dias)",
         entrada([sol(1, "FSN02", "RES")], usuario=7), seed=[fila("R", 5), fila("A", 4), fila("T", 6), fila("C", 7)])
    caso("estado-rescate-backoffice-T", "FM-FSA-06: RES backoffice de FSN02 => EstadoFondo T (EstadoRescateBackoffice) => fila de estado T (6 dias)",
         entrada([sol(1, "FSN02", "RES")], usuario=8), seed=[fila("R", 5), fila("A", 4), fila("T", 6), fila("C", 7)])
    caso("estado-aporte-comercial-A", "FM-FSA-06: APO comercial de FSN02 => EstadoFondo A (EstadoAporteComercial) => fila de estado A (4 dias), no la R de rescate",
         entrada([sol(1, "FSN02", "APO")], usuario=7), seed=[fila("R", 5), fila("A", 4), fila("T", 6), fila("C", 7)])
    caso("estado-aporte-backoffice-C", "FM-FSA-06: APO backoffice de FSN02 => EstadoFondo C (EstadoAporteBackoffice) => fila de estado C (7 dias)",
         entrada([sol(1, "FSN02", "APO")], usuario=8), seed=[fila("R", 5), fila("A", 4), fila("T", 6), fila("C", 7)])
    caso("estado-en-lista-rescate-usa-estado-de-rescate", "FM-FSA-05/06: lista de 2 (APO+RES de FSN02/FSN01) comercial: cada una usa el estado de su tipo",
         entrada([sol(1, "FSN02", "APO"), sol(2, "FSN02", "RES")], usuario=7), seed=[fila("R", 5), fila("A", 4)])
    caso("serie-con-estados-distintos-por-perfil", "FM-FSA-06: fondo FSX01 con liquidez comercial A y backoffice A, rescate comercial A y backoffice C: la combinacion "
         "estado/liquidez decide la rama de certificacion (perfil backoffice)",
         entrada([sol(1, "FSX01", "RES")], usuario=8), seed=[update(FONDO, {"EstadoLiquidezBackoffice": "A"}, "\"IdFondo\" = '51'")])
    caso("rescate-extranjero-comercial-estado-a-a", "FM-FSA-06: RES extranjero perfil comercial: estado A y liquidez A => valutas del solicitante en el calculo de certificacion",
         entrada([sol(1, "FSX03", "RES", moneda="USD")]))
    caso("res-dos-extranjeras-backoffice", "FM-FSA-06: 2 RES extranjeras mismo emisor, perfil backoffice (estados C/C): misma regla 2/1/2 con otra rama de estados",
         entrada([sol(1, "FSX01", "RES", moneda="USD"), sol(2, "FSX02", "RES", moneda="USD")], usuario=8))
    for sid in ("FSX05", "FSX06", "FSX07"):
        for usu, perfil in ((7, "comercial"), (8, "backoffice")):
            caso("res-%s-%s" % (sid.lower(), perfil), "FM-FSA-06 FSA-C01/C02: RES de %s (pais del fondo distinto del pais del emisor) con perfil %s: estados A/A (comercial) "
                 "o C/C (backoffice) cambian la rama de calculo de la certificacion y la liquidacion" % (sid, perfil),
                 entrada([sol(1, sid, "RES", moneda="USD")], usuario=usu))
    caso("res-dos-extranjeras-pais-distinto-comercial", "FM-FSA-05/06: 2 RES (FSX05, FSX06, mismo emisor EXT3) con paises distintos: regla 2/1/2 con perfil comercial",
         entrada([sol(1, "FSX05", "RES", moneda="USD"), sol(2, "FSX06", "RES", moneda="USD")]))
    caso("res-dos-extranjeras-pais-distinto-backoffice", "FM-FSA-05/06: igual con perfil backoffice",
         entrada([sol(1, "FSX05", "RES", moneda="USD"), sol(2, "FSX06", "RES", moneda="USD")], usuario=8))
    caso("res-sin-configuracion-de-fechas", "FM-FSA-08: sin filas de ConfiguracionFechasFondoMutuo activas el colaborador falla/devuelve segun el legacy",
         entrada([sol(1, "FSN01", "RES")]), seed=cfg_borrar())
    caso("parametro-dia-no-habil-money-market", "FM-FSA-08: PERMITE_DIA_NO_HABIL_FECHA_VALOR_CUOTA_MONEY_MARKET=S con serie money market (cambia la fecha valor cuota)",
         entrada([sol(1, "FSN01", "APO")]),
         seed=[update(FONDO, {"MoneyMarket": "S"}, "\"IdFondo\" = '41'"), update('"BSCL.FondoMutuo.BD::FMM.Parametro"', {"ValorParametro": "S"},
                                                                                "\"NombreTipoParametro\" = 'PERMITE_DIA_NO_HABIL_FECHA_VALOR_CUOTA_MONEY_MARKET'")])
    caso("db-nulos-emisor-npe", "FM-FSA-05 D-F3A-01 (SINTETICO: HANA declara IdEmisor NOT NULL): emisor nulo con 2 solicitudes => NullPointerException",
         entrada([sol(1, "FSX01", "RES"), sol(2, "FSX02", "RES")]),
         seed=['ALTER TABLE %s ALTER COLUMN "IdEmisor" SET NULL;' % FONDO, update(FONDO, {"IdEmisor": None}, "\"IdFondo\" = '51'")])
    caso("db-nulos-dias-retencion-rescate-npe", "FM-FSA-06 (SINTETICO: DiasRetencionesRescate es NOT NULL en HANA): ternario con Long nulo => NullPointerException",
         entrada([sol(1, "FSN01", "RES")]),
         seed=['ALTER TABLE %s ALTER COLUMN "DiasRetencionesRescate" SET NULL;' % SERIE, update(SERIE, {"DiasRetencionesRescate": None}, "\"IdSerieFondo\" = 'FSN01'")])
    caso("db-nulos-dias-aporte-sin-npe", "FM-FSA-06 (SINTETICO): dias de aporte nulos se asignan sin desempaquetar: el colaborador los trata como 0",
         entrada([sol(1, "FSN01", "APO")]),
         seed=['ALTER TABLE %s ALTER COLUMN "DiasRetencionesAporte" SET NULL;' % SERIE, update(SERIE, {"DiasRetencionesAporte": None}, "\"IdSerieFondo\" = 'FSN01'")])
    caso("p-solicitud-con-campo-desconocido", "FM-FSA-01: campos de mas en la solicitud (Monto, IdCliente) se ignoran al decodificar",
         entrada([sol(1, "FSN01", "APO", Monto=1000, IdCliente=100)]))
    caso("ciento-veinte-solicitudes-determinismo", "FM-FSA-08 parallelStream: 120 solicitudes (6 series, 3 fechas, APO y RES alternados, ids no correlativos): orden y fechas estables en 20 corridas",
         entrada([sol(1000 - 7 * i, ["FSN01", "FSN02", "FSX01", "FSX02", "FSX03", "FSD01"][i % 6], "APO" if i % 2 else "RES",
                      emision=["2026-03-02", "2026-03-06", "2026-03-09"][i % 3], moneda="USD") for i in range(120)]))
