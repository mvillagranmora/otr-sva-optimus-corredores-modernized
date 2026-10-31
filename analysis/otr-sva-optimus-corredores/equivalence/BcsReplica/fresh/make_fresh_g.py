"""BcsReplica, verificacion independiente 2026-10-30 (ronda g): ENTRADAS frescas PROPIAS del verificador, ids REP-FRG-nnn (distintas de las REP-FRE-001..059 de la ronda anterior).
Escribe SOLO en fresh/inputs/REP-FRG-* (case.json + seed.sql). NO borra nada (a diferencia de make_fresh.py, que hace rmtree de fresh/inputs/ y por eso no se ejecuto).
No toca cases/ ni ningun golden ni fresh/legacy ni fresh/new. Reutiliza la biblioteca de generadores del oraculo (cases_rep_lib) con la carpeta de salida cambiada.

  py -3 -I make_fresh_g.py
"""
import json
import pathlib
import sys

AQUI = pathlib.Path(__file__).resolve().parent
sys.path.insert(0, str(AQUI.parent))
import cases_rep_lib as L  # noqa: E402
from cases_rep_lib import *  # noqa: E402,F401,F403
from cases_rep_env import semilla_ci, semilla_cus  # noqa: E402
import cases_rep_utils as U  # noqa: E402

L.CASES = AQUI / "inputs"
assert L.CASES.exists()  # solo se agrega; nunca se borra nada

SVC = "EnviarTablaReplica.logica"


def colab_fin(offset="0", **kw):
    c = colab()
    c["ConfiguracionSistema.getParametroConfiguracionLong"] = {"defecto": offset}
    c.update(kw)
    return c


def retencion(fecha_sistema):
    lim = sumar_dias(fecha_sistema, -10)
    s = []
    for k, f in enumerate((sumar_dias(lim, -1), lim, sumar_dias(lim, 1), sumar_dias(fecha_sistema, -1))):
        i = 80 + k
        s += [saldo(T_SEF, f, i), saldo(T_SFM, f, i), custodia(T_HLI, f, i, 800 + i), custodia(T_HOP, f, i, 800 + i)]
    return s


def sebra_sem(per, prev):
    return semilla_tbc_periodo(SEBRA13, [(per, 1), (per, 2), (prev, 3)], [(per, 21), (per, 22), (prev, 23)], None)


BASE_RV = "tables:Custodia_CarteraRV;BCS_query:QueryReplica;Fecha:"
COLA_RV = " ;serialDelete:false;truncarDestino:true;serialLaunch:false"
BASE_PAT = ("tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,"
            "Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:")
COLA_PAT = ";serialDelete:false;truncarDestino:false;serialLaunch:false"


def fecha_larga(base, cola, n, prefijo, relleno):
    """fecha tal que len(Parameters) == n: empieza con `prefijo` y se completa repitiendo `relleno`"""
    largo = n - len(base) - len(cola)
    f = prefijo
    while len(f) < largo:
        f += relleno
    f = f[:largo]
    assert len(base + f + cola) == n
    return f


g = Gen("FRG")
TW = [dump_ws()]
TCUS = [dump_ws(), dump_hli(), dump_hop()]
TINI = [dump_ws(), dump_sef(), dump_sfm(), dump_hli(), dump_hop()]
TFIN = [dump_ws(), dump_res(), dump_sef(), dump_sfm(), dump_hli(), dump_hop()]
ESP = "'=<>&\""   # los caracteres que el escape HTML de Gson convierte en \uXXXX

# ---- Parameters (VARCHAR(500)) con caracteres especiales, tildes y 499/500/501
g.caso("cartera-rv-501-con-especiales-falla-insert", SVC, entrada_env("VALORIZAR_CARTERA_RV_PRECIO_MERCADO", fecha_larga(BASE_RV, COLA_RV, 501, "2026-03-04" + ESP, "z")), ["B-REP-12", "D-REP-15"], colab(), tablas=TW)
g.caso("patrimonio-501-con-especiales-falla-insert", SVC, entrada_env("PAUTA_PATRIMONIO", fecha_larga(BASE_PAT, COLA_PAT, 501, "2026-03-04=<>&'", "=")), ["B-REP-08", "D-REP-15"], colab(), tablas=TW)
g.caso("cartera-rv-500-con-especiales-inserta", SVC, entrada_env("VALORIZAR_CARTERA_RV_PRECIO_MERCADO", fecha_larga(BASE_RV, COLA_RV, 500, "2026-03-04" + ESP, "&")), ["B-REP-12", "D-REP-15"], colab(), tablas=TW)
for n in (499, 500, 501):
    g.caso("cartera-rv-%d-con-tildes" % n, SVC, entrada_env("VALORIZAR_CARTERA_RV_PRECIO_MERCADO", fecha_larga(BASE_RV, COLA_RV, n, "2026-03-04", "áéíóúñÁÉÍÓÚÑ")), ["B-REP-12", "D-REP-15"], colab(), tablas=TW)
