"""FMM-IFM cases (ConsultarInstrumentosFondosMutuos). Ids: FM-IFM-01..06 (cards, RULES_FONDOMUTUO_F3_A.md 8.5), D-F3A-12/13/25/27 and D-REQ-7 (section 9),
matrix IFM-C01..C06 (section 11). Inputs only: expected.json is recorded from the legacy by record_ifm.sh."""
from cases_ifm_lib import *  # noqa: F401,F403


def sin(ent, *claves):
    d = dict(ent)
    for c in claves:
        d.pop(c, None)
    return d


def generar():
    # ---- FM-IFM-01 contract and validation (IFM-C01)
    caso("entrada-nula-explicita", "FM-IFM-01 IFM-C01: p_EntradaFondosMutuos nulo => ContadorErrores 1, mensaje UI_SIN_VALORES (id 0, tipo ERROR), FondosMutuos []; ROLLBACK (ContadorErrores no es el contador del anfitrion, D-F3A-12/27)",
         entrada(fondos=None))
    caso("entrada-ausente", "FM-IFM-01: la clave p_EntradaFondosMutuos ausente = nulo en el pojo => igual que el caso nulo explicito", entrada(fondos="__ausente__"))
    caso("entrada-nula-sin-texto-en-diccionario", "FM-IFM-01: sin la fila UI_SIN_VALORES en PLA.Diccionario el mensaje sale '<codigo> sin traduccion'",
         entrada(fondos=None), seed=['DELETE FROM %s WHERE "CodDiccionario" = \'UI_SIN_VALORES\';' % DICC])
    caso("entrada-nula-empresa-sin-configuracion", "FM-IFM-01: empresa 2 sin PLA.LocalizacionSistema => el diccionario no se puede cargar: lo que haga el legacy queda fijado",
         entrada(fondos=None, empresa=2))
    caso("objeto-vacio-todo-por-defecto", "FM-IFM-01/02/03 IFM-C02: {} => el constructor pone '' y OcultarBloqueados 'N' => rama normal con MostrarBloqueados S: todas las series de la empresa 1, "
         "con ListaRegimen, contrato FondosMutuos/Mensajes/ContadorErrores", entrada())
    caso("usuario-y-modulo-no-se-usan", "FM-IFM-06: p_IdUsuario inexistente y p_IdModulo basura: sin validaciones, mismo resultado que el caso del objeto vacio con filtro", entrada(ef(IdSerieFondo="S001"), usuario=987654, modulo="XXX"))
    caso("campos-desconocidos-se-ignoran", "FM-IFM-01/06: claves desconocidas (tambien dentro de p_EntradaFondosMutuos) se ignoran con el Gson plano",
         entrada(ef(IdSerieFondo="S001", Otro=1), Extra={"a": 1}))
    caso("entrada-como-texto-json-syntax", "FM-IFM-01: p_EntradaFondosMutuos como texto => JsonSyntaxException: HTTP 500", entrada(fondos="abc"))
    caso("empresa-como-texto-json-syntax", "FM-IFM-01: p_IdEmpresa 'abc' => JsonSyntaxException (NumberFormatException)", entrada(ef(IdSerieFondo="S001"), empresa="abc"))
    caso("regimen-nulo-explicito-npe", "FM-IFM-01/02 D-F3A-12: IdRegimenFondoMutuo null explicito => size() sobre nulo: NullPointerException (HTTP 500)", entrada(ef(IdRegimenFondoMutuo=None)))
    caso("regimen-como-texto-json-syntax", "FM-IFM-01: IdRegimenFondoMutuo como texto en vez de lista => JsonSyntaxException", entrada(ef(IdRegimenFondoMutuo="18Q")))
    caso("empresa-ausente-es-cero", "FM-IFM-03: p_IdEmpresa ausente (0): SER.IdEmpresa = 0 => ninguna serie, sin error ni mensaje", sin(entrada(ef(IdSerieFondo="S001")), "p_IdEmpresa"))

    # ---- FM-IFM-02 normal or reinversion branch
    caso("normal-ocultar-bloqueados-n", "FM-IFM-02/03 IFM-C02: OcultarBloqueados N => MostrarBloqueados S: salen la serie del fondo bloqueado (S005) y la de aporte restringido (S006)",
         entrada(ef(OcultarBloqueados="N")))
    caso("normal-ocultar-bloqueados-s", "FM-IFM-02/03 IFM-C02: OcultarBloqueados S => MostrarBloqueados N: exige BloqueoAporte N y RestringeAporte N (S005 y S006 no salen)",
         entrada(ef(OcultarBloqueados="S")))
    caso("normal-ocultar-bloqueados-s-minuscula", "FM-IFM-02 IFM-C02: 's' en minuscula tambien oculta (equalsIgnoreCase)", entrada(ef(OcultarBloqueados="s")))
    caso("normal-ocultar-bloqueados-n-minuscula", "FM-IFM-02: 'n' minuscula => muestra", entrada(ef(OcultarBloqueados="n")))
    caso("normal-ocultar-bloqueados-vacio", "FM-IFM-02: '' => muestra bloqueados (solo S oculta)", entrada(ef(OcultarBloqueados="")))
    caso("normal-ocultar-bloqueados-nulo", "FM-IFM-02: OcultarBloqueados null explicito => equalsIgnoreCase(null) falso => muestra", entrada(ef(OcultarBloqueados=None)))
    caso("normal-ocultar-bloqueados-basura", "FM-IFM-02: valor 'SI' no es 'S' => muestra", entrada(ef(OcultarBloqueados="SI")))
    caso("origen-vacio-es-rama-normal", "FM-IFM-02: IdSerieFondoOrigen '' exacto => rama normal", entrada(ef(IdSerieFondoOrigen="", IdSerieFondo="S001")))
    caso("origen-espacio-es-rama-reinversion", "FM-IFM-02 D-F3A-13: IdSerieFondoOrigen ' ' (un espacio) NO es '' => rama de reinversion con origen ' ': ninguna serie lo tiene => lista vacia",
         entrada(ef(IdSerieFondoOrigen=" ")))
    caso("origen-nulo-explicito-rama-reinversion", "FM-IFM-02 D-F3A-12/13: IdSerieFondoOrigen null explicito => rama de reinversion con parametro nulo (\"\".equals(null) es falso)",
         entrada(ef(IdSerieFondoOrigen=None)))

    # ---- FM-IFM-03 filters of the normal branch
    caso("filtro-serie", "FM-IFM-03: IdSerieFondo S001 => una fila con ListaRegimen 18Q y 18T", entrada(ef(IdSerieFondo="S001")))
    caso("filtro-serie-inexistente", "FM-IFM-03/05 IFM-C06: serie inexistente => FondosMutuos [] sin mensaje (el SELECT de regimenes corre con IN (NULL))", entrada(ef(IdSerieFondo="NOEXISTE")))
    caso("filtro-serie-nula", "FM-IFM-03: IdSerieFondo null explicito = sin filtro (COALESCE)", entrada(ef(IdSerieFondo=None, IdFamiliaFondo="FAM3")))
    caso("filtro-familia", "FM-IFM-03: IdFamiliaFondo FAM2 => S003 y S008", entrada(ef(IdFamiliaFondo="FAM2")))
    caso("filtro-estado-act", "FM-IFM-03: IdEstado ACT (S007 INA no sale)", entrada(ef(IdEstado="ACT")))
    caso("filtro-estado-ina", "FM-IFM-03: IdEstado INA => S007", entrada(ef(IdEstado="INA")))
    caso("filtro-owp-s", "FM-IFM-03: InstrumentoOWP S => S008", entrada(ef(InstrumentoOWP="S")))
    caso("filtro-owp-n", "FM-IFM-03: InstrumentoOWP N => series con 'N' (la de FM001A y las demas; S008 no)", entrada(ef(InstrumentoOWP="N")))
    caso("filtro-owp-nulo-en-base", "FM-IFM-03: InstrumentoOWP N no devuelve una serie con InstrumentoOWP NULL (la columna admite nulo en HANA)", entrada(ef(InstrumentoOWP="N")),
         seed=[update(SERIE, {"InstrumentoOWP": None}, '"IdSerieFondo" = \'S002\'')])
    caso("filtro-apv-s", "FM-IFM-03: EsAPV S => S008", entrada(ef(EsAPV="S")))
    caso("filtro-emisor", "FM-IFM-03: IdEmisor EXT1 => S004 con NombreEmisor de la vista", entrada(ef(IdEmisor="EXT1")))
    caso("filtro-emisor-fuera-de-la-vista", "FM-IFM-03: IdEmisor AGF3 (no esta en MER.ConsultarEmisor): el LEFT JOIN deja NombreEmisor '' (COALESCE)", entrada(ef(IdEmisor="AGF3")))
    caso("filtro-categoria-cat1", "FM-IFM-03: IdCategoriaPerfilInversionista CAT1 (se envia como IdCategoriaPerfilFondoMutuo)", entrada(ef(IdCategoriaPerfilInversionista="CAT1")))
    caso("filtro-categoria-fuera-de-la-vista", "FM-IFM-03: CAT3 existe en el fondo S007 pero no en la vista: NombreCategoriaPerfilInversionista ''", entrada(ef(IdCategoriaPerfilInversionista="CAT3")))
    caso("filtro-moneda-usd", "FM-IFM-03: IdMoneda USD => S004 y S012", entrada(ef(IdMoneda="USD")))
    caso("filtro-extranjero-s", "FM-IFM-03/05: FondoExtranjero S => S004 con PermiteMLT N en sus dos regimenes", entrada(ef(FondoExtranjero="S")))
    caso("filtro-extranjero-n", "FM-IFM-03/05: FondoExtranjero N => nacionales, PermiteMLT = RescateConMandato del regimen", entrada(ef(FondoExtranjero="N")))
    caso("filtro-extranjero-x-permitemlt-nulo", "FM-IFM-05 IFM-C05: FondoExtranjero X (S008) => PermiteMLT nulo (el CASE sin ELSE) y la clave se omite en el JSON plano", entrada(ef(FondoExtranjero="X")))
    caso("filtros-combinados", "FM-IFM-03: estado ACT + moneda CLP + familia FAM1 + categoria CAT1 + extranjero N + OWP N", entrada(ef(IdEstado="ACT", IdMoneda="CLP", IdFamiliaFondo="FAM1",
         IdCategoriaPerfilInversionista="CAT1", FondoExtranjero="N", InstrumentoOWP="N")))
    caso("filtros-con-nulos-explicitos", "FM-IFM-03 FM-IFM-06: todos los filtros de texto null explicito = sin filtro (COALESCE(NULL,'')='')",
         entrada(ef(IdSerieFondo=None, IdEmisor=None, IdFamiliaFondo=None, IdCategoriaPerfilInversionista=None, IdEstado=None, IdMoneda=None, FondoExtranjero=None, InstrumentoOWP=None,
                    EsAPV=None)))
    caso("filtros-en-minuscula-no-coinciden", "FM-IFM-03: IdEstado 'act' y IdMoneda 'clp' en minuscula (comparacion exacta) => lista vacia", entrada(ef(IdEstado="act", IdMoneda="clp")))
    caso("regimen-lista-18q", "FM-IFM-03 IFM-C03: IdRegimenFondoMutuo [18Q] => EXISTS de un regimen ACT de la serie en la lista (S009 con 18Q INA no sale)", entrada(ef(IdRegimenFondoMutuo=["18Q"])))
    caso("regimen-lista-dos-regimenes-sin-duplicados", "FM-IFM-03: [18Q,18T] => S001 y S004 salen UNA vez (EXISTS, no JOIN), con los dos regimenes en su ListaRegimen",
         entrada(ef(IdRegimenFondoMutuo=["18Q", "18T"], IdFamiliaFondo="FAM1")))
    caso("regimen-lista-con-desconocido", "FM-IFM-03: ['ZZZ'] => lista vacia", entrada(ef(IdRegimenFondoMutuo=["ZZZ"])))
    caso("regimen-lista-vacia-sin-filtro", "FM-IFM-03/05 IFM-C03: [] => FiltroRegimen 0: sin filtro de regimen", entrada(ef(IdRegimenFondoMutuo=[], IdEstado="ACT")))
    caso("regimen-lista-con-nulo-dentro", "FM-IFM-03: [null, '18T'] => el IN recibe un nulo: lo que haga el legacy queda fijado", entrada(ef(IdRegimenFondoMutuo=[None, "18T"])))
    caso("regimen-gen-solo-con-activo", "FM-IFM-03: [GEN] => FM001A y S009 (su regimen GEN esta ACT)", entrada(ef(IdRegimenFondoMutuo=["GEN"])))
    caso("regimen-57bis-serie-sin-valor-cuota", "FM-IFM-03/05: [57BIS] => S003 SIN fila en ValorCuota: ValorCuota 0 y FechaValorCuota '0001-01-01' (COALESCE), montos 0", entrada(ef(IdRegimenFondoMutuo=["57BIS"])))
    caso("empresa-2", "FM-IFM-03: p_IdEmpresa 2 => solo S011 (SER.IdEmpresa = 2)", entrada(ef(), empresa=2), seed=[])
    caso("empresa-inexistente", "FM-IFM-03: p_IdEmpresa 99 => lista vacia sin mensaje", entrada(ef(), empresa=99))
    caso("valores-de-una-serie", "FM-IFM-03/04 D-REQ-7: campos del pojo de S002 (cuota 987.65 de 2026-02-27, MontoMinimo 2500, MontoMaximo 900000 como Pesos y Total, CobraComision N, patrimonio 250000.5, decimales 2)",
         entrada(ef(IdSerieFondo="S002")))
    caso("cuota-sin-filtro-de-fecha", "FM-IFM-03 D-REQ-7: la cuota se une por serie SIN filtro de fecha: S002 trae la de 2020-01-01 aunque es muy antigua", entrada(ef(IdSerieFondo="S002")),
         seed=[L.valor_cuota("S002", 1.5, "2020-01-01")])
    caso("emisor-repetido-en-la-vista-duplica-serie", "FM-IFM-03 SINTETICO: dos filas de MER.ConsultarEmisor para AGF1 (la vista de HANA podria repetir) => la serie sale dos veces y cada una recibe la ListaRegimen",
         entrada(ef(IdSerieFondo="S001")), seed=[insert(V_EMISOR, {"IdEmisor": "AGF1", "NombreCompleto": "Administradora Uno S.A. (otra fila)"})])
    caso("categoria-repetida-en-la-vista-duplica-serie", "FM-IFM-03 SINTETICO: dos filas de la vista de categorias => la serie sale dos veces", entrada(ef(IdSerieFondo="S004")),
         seed=[insert(V_CATEGORIA, {"IdCategoriaPerfilFondoMutuo": "CAT2", "NombreCategoriaPerfilFondoMutuo": "Moderado (otra fila)"})])

    # ---- FM-IFM-04 reinversion branch (IFM-C04)
    caso("reinversion-origen-s001", "FM-IFM-04 IFM-C04: origen S001 (18Q,18T; config 18Q->18T, 18T->57BIS): destinos con 18Q/18T/57BIS, sin S001, sin S005 (fondo bloqueado) ni S006 (aporte restringido); incluye S007 INA y S009 (18Q INA cuenta)",
         entrada(ef(IdSerieFondoOrigen="S001")))
    caso("reinversion-origen-fm001a-regimen-gen", "FM-IFM-04: origen FM001A (GEN; config GEN->18Q): destinos que comparten GEN o 18Q", entrada(ef(IdSerieFondoOrigen="FM001A")))
    caso("reinversion-origen-ninguno-acepta-cualquier-regimen", "FM-IFM-04 IFM-C04: origen S012 (NINGUNO): la condicion CASE deja pasar a toda serie con algun regimen (no a las sin regimen)",
         entrada(ef(IdSerieFondoOrigen="S012")))
    caso("reinversion-origen-sin-regimen", "FM-IFM-04: origen S010 sin regimenes => REF vacio => lista vacia", entrada(ef(IdSerieFondoOrigen="S010")))
    caso("reinversion-origen-desconocido", "FM-IFM-04/06: origen NOEXISTE => lista vacia sin mensaje", entrada(ef(IdSerieFondoOrigen="NOEXISTE")))
    caso("reinversion-origen-en-minuscula", "FM-IFM-04: 's001' en minuscula no existe => lista vacia", entrada(ef(IdSerieFondoOrigen="s001")))
    caso("reinversion-origen-otra-empresa", "FM-IFM-04: origen S011 (empresa 2) pedido con empresa 1: sus regimenes valen, las candidatas son de la empresa 1", entrada(ef(IdSerieFondoOrigen="S011")))
    caso("reinversion-ignora-ocultar-bloqueados", "FM-IFM-04 D-F3A-13: OcultarBloqueados no se usa en reinversion: S y N dan lo mismo (el fondo bloqueado siempre queda fuera)",
         entrada(ef(IdSerieFondoOrigen="S001", OcultarBloqueados="S")))
    caso("reinversion-sin-decimales-ni-patrimonio", "FM-IFM-04 D-F3A-13: la lista de destinos no trae DecimalesValorCuota ni MontoPatrimonioEmisor (claves ausentes) y la cuota de cada una", entrada(ef(IdSerieFondoOrigen="S001", IdSerieFondo="S002")))
    caso("reinversion-filtro-estado-act", "FM-IFM-04: IdEstado ACT filtra tambien los destinos (S007 INA sale)", entrada(ef(IdSerieFondoOrigen="S001", IdEstado="ACT")))
    caso("reinversion-filtro-moneda", "FM-IFM-04: IdMoneda USD con origen S012", entrada(ef(IdSerieFondoOrigen="S012", IdMoneda="USD")))
    caso("reinversion-filtro-regimen-sin-exigir-activo", "FM-IFM-04 D-F3A-13: filtro de regimen [18Q] en reinversion NO exige estado ACT: S009 (18Q INA) SI sale (en la rama normal no)",
         entrada(ef(IdSerieFondoOrigen="S001", IdRegimenFondoMutuo=["18Q"])))
    caso("reinversion-filtro-serie-igual-origen", "FM-IFM-04: IdSerieFondo igual al origen => lista vacia (origen excluido)", entrada(ef(IdSerieFondoOrigen="S001", IdSerieFondo="S001")))
    caso("reinversion-filtro-emisor-fuera-de-la-vista", "FM-IFM-04: filtro IdEmisor AGF3 con origen S001 => S008 (18T), NombreEmisor ''", entrada(ef(IdSerieFondoOrigen="S001", IdEmisor="AGF3")))
    caso("reinversion-owp-y-apv", "FM-IFM-04: InstrumentoOWP S + EsAPV S con origen S001 => S008", entrada(ef(IdSerieFondoOrigen="S001", InstrumentoOWP="S", EsAPV="S")))
    caso("reinversion-config-duplicada-no-duplica-filas", "FM-IFM-04 SINTETICO: la vista de configuracion de regimen repite 18Q->18T: el EXISTS no duplica series",
         entrada(ef(IdSerieFondoOrigen="S001")), seed=[insert(V_CONFREG, {"IdRegimenFondoMutuoOrigen": "18Q", "IdRegimenFondoMutuoDestino": "18T"})])

    # ---- FM-IFM-05 regimes and PermiteMLT (IFM-C05, IFM-C06)
    caso("regimenes-orden-ascendente-aunque-se-inserten-al-reves", "FM-IFM-05: ListaRegimen ordenada por IdRegimenFondoMutuo ascendente (se insertan 18T y luego 18Q)", entrada(ef(IdSerieFondo="S001")),
         seed=['DELETE FROM %s WHERE "IdSerieFondo" = \'S001\';' % REGSERIE, reg_serie("S001", "18T"), reg_serie("S001", "18Q")])
    caso("permitemlt-nacional-por-regimen", "FM-IFM-05 IFM-C05: serie nacional S001: 18Q => RescateConMandato S; 18T => N (AceptaMandatoEnReinversion no cuenta aqui)", entrada(ef(IdSerieFondo="S001")))
    caso("permitemlt-extranjero-siempre-n", "FM-IFM-05 IFM-C05: serie extranjera S004 => N en ambos regimenes aunque 18Q tenga RescateConMandato S", entrada(ef(IdSerieFondo="S004")))
    caso("permitemlt-otro-valor-nulo", "FM-IFM-05 IFM-C05: FondoExtranjero 'X' (S008) => PermiteMLT nulo", entrada(ef(IdSerieFondo="S008")))
    caso("serie-sin-regimenes-lista-vacia", "FM-IFM-05: S010 sin regimenes => ListaRegimen []", entrada(ef(IdSerieFondo="S010")))
    caso("regimen-inactivo-no-se-lista", "FM-IFM-05: S009 tiene 18Q INA y GEN ACT: ListaRegimen solo trae GEN", entrada(ef(IdSerieFondo="S009")))
    caso("regimen-sin-maestro-no-se-lista", "FM-IFM-05 SINTETICO: un regimen ACT de la serie que no existe en RegimenFondoMutuo se pierde por el INNER JOIN", entrada(ef(IdSerieFondo="S010")),
         seed=[reg_serie("S010", "ZZ9")])
    caso("regimen-con-descripcion", "FM-IFM-05: DescripcionRegimen sale de RegimenFondoMutuo.Descripcion", entrada(ef(IdSerieFondo="S003")))
    caso("sin-series-in-null", "FM-IFM-05 IFM-C06: ninguna serie => SELECT de regimenes con IN (NULL), sin filas; Mensajes [] y ContadorErrores 0; la decision del servlet es ROLLBACK (D-F3A-27)",
         entrada(ef(IdSerieFondo="NOEXISTE", OcultarBloqueados="S")))
    caso("muchas-series-determinismo", "FM-IFM-05: 20 series con regimenes (el armado usa parallelStream): la salida debe ser estable en las 20 corridas del oraculo", entrada(ef()),
         seed=_muchas_series())
    caso("regimen-de-la-serie-repetida-en-la-tabla", "FM-IFM-05 SINTETICO: el mismo regimen ACT repetido para S002 (la tabla de HANA lo impide por clave) => aparece dos veces en ListaRegimen", entrada(ef(IdSerieFondo="S002")),
         seed=[reg_serie("S002", "18Q")])

    # ---- FM-IFM-06 what IFM does not do (IFM-C01)
    caso("sin-validacion-de-formato", "FM-IFM-06: filtros con texto largo y caracteres raros (comilla, %, espacios) no se validan: lista vacia sin mensaje", entrada(ef(IdSerieFondo="S001'; DROP", IdEmisor="%", IdFamiliaFondo=" ")))
    caso("tablas-no-cambian", "FM-IFM-06: solo lectura: las tablas del volcado quedan intactas; con contador 0 pero sin P_OUT_ERRORESCONTADOR el servlet decide ROLLBACK", entrada(ef(IdSerieFondo="S001")))


def _muchas_series():
    s = []
    for i in range(1, 21):
        sid = "M%03d" % i
        fondo = "8%02d" % i
        s += clonar_serie(sid, fondo, fondo={"IdEmisor": "AGF1" if i % 2 else "AGF2", "NombreFondo": "Fondo Masivo %d" % i, "Nemotecnico": "FM%03d" % i, "IdCategoriaPerfilFondoMutuo": "CAT1"},
                          serie={"NombreSerieFondo": "Serie Masiva %d" % i, "CodigoSerie": "M", "IdFamiliaFondo": "FAM9", "InstrumentoOWP": "N", "EsAPV": "N", "RestringeAporte": "N",
                                 "MontoPatrimonioEmisor": i * 1000, "CobraComision": "N", "DecimalesValorCuota": 4})
        s.append(reg_serie(sid, "18Q" if i % 2 else "18T"))
        if i % 3 == 0:
            s.append(reg_serie(sid, "57BIS"))
        s.append(L.valor_cuota(sid, 100 + i, "2026-02-%02d" % (i if i <= 28 else 28)))
    return s
