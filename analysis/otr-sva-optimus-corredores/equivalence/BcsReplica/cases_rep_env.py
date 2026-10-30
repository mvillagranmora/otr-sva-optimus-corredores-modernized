"""BcsReplica oracle: inputs of the EnviarTablaReplica cases (families ENV: logica per process, INH: respaldarDataDiaInhabil, CAL: the call stubs).

Calendar used: 2026-03-06 is a Friday, so 2026-03-07 and 2026-03-08 are the weekend and 2026-03-09 is the Monday (the collaborator Feriados is scripted per date).
"""
from cases_rep_lib import *  # noqa: F401,F403

SVC = "EnviarTablaReplica.logica"
TABLAS_CI = [dump_ws(), dump_sef(), dump_sfm()]
TABLAS_CUS = [dump_ws(), dump_hli(), dump_hop()]
TABLAS_INICIO = [dump_ws(), dump_sef(), dump_sfm(), dump_hli(), dump_hop()]
FALLO_WS = fallo('"Stamp","Parameters")', "no se puede insertar")
ODS_CORTA = {"ObtenerDiaSiguiente.logica": {"defecto": {"P_OUT_ERRORESCONTADOR": 1, "p_out_fechas": []}}}


def semilla_ci(origen="2026-03-06", destino=None, donde="vista"):
    """cuenta inversion balances of the day `origen`: donde="vista" = rows the analytic views return (the Historica inserts bring them into the tables), "tabla" = rows already in
    the tables, "ambas" = the tables hold stale amounts and the views the current ones; `destino` = a stale row of that day in the tables"""
    def filas(tv, tt, stale):
        d = 111 if stale else 0
        r = [(1, dict(cliente=10, portafolio=100, disp=1000, garantia=5, aplazo=7, retiro=1, retenido=2, ultima=50, matriz=3)),
             (2, dict(cliente=11, portafolio=101, disp=0, ultima=51, matriz=4)),
             (3, dict(cliente=12, portafolio=102, disp=-250, ultima=52, matriz=None))]
        return [saldo(tv if not stale else tt, origen, i, **dict(k, disp=(k["disp"] + d if k["disp"] else k["disp"]))) for i, k in r]
    s = []
    if donde in ("vista", "ambas"):
        s += filas(T_V_SEF, T_SEF, False) + filas(T_V_SFM, T_SFM, False)[:2]
    if donde == "tabla":
        s += filas(T_SEF, T_SEF, False) + filas(T_SFM, T_SFM, False)[:2]
    if donde == "ambas":
        s += filas(T_V_SEF, T_SEF, True) + filas(T_V_SFM, T_SFM, True)[:2]
    if destino:
        s += [saldo(T_SEF, destino, 99, disp=1), saldo(T_SFM, destino, 99, disp=1)]
    return s


def semilla_cus(origen="2026-03-06", destino=None, donde="vista"):
    """custody rows of the day `origen`: same meaning of `donde` as semilla_ci"""
    def filas(tl, to):
        return [custodia(tl, origen, 10, 500, 25), custodia(tl, origen, 11, 501, 30), custodia(to, origen, 10, 500, 26), custodia(to, origen, 11, 502, 31)]
    s = []
    if donde in ("vista", "ambas"):
        s += filas(T_V_HLI, T_V_HOP)
    if donde == "tabla":
        s += filas(T_HLI, T_HOP)
    if donde == "ambas":
        s += filas(T_HLI, T_HOP)
    if destino:
        s += [custodia(T_HLI, destino, 99, 999, 1), custodia(T_HOP, destino, 99, 999, 1)]
    return s


def semilla_retencion():
    """rows around the 10-day retention of the Historica deletes for the system date 2026-03-06 (limit 2026-02-24): kept, kept, boundary, older"""
    s = []
    for f, i in (("2026-02-20", 71), ("2026-02-24", 72), ("2026-02-25", 73), ("2026-03-05", 74)):
        s += [saldo(T_SEF, f, i), saldo(T_SFM, f, i), custodia(T_HLI, f, i, 700 + i), custodia(T_HOP, f, i, 700 + i)]
    return s


