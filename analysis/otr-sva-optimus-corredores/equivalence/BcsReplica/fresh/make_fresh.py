"""BcsReplica, verificacion independiente 2026-10-30: ENTRADAS frescas (las que el desarrollo NO uso). Escribe solo en fresh/inputs/ (case.json + seed.sql).
No toca cases/ ni ningun golden. Reutiliza la biblioteca de generadores del oraculo (cases_rep_lib) con la carpeta de salida cambiada.

  py -3 -I make_fresh.py
"""
import json
import pathlib
import shutil
import sys

AQUI = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(AQUI.parent))
import cases_rep_lib as L  # noqa: E402
from cases_rep_lib import *  # noqa: E402,F401,F403
import cases_rep_env as E  # noqa: E402
from cases_rep_env import semilla_ci, semilla_cus  # noqa: E402
import cases_rep_utils as U  # noqa: E402

L.CASES = AQUI / "inputs"
if L.CASES.exists():
    shutil.rmtree(L.CASES)
L.CASES.mkdir()

SVC = "EnviarTablaReplica.logica"
CFG_S = U.CFG_S


def colab_fin(offset="0", **kw):
    c = colab()
    c["ConfiguracionSistema.getParametroConfiguracionLong"] = {"defecto": offset}
    c.update(kw)
    return c


def retencion(fecha_sistema):
    """filas alrededor del limite de retencion (fecha - 10 dias): limite - 1, limite, limite + 1, fecha - 1"""
    lim = sumar_dias(fecha_sistema, -10)
    s = []
    for k, f in enumerate((sumar_dias(lim, -1), lim, sumar_dias(lim, 1), sumar_dias(fecha_sistema, -1))):
        i = 80 + k
        s += [saldo(T_SEF, f, i), saldo(T_SFM, f, i), custodia(T_HLI, f, i, 800 + i), custodia(T_HOP, f, i, 800 + i)]
    return s


def sebra_sem(per, prev):
    return semilla_tbc_periodo(SEBRA13, [(per, 1), (per, 2), (prev, 3)], [(per, 21), (per, 22), (prev, 23)], None)


def largo_cartera_rv(n):
    base = "tables:Custodia_CarteraRV;BCS_query:QueryReplica;Fecha:"
    cola = " ;serialDelete:false;truncarDestino:true;serialLaunch:false"
    return n - len(base) - len(cola)


def largo_patrimonio(n):
    base = ("tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,"
            "Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:")
    cola = ";serialDelete:false;truncarDestino:false;serialLaunch:false"
    return n - len(base) - len(cola)


g = Gen("FRE")
TW = [dump_ws()]
TCI = [dump_ws(), dump_sef(), dump_sfm()]
TCUS = [dump_ws(), dump_hli(), dump_hop()]
TINI = [dump_ws(), dump_sef(), dump_sfm(), dump_hli(), dump_hop()]
TFIN = [dump_ws(), dump_res(), dump_sef(), dump_sfm(), dump_hli(), dump_hop()]

# ---- 8 procesos de EnviarTablaReplica con OTRA capitalizacion (cierra la laguna de M02: el desarrollo solo tiene minusculas para INICIO_PAUTA)
g.caso("valorizar-ffmm-minusculas-fin-de-semana", SVC, entrada_env("valorizar_ffmm", "2026-06-26"), ["B-REP-05", "B-REP-07", "B-REP-14"],
       colab(**dict(origen="2026-06-26", habiles=[False, False, True])), tablas=TCUS, seed=semilla_cus("2026-06-26", donde="tabla"))
g.caso("pauta-patrimonio-minusculas", SVC, entrada_env("pauta_patrimonio", "2026-06-30"), ["B-REP-05", "B-REP-08"], colab(), tablas=TW)
g.caso("copia-patrimonio-mixto-feriados-contiguos", SVC, entrada_env("Copia_Patrimonio_Para_Inhabil", "2026-09-17"), ["B-REP-05", "B-REP-09"],
       colab(**dict(origen="2026-09-17", habiles=[False, False, False, True])), tablas=TW)
