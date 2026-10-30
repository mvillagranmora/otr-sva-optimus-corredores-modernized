"""BcsReplica oracle: inputs of the ReplicaUtils cases (families SEB: procesarRespaldoSebra, DIA: procesarRespaldoDiarioCarterasSebra, OPT: procesarRespaldoOptimus, POS: guardarEnvioPostPauta)."""
from cases_rep_lib import *  # noqa: F401,F403
from cases_rep_env import semilla_ci, semilla_cus, ODS_CORTA

CFG_S = "ConfiguracionSistema.getParametroConfiguracionString"
ORDEN_SEBRA = SEBRA13


def cfg(valor):
    return {CFG_S: {"defecto": valor}}


def entrada_u(fecha="2026-03-06", empresa=1):
    return {"fechaSistemaEjecucion": fecha, "idEmpresa": empresa}


def semilla_sebra(tablas=None):
    tablas = tablas or SEBRA13
    return semilla_tbc_periodo(tablas, [("2026-03", 1), ("2026-03", 2), ("2026-02", 3)], [("2026-03", 11), ("2026-03", 12), ("2026-02", 13), ("2026-04", 14)], None)


def generar():
    ids = []
    # ---------------------------------------------------------------- SEB
    g = Gen("SEB")
    S = "ReplicaUtils.procesarRespaldoSebra"
    TT = dump_tbc_todas()
    g.caso("param-S-reemplaza-el-periodo-en-las-13-tablas", S, entrada_u(), ["B-REP-03", "B-REP-04", "B-REP-17"], cfg("S"), tablas=TT, seed=semilla_sebra())
    g.caso("param-s-minuscula-tambien-activa", S, entrada_u(), ["B-REP-03", "B-REP-17"], cfg("s"), tablas=TT, seed=semilla_sebra())
    for v, slug in (("N", "N"), ("n", "n-minuscula"), ("", "vacio"), ("X", "X"), ("SI", "SI"), (" S", "S-con-espacio"), ("S ", "S-con-espacio-final"), ("true", "true"), ("1", "uno")):
        g.caso("param-%s-no-hace-nada" % slug, S, entrada_u(), ["B-REP-03"], cfg(v), tablas=TT, seed=semilla_sebra())
    g.caso("param-nulo-falla-al-comparar", S, entrada_u(), ["B-REP-03"], cfg(None), tablas=TT, seed=semilla_sebra())
    g.caso("param-ausente-se-propaga", S, entrada_u(), ["B-REP-03", "D-REP-12"], {CFG_S: {"respuestas": [{"error": "Parametro PLA.RespaldoReplicaTablasSebra en modulo PLATAFORMA no encontrado"}]}},
           tablas=TT, seed=semilla_sebra())
    g.caso("param-ausente-con-fecha-nula-se-propaga-igual", S, entrada_u(None), ["D-REP-12"], {CFG_S: {"respuestas": [{"error": "Parametro no encontrado"}]}}, tablas=TT)
    g.caso("empresa-nula-se-pasa-al-colaborador", S, entrada_u("2026-03-06", None), ["B-REP-03"], cfg("S"), tablas=TT, seed=semilla_sebra())
    g.caso("otro-periodo-no-toca-marzo", S, entrada_u("2026-02-27"), ["B-REP-04", "B-REP-17"], cfg("S"), tablas=TT, seed=semilla_sebra())
    g.caso("periodo-sin-filas-en-origen-vacia-el-destino", S, entrada_u("2026-05-10"), ["B-REP-17"], cfg("S"), tablas=TT,
           seed=semilla_tbc_periodo(SEBRA13, [("2026-05", 1), ("2026-04", 2)], [("2026-04", 12)], None))
    g.caso("fecha-con-hora-usa-los-primeros-siete", S, entrada_u("2026-03-06 10:00:00"), ["B-REP-04"], cfg("S"), tablas=TT, seed=semilla_sebra())
    g.caso("fecha-con-barras-usa-siete-caracteres", S, entrada_u("2026/03/06"), ["B-REP-04"], cfg("S"), tablas=TT, seed=semilla_sebra())
    g.caso("fecha-corta-falla-al-recortar", S, entrada_u("2026"), ["B-REP-04"], cfg("S"), tablas=TT, seed=semilla_sebra())
    g.caso("fecha-de-siete-caracteres-basta", S, entrada_u("2026-03"), ["B-REP-04"], cfg("S"), tablas=TT, seed=semilla_sebra())
    g.caso("fecha-nula-con-S-falla-al-recortar", S, entrada_u(None), ["B-REP-04", "D-REP-11"], cfg("S"), tablas=TT, seed=semilla_sebra())
    g.caso("fecha-nula-con-N-no-falla", S, entrada_u(None), ["B-REP-03", "D-REP-11"], cfg("N"), tablas=TT, seed=semilla_sebra())
    for i, t in enumerate(ORDEN_SEBRA):
        if i in (0, 4, 12):
            g.caso("falla-borrado-de-%s-propaga-y-corta" % t.lower(), S, entrada_u(), ["B-REP-17", "D-REP-13"], cfg("S"),
                   fallos=[fallo('delete from "BSCL.Replica.BD::%s" where' % t, "bloqueo de tabla")], tablas=TT, seed=semilla_sebra())
            g.caso("falla-insercion-de-%s-deja-el-periodo-vacio-y-propaga" % t.lower(), S, entrada_u(), ["B-REP-17", "D-REP-13"], cfg("S"),
                   fallos=[fallo('INSERT INTO "BSCL.Replica.BD::%s"' % t, "disco lleno")], tablas=TT, seed=semilla_sebra())
    g.caso("clave-duplicada-en-el-origen-viola-la-clave-primaria", S, entrada_u(), ["B-REP-17", "D-REP-13"], cfg("S"), tablas=TT,
           seed=semilla_sebra() + [tbc("TBCTCIRF", "2026-03", 11, origen=True)])
    g.caso("origen-con-fila-ya-en-el-destino-la-reemplaza-sin-violar-la-clave", S, entrada_u(), ["B-REP-17"], cfg("S"), tablas=TT,
           seed=semilla_tbc_periodo(["TBCPDTPC"], [], [("2026-03", 11)], None) + [tbc("TBCPDTPC", "2026-03", 11)])
    ids += g.ids

    # ---------------------------------------------------------------- DIA
    g = Gen("DIA")
    D = "ReplicaUtils.procesarRespaldoDiarioCarterasSebra"
    DIARIAS = ["TBCTCIFM", "TBCTCIRV", "TBCTCIIF", "TBCTCIRF"]
    TD = [dump_tbc(t) for t in DIARIAS]

    def semilla_dia(dia="9006-01"):
        s = []
        for t in DIARIAS:
            s += [tbc(t, "2026-03", 1), tbc(t, "2026-03", 2), tbc(t, "2026-02", 3), tbc(t, dia, 91)]
        return s

    g.caso("param-S-copia-el-periodo-al-dia", D, entrada_u("2026-03-06"), ["B-REP-18", "D-REP-14"], cfg("S"), tablas=TD, seed=semilla_dia())
    g.caso("param-s-minuscula-tambien-activa", D, entrada_u("2026-03-06"), ["B-REP-18"], cfg("s"), tablas=TD, seed=semilla_dia())
    for v, slug in (("N", "N"), ("", "vacio"), ("X", "X"), ("SI", "SI")):
        g.caso("param-%s-no-hace-nada" % slug, D, entrada_u("2026-03-06"), ["B-REP-18"], cfg(v), tablas=TD, seed=semilla_dia())
    g.caso("param-ausente-se-traga-y-no-respalda", D, entrada_u("2026-03-06"), ["B-REP-23", "D-REP-12"],
           {CFG_S: {"respuestas": [{"error": "Parametro PLA.RespaldoDiarioReplicaCarteraSebra en modulo PLATAFORMA no encontrado"}]}}, tablas=TD, seed=semilla_dia())
    g.caso("param-nulo-falla-al-comparar-fuera-del-try", D, entrada_u("2026-03-06"), ["B-REP-23"], cfg(None), tablas=TD, seed=semilla_dia())
    g.caso("dia-uno-genera-9001-01", D, entrada_u("2026-04-01"), ["B-REP-18"], cfg("S"), tablas=TD,
           seed=[tbc(t, "2026-04", 5) for t in DIARIAS] + [tbc(t, "9001-01", 92) for t in DIARIAS])
    g.caso("dia-treinta-y-uno-genera-9031-01", D, entrada_u("2026-03-31"), ["B-REP-18"], cfg("S"), tablas=TD, seed=semilla_dia("9031-01"))
    g.caso("mismo-dia-de-otro-mes-pisa-el-respaldo-anterior", D, entrada_u("2026-04-06"), ["B-REP-18", "D-REP-14"], cfg("S"), tablas=TD,
           seed=semilla_dia() + [tbc(t, "2026-04", 7) for t in DIARIAS])
    g.caso("periodo-sin-filas-borra-el-respaldo-del-dia", D, entrada_u("2026-05-06"), ["B-REP-18", "D-REP-14"], cfg("S"), tablas=TD, seed=semilla_dia())
    g.caso("fecha-con-hora-usa-el-dia-de-las-posiciones-8-a-10", D, entrada_u("2026-03-06 23:59:59"), ["B-REP-18"], cfg("S"), tablas=TD, seed=semilla_dia())
    g.caso("fecha-sin-guiones-toma-posiciones-fijas", D, entrada_u("20260306xx"), ["B-REP-18"], cfg("S"), tablas=TD, seed=semilla_dia("9006-01"))
    g.caso("fecha-corta-falla-al-recortar-el-dia", D, entrada_u("2026-03"), ["B-REP-18"], cfg("S"), tablas=TD, seed=semilla_dia())
    g.caso("fecha-nula-con-S-falla", D, entrada_u(None), ["B-REP-18", "D-REP-11"], cfg("S"), tablas=TD, seed=semilla_dia())
    g.caso("fecha-nula-con-N-no-falla", D, entrada_u(None), ["B-REP-18"], cfg("N"), tablas=TD, seed=semilla_dia())
    g.caso("empresa-nula-se-pasa-al-colaborador", D, entrada_u("2026-03-06", None), ["B-REP-18"], cfg("S"), tablas=TD, seed=semilla_dia())
    for i, t in enumerate(["TBCTCIFM", "TBCTCIRV", "TBCTCIIF", "TBCTCIRF"]):
        if i in (0, 3):
            g.caso("falla-borrado-diario-de-%s-propaga" % t.lower(), D, entrada_u("2026-03-06"), ["B-REP-18", "D-REP-13"], cfg("S"),
                   fallos=[fallo('delete from "BSCL.Replica.BD::%s" where COD_PER = \'9006-01\'' % t, "bloqueo de tabla")], tablas=TD, seed=semilla_dia())
        g.caso("falla-insercion-diaria-de-%s-propaga" % t.lower(), D, entrada_u("2026-03-06"), ["B-REP-18", "D-REP-13"], cfg("S"),
               fallos=[fallo('INSERT INTO "BSCL.Replica.BD::%s"' % t, "disco lleno")], tablas=TD, seed=semilla_dia())
    ids += g.ids

    # ---------------------------------------------------------------- OPT
    g = Gen("OPT")
    O = "ReplicaUtils.procesarRespaldoOptimus"
    TO = [dump_sef(), dump_sfm(), dump_hli(), dump_hop(), dump_post()]
    SO = semilla_ci("2026-03-06") + semilla_cus("2026-03-06")
    g.caso("sin-dias-inhabiles-guarda-cuatro-envios", O, entrada_u("2026-03-06"), ["B-REP-19", "B-REP-20"], colab(**dict(origen="2026-03-06", habiles=[True])), tablas=TO, seed=SO)
    g.caso("fin-de-semana-copia-dos-dias-y-guarda-doce-envios", O, entrada_u("2026-03-06"), ["B-REP-19", "B-REP-20", "B-REP-22"], colab(**dict(origen="2026-03-06", habiles=[False, False, True])), tablas=TO, seed=SO)
    g.caso("el-pais-que-recibe-el-colaborador-es-el-id-de-empresa", O, entrada_u("2026-03-06", 2), ["D-REP-02"],
           dict(colab(**dict(pais=152, origen="2026-03-06", habiles=[False, True], pais_ods=None)), **{}), tablas=TO, seed=SO)
    g.caso("pais-igual-al-id-de-empresa-no-delata-el-defecto", O, entrada_u("2026-03-06", 1), ["D-REP-02"], colab(**dict(pais=1, origen="2026-03-06", habiles=[False, True])), tablas=TO, seed=SO)
    g.caso("cinco-dias-inhabiles", O, entrada_u("2026-04-01"), ["B-REP-19"], colab(**dict(origen="2026-04-01", habiles=[False] * 5 + [True])), tablas=TO, seed=semilla_ci("2026-04-01") + semilla_cus("2026-04-01"))
    g.caso("destino-con-datos-previos-se-reemplaza", O, entrada_u("2026-03-06"), ["B-REP-19"], colab(**dict(origen="2026-03-06", habiles=[False, True])), tablas=TO,
           seed=semilla_ci("2026-03-06", destino="2026-03-07") + semilla_cus("2026-03-06", destino="2026-03-07"))
    g.caso("borra-la-fecha-del-sistema-antes-de-insertar", O, entrada_u("2026-03-06"), ["B-REP-19"], colab(**dict(origen="2026-03-06", habiles=[True])), tablas=TO, seed=SO)
    g.caso("envio-ya-registrado-no-se-duplica", O, entrada_u("2026-03-06"), ["B-REP-20"], colab(**dict(origen="2026-03-06", habiles=[False, True])), tablas=TO,
           seed=SO + [insertar(T_POST, FechaSistema="2026-03-06", Tabla="Custodia_CustodiaFechaOperacionHis", Fecha="2026-03-06")])
    g.caso("error-dia-siguiente-corta-sin-excepcion", O, entrada_u("2026-03-06"), ["B-REP-19", "D-REP-04"], colab(**dict(origen="2026-03-06", habiles=[False, False, True], contador_en=1)), tablas=TO, seed=SO)
    g.caso("error-en-el-primer-dia-solo-queda-el-bloque-del-dia", O, entrada_u("2026-03-06"), ["B-REP-19", "D-REP-04"], colab(**dict(origen="2026-03-06", habiles=[False, True], contador_en=0)), tablas=TO, seed=SO)
    g.caso("colaborador-dia-siguiente-lanza-error-se-propaga", O, entrada_u("2026-03-06"), ["B-REP-19", "D-REP-13"], colab(**dict(origen="2026-03-06", habiles=[False, True], error_ods_en=0)), tablas=TO, seed=SO)
    g.caso("feriados-lanza-error-en-el-segundo-dia", O, entrada_u("2026-03-06"), ["B-REP-19", "D-REP-13"], colab(**dict(origen="2026-03-06", habiles=[False, False, True], error_fer_en=1)), tablas=TO, seed=SO)
    g.caso("lista-de-fechas-vacia-falla-con-indice", O, entrada_u("2026-03-06"), ["B-REP-19"], colab(**dict(origen="2026-03-06", habiles=[False, True], vacia_en=0)), tablas=TO, seed=SO)
    g.caso("empresa-inexistente-despues-del-primer-bloque", O, entrada_u("2026-03-06"), ["B-REP-19", "D-REP-13"], {"ConsultarEmpresa.logica": {"respuestas": [{"error": "Empresa no existe"}]}}, tablas=TO, seed=SO)
    g.caso("falla-el-primer-borrado", O, entrada_u("2026-03-06"), ["B-REP-19", "D-REP-13"], colab(**dict(origen="2026-03-06", habiles=[True])), fallos=[fallo("DELETE FROM", "bloqueo")], tablas=TO, seed=SO)
    g.caso("falla-la-insercion-historica-de-saldo-movimiento", O, entrada_u("2026-03-06"), ["B-REP-19", "D-REP-13"], colab(**dict(origen="2026-03-06", habiles=[True])),
           fallos=[fallo("ConsultarSaldoCuentaInversionFechaMovimientoAnalitica", "vista no disponible")], tablas=TO, seed=SO)
    g.caso("falla-el-upsert-del-envio", O, entrada_u("2026-03-06"), ["B-REP-20", "D-REP-13"], colab(**dict(origen="2026-03-06", habiles=[True])), fallos=[fallo("EnvioPostPauta", "tabla bloqueada", 2)], tablas=TO, seed=SO)
    g.caso("falla-la-copia-inhabil-de-custodia-operacion", O, entrada_u("2026-03-06"), ["B-REP-19", "D-REP-13"], colab(**dict(origen="2026-03-06", habiles=[False, True])),
           fallos=[fallo("FROM \"BSCL.Custodia.BD::HIS.CustodiaFechaOperacion\" WHERE", "disco lleno")], tablas=TO, seed=SO)
    g.caso("fecha-nula-falla-en-el-primer-borrado", O, entrada_u(None), ["D-REP-11"], colab(), tablas=TO, seed=SO)
    g.caso("fecha-con-hora-pasa-tal-cual", O, entrada_u("2026-03-06 10:00:00"), ["D-REP-11"], colab(**dict(origen="2026-03-06 10:00:00", habiles=[True])), tablas=TO, seed=SO)
    g.caso("empresa-nula-llega-al-colaborador-de-empresa", O, entrada_u("2026-03-06", None), ["B-REP-19"], colab(**dict(origen="2026-03-06", habiles=[True])), tablas=TO, seed=SO)
    ids += g.ids

    # ---------------------------------------------------------------- POS
    g = Gen("POS")
    P = "ReplicaUtils.guardarEnvioPostPauta"
    TP = [dump_post()]

    def e(tabla="Custodia_CarteraFM", sistema="2026-03-06", envio="2026-03-09"):
        return {"tabla": tabla, "fechasistema": sistema, "fechaenvio": envio}

    g.caso("guarda-una-terna", P, e(), ["B-REP-20"], None, tablas=TP)
    g.caso("misma-terna-no-duplica", P, e(), ["B-REP-20", "D-REP-10"], None, tablas=TP, seed=[insertar(T_POST, FechaSistema="2026-03-06", Tabla="Custodia_CarteraFM", Fecha="2026-03-09")])
    g.caso("otra-fecha-de-envio-agrega-fila", P, e(), ["B-REP-20", "D-REP-10"], None, tablas=TP, seed=[insertar(T_POST, FechaSistema="2026-03-06", Tabla="Custodia_CarteraFM", Fecha="2026-03-07")])
    g.caso("conserva-las-otras-filas", P, e("Otra"), ["B-REP-20"], None, tablas=TP,
           seed=[insertar(T_POST, FechaSistema="2026-03-06", Tabla="Custodia_CarteraFM", Fecha="2026-03-09"), insertar(T_POST, FechaSistema="2026-03-05", Tabla="Otra", Fecha="2026-03-06")])
    g.caso("tabla-nula-viola-la-clave", P, e(None), ["B-REP-20"], None, tablas=TP)
    g.caso("fecha-sistema-nula-viola-la-clave", P, e(sistema=None), ["B-REP-20"], None, tablas=TP)
    g.caso("fecha-envio-nula-viola-la-clave", P, e(envio=None), ["B-REP-20"], None, tablas=TP)
    g.caso("tabla-de-100-caracteres-cabe", P, e("T" * 100), ["B-REP-20"], None, tablas=TP)
    g.caso("tabla-de-101-caracteres-no-cabe", P, e("T" * 101), ["B-REP-20"], None, tablas=TP)
    g.caso("fecha-de-11-caracteres-no-cabe", P, e(envio="2026-03-091"), ["B-REP-20"], None, tablas=TP)
    g.caso("fecha-con-hora-no-cabe", P, e(sistema="2026-03-06 10:00:00"), ["B-REP-20"], None, tablas=TP)
    g.caso("valores-sin-formato-se-guardan", P, e("tabla con espacios y ñ", "x", "y"), ["B-REP-20"], None, tablas=TP)
    g.caso("falla-el-upsert-propaga", P, e(), ["B-REP-20"], None, fallos=[fallo("EnvioPostPauta", "tabla bloqueada")], tablas=TP)
    g.caso("comillas-en-la-tabla-van-como-parametro", P, e("a'b"), ["B-REP-20"], None, tablas=TP)
    ids += g.ids
    return ids