def generar():
    ids = []
    # ---------------------------------------------------------------- ENV: INICIO_PAUTA
    g = Gen("ENV")
    P = "INICIO_PAUTA"

    g.caso("inicio-pauta-sin-inhabiles", SVC, entrada_env(P, "2026-03-04"), ["B-REP-05", "B-REP-06", "B-REP-15"],
           colab(**dict(origen="2026-03-04", habiles=[True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-fin-de-semana-copia-dos-dias", SVC, entrada_env(P, "2026-03-06"), ["B-REP-06", "B-REP-14", "B-REP-16", "B-REP-22"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-un-solo-dia-inhabil", SVC, entrada_env(P, "2026-03-07"), ["B-REP-06", "B-REP-14"],
           colab(**dict(origen="2026-03-07", habiles=[False, True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-07"))
    g.caso("inicio-pauta-cinco-dias-inhabiles-semana-santa", SVC, entrada_env(P, "2026-04-01"), ["B-REP-06", "B-REP-14"],
           colab(**dict(origen="2026-04-01", habiles=[False, False, False, False, False, True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-04-01"))
    g.caso("inicio-pauta-borra-lo-que-ya-habia-en-el-destino", SVC, entrada_env(P, "2026-03-06"), ["B-REP-06", "B-REP-14"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06", destino="2026-03-07"))
    g.caso("inicio-pauta-retencion-diez-dias-limite-inclusivo", SVC, entrada_env(P, "2026-03-06"), ["B-REP-21"],
           colab(**dict(origen="2026-03-06", habiles=[True])), tablas=TABLAS_INICIO, seed=semilla_retencion())
    g.caso("inicio-pauta-borra-la-fecha-del-sistema-antes-del-historico", SVC, entrada_env(P, "2026-03-06"), ["B-REP-06", "D-REP-08"],
           colab(**dict(origen="2026-03-06", habiles=[True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06", donde="ambas") + semilla_cus("2026-03-06", donde="ambas"))
    g.caso("inicio-pauta-copia-no-trae-lo-de-otras-fechas", SVC, entrada_env(P, "2026-03-06"), ["B-REP-14", "B-REP-22"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), tablas=TABLAS_INICIO,
           seed=semilla_ci("2026-03-06") + [saldo(T_V_SEF, "2026-03-05", 8), saldo(T_V_SFM, "2026-03-05", 8), saldo(T_V_SEF, "2026-03-10", 9), saldo(T_SEF, "2026-03-05", 7)])
    g.caso("inicio-pauta-dia-siguiente-con-error-corta-y-encola-lo-previo", SVC, entrada_env(P, "2026-03-06"), ["D-REP-04"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True], contador_en=0)), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-error-en-el-segundo-dia-conserva-el-primero", SVC, entrada_env(P, "2026-03-06"), ["D-REP-04"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True], contador_en=1)), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-contador-dos-tambien-corta", SVC, entrada_env(P, "2026-03-06"), ["D-REP-04"],
           colab(**dict(origen="2026-03-06", habiles=[False, True], contador_en=0, contador=2)), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-empresa-inexistente-propaga-tras-los-borrados", SVC, entrada_env(P, "2026-03-06"), ["B-REP-06", "D-REP-13"],
           {"ConsultarEmpresa.logica": {"respuestas": [{"error": "Empresa no existe"}]}}, tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-colaborador-dia-siguiente-lanza-error", SVC, entrada_env(P, "2026-03-06"), ["B-REP-06"],
           colab(**dict(origen="2026-03-06", habiles=[False, True], error_ods_en=0)), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-colaborador-feriados-lanza-error", SVC, entrada_env(P, "2026-03-06"), ["B-REP-06"],
           colab(**dict(origen="2026-03-06", habiles=[False, True], error_fer_en=0)), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-lista-de-fechas-vacia-falla-con-indice", SVC, entrada_env(P, "2026-03-06"), ["B-REP-06", "D-REP-11"],
           colab(**dict(origen="2026-03-06", habiles=[False, True], vacia_en=0)), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-primer-drop-falla-se-traga-y-omite-el-segundo", SVC, entrada_env(P, "2026-03-04"), ["D-REP-06"],
           colab(**dict(origen="2026-03-04", habiles=[True])), fallos=[fallo("DROP TRIGGER", "no existe el trigger")], tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-segundo-drop-falla-se-traga", SVC, entrada_env(P, "2026-03-04"), ["D-REP-06"],
           colab(**dict(origen="2026-03-04", habiles=[True])), fallos=[fallo("DROP TRIGGER", "no existe el trigger", 2)], tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-falla-borrado-historico-propaga", SVC, entrada_env(P, "2026-03-04"), ["B-REP-06", "D-REP-13"],
           colab(**dict(origen="2026-03-04", habiles=[True])), fallos=[fallo("ADD_DAYS", "tabla bloqueada")], tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-falla-borrado-de-la-fecha-actual-propaga", SVC, entrada_env(P, "2026-03-04"), ["B-REP-06", "D-REP-13"],
           colab(**dict(origen="2026-03-04", habiles=[True])), fallos=[fallo("\"FechaSaldo\" = '2026-03-04'", "tabla bloqueada")], tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-falla-insercion-historica-propaga", SVC, entrada_env(P, "2026-03-04"), ["B-REP-06", "D-REP-13"],
           colab(**dict(origen="2026-03-04", habiles=[True])), fallos=[fallo("ConsultarSaldoCuentaInversionFechaMovimientoAnalitica", "vista no disponible")], tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-falla-copia-dia-inhabil-deja-la-mitad", SVC, entrada_env(P, "2026-03-06"), ["B-REP-14", "D-REP-13", "D-REP-21"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True])), fallos=[fallo("FROM BSCL.\"BSCL.CuentaInversion.BD::CTA.SaldoFechaMovimiento\" WHERE", "disco lleno")], tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-falla-borrado-del-destino-inhabil", SVC, entrada_env(P, "2026-03-06"), ["B-REP-14", "D-REP-13"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), fallos=[fallo("\"FechaSaldo\" = '2026-03-07'", "bloqueo")], tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-falla-insercion-del-workstack", SVC, entrada_env(P, "2026-03-04"), ["B-REP-15", "D-REP-13"],
           colab(**dict(origen="2026-03-04", habiles=[True])), fallos=[FALLO_WS], tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-fecha-nula-falla-despues-de-apagar-triggers", SVC, {"p_IdEmpresa": 1, "p_Proceso": P, "p_Parametros": {"FechaSistemaEjecucion": None}}, ["D-REP-11"],
           colab(), tablas=TABLAS_INICIO)
    g.caso("inicio-pauta-sin-clave-de-fecha-falla-igual", SVC, {"p_IdEmpresa": 1, "p_Proceso": P, "p_Parametros": {}}, ["D-REP-11"], colab(), tablas=TABLAS_INICIO)
    g.caso("inicio-pauta-parametros-nulos-falla-al-leer-la-fecha", SVC, {"p_IdEmpresa": 1, "p_Proceso": P, "p_Parametros": None}, ["D-REP-11"], colab(), tablas=TABLAS_INICIO)
    g.caso("inicio-pauta-fecha-numerica-no-es-texto", SVC, {"p_IdEmpresa": 1, "p_Proceso": P, "p_Parametros": {"FechaSistemaEjecucion": 20260304}}, ["D-REP-11"], colab(), tablas=TABLAS_INICIO)
    g.caso("inicio-pauta-fecha-con-hora-pasa-tal-cual", SVC, entrada_env(P, "2026-03-04 10:00:00"), ["D-REP-11"],
           colab(**dict(origen="2026-03-04 10:00:00", habiles=[True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-fecha-con-basura-falla-al-calcular-la-retencion", SVC, entrada_env(P, "2026-03-04" + "x" * 400), ["D-REP-11"],
           colab(**dict(origen="2026-03-04" + "x" * 400, habiles=[True])), tablas=TABLAS_INICIO)
    g.caso("inicio-pauta-proceso-en-minusculas", SVC, entrada_env("inicio_pauta", "2026-03-04"), ["B-REP-05"],
           colab(**dict(origen="2026-03-04", habiles=[True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-proceso-con-espacio-no-coincide", SVC, entrada_env(" INICIO_PAUTA", "2026-03-04"), ["B-REP-05", "D-REP-16"], colab(), tablas=TABLAS_INICIO)
    g.caso("inicio-pauta-otra-empresa-otro-pais", SVC, entrada_env(P, "2026-03-06", empresa=2), ["B-REP-14"],
           colab(**dict(pais=152, origen="2026-03-06", habiles=[False, False, True], pais_ods=152)), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06"))
    g.caso("inicio-pauta-empresa-nula-se-pasa-al-colaborador", SVC, {"p_IdEmpresa": None, "p_Proceso": P, "p_Parametros": {"FechaSistemaEjecucion": "2026-03-04"}}, ["B-REP-06"],
           colab(**dict(origen="2026-03-04", habiles=[True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-pais-nulo-se-pasa-como-ausente", SVC, entrada_env(P, "2026-03-04"), ["B-REP-06"],
           dict(colab(**dict(origen="2026-03-04", habiles=[True])), **{"ConsultarEmpresa.logica": {"defecto": {"IdEmpresa": 1}}}), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    g.caso("inicio-pauta-proceso-nulo", SVC, {"p_IdEmpresa": 1, "p_Proceso": None, "p_Parametros": {"FechaSistemaEjecucion": "2026-03-04"}}, ["B-REP-05"], colab(), tablas=TABLAS_INICIO)
    g.caso("inicio-pauta-sin-proceso", SVC, {"p_IdEmpresa": 1, "p_Parametros": {"FechaSistemaEjecucion": "2026-03-04"}}, ["B-REP-05"], colab(), tablas=TABLAS_INICIO)
    g.caso("inicio-pauta-parametros-extra-se-ignoran", SVC, {"p_IdEmpresa": 1, "p_Proceso": P, "p_Parametros": {"FechaSistemaEjecucion": "2026-03-04", "Otro": 7, "Flag": True}}, ["B-REP-05"],
           colab(**dict(origen="2026-03-04", habiles=[True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))

    # ---------------------------------------------------------------- ENV: VALORIZAR_FFMM
    P = "VALORIZAR_FFMM"
    g.caso("valorizar-ffmm-sin-inhabiles", SVC, entrada_env(P, "2026-03-04"), ["B-REP-07", "B-REP-15"],
           colab(**dict(origen="2026-03-04", habiles=[True])), tablas=TABLAS_CUS, seed=semilla_cus("2026-03-04", donde="tabla"))
    g.caso("valorizar-ffmm-fin-de-semana-copia-custodia", SVC, entrada_env(P, "2026-03-06"), ["B-REP-07", "B-REP-14", "B-REP-16"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True])), tablas=TABLAS_CUS, seed=semilla_cus("2026-03-06", donde="tabla"))
    g.caso("valorizar-ffmm-borra-el-destino-antes-de-copiar", SVC, entrada_env(P, "2026-03-06"), ["B-REP-14"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), tablas=TABLAS_CUS, seed=semilla_cus("2026-03-06", destino="2026-03-07", donde="tabla"))
    g.caso("valorizar-ffmm-cuatro-dias-inhabiles", SVC, entrada_env(P, "2026-12-23"), ["B-REP-14"],
           colab(**dict(origen="2026-12-23", habiles=[False, False, False, False, True])), tablas=TABLAS_CUS, seed=semilla_cus("2026-12-23", donde="tabla"))
    g.caso("valorizar-ffmm-error-dia-siguiente-conserva-lo-previo", SVC, entrada_env(P, "2026-03-06"), ["D-REP-04"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True], contador_en=1)), tablas=TABLAS_CUS, seed=semilla_cus("2026-03-06", donde="tabla"))
    g.caso("valorizar-ffmm-error-en-el-primer-dia", SVC, entrada_env(P, "2026-03-06"), ["D-REP-04"],
           colab(**dict(origen="2026-03-06", habiles=[False, True], contador_en=0)), tablas=TABLAS_CUS, seed=semilla_cus("2026-03-06", donde="tabla"))
    g.caso("valorizar-ffmm-empresa-inexistente", SVC, entrada_env(P, "2026-03-06"), ["B-REP-07"],
           {"ConsultarEmpresa.logica": {"respuestas": [{"error": "Empresa no existe"}]}}, tablas=TABLAS_CUS, seed=semilla_cus("2026-03-06", donde="tabla"))
    g.caso("valorizar-ffmm-falla-copia-liquidacion", SVC, entrada_env(P, "2026-03-06"), ["B-REP-14", "D-REP-13"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), fallos=[fallo("FROM \"BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion\" WHERE", "disco lleno")], tablas=TABLAS_CUS, seed=semilla_cus("2026-03-06", donde="tabla"))
    g.caso("valorizar-ffmm-falla-copia-operacion-deja-liquidacion", SVC, entrada_env(P, "2026-03-06"), ["B-REP-14", "D-REP-13"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), fallos=[fallo("FROM \"BSCL.Custodia.BD::HIS.CustodiaFechaOperacion\" WHERE", "disco lleno")], tablas=TABLAS_CUS, seed=semilla_cus("2026-03-06", donde="tabla"))
    g.caso("valorizar-ffmm-fecha-nula-solo-concatena", SVC, {"p_IdEmpresa": 1, "p_Proceso": P, "p_Parametros": {"FechaSistemaEjecucion": None}}, ["D-REP-11"],
           dict(colab(), **ODS_CORTA), tablas=TABLAS_CUS)
    g.caso("valorizar-ffmm-fecha-numerica", SVC, {"p_IdEmpresa": 1, "p_Proceso": P, "p_Parametros": {"FechaSistemaEjecucion": 5}}, ["D-REP-11"], colab(), tablas=TABLAS_CUS)
    g.caso("valorizar-ffmm-falla-insercion-del-workstack", SVC, entrada_env(P, "2026-03-04"), ["B-REP-15"],
           colab(**dict(origen="2026-03-04", habiles=[True])), fallos=[FALLO_WS], tablas=TABLAS_CUS, seed=semilla_cus("2026-03-04", donde="tabla"))

    # ---------------------------------------------------------------- ENV: PAUTA_PATRIMONIO, CAMBIO_FECHA_SISTEMA, PAUTA_RESPALDAR_CUSTODIA, VALORIZAR_CARTERA_RV...
    for proceso, ref in (("PAUTA_PATRIMONIO", "B-REP-08"), ("CAMBIO_FECHA_SISTEMA", "B-REP-11"), ("PAUTA_RESPALDAR_CUSTODIA", "B-REP-10"),
                         ("VALORIZAR_CARTERA_RV_PRECIO_MERCADO", "B-REP-12")):
        slug = proceso.lower().replace("_", "-")
        g.caso(slug + "-encola-sin-tocar-datos", SVC, entrada_env(proceso, "2026-03-04"), [ref, "B-REP-15"], colab(), tablas=[dump_ws()])
        g.caso(slug + "-fecha-nula-concatena-null", SVC, {"p_IdEmpresa": 1, "p_Proceso": proceso, "p_Parametros": {"FechaSistemaEjecucion": None}}, [ref, "D-REP-11"], colab(), tablas=[dump_ws()])
        g.caso(slug + "-falla-insercion-workstack", SVC, entrada_env(proceso, "2026-03-04"), [ref], colab(), fallos=[FALLO_WS], tablas=[dump_ws()])
    g.caso("pauta-patrimonio-fecha-larga-excede-500", SVC, entrada_env("PAUTA_PATRIMONIO", "2026-03-04" + "y" * 200), ["B-REP-08", "D-REP-15"], colab(), tablas=[dump_ws()])
    g.caso("pauta-patrimonio-minusculas-y-mezcla", SVC, entrada_env("Pauta_Patrimonio", "2026-03-04"), ["B-REP-05", "B-REP-08"], colab(), tablas=[dump_ws()])
    g.caso("pauta-patrimonio-sin-parametros-no-usa-colaboradores", SVC, {"p_IdEmpresa": 1, "p_Proceso": "PAUTA_PATRIMONIO", "p_Parametros": {}}, ["B-REP-08", "D-REP-11"], colab(), tablas=[dump_ws()])
    g.caso("cambio-fecha-sistema-parametros-nulos", SVC, {"p_IdEmpresa": 1, "p_Proceso": "CAMBIO_FECHA_SISTEMA", "p_Parametros": None}, ["B-REP-11", "D-REP-11"], colab(), tablas=[dump_ws()])
    g.caso("valorizar-cartera-rv-espacio-sobrante-antes-de-serialdelete", SVC, entrada_env("VALORIZAR_CARTERA_RV_PRECIO_MERCADO", "2026-03-04"), ["B-REP-12", "D-REP-05"], colab(), tablas=[dump_ws()])

    # ---------------------------------------------------------------- ENV: COPIA_PATRIMONIO_PARA_INHABIL
    P = "COPIA_PATRIMONIO_PARA_INHABIL"
    g.caso("copia-patrimonio-siguiente-habil-no-encola", SVC, entrada_env(P, "2026-03-04"), ["B-REP-09", "B-REP-15"], colab(**dict(origen="2026-03-04", habiles=[True])), tablas=[dump_ws()])
    g.caso("copia-patrimonio-fin-de-semana-dos-workstack", SVC, entrada_env(P, "2026-03-06"), ["B-REP-09", "B-REP-15"], colab(**dict(origen="2026-03-06", habiles=[False, False, True])), tablas=[dump_ws()])
    g.caso("copia-patrimonio-cinco-dias", SVC, entrada_env(P, "2026-04-01"), ["B-REP-09"], colab(**dict(origen="2026-04-01", habiles=[False] * 5 + [True])), tablas=[dump_ws()])
    g.caso("copia-patrimonio-error-en-el-primer-dia-no-encola-nada", SVC, entrada_env(P, "2026-03-06"), ["D-REP-03"],
           colab(**dict(origen="2026-03-06", habiles=[False, True], contador_en=0)), tablas=[dump_ws()])
    g.caso("copia-patrimonio-error-en-el-segundo-dia-pierde-el-primero", SVC, entrada_env(P, "2026-03-06"), ["D-REP-03"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True], contador_en=1)), tablas=[dump_ws()])
    g.caso("copia-patrimonio-empresa-inexistente", SVC, entrada_env(P, "2026-03-06"), ["B-REP-09"], {"ConsultarEmpresa.logica": {"respuestas": [{"error": "Empresa no existe"}]}}, tablas=[dump_ws()])
    g.caso("copia-patrimonio-colaborador-dia-siguiente-lanza-error", SVC, entrada_env(P, "2026-03-06"), ["B-REP-09"],
           colab(**dict(origen="2026-03-06", habiles=[False, True], error_ods_en=0)), tablas=[dump_ws()])
    g.caso("copia-patrimonio-feriados-lanza-error-en-el-segundo", SVC, entrada_env(P, "2026-03-06"), ["B-REP-09"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True], error_fer_en=1)), tablas=[dump_ws()])
    g.caso("copia-patrimonio-lista-vacia-falla-con-indice", SVC, entrada_env(P, "2026-03-06"), ["B-REP-09"],
           colab(**dict(origen="2026-03-06", habiles=[False, True], vacia_en=0)), tablas=[dump_ws()])
    g.caso("copia-patrimonio-falla-insercion-del-workstack", SVC, entrada_env(P, "2026-03-06"), ["B-REP-15"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), fallos=[FALLO_WS], tablas=[dump_ws()])
    g.caso("copia-patrimonio-no-borra-ni-copia-datos", SVC, entrada_env(P, "2026-03-06"), ["B-REP-09"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-06") + semilla_cus("2026-03-06"))
    g.caso("copia-patrimonio-fecha-nula", SVC, {"p_IdEmpresa": 1, "p_Proceso": P, "p_Parametros": {"FechaSistemaEjecucion": None}}, ["D-REP-11"],
           dict(colab(), **ODS_CORTA), tablas=[dump_ws()])

    # ---------------------------------------------------------------- ENV: FIN_PAUTA
    P = "FIN_PAUTA"
    TFIN = [dump_ws(), dump_res(), dump_sef(), dump_sfm(), dump_hli(), dump_hop()]

    def colab_fin(offset="0", **kw):
        c = colab()
        c["ConfiguracionSistema.getParametroConfiguracionLong"] = {"defecto": offset}
        c.update(kw)
        return c

    g.caso("fin-pauta-completo-offset-cero", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13", "B-REP-15", "D-REP-09"], colab_fin("0"), tablas=TFIN, seed=semilla_ci("2026-03-06") + semilla_retencion())
    g.caso("fin-pauta-offset-positivo-suma-segundos", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13", "D-REP-09"], colab_fin("3600"), tablas=TFIN)
    g.caso("fin-pauta-offset-negativo-resta-segundos", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13", "D-REP-09"], colab_fin("-14400"), tablas=TFIN)
    g.caso("fin-pauta-offset-cruza-la-medianoche", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13", "D-REP-09"], colab_fin("33000"), tablas=TFIN)
    g.caso("fin-pauta-reloj-fin-de-ano", SVC, entrada_env(P, "2026-12-31"), ["B-REP-13", "D-REP-09"], colab_fin("7200"), tablas=TFIN, reloj="2026-12-31 22:30:00")
    g.caso("fin-pauta-parametro-ausente-falla-antes-de-escribir", SVC, entrada_env(P, "2026-03-06"),  ["B-REP-13"],
           dict(colab(), **{"ConfiguracionSistema.getParametroConfiguracionLong": {"respuestas": [{"error": "Parametro PLA.SegundosZonaHoraria en modulo PLATAFORMA no encontrado"}]}}), tablas=TFIN, seed=semilla_ci("2026-03-06"))
    g.caso("fin-pauta-parametro-no-numerico", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13"], colab_fin("abc"), tablas=TFIN)
    g.caso("fin-pauta-parametro-vacio", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13"], colab_fin(""), tablas=TFIN)
    g.caso("fin-pauta-parametro-nulo", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13"], colab_fin(None), tablas=TFIN)
    g.caso("fin-pauta-fecha-con-hora-para-el-resumen", SVC, entrada_env(P, "2026-03-06 09:15:00"), ["B-REP-13", "D-REP-19"], colab_fin("0"), tablas=TFIN)
    g.caso("fin-pauta-fecha-mal-formada-falla-al-convertir", SVC, entrada_env(P, "2026-3-6"), ["B-REP-13", "D-REP-19"], colab_fin("0"), tablas=TFIN)
    g.caso("fin-pauta-fecha-nula-falla-al-convertir", SVC, {"p_IdEmpresa": 1, "p_Proceso": P, "p_Parametros": {"FechaSistemaEjecucion": None}}, ["D-REP-11"], colab_fin("0"), tablas=TFIN)
    g.caso("fin-pauta-create-trigger-custodia-falla-se-traga-y-omite-resumen", SVC, entrada_env(P, "2026-03-06"), ["D-REP-07"], colab_fin("0"),
           fallos=[fallo("CREATE TRIGGER", "trigger invalido")], tablas=TFIN, seed=semilla_ci("2026-03-06"))
    g.caso("fin-pauta-create-trigger-cuenta-inversion-falla-se-traga", SVC, entrada_env(P, "2026-03-06"), ["D-REP-07"], colab_fin("0"),
           fallos=[fallo("CREATE TRIGGER", "trigger invalido", 2)], tablas=TFIN, seed=semilla_ci("2026-03-06"))
    g.caso("fin-pauta-upsert-resumen-falla-se-traga", SVC, entrada_env(P, "2026-03-06"), ["D-REP-07"], colab_fin("0"),
           fallos=[fallo("PLA.ResumenReplica", "tabla bloqueada")], tablas=TFIN, seed=semilla_ci("2026-03-06"))
    g.caso("fin-pauta-mismo-segundo-actualiza-la-fila-del-resumen", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13", "D-REP-09"], colab_fin("0"), tablas=TFIN,
           seed=[insertar(T_RES, Tipo="PAUTA", Nombre="FIN_PAUTA", Estado="ERROR", Detalle="previo", FechaSistema="2026-03-06", UltimaActualizacion="2026-03-02 14:30:00"),
                 insertar(T_RES, Tipo="PAUTA", Nombre="FIN_PAUTA", Estado="OK", Detalle="FIN_PAUTA", FechaSistema="2026-03-05", UltimaActualizacion="2026-03-02 14:30:00")])
    g.caso("fin-pauta-otro-segundo-agrega-fila-al-resumen", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13", "D-REP-09"], colab_fin("0"), tablas=TFIN,
           seed=[insertar(T_RES, Tipo="PAUTA", Nombre="FIN_PAUTA", Estado="OK", Detalle="FIN_PAUTA", FechaSistema="2026-03-06", UltimaActualizacion="2026-03-02 14:29:59")])
    g.caso("fin-pauta-falla-borrado-historico-despues-del-resumen", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13", "D-REP-13"], colab_fin("0"),
           fallos=[fallo("ADD_DAYS", "tabla bloqueada")], tablas=TFIN, seed=semilla_ci("2026-03-06"))
    g.caso("fin-pauta-falla-insercion-historica", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13", "D-REP-13"], colab_fin("0"),
           fallos=[fallo("ConsultarSaldoCuentaInversionFechaEfectivaAnalitica", "vista no disponible")], tablas=TFIN, seed=semilla_ci("2026-03-06"))
    g.caso("fin-pauta-falla-insercion-del-workstack-al-final", SVC, entrada_env(P, "2026-03-06"), ["B-REP-15", "D-REP-13"], colab_fin("0"),
           fallos=[FALLO_WS], tablas=TFIN)
    g.caso("fin-pauta-no-consulta-dias-inhabiles", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13"], colab_fin("0"), tablas=TFIN, seed=semilla_ci("2026-03-06") + semilla_cus("2026-03-06"))
    g.caso("fin-pauta-borra-fecha-actual-antes-del-historico", SVC, entrada_env(P, "2026-03-06"), ["B-REP-13", "B-REP-21"], colab_fin("0"), tablas=TFIN,
           seed=semilla_ci("2026-03-06") + semilla_cus("2026-03-06") + semilla_retencion())

    # ---------------------------------------------------------------- ENV: otros procesos
    for p, slug in (("EJECUTAR_PROCESOS_DIAINHABIL", "proceso-del-main-del-legacy"), ("", "proceso-vacio"), ("OTRO", "proceso-desconocido"), ("FIN_PAUTA ", "fin-pauta-con-espacio-final"),
                    ("INICIO_PAUTA_2", "prefijo-no-basta"), ("VALORIZAR_CARTERA_RV", "nombre-truncado")):
        g.caso("no-hace-nada-" + slug, SVC, entrada_env(p, "2026-03-04"), ["B-REP-05", "D-REP-16"], colab(), tablas=TABLAS_INICIO, seed=semilla_ci("2026-03-04"))
    ids += g.ids

    # ---------------------------------------------------------------- INH: respaldarDataDiaInhabil (public)
    g = Gen("INH")
    S = "EnviarTablaReplica.respaldarDataDiaInhabil"
    PC = "tables:CuentaInversion_SaldoFechaEfectivaHist,CuentaInversion_SaldoFechaMovimientoHist;BCS_query:QueryReplicaDelta;Fecha:"
    PU = "tables:Custodia_CustodiaFechaOperacionHist,Custodia_CustodiaFechaLiquidacionHist;BCS_query:QueryReplicaDelta;Fecha:"

    def inh(tabla, pref, fecha="2026-03-06", empresa=1):
        return {"idEmpresa": empresa, "fechaOrigen": fecha, "tablaWorkstack": tabla, "parametrosWorkstack": pref}

    TODAS = [dump_sef(), dump_sfm(), dump_hli(), dump_hop()]
    g.caso("cuenta-inversion-fin-de-semana", S, inh("CUENTA INVERSION DIA INHABIL", PC), ["B-REP-14", "B-REP-16", "B-REP-22"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True])), tablas=TODAS, seed=semilla_ci("2026-03-06", donde="tabla") + semilla_cus("2026-03-06", donde="tabla"))
    g.caso("custodia-fin-de-semana", S, inh("CUSTODIA ACTUAL DIA INHABIL", PU), ["B-REP-14", "B-REP-16"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True])), tablas=TODAS, seed=semilla_ci("2026-03-06", donde="tabla") + semilla_cus("2026-03-06", donde="tabla"))
    g.caso("tabla-desconocida-encola-sin-copiar", S, inh("OTRA TABLA", "x;Fecha:"), ["B-REP-14", "D-REP-20"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True])), tablas=TODAS, seed=semilla_ci("2026-03-06", donde="tabla") + semilla_cus("2026-03-06", donde="tabla"))
    g.caso("tabla-en-minusculas-no-coincide", S, inh("custodia actual dia inhabil", PU), ["B-REP-14", "D-REP-20"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), tablas=TODAS, seed=semilla_cus("2026-03-06", donde="tabla"))
    g.caso("tabla-nula-es-una-tabla-desconocida", S, {"idEmpresa": 1, "fechaOrigen": "2026-03-06", "tablaWorkstack": None, "parametrosWorkstack": PC}, ["B-REP-14", "D-REP-20"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), tablas=TODAS)
    g.caso("prefijo-nulo-concatena-null", S, {"idEmpresa": 1, "fechaOrigen": "2026-03-06", "tablaWorkstack": "OTRA", "parametrosWorkstack": None}, ["B-REP-16"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), tablas=[dump_ws()])
    g.caso("siguiente-habil-devuelve-lista-vacia", S, inh("CUENTA INVERSION DIA INHABIL", PC, "2026-03-04"), ["B-REP-14"],
           colab(**dict(origen="2026-03-04", habiles=[True])), tablas=TODAS, seed=semilla_ci("2026-03-04", donde="tabla"))
    g.caso("error-dia-siguiente-devuelve-lo-acumulado", S, inh("CUENTA INVERSION DIA INHABIL", PC), ["D-REP-04"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True], contador_en=1)), tablas=TODAS, seed=semilla_ci("2026-03-06", donde="tabla"))
    g.caso("error-en-el-primer-dia-devuelve-lista-vacia", S, inh("CUSTODIA ACTUAL DIA INHABIL", PU), ["D-REP-04"],
           colab(**dict(origen="2026-03-06", habiles=[False, True], contador_en=0)), tablas=TODAS, seed=semilla_cus("2026-03-06", donde="tabla"))
    g.caso("empresa-inexistente", S, inh("CUENTA INVERSION DIA INHABIL", PC), ["B-REP-14"], {"ConsultarEmpresa.logica": {"respuestas": [{"error": "Empresa no existe"}]}}, tablas=TODAS)
    g.caso("falla-un-borrado-propaga", S, inh("CUENTA INVERSION DIA INHABIL", PC), ["B-REP-14", "D-REP-13"],
           colab(**dict(origen="2026-03-06", habiles=[False, False, True])), fallos=[fallo("\"FechaSaldo\" = '2026-03-08'", "bloqueo")], tablas=TODAS, seed=semilla_ci("2026-03-06", donde="tabla"))
    g.caso("falla-la-copia-de-custodia-operacion", S, inh("CUSTODIA ACTUAL DIA INHABIL", PU), ["B-REP-14", "D-REP-13"],
           colab(**dict(origen="2026-03-06", habiles=[False, True])), fallos=[fallo("FROM \"BSCL.Custodia.BD::HIS.CustodiaFechaOperacion\" WHERE", "disco lleno")], tablas=TODAS, seed=semilla_cus("2026-03-06", donde="tabla"))
    g.caso("origen-con-fecha-hora", S, inh("CUENTA INVERSION DIA INHABIL", PC, "2026-03-06 10:00:00"), ["B-REP-14", "D-REP-11"],
           colab(**dict(origen="2026-03-06 10:00:00", habiles=[False, True])), tablas=TODAS, seed=semilla_ci("2026-03-06", donde="tabla"))
    g.caso("origen-nulo-concatena-y-pasa-null-al-colaborador", S, {"idEmpresa": 1, "fechaOrigen": None, "tablaWorkstack": "OTRA", "parametrosWorkstack": "p;Fecha:"}, ["D-REP-11"],
           {"ConsultarEmpresa.logica": empresa(), "ObtenerDiaSiguiente.logica": {"defecto": {"P_OUT_ERRORESCONTADOR": 1, "p_out_fechas": []}}}, tablas=[dump_ws()])
    g.caso("empresa-nula", S, {"idEmpresa": None, "fechaOrigen": "2026-03-06", "tablaWorkstack": "OTRA", "parametrosWorkstack": "p;Fecha:"}, ["B-REP-14"],
           colab(**dict(origen="2026-03-06", habiles=[True])), tablas=[dump_ws()])
    ids += g.ids

    # ---------------------------------------------------------------- CAL: the call stubs
    g = Gen("CAL")
    g.caso("enviar-tabla-replica-call-devuelve-null", "EnviarTablaReplica.call", {"p_IdEmpresa": 1, "p_Proceso": "INICIO_PAUTA", "p_Parametros": {"FechaSistemaEjecucion": "2026-03-04"}}, ["D-REP-01"],
           colab(), tablas=[dump_ws()])
    g.caso("enviar-tabla-replica-call-con-entrada-vacia", "EnviarTablaReplica.call", {}, ["D-REP-01"], colab(), tablas=[dump_ws()])
    g.caso("replica-utils-call-devuelve-null", "ReplicaUtils.call", {"p_IdEmpresa": 1}, ["D-REP-01"], colab(), tablas=[dump_ws()])
    ids += g.ids
    return ids