g.caso("pauta-respaldar-custodia-minusculas", SVC, entrada_env("pauta_respaldar_custodia", "2028-02-29"), ["B-REP-05", "B-REP-10"], colab(), tablas=TW)
g.caso("cambio-fecha-sistema-mixto", SVC, entrada_env("Cambio_Fecha_Sistema", "2027-01-01"), ["B-REP-05", "B-REP-11"], colab(), tablas=TW)
g.caso("valorizar-cartera-rv-minusculas", SVC, entrada_env("valorizar_cartera_rv_precio_mercado", "2026-12-31"), ["B-REP-05", "B-REP-12", "D-REP-05"], colab(), tablas=TW)
g.caso("fin-pauta-minusculas-offset-3h-bisiesto", SVC, entrada_env("fin_pauta", "2028-02-29"), ["B-REP-05", "B-REP-13"], colab_fin("10800"), tablas=TFIN,
       seed=semilla_ci("2028-02-29") + retencion("2028-02-29"), reloj="2028-02-29 22:00:00")
g.caso("inicio-pauta-mixto-bisiesto-retencion", SVC, entrada_env("Inicio_Pauta", "2028-03-01"), ["B-REP-05", "B-REP-06", "B-REP-21"],
       colab(**dict(origen="2028-03-01", habiles=[True])), tablas=TINI, seed=semilla_ci("2028-03-01") + retencion("2028-03-01"))
g.caso("inicio-pauta-fin-de-ano-cuatro-inhabiles", SVC, entrada_env("INICIO_PAUTA", "2026-12-30"), ["B-REP-06", "B-REP-14"],
       colab(**dict(origen="2026-12-30", habiles=[False, False, False, False, True])), tablas=TINI, seed=semilla_ci("2026-12-30") + semilla_cus("2026-12-30"))
g.caso("inicio-pauta-otra-empresa-pais-distinto-feriados", SVC, entrada_env("INICIO_PAUTA", "2026-09-17", empresa=3), ["B-REP-06", "B-REP-14"],
       colab(**dict(pais=604, origen="2026-09-17", habiles=[False, False, True], pais_ods=604)), tablas=TINI, seed=semilla_ci("2026-09-17"))
g.caso("valorizar-ffmm-mayuscula-otra-empresa", SVC, entrada_env("VALORIZAR_FFMM", "2026-07-03", empresa=7), ["B-REP-07", "B-REP-14"],
       colab(**dict(pais=32, origen="2026-07-03", habiles=[False, False, True])), tablas=TCUS, seed=semilla_cus("2026-07-03", donde="tabla"))
g.caso("fin-pauta-reloj-bisiesto-offset-negativo", SVC, entrada_env("FIN_PAUTA", "2028-02-28"), ["B-REP-13"], colab_fin("-86400"), tablas=TFIN, reloj="2028-03-01 00:00:05")
g.caso("fin-pauta-fecha-31-de-junio-inexistente", SVC, entrada_env("FIN_PAUTA", "2026-06-31"), ["B-REP-13", "D-REP-19"], colab_fin("0"), tablas=TFIN)
# Parameters alrededor del limite VARCHAR(500)
for n in (499, 500, 501):
    g.caso("cartera-rv-parameters-de-%d-caracteres" % n, SVC, entrada_env("VALORIZAR_CARTERA_RV_PRECIO_MERCADO", "2026-03-04" + "z" * (largo_cartera_rv(n) - 10)), ["B-REP-12", "D-REP-15"], colab(), tablas=TW)
for n in (500, 501):
    g.caso("patrimonio-parameters-de-%d-caracteres" % n, SVC, entrada_env("PAUTA_PATRIMONIO", "2026-03-04" + "z" * (largo_patrimonio(n) - 10)), ["B-REP-08", "D-REP-15"], colab(), tablas=TW)