for n in (499, 500, 501):
    g.caso("patrimonio-%d-con-tildes" % n, SVC, entrada_env("PAUTA_PATRIMONIO", fecha_larga(BASE_PAT, COLA_PAT, n, "2026-03-04", "üñé")), ["B-REP-08", "D-REP-15"], colab(), tablas=TW)
g.caso("patrimonio-501-tildes-y-especiales-falla-insert", SVC, entrada_env("PAUTA_PATRIMONIO", fecha_larga(BASE_PAT, COLA_PAT, 501, "2026-03-04ñ'<é>&=á", "ó")), ["B-REP-08", "D-REP-15"], colab(), tablas=TW)

# ---- cambio de anio, bisiesto (2024 si, 2100 no), otras empresas y paises
g.caso("inicio-pauta-cambio-de-anio-2024", SVC, entrada_env("INICIO_PAUTA", "2024-12-31", empresa=12), ["B-REP-06", "B-REP-14"],
       colab(**dict(pais=840, origen="2024-12-31", habiles=[False, False, True], pais_ods=840)), tablas=TINI, seed=semilla_ci("2024-12-31") + semilla_cus("2024-12-31") + retencion("2024-12-31"))
g.caso("inicio-pauta-bisiesto-2024-02-28", SVC, entrada_env("Inicio_Pauta", "2024-02-28", empresa=2), ["B-REP-06", "B-REP-14"],
       colab(**dict(pais=152, origen="2024-02-28", habiles=[False, True], pais_ods=152)), tablas=TINI, seed=semilla_ci("2024-02-28") + retencion("2024-02-28"))
g.caso("inicio-pauta-2100-no-es-bisiesto", SVC, entrada_env("inicio_pauta", "2100-02-28", empresa=5), ["B-REP-06", "B-REP-14"],
       colab(**dict(pais=724, origen="2100-02-28", habiles=[False, False, True], pais_ods=724)), tablas=TINI, seed=semilla_ci("2100-02-28") + retencion("2100-02-28"))
g.caso("inicio-pauta-semana-santa-otra-empresa", SVC, entrada_env("INICIO_PAUTA", "2026-04-02", empresa=25), ["B-REP-06", "B-REP-14"],
       colab(**dict(pais=76, origen="2026-04-02", habiles=[False, False, False, True], pais_ods=76)), tablas=TINI, seed=semilla_ci("2026-04-02") + semilla_cus("2026-04-02"))
g.caso("valorizar-ffmm-fin-de-anio-empresa-12", SVC, entrada_env("VALORIZAR_FFMM", "2027-12-31", empresa=12), ["B-REP-07", "B-REP-14"],
       colab(**dict(pais=840, origen="2027-12-31", habiles=[False, False, False, True])), tablas=TCUS, seed=semilla_cus("2027-12-31", donde="tabla"))
g.caso("copia-patrimonio-cambio-de-anio-pais-392", SVC, entrada_env("COPIA_PATRIMONIO_PARA_INHABIL", "2025-12-31", empresa=9), ["B-REP-05", "B-REP-09"],
       colab(**dict(pais=392, origen="2025-12-31", habiles=[False, True])), tablas=TW)
g.caso("cambio-fecha-sistema-bisiesto-2028", SVC, entrada_env("CAMBIO_FECHA_SISTEMA", "2028-02-29", empresa=6), ["B-REP-05", "B-REP-11"], colab(), tablas=TW)

# ---- FIN_PAUTA: reloj y desfases
g.caso("fin-pauta-cruza-el-anio-offset-120", SVC, entrada_env("FIN_PAUTA", "2026-12-31"), ["B-REP-13"], colab_fin("120"), tablas=TFIN,
       seed=semilla_ci("2026-12-31") + retencion("2026-12-31"), reloj="2026-12-31 23:59:30")
g.caso("fin-pauta-offset-con-signo-mas", SVC, entrada_env("FIN_PAUTA", "2028-02-29"), ["B-REP-13"], colab_fin("+60"), tablas=TFIN, reloj="2028-02-29 12:00:00")
g.caso("fin-pauta-offset-con-ceros-a-la-izquierda", SVC, entrada_env("fin_pauta", "2026-06-30"), ["B-REP-13"], colab_fin("007"), tablas=TFIN, reloj="2026-06-30 08:15:00")
g.caso("fin-pauta-offset-grande-negativo-cruza-bisiesto", SVC, entrada_env("Fin_Pauta", "2024-03-01", empresa=3), ["B-REP-13"], colab_fin("-172800"), tablas=TFIN, reloj="2024-03-01 00:00:01")

# ---- Sebra: otros valores de PLA.RespaldoReplicaTablasSebra
S = "ReplicaUtils.procesarRespaldoSebra"
TT = dump_tbc_todas()
for v, slug in (("S ", "S-espacio-final"), ("Sí", "S-tilde"), ("SÍ", "S-tilde-mayuscula"), ("true", "true"), ("1", "uno"), (" S", "espacio-inicial-S"), ("S", "S-mayuscula")):
    g.caso("sebra-param-%s" % slug, S, U.entrada_u("2024-02-29", 8), ["B-REP-03"], U.cfg(v), tablas=TT, seed=sebra_sem("2024-02", "2024-01"))
