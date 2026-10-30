"""BcsReplica oracle: inputs of the IniciarReplicaFull / IniciarReplicaDelta cases (family INI): logica and the JavaCall entry."""
from cases_rep_lib import *  # noqa: F401,F403

TINI = [{"nombre": T_WS, "orden": '"IdStack"'}]


def colab_ini(secuencia=(41,), fecha="2026-03-04"):
    c = {}
    if secuencia is not None:
        c["Utils.getSecuencia"] = {"defecto": list(secuencia)}
    c["Utils.getFechaSistema"] = {"defecto": fecha}
    return c


def entrada_i(empresa=1, usuario=4):
    return {"p_IdEmpresa": empresa, "p_IdUsuario": usuario}


def generar():
    g = Gen("INI")
    for clase, consulta in (("IniciarReplicaFull", "QueryReplica"), ("IniciarReplicaDelta", "QueryReplicaDelta")):
        corto = clase[len("IniciarReplica"):].lower()
        L, C = clase + ".logica", clase + ".call"
        g.caso("%s-logica-encola-la-pauta" % corto, L, entrada_i(), ["B-REP-01", "B-REP-02", "D-REP-17"], colab_ini(), tablas=TINI)
        g.caso("%s-call-devuelve-el-mensaje-ok" % corto, C, entrada_i(), ["B-REP-01", "B-REP-02", "B-REP-24"], colab_ini(), tablas=TINI)
        g.caso("%s-logica-secuencia-grande-y-fecha-de-fin-de-ano" % corto, L, entrada_i(7, 99), ["B-REP-01", "B-REP-02"], colab_ini((9007199254740993,), "2026-12-31"), tablas=TINI, reloj="2026-12-31 23:59:59")
        g.caso("%s-logica-la-empresa-llega-a-la-fecha-del-sistema" % corto, L, entrada_i(7), ["B-REP-01"],
               {"Utils.getSecuencia": {"defecto": [5]}, "Utils.getFechaSistema": {"respuestas": [{"cuando": {"1": 7}, "respuesta": "2026-05-05"}, {"respuesta": "1999-01-01"}]}}, tablas=TINI)
        g.caso("%s-logica-usuario-nulo-no-importa" % corto, L, {"p_IdEmpresa": 1, "p_IdUsuario": None}, ["D-REP-17"], colab_ini(), tablas=TINI)
        g.caso("%s-logica-empresa-nula-falla-al-leer-la-fecha" % corto, L, {"p_IdEmpresa": None, "p_IdUsuario": 4}, ["D-REP-18"], colab_ini(), tablas=TINI)
        g.caso("%s-logica-secuencia-vacia-falla-con-indice" % corto, L, entrada_i(), ["B-REP-02"], colab_ini(()), tablas=TINI)
        g.caso("%s-logica-secuencia-con-varios-valores-usa-el-primero" % corto, L, entrada_i(), ["B-REP-02"], colab_ini((41, 42, 43)), tablas=TINI)
        g.caso("%s-logica-colaborador-secuencia-lanza-error" % corto, L, entrada_i(), ["B-REP-02"], {"Utils.getSecuencia": {"respuestas": [{"error": "secuencia agotada"}]}, "Utils.getFechaSistema": {"defecto": "2026-03-04"}}, tablas=TINI)
        g.caso("%s-logica-colaborador-fecha-lanza-error" % corto, L, entrada_i(), ["B-REP-01"], {"Utils.getSecuencia": {"defecto": [41]}, "Utils.getFechaSistema": {"respuestas": [{"error": "sin fecha de sistema"}]}}, tablas=TINI)
        g.caso("%s-logica-fecha-nula-concatena-null" % corto, L, entrada_i(), ["B-REP-01"], colab_ini((41,), None), tablas=TINI)
        g.caso("%s-logica-fecha-con-hora-pasa-tal-cual" % corto, L, entrada_i(), ["B-REP-01"], colab_ini((41,), "2026-03-04 10:00:00"), tablas=TINI)
        g.caso("%s-logica-falla-la-insercion-propaga" % corto, L, entrada_i(), ["B-REP-02", "D-REP-13"], colab_ini(), fallos=[fallo("PLA.Replica.WorkStack", "no se puede insertar")], tablas=TINI)
        g.caso("%s-logica-idstack-repetido-viola-la-clave" % corto, L, entrada_i(), ["B-REP-02"], colab_ini(), tablas=TINI,
               seed=[insertar(T_WS, IdStack=41, Key="x", Table="t", Action="F", Status=0, Parameters="p")])
        g.caso("%s-logica-no-toca-las-filas-existentes" % corto, L, entrada_i(), ["B-REP-02"], colab_ini((42,)), tablas=TINI,
               seed=[insertar(T_WS, IdStack=41, Key="x", Table="t", Action="F", Status=0, Parameters="p")])
        g.caso("%s-call-sin-empresa-falla" % corto, C, {"p_IdUsuario": 4}, ["D-REP-18"], colab_ini(), tablas=TINI)
        g.caso("%s-call-json-vacio-falla" % corto, C, {}, ["D-REP-18"], colab_ini(), tablas=TINI)
        g.caso("%s-call-empresa-como-texto-numerico-se-acepta" % corto, C, {"p_IdEmpresa": "1", "p_IdUsuario": "4"}, ["B-REP-24"], colab_ini(), tablas=TINI)
        g.caso("%s-call-empresa-no-numerica-falla-al-leer-el-json" % corto, C, {"p_IdEmpresa": "abc", "p_IdUsuario": 4}, ["D-REP-18"], colab_ini(), tablas=TINI)
        g.caso("%s-call-empresa-decimal-entero-se-acepta" % corto, C, {"p_IdEmpresa": 1.0, "p_IdUsuario": 4}, ["B-REP-24"], colab_ini(), tablas=TINI)
        g.caso("%s-call-empresa-con-decimales-falla" % corto, C, {"p_IdEmpresa": 1.5, "p_IdUsuario": 4}, ["D-REP-18"], colab_ini(), tablas=TINI)
        g.caso("%s-call-campos-extra-se-ignoran" % corto, C, {"p_IdEmpresa": 1, "p_IdUsuario": 4, "otro": [1, 2], "p_Proceso": "X"}, ["B-REP-24"], colab_ini(), tablas=TINI)
        g.caso("%s-call-falla-la-secuencia-propaga" % corto, C, entrada_i(), ["B-REP-24"], {"Utils.getSecuencia": {"respuestas": [{"error": "secuencia agotada"}]}, "Utils.getFechaSistema": {"defecto": "2026-03-04"}}, tablas=TINI)
        g.caso("%s-call-falla-la-insercion-propaga" % corto, C, entrada_i(), ["B-REP-24", "D-REP-13"], colab_ini(), fallos=[fallo("PLA.Replica.WorkStack", "no se puede insertar")], tablas=TINI)
        g.caso("%s-call-empresa-negativa-se-pasa-igual" % corto, C, entrada_i(-1), ["B-REP-24"], colab_ini(), tablas=TINI)
    return g.ids
