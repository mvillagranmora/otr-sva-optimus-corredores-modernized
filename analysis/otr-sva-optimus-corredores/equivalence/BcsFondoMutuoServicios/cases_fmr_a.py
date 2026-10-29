"""FMM-FMR cases (ConsultarFondoMutuoReinversion). Ids: FM-FMR-01..04 (cards, RULES_FONDOMUTUO_F3_A.md 8.4), D-F3A-11/25/27 and D-REQ-7 (section 9), matrix
FMR-C01..C03 (section 11). Inputs only: expected.json is recorded from the legacy by record_fmr.sh."""
from cases_fmr_lib import *  # noqa: F401,F403


def sin(ent, *claves):
    d = dict(ent)
    for c in claves:
        d.pop(c, None)
    return d


def generar():
    # ---- FM-FMR-01 contract / FM-FMR-04 copy: every filter open
    caso("todo-comodin-estado-vacio", "FM-FMR-01/02/03/04 FMR-C01: filtros '-' y estado '' = sin filtro: una fila por serie y por regimen ACT, MLT, IFNULL de cuota y montos",
         entrada(estado=""))
    caso("estado-act", "FM-FMR-02 FMR-C01: p_IdEstado ACT filtra IdEstado de la serie (S007 INA sale)", entrada(estado="ACT"))
    caso("estado-ina", "FM-FMR-02: p_IdEstado INA => solo S007", entrada(estado="INA"))
    caso("estado-nulo-es-sin-filtro", "FM-FMR-02 FMR-C03: p_IdEstado nulo explicito = sin filtro (el CASE con nulo cae al ELSE)", entrada(estado=None))
    caso("estado-ausente-es-sin-filtro", "FM-FMR-01/02: la clave p_IdEstado ausente (nulo en el pojo) = sin filtro", sin(entrada(), "p_IdEstado"))
    caso("estado-minuscula-no-coincide", "FM-FMR-02: 'act' en minuscula no es ACT (comparacion exacta) => lista vacia", entrada(estado="act"))
    caso("estado-con-espacio-no-coincide", "FM-FMR-02: 'ACT ' con espacio final => la comparacion de texto es exacta en H2 (lista vacia); HANA NVARCHAR tampoco rellena, pero no esta probado (Q-F3A-9)", entrada(estado="ACT "))

    # ---- FM-FMR-02 each wildcard filter
    caso("moneda-usd", "FM-FMR-02: p_IdMoneda USD => series de fondos en USD (S004, S012)", entrada(moneda="USD"))
    caso("moneda-nula-es-sin-filtro", "FM-FMR-02 FMR-C03: p_IdMoneda nulo = sin filtro", entrada(moneda=None))
    caso("moneda-vacia-no-es-comodin", "FM-FMR-02: p_IdMoneda '' NO es comodin (solo '-' lo es): ninguna serie tiene moneda ''", entrada(moneda=""))
    caso("moneda-inexistente", "FM-FMR-02: p_IdMoneda EUR => lista vacia, P_OUT_LISTA [] (no es error)", entrada(moneda="EUR"))
    caso("fondo-extranjero-s", "FM-FMR-02: p_FondoExtranjero S => solo S004", entrada(extranjero="S"))
    caso("fondo-extranjero-n", "FM-FMR-02: p_FondoExtranjero N => nacionales (S008 con 'X' sale)", entrada(extranjero="N"))
    caso("fondo-extranjero-x", "FM-FMR-02: p_FondoExtranjero X (valor raro presente en S008) => solo S008", entrada(extranjero="X"))
    caso("fondo-extranjero-nulo", "FM-FMR-02: p_FondoExtranjero nulo = sin filtro", entrada(extranjero=None))
    caso("familia-fam1", "FM-FMR-02: p_IdFamiliaFondo FAM1 => FM001A, S001, S002, S004?, S010... (las series FAM1 del universo)", entrada(familia="FAM1"))
    caso("familia-fam3", "FM-FMR-02: p_IdFamiliaFondo FAM3 => solo S004", entrada(familia="FAM3"))
    caso("familia-nula", "FM-FMR-02: p_IdFamiliaFondo nulo = sin filtro", entrada(familia=None))
    caso("emisor-agf1", "FM-FMR-02: p_IdEmisor AGF1 => series de fondos del emisor AGF1", entrada(emisor="AGF1"))
    caso("emisor-agf3-fuera-de-la-vista", "FM-FMR-02: p_IdEmisor AGF3 (no esta en MER.ConsultarEmisor; FMR no usa la vista) => S008", entrada(emisor="AGF3"))
    caso("emisor-nulo", "FM-FMR-02: p_IdEmisor nulo = sin filtro", entrada(emisor=None))
    caso("emisor-en-minuscula", "FM-FMR-02: p_IdEmisor 'agf1' (comparacion exacta) => lista vacia", entrada(emisor="agf1"))

    # ---- FM-FMR-02/03 regime filter
    caso("regimen-18q", "FM-FMR-02/03 FMR-C01: p_IdRegimenFondoMutuo 18Q => solo las filas del regimen 18Q (S001, S002, S004; S009 tiene 18Q INA: no), MLT S",
         entrada(regimen="18Q"))
    caso("regimen-18t", "FM-FMR-03: regimen 18T => MLT S por AceptaMandatoEnReinversion (RescateConMandato N)", entrada(regimen="18T"))
    caso("regimen-57bis", "FM-FMR-03: regimen 57BIS => MLT N", entrada(regimen="57BIS"))
    caso("regimen-gen", "FM-FMR-03: regimen GEN (FM001A y S009) => MLT N", entrada(regimen="GEN"))
    caso("regimen-inexistente", "FM-FMR-02: regimen ZZZ => lista vacia", entrada(regimen="ZZZ"))
    caso("regimen-nulo-es-sin-filtro", "FM-FMR-02 FMR-C03: p_IdRegimenFondoMutuo nulo = sin filtro (incluye la serie sin regimen)", entrada(regimen=None))
    caso("regimen-vacio-no-es-comodin", "FM-FMR-02: p_IdRegimenFondoMutuo '' NO es comodin: el LEFT JOIN no encuentra regimen y COALESCE(NULL,'')='' deja pasar a todas las series, con el regimen ausente", entrada(regimen=""))
    caso("regimen-ina-no-cuenta", "FM-FMR-03: S009 tiene 18Q con estado INA: el LEFT JOIN exige ACT, asi que con filtro 18Q no sale", entrada(regimen="18Q", estado=""))

    # ---- FM-FMR-03 one row per serie and regime, series without regime
    caso("serie-sin-regimen-con-comodin", "FM-FMR-03 D-F3A-11 FMR-C02: S010 sin regimenes sale UNA vez con IdRegimenFondoMutuo ausente y MLT N (LEFT JOIN)", entrada(estado="ACT", regimen="-"))
    caso("serie-con-dos-regimenes-salen-dos-filas", "FM-FMR-03 D-F3A-11 FMR-C02: S001 y S004 con 2 regimenes salen dos veces (18Q y 18T), sin ORDER BY", entrada(emisor="EXT1"))
    caso("serie-dos-regimenes-filtro-uno", "FM-FMR-03: S001 con filtro 18T sale una vez (el filtro acota las filas del LEFT JOIN)", entrada(familia="FAM1", regimen="18T"))

    # ---- FM-FMR-02 origin series excluded
    caso("origen-excluye-la-serie", "FM-FMR-02 FMR-C01: p_IdSerieFondoOrigen S001 => S001 no aparece (IdSerieFondo <> origen)", entrada(origen="S001"))
    caso("origen-con-regimen-como-el-ejemplo-de-la-tarjeta", "FM-FMR-02 FMR-C01: ACT + regimen 18Q + origen S001 (ejemplo de la tarjeta): series activas con 18Q sin S001",
         entrada(estado="ACT", regimen="18Q", origen="S001"))
    caso("origen-nulo-no-excluye", "FM-FMR-02 FMR-C03: p_IdSerieFondoOrigen nulo => el CASE da '-' y todas pasan", entrada(origen=None))
    caso("origen-inexistente-no-excluye", "FM-FMR-02: origen que no existe => nada se excluye", entrada(origen="NOEXISTE"))
    caso("origen-vacio-no-es-comodin", "FM-FMR-02: origen '' (distinto de '-') => IdSerieFondo <> '' es verdadero para todas", entrada(origen=""))
    caso("origen-en-minuscula-no-excluye", "FM-FMR-02: origen 's001' en minuscula no excluye a S001 (comparacion exacta)", entrada(origen="s001", estado="ACT", familia="FAM1"))

    # ---- empresa
    caso("empresa-2-solo-sus-series", "FM-FMR-01/02: p_IdEmpresa 2 => solo las series de la empresa 2 (S011; con regimen 18Q de la empresa 2)", entrada(empresa=2))
    caso("empresa-inexistente", "FM-FMR-02: p_IdEmpresa 99 => lista vacia", entrada(empresa=99))
    caso("empresa-ausente-es-cero", "FM-FMR-01 D-F3A-25 Q-F3A-9 cerrada contra HANA: p_IdEmpresa es long primitivo: ausente = 0; en HANA el parametro BIGINT enlazado <> '' es siempre verdadero: el CASE toma :IdEmpresa y filtra IdEmpresa = 0 => lista vacia", sin(entrada(), "p_IdEmpresa"))
    caso("empresa-nula-explicita", "FM-FMR-01 Q-F3A-9 cerrada contra HANA: p_IdEmpresa null explicito en el JSON: Gson no pisa el primitivo (queda 0) => igual que ausente", entrada(empresa=None))

    # ---- FM-FMR-01 contract, FM-FMR-04 copy
    caso("usuario-y-modulo-no-se-usan", "FM-FMR-01: p_IdUsuario y p_IdModulo no se leen: usuario inexistente y modulo basura dan el mismo resultado que el caso 02",
         entrada(estado="ACT", usuario=987654, modulo="XXX"))
    caso("campos-desconocidos-se-ignoran", "FM-FMR-01: claves desconocidas del JSON de entrada se ignoran (Gson plano)", entrada(estado="INA", Otro=1, p_Extra={"a": 1}))
    caso("entrada-vacia", "FM-FMR-01/02 Q-F3A-9 cerrada contra HANA: JSON {} => todos los campos nulos y empresa 0 => filtra IdEmpresa = 0 => lista vacia, sin contador ni mensajes", {})
    caso("tipo-incorrecto-empresa-texto", "FM-FMR-01: p_IdEmpresa 'abc' => JsonSyntaxException (NumberFormatException) antes de leer la base: HTTP 500",
         entrada(empresa="abc"))
    caso("tipo-incorrecto-estado-objeto", "FM-FMR-01: p_IdEstado como objeto => JsonSyntaxException: HTTP 500", entrada(estado={"a": 1}))
    caso("empresa-como-texto-numerico", "FM-FMR-01: p_IdEmpresa '1' (texto numerico) => Gson lo convierte a long 1", entrada(empresa="1", estado="INA"))
    caso("empresa-decimal", "FM-FMR-01: p_IdEmpresa 1.0 => Gson (long) rechaza o acepta segun su regla: lo fija el oraculo", entrada(empresa=1.0, estado="INA"))
    caso("p-idempresa-grande", "FM-FMR-01: p_IdEmpresa 9223372036854775807 (Long.MAX) => lista vacia", entrada(empresa=9223372036854775807))
    caso("p-idempresa-negativa", "FM-FMR-01: p_IdEmpresa -1 => lista vacia", entrada(empresa=-1))

    # ---- FM-FMR-03 values: IFNULL, quota without date, amounts
    caso("valores-ifnull-cuota-y-montos", "FM-FMR-03 FMR-C02: ValorCuotaInstrumento (cuota de la serie; 0 si no hay fila: S003), MontoPatrimonioEmisor y MontoMinimo con IFNULL",
         entrada(estado="ACT", familia="FAM2"))
    caso("valor-cuota-escala-ocho", "FM-FMR-03/04: la cuota se copia con la escala del tipo DECIMAL(26,8): 1234.5678 sale como 1234.56780000 en el texto plano de Gson",
         entrada(estado="ACT", regimen="18T", emisor="AGF1"))
    caso("monto-patrimonio-decimal", "FM-FMR-03: MontoPatrimonioEmisor 250000.5 de S002 (DECIMAL(26,8))", entrada(estado="ACT", regimen="18Q", emisor="AGF1", familia="FAM1", origen="S001"))
    caso("serie-con-cuota-historica-no-aplica-fecha", "FM-FMR-03 D-REQ-7: la cuota se une por serie SIN filtro de fecha: S002 trae la de 2026-02-27 aunque la vigente sea otra",
         entrada(estado="ACT", regimen="18Q", origen="S001", emisor="AGF1"),
         seed=[update(VCUOTA, {"ValorCuota": 1.5, "FechaCierre": "2020-01-01"}, '"IdSerieFondo" = \'S002\'')])
    caso("minimo-nulo-no-ocurre-monto-cero", "FM-FMR-03: MontoMinimo 0 de S003 sale 0 (no nulo)", entrada(estado="ACT", familia="FAM2", regimen="57BIS"))
    caso("sin-filas-no-es-error", "FM-FMR-01: una busqueda sin resultados devuelve {\"P_OUT_LISTA\":[]}: sin contador ni mensajes ni error", entrada(estado="ACT", moneda="JPY"))
    caso("tablas-no-cambian", "FM-FMR-01: la clase es de solo lectura: las tablas del volcado quedan intactas y la decision del servlet es ROLLBACK (no hay contador, D-F3A-27)",
         entrada(estado="ACT", regimen="18Q"))