g.caso("sebra-cambio-de-anio-empresa-8", S, U.entrada_u("2025-01-01", 8), ["B-REP-03", "B-REP-17"], U.cfg("s"), tablas=TT, seed=sebra_sem("2025-01", "2024-12"))

# ---- diario: otros valores de PLA.RespaldoDiarioReplicaCarteraSebra y codigos de dia
D = "ReplicaUtils.procesarRespaldoDiarioCarterasSebra"
DIARIAS = ["TBCTCIFM", "TBCTCIRV", "TBCTCIIF", "TBCTCIRF"]
TD = [dump_tbc(t) for t in DIARIAS]


def sem_dia(per, dia):
    s = []
    for t in DIARIAS:
        s += [tbc(t, per, 1), tbc(t, per, 2), tbc(t, dia, 91), tbc(t, "2025-01", 5)]
    return s


g.caso("diario-S-bisiesto-2024-dia-29", D, U.entrada_u("2024-02-29", 8), ["B-REP-18"], U.cfg("S"), tablas=TD, seed=sem_dia("2024-02", "9029-01"))
g.caso("diario-S-primero-de-anio-9001", D, U.entrada_u("2025-01-01", 8), ["B-REP-18"], U.cfg("S"), tablas=TD, seed=sem_dia("2025-01", "9001-01"))
g.caso("diario-s-minuscula-dia-09", D, U.entrada_u("2026-10-09", 3), ["B-REP-18"], U.cfg("s"), tablas=TD, seed=sem_dia("2026-10", "9009-01"))
for v, slug in (("S ", "S-espacio-final"), (" S", "espacio-inicial-S"), ("Sí", "S-tilde"), ("true", "true"), ("N", "N")):
    g.caso("diario-param-%s" % slug, D, U.entrada_u("2026-11-15", 3), ["B-REP-18"], U.cfg(v), tablas=TD, seed=sem_dia("2026-11", "9015-01"))

# ---- Optimus
O = "ReplicaUtils.procesarRespaldoOptimus"
TO = [dump_sef(), dump_sfm(), dump_hli(), dump_hop(), dump_post()]
g.caso("optimus-cambio-de-anio-2024", O, U.entrada_u("2024-12-31", 12), ["B-REP-19"], colab(**dict(pais=840, origen="2024-12-31", habiles=[False, False, True])),
       tablas=TO, seed=semilla_ci("2024-12-31") + semilla_cus("2024-12-31"))
g.caso("optimus-2100-02-27-no-bisiesto", O, U.entrada_u("2100-02-27", 5), ["B-REP-19"], colab(**dict(pais=724, origen="2100-02-27", habiles=[False, False, True])),
       tablas=TO, seed=semilla_ci("2100-02-27") + semilla_cus("2100-02-27"))

# ---- guardarEnvioPostPauta: texto con = ' < > & y tildes en la fila que se reporta si el INSERT falla (Fecha NVARCHAR(10), Tabla NVARCHAR(100), FechaSistema NVARCHAR(10))
P = "ReplicaUtils.guardarEnvioPostPauta"
g.caso("post-pauta-cambio-de-anio", P, {"tabla": "Custodia_CarteraRVHis", "fechasistema": "2024-12-31", "fechaenvio": "2025-01-02"}, ["B-REP-20"], None, tablas=[dump_post()])
g.caso("post-pauta-fecha-larga-con-especiales-falla", P, {"tabla": "Custodia_X'<>&=", "fechasistema": "2026-06-30", "fechaenvio": "2026-07-01'<>&=ñ"}, ["B-REP-20"], None, tablas=[dump_post()])
g.caso("post-pauta-tabla-con-tildes-y-fecha-sistema-larga-falla", P, {"tabla": "Custodia_Ñandú_Áé", "fechasistema": "2026-06-30T00:00:00Z", "fechaenvio": "2026-07-01"}, ["B-REP-20"], None, tablas=[dump_post()])
g.caso("post-pauta-tabla-de-101-caracteres-falla", P, {"tabla": "T" * 99 + "&<", "fechasistema": "2028-02-29", "fechaenvio": "2028-03-01"}, ["B-REP-20"], None, tablas=[dump_post()])

# ---- IniciarReplicaFull / Delta
from cases_rep_ini import colab_ini, entrada_i  # noqa: E402
TI = [{"nombre": T_WS, "orden": '"IdStack"'}]
g.caso("full-logica-cambio-de-anio-secuencia-2", "IniciarReplicaFull.logica", entrada_i(11, 5), ["B-REP-01", "B-REP-02"], colab_ini((2,), "2025-01-01"), tablas=TI, reloj="2025-01-01 00:00:00")
g.caso("delta-logica-bisiesto-2024-empresa-11", "IniciarReplicaDelta.logica", entrada_i(11, 5), ["B-REP-01", "B-REP-02"], colab_ini((77,), "2024-02-29"), tablas=TI, reloj="2024-02-29 23:59:59")

print("fresh inputs g: %d casos en %s" % (len(g.ids), L.CASES))
(AQUI / "fresh-ids-g.txt").write_text("\n".join(g.ids) + "\n", encoding="utf-8", newline="\n")