# registros mal formados (no hay INTENT.md: se prueban)
g.caso("proceso-con-tabulador-final", SVC, entrada_env("FIN_PAUTA\t", "2026-06-30"), ["D-REP-16"], colab_fin("0"), tablas=TFIN)
g.caso("proceso-con-acento-no-coincide", SVC, entrada_env("INICIO_PAUTÁ", "2026-06-30"), ["D-REP-16"], colab(), tablas=TINI)
g.caso("pauta-patrimonio-fecha-con-comillas-y-unicode", SVC, entrada_env("PAUTA_PATRIMONIO", "2026-06-30'\"ñ\t"), ["B-REP-08", "D-REP-11"], colab(), tablas=TW)
g.caso("fecha-como-booleano-es-un-class-cast", SVC, {"p_IdEmpresa": 1, "p_Proceso": "PAUTA_PATRIMONIO", "p_Parametros": {"FechaSistemaEjecucion": True}}, ["D-REP-11"], colab(), tablas=TW)
g.caso("empresa-negativa-y-proceso-mixto", SVC, entrada_env("Valorizar_Ffmm", "2026-06-30", empresa=-5), ["B-REP-05", "B-REP-07"],
       colab(**dict(origen="2026-06-30", habiles=[True])), tablas=TCUS, seed=semilla_cus("2026-06-30", donde="tabla"))

# ---- ReplicaUtils.procesarRespaldoSebra: parametro PLA.RespaldoReplicaTablasSebra
S = "ReplicaUtils.procesarRespaldoSebra"
TT = dump_tbc_todas()
g.caso("sebra-S-bisiesto-fin-de-mes", S, U.entrada_u("2028-02-29"), ["B-REP-03", "B-REP-04", "B-REP-17"], U.cfg("S"), tablas=TT, seed=sebra_sem("2028-02", "2028-01"))
g.caso("sebra-S-fin-de-ano-otra-empresa", S, U.entrada_u("2026-12-31", 4), ["B-REP-03", "B-REP-17"], U.cfg("S"), tablas=TT, seed=sebra_sem("2026-12", "2026-11"))
for v, slug in (("Si", "Si"), ("SS", "SS"), ("\tS", "tab-S"), ("S\n", "S-salto"), ("y", "y"), ("s", "s-minuscula-dic")):
    g.caso("sebra-param-%s" % slug, S, U.entrada_u("2026-06-30"), ["B-REP-03"], U.cfg(v), tablas=TT, seed=sebra_sem("2026-06", "2026-05"))
g.caso("sebra-fecha-sin-guiones-ocho-digitos", S, U.entrada_u("20260630"), ["B-REP-04"], U.cfg("S"), tablas=TT, seed=sebra_sem("2026063", "2026-06"))
g.caso("sebra-fecha-vacia-con-S", S, U.entrada_u(""), ["B-REP-04"], U.cfg("S"), tablas=TT, seed=sebra_sem("2026-06", "2026-05"))

# ---- ReplicaUtils.procesarRespaldoDiarioCarterasSebra: parametro PLA.RespaldoDiarioReplicaCarteraSebra y codigo de dia
D = "ReplicaUtils.procesarRespaldoDiarioCarterasSebra"
DIARIAS = ["TBCTCIFM", "TBCTCIRV", "TBCTCIIF", "TBCTCIRF"]
TD = [dump_tbc(t) for t in DIARIAS]


def sem_dia(per, dia):
    s = []
    for t in DIARIAS:
        s += [tbc(t, per, 1), tbc(t, per, 2), tbc(t, dia, 91), tbc(t, "2026-01", 5)]
    return s


g.caso("diario-S-bisiesto-29", D, U.entrada_u("2028-02-29"), ["B-REP-18"], U.cfg("S"), tablas=TD, seed=sem_dia("2028-02", "9029-01"))
g.caso("diario-S-fin-de-ano-31", D, U.entrada_u("2026-12-31"), ["B-REP-18", "D-REP-14"], U.cfg("S"), tablas=TD, seed=sem_dia("2026-12", "9031-01"))
g.caso("diario-S-dia-10-es-dos-digitos", D, U.entrada_u("2026-09-10"), ["B-REP-18"], U.cfg("S"), tablas=TD, seed=sem_dia("2026-09", "9010-01"))
for v, slug in (("Si", "Si"), ("SS", "SS"), ("\tS", "tab-S"), ("n", "n-minuscula"), ("0", "cero")):
    g.caso("diario-param-%s" % slug, D, U.entrada_u("2026-06-30"), ["B-REP-18"], U.cfg(v), tablas=TD, seed=sem_dia("2026-06", "9030-01"))
g.caso("diario-fecha-con-barras", D, U.entrada_u("2026/06/30"), ["B-REP-18"], U.cfg("S"), tablas=TD, seed=sem_dia("2026/06", "9030-01"))

# ---- ReplicaUtils.procesarRespaldoOptimus y guardarEnvioPostPauta
O = "ReplicaUtils.procesarRespaldoOptimus"
TO = [dump_sef(), dump_sfm(), dump_hli(), dump_hop(), dump_post()]
g.caso("optimus-bisiesto-fin-de-semana", O, U.entrada_u("2028-02-25", 5), ["B-REP-19", "D-REP-02"], colab(**dict(pais=56, origen="2028-02-25", habiles=[False, False, True])),
       tablas=TO, seed=semilla_ci("2028-02-25") + semilla_cus("2028-02-25"))
g.caso("optimus-fin-de-ano-feriados-contiguos", O, U.entrada_u("2026-12-31", 2), ["B-REP-19"], colab(**dict(pais=2, origen="2026-12-31", habiles=[False, False, True])),
       tablas=TO, seed=semilla_ci("2026-12-31") + semilla_cus("2026-12-31"))
P = "ReplicaUtils.guardarEnvioPostPauta"
for slug, tabla, a, b in (("fin-de-mes", "Custodia_CarteraRVHis", "2026-06-30", "2026-07-01"), ("bisiesto", "Custodia_GarantiaHis", "2028-02-29", "2028-03-01"),
                          ("fin-de-ano", "Custodia_CarteraFMHis", "2026-12-31", "2027-01-04")):
    g.caso("post-pauta-%s" % slug, P, {"tabla": tabla, "fechasistema": a, "fechaenvio": b}, ["B-REP-20"], None, tablas=[dump_post()])

# ---- IniciarReplicaFull / Delta
for clase in ("IniciarReplicaFull", "IniciarReplicaDelta"):
    corto = clase[len("IniciarReplica"):].lower()
    from cases_rep_ini import colab_ini, entrada_i  # noqa: E402
    TI = [{"nombre": T_WS, "orden": '"IdStack"'}]
    g.caso("%s-logica-bisiesto-secuencia-alta" % corto, clase + ".logica", entrada_i(9, 123), ["B-REP-01", "B-REP-02"], colab_ini((100000,), "2028-02-29"), tablas=TI, reloj="2028-02-29 00:00:01")
    g.caso("%s-call-empresa-con-espacios-como-texto" % corto, clase + ".call", {"p_IdEmpresa": " 7 ", "p_IdUsuario": 4}, ["D-REP-18"], colab_ini(), tablas=TI)
    g.caso("%s-call-empresa-booleana" % corto, clase + ".call", {"p_IdEmpresa": True, "p_IdUsuario": 4}, ["D-REP-18"], colab_ini(), tablas=TI)
    g.caso("%s-call-empresa-notacion-cientifica" % corto, clase + ".call", {"p_IdEmpresa": 1e1, "p_IdUsuario": 4}, ["B-REP-24"], colab_ini(), tablas=TI)
    g.caso("%s-call-empresa-arreglo" % corto, clase + ".call", {"p_IdEmpresa": [1], "p_IdUsuario": 4}, ["D-REP-18"], colab_ini(), tablas=TI)
    g.caso("%s-call-empresa-cero-con-fecha-con-tabulador" % corto, clase + ".call", entrada_i(0), ["B-REP-24"], colab_ini((7,), "2026-06-30\t"), tablas=TI)

print("fresh inputs: %d casos en %s" % (len(g.ids), L.CASES))
(AQUI / "fresh-ids.txt").write_text("\n".join(g.ids) + "\n", encoding="utf-8", newline="\n")
